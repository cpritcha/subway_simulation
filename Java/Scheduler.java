package Subway;

import GenCol.*;
import view.modeling.ViewableAtomic;
import model.modeling.message;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.ArrayList;

public class Scheduler extends ViewableAtomic {
	
	// Use a priority queue to keep track of waiting trains.
	// This way we can order the queue by station number and work
	// backwards to completely clear any jams.
	protected PriorityQueue waitingTrains;
	
	// Containers for the passed in loops, trains, and train positions
	SubwayLoop loop;
	TrainGroup trains;
	ArrayList<Integer> trainPositions;
	
	// Use a map to store the train positions
	private Map<String, Integer> eastTrainPositions;
	private Map<String, Integer> westTrainPositions;
	
	public Scheduler(SubwayLoop Loop, TrainGroup Trains, ArrayList<Integer> InitialTrainPositions) {
		super("Scheduler");
		
		// Create a queue to store waiting trains
		waitingTrains = new PriorityQueue();
		
		loop = Loop;
		trains = Trains;
		trainPositions = InitialTrainPositions;
		
		// Create input ports and output ports
		
		// From the train group
		addInport(Train.OUT_PASSENGER_UNLOAD_PORT);
		addInport(Train.OUT_REQUEST_MOVE_TO_STATION_PORT);
		addInport(Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT);
		
		// From the subway loop
		addInport(Train.IN_PASSENGER_LOAD_PORT);
		
		// To the train group
		addOutport(Train.IN_BREAKDOWN_PORT);
		addOutport(Train.IN_MOVE_TO_STATION_PORT);
		addOutport(Train.IN_MOVE_TO_TRACK_SECTION_PORT);
		
		// To the subway group
		addOutport(Train.OUT_PASSENGER_UNLOAD_PORT);
		
	}
	
	public void initialize() {
		passivate();
		super.initialize();
	}
	
	public void delext(double e, message x) {
		Continue(e);
		
		if (phaseIs("passivate")) {
			for (int k=0; k<x.size(); k++) {
				// Handle the move into station requests
				if (messageOnPort(x,Train.OUT_REQUEST_MOVE_TO_STATION_PORT,k)) {
					// Message form is ("Port",TrainID)
				}
				else if (messageOnPort(x,Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT,k)) {
					// Message form is ("Port",TrainID)
				}
				else if (messageOnPort(x,Train.OUT_PASSENGER_UNLOAD_PORT,k)) {
					// Message form is ("Port",(TrainID,Passengers,Capacity))
				}
			}
		}
	}
	
	public void deltint() {
		passivate();
	}
}
