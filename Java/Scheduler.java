package Subway;

import GenCol.*;
import model.modeling.content;
import view.modeling.ViewableAtomic;
import model.modeling.message;
import java.util.HashMap;
import java.util.PriorityQueue;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

public class Scheduler extends ViewableAtomic {

	// Use a priority queue to keep track of waiting trains.
	// This way we can order the queue by station number and work
	// backwards to completely clear any jams.
	protected PriorityQueue<UUID> waitingTrains;

	// Containers for the passed in loops, trains, and train positions
	SubwayLoop loop;
	TrainGroup trains;

	// Use a map to store the train positions
	private HashMap<UUID, Integer> trainPositions;
	private ArrayList<Boolean> segmentPopulated;

	// Convenience map to associate stations to trains
	private HashMap<UUID, UUID> stationToTrainMap;

	private message outMessages;

	protected static final String IN_PASSENGER_UNLOAD_PORT = "inPassengerUnload";
	protected static final String OUT_PASSENGER_LOAD_PORT = "outPassengerLoad";
	protected static final String OUT_N_PASSENGERS_DELIVERED_PORT = "nPassengersDelivered";

	protected boolean _logResults;

	// A log file to save train histories
	private FileWriter logWriter;

	// Use an internal clock
	protected double clock;

	public Scheduler(String name, SubwayLoop Loop, TrainGroup Trains, ArrayList<Integer> InitialTrainPositions, boolean logResults) {
		super(name);

		// Create a queue to store waiting trains
		waitingTrains = new PriorityQueue<UUID>();

		// Initialize the station-to-train map
		stationToTrainMap = new HashMap<UUID,UUID>();

		loop = Loop;
		trains = Trains;

		// Enable logging
		_logResults = logResults;

		// Populate the train positions
		trainPositions = new HashMap<UUID,Integer>();
		for (int k=0; k<trains.size(); k++) {
			trainPositions.put(trains.get(k).getID(), InitialTrainPositions.get(k));
		}

		// Use a boolean array to keep track of which stations/tracks
		// are currently populated.
		segmentPopulated = new ArrayList<Boolean>(loop.size());
		for (int k=0; k<loop.size(); k++) {
			segmentPopulated.add(false);
		}

		// Now include the initial train positions
		for (int k=0; k<trains.size(); k++) {
			segmentPopulated.set(InitialTrainPositions.get(k), true);
		}

		// Create input ports and output ports
		// From the train group
		addInport(IN_PASSENGER_UNLOAD_PORT);
		addInport(Train.OUT_REQUEST_MOVE_TO_STATION_PORT);
		addInport(Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT);

		// From the subway loop
		addInport(Train.IN_PASSENGER_LOAD_PORT);
		addInport(TrackSection.OUT_ACQUIRE_PORT);
		addInport(TrackSection.OUT_RELEASE_PORT);

		// To the train group
		addOutport(Train.IN_BREAKDOWN_PORT);
		addOutport(Train.IN_MOVE_TO_STATION_PORT);
		addOutport(Train.IN_MOVE_TO_TRACK_SECTION_PORT);
		addOutport(OUT_PASSENGER_LOAD_PORT);

		// To the subway group
		// Stations
		addOutport(Train.OUT_PASSENGER_UNLOAD_PORT);
		// Tracks
		addOutport(TrackSection.IN_ACQUIRE_PORT);
		addOutport(TrackSection.IN_RELEASE_PORT);

		// For the transducer
		addOutport(OUT_N_PASSENGERS_DELIVERED_PORT);

		// Open the log file
		if (_logResults) {
			try {
				logWriter = new FileWriter(new File("data/" + this.name + ".log"));
				logWriter.write(String.format("%-10s %-40s %-15s\n", "Clock", "Train Name", "Next Position"));
				logWriter.flush();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}

	}

	public void initialize() {
		clock = 0;
		passivate();
		super.initialize();
	}

	public void deltext(double e, message x) {
		clock = clock + e;
		Continue(e);

		// Create a new instance for the output messages
		outMessages = new message();

		System.out.println("Entering deltext...");

		if (phaseIs("passive")) {

			for (int k=0; k<x.size(); k++) {
				// Handle the move into station requests
				if (messageOnPort(x,Train.OUT_REQUEST_MOVE_TO_STATION_PORT,k)) {
					// Message form is ("Port",TrainID)
					System.out.println("Scheduler Request: Move to station");
					KeyEntity ent = (KeyEntity)x.getValOnPort(Train.OUT_REQUEST_MOVE_TO_STATION_PORT, k);
					UUID trainID = ent.getID();
					System.out.println("Train ID: "+trainID);

					processTrainMove(trainID);
				}
				else if (messageOnPort(x,Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT,k)) {
					// Message form is ("Port",TrainID)
					KeyEntity ent = (KeyEntity)x.getValOnPort(Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT, k);
					UUID trainID = ent.getID();

					processTrainMove(trainID);
				}
				else if (messageOnPort(x,IN_PASSENGER_UNLOAD_PORT,k)) {
					// Message form is ("Port",(TrainID,Passengers,Capacity))

					// Get the station ID from the train ID and repackage the message
					KeyValueEntity ent = (KeyValueEntity)x.getValOnPort(IN_PASSENGER_UNLOAD_PORT, k);
					UUID trainID = ent.getID();

					// Pull out the passenger unload request as well
					PassengerUnloadRequest pur = (PassengerUnloadRequest)ent.getValue();

					int trainPosition = trainPositions.get(trainID);
					UUID stationID = ((Station)loop.get(trainPosition)).getID();

					System.out.println("Unloading "+pur.getPassengers().size()+" from train "+trainID+
							" to station "+stationID);
					System.out.println("Remaining capacity: "+pur.getRemainingCapacity());

					// Associate this station id with this train.
					// We should immediately be receiving a follow-up
					// message from the station with a set of passengers
					// to board, so this mapping will save a bit of
					// work when that message is received
					stationToTrainMap.put(stationID, trainID);

					// Package a message for that station
					outMessages.add(makeContent(Train.OUT_PASSENGER_UNLOAD_PORT,
							new KeyValueEntity<>(stationID, pur)));

					// Also add the number of passengers unloaded for the
					// transducer to track
					outMessages.add(makeContent(OUT_N_PASSENGERS_DELIVERED_PORT,
							new intEnt(pur.getPassengers().size())));
				}
				else if (messageOnPort(x,Train.IN_PASSENGER_LOAD_PORT,k)) {

					// Get the station id and the passenger list
					KeyValueEntity ent = (KeyValueEntity)x.getValOnPort(Train.IN_PASSENGER_LOAD_PORT, k);
					UUID stationID = ent.getID();
					PassengerList passengers = (PassengerList)ent.getValue();

					UUID trainID = stationToTrainMap.get(stationID);
					System.out.println("Providing "+passengers.size()+" passengers to board train "+trainID);

					// Generate the output message
					outMessages.add(makeContent(OUT_PASSENGER_LOAD_PORT,
							new KeyValueEntity<>(trainID,passengers)));
				}
			}

			// Pull out all of the waiting trains and reprocess
			ArrayList<UUID> trainsToProcess = new ArrayList<UUID>();
			while (waitingTrains.size()>0) {
				trainsToProcess.add(waitingTrains.poll());
			}
			for (UUID waitingTrain : trainsToProcess) {
				processTrainMove(waitingTrain);
			}

			// The messages are ready to be sent out
			holdIn("Messages Processed",0);
		}
	}

	private void processTrainMove(UUID trainID) {
		// Get the train position
		int trainPosition = trainPositions.get(trainID);
		String mode = "toStation";
		String messagePort = Train.IN_MOVE_TO_STATION_PORT;
		if (trainPosition%2==0) {
			mode = "toTrack";
			messagePort = Train.IN_MOVE_TO_TRACK_SECTION_PORT;
		}
		int nextPosition = (trainPosition+1)%loop.size();

		// Get the next track instance
		System.out.println("Current Position: "+trainPosition);
		System.out.println("Loop size: "+loop.size());
		TrackSection nextTrack;
		Station nextStation;

		content outMessage;
		if (mode.equals("toTrack")) {
			nextTrack = (TrackSection)loop.get(nextPosition);
			System.out.println("Next track: "+nextTrack.getName());
			outMessage = makeContent(messagePort,new KeyValueEntity<Double>(trainID,
					(double)nextTrack.getTravelTime()));
		}
		else { // toStation
			nextStation = (Station)loop.get(nextPosition);
			System.out.println("Next station: "+nextStation.getName());
			outMessage = makeContent(messagePort,new KeyValueEntity<UUID>(trainID,
					nextStation.getID()));
		}

		// Check if the next segment is populated
		if (!segmentPopulated.get(nextPosition)) {
			// Tell the train it can move and increment all
			// the appropriate placeholders/indexes

			// Set the 'move to' segment as true and the 'move from' as false
			segmentPopulated.set(nextPosition, true);
			segmentPopulated.set(trainPosition, false);

			// Locally increment the train position
			trainPositions.put(trainID, nextPosition);
			System.out.println("Next position: "+nextPosition);

			// Sent OK to move message
			outMessages.add(outMessage);

			// Write the time, the train, and the next position to the log
			if (_logResults){
			    try {
                    if (nextPosition%2==0) {
                        logWriter.write(String.format("%-10.2f %-40s %-3d\n", clock, trains.get(trainID).getName(), nextPosition));
                    }
                    else {
                        logWriter.write(String.format("%-10.2f %-40s %-3d\n", clock, trains.get(trainID).getName(), trainPosition));
                    }
                    logWriter.flush();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
			}
		}
		else {
			// Add the train to the waiting list
			waitingTrains.add(trainID);
		}
	}

	public void deltint() {
		clock = clock + sigma;
		passivate();
	}

	public message out() {
		if (phaseIs("Messages Processed")) {
			// The messages are ready to go, send them
			return outMessages;
		}
		else {
			return new message();
		}
	}
}
