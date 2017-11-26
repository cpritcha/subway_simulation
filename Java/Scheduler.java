package Subway;

import GenCol.*;
import view.modeling.ViewableAtomic;
import model.modeling.message;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.ArrayList;
import java.util.UUID;

public class Scheduler extends ViewableAtomic {
	
	// Use a priority queue to keep track of waiting trains.
	// This way we can order the queue by station number and work
	// backwards to completely clear any jams.
	protected PriorityQueue waitingTrains;
	
	// Containers for the passed in loops, trains, and train positions
	SubwayLoop loop;
	TrainGroup trains;
	
	// Use a map to store the train positions
	private Map<UUID, Integer> trainPositions;
	private ArrayList<Boolean> segmentPopulated;
	
	private message outMessages;
	
	protected static final String IN_PASSENGER_UNLOAD_PORT = "inPassengerUnload";
	
	public Scheduler(SubwayLoop Loop, TrainGroup Trains, ArrayList<Integer> InitialTrainPositions) {
		super("Scheduler");
		
		// Create a queue to store waiting trains
		waitingTrains = new PriorityQueue();
		
		loop = Loop;
		trains = Trains;
		
		// Populate the train positions
		for (int k=0; k<trains.size(); k++) {
			trainPositions.put(trains.get(k).getID(), InitialTrainPositions.get(k));
		}
		
		// Use a boolean array to keep track of which stations/tracks
		// are currently populated.
		segmentPopulated = new ArrayList<Boolean>();
		for (int k=0; k<loop.size(); k++) {
			segmentPopulated.set(k, false);
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
		
		
		// To the subway group
		// Stations
		addOutport(Train.OUT_PASSENGER_UNLOAD_PORT);
		// Tracks
		addOutport(TrackSection.IN_ACQUIRE_PORT);
		addOutport(TrackSection.IN_RELEASE_PORT);
		
	}
	
	public void initialize() {
		passivate();
		super.initialize();
	}
	
	public void delext(double e, message x) {
		Continue(e);
		
		// Create a new instance for the output messages
		outMessages = new message();
		
		if (phaseIs("passivate")) {
			
			for (int k=0; k<x.size(); k++) {
				// Handle the move into station requests
				if (messageOnPort(x,Train.OUT_REQUEST_MOVE_TO_STATION_PORT,k)) {
					// Message form is ("Port",TrainID)
				}
				else if (messageOnPort(x,Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT,k)) {
					// Message form is ("Port",TrainID)
					KeyEntity ent = (KeyEntity)x.getValOnPort(Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT, k);
					UUID trainID = ent.getID();
					
					// Get the train position
					int trainPosition = trainPositions.get(trainID);
					int nextPosition = (trainPosition+1)%loop.size();
					
					// Get the next track instance
					TrackSection nextTrack = (TrackSection)loop.get(trainPosition+1);
					
					// Check if the next segment is populated
					if (!segmentPopulated.get(trainPosition+1)) {
						// Tell the train it can move and increment all
						// the appropriate placeholders/indexes
						outMessages.add(makeContent(Train.IN_MOVE_TO_TRACK_SECTION_PORT,
								new KeyValueEntity<>(trainID,(double)nextTrack.getTravelTime())));
						
						// Set the 'move to' segment as true and the 'move from' as false
						segmentPopulated.set(nextPosition, true);
						segmentPopulated.set(trainPosition, false);
						
						// Locally increment the train position
						trainPositions.put(trainID, nextPosition);		
					}
				}
				else if (messageOnPort(x,Train.OUT_PASSENGER_UNLOAD_PORT,k)) {
					// Message form is ("Port",(TrainID,Passengers,Capacity))
				}
			}
			
			// The messages are ready to be sent out
			holdIn("Messages Processed",0);
		}
	}
	
	public void deltint() {
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
