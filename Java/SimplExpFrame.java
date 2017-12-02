package Subway;

import view.modeling.ViewableDigraph;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;

import java.awt.Dimension;
import java.awt.Point;
import java.util.*;
import java.util.stream.Collectors;

public class SimplExpFrame extends ViewableDigraph {

	public SimplExpFrame() {
		super("Simple Experimental Frame");

		// Define the track lengths from Kennedy to McCowan.
		// The last element is the long segment from McCowan to Kennedy
		ArrayList<Integer> trackLengths = new ArrayList<Integer>(Arrays.asList(3,4));
		System.out.println("trackLengths.size = "+trackLengths.size());
		ArrayList<TrackSection> tracks = new ArrayList<TrackSection>(trackLengths.size());
		System.out.println("tracks.size = "+tracks.size());
		for (int k=0; k<trackLengths.size(); k++) {
			System.out.println("k = "+k);
			tracks.add(k, new TrackSection(trackLengths.get(k)));
		}
		
		// Found typical business day ridership here:
		// https://www1.toronto.ca/wps/portal/contentonly?vgnextoid=c077c316f16e8410VgnVCM10000071d60f89RCRD
		//
		// To compute rates below, take values from spreadsheet, divide riders over
		// 12 hours and then divide by 2 (since we have one 'station' per direction
		// for each stop)

		Object[][] stationData = {
				// Station Name, Passenger Creation Rate
				{"Kennedy", 13 /* 17,969 */},
				{"Lawrence East", 3 /* 4,326 */},
		};

		ArrayList<Station.Builder> stationBuilders = Station.Builder.fromData(stationData);

		ArrayList<Station> stations = stationBuilders.stream()
				.map(Station.Builder::createStation)
				.collect(Collectors.toCollection(ArrayList::new));

		// Create an array of loops for passing to the scheduler
		SubwayLoop eastLoop = new SubwayLoop("Scarborough East", tracks, stations);
		ArrayList<SubwayLoop> loops = new ArrayList<SubwayLoop>();
		loops.add(eastLoop);
		
		// Trains
		// Note: According to the wikipedia Line 3 pages, 6 four-car trains operate
		// on the route, though it is reduced to 5 for construction until 2018.
		// From here: https://en.wikipedia.org/wiki/S-series_(Toronto_subway) the
		// train capacity is 30 seated, 55 standing
		//
		// Add three trains for each direction
		ArrayList<String> eastTrainNames = new ArrayList<String>(Arrays.asList("ET1"));
		
		// Put the trains into groups
		double minLoadTime = 15.0/60.0; // Minimum load time in minutes
		double maxLoadDisturbanceTime = 0.0/60.0; // Maximum additional load time in minutes (random uniform distribution)
		TrainGroup eastTrainGroup = new TrainGroup("East Trains",eastTrainNames,minLoadTime,maxLoadDisturbanceTime);
		
		// Add all the train groups into a list
		ArrayList<TrainGroup> trainGroups = new ArrayList<TrainGroup>();
		trainGroups.add(eastTrainGroup);
		
		// Specify the train starting positions
		ArrayList<Integer> eastTrainPositions = new ArrayList<Integer>(Arrays.asList(1));

		// Create an array of train starting positions for passing to the scheduler
		ArrayList<ArrayList<Integer>> initialTrainPositions = new ArrayList<ArrayList<Integer>>();
		initialTrainPositions.add(eastTrainPositions);
		
		// For each of the train groups and Subway Loops, create scheduler
		TrainGroup currentTrainGroup;
		Scheduler currentScheduler;
		SubwayLoop currentLoop;
		ArrayList<Integer> currentInitialPositions;
		
		// Add the transducer
		ViewableAtomic transducer = new Transducer();
		add(transducer);
		
		ArrayList<Scheduler> schedulers = new ArrayList<Scheduler>();
		for (int k=0; k<loops.size(); k++) {
			currentLoop = loops.get(k);
			currentTrainGroup = trainGroups.get(k);
			currentInitialPositions = initialTrainPositions.get(k);
			currentScheduler = new Scheduler("Scheduler_"+currentLoop.getName().replaceAll(" ", ""),currentLoop,currentTrainGroup,currentInitialPositions);
			schedulers.add(currentScheduler);
			
			// Add the objects to the experimental frame
			add(currentLoop);
			add(currentTrainGroup);
			add(currentScheduler);
			
			// Connect the train group to the scheduler
			addCoupling(currentTrainGroup,Train.OUT_PASSENGER_UNLOAD_PORT,currentScheduler,Scheduler.IN_PASSENGER_UNLOAD_PORT);
			addCoupling(currentTrainGroup,Train.OUT_REQUEST_MOVE_TO_STATION_PORT,currentScheduler,Train.OUT_REQUEST_MOVE_TO_STATION_PORT);
			addCoupling(currentTrainGroup,Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT,currentScheduler,Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT);
			
			addCoupling(currentScheduler,Train.IN_BREAKDOWN_PORT,currentTrainGroup,Train.IN_BREAKDOWN_PORT);
			addCoupling(currentScheduler,Train.IN_MOVE_TO_STATION_PORT,currentTrainGroup,Train.IN_MOVE_TO_STATION_PORT);
			addCoupling(currentScheduler,Train.IN_MOVE_TO_TRACK_SECTION_PORT,currentTrainGroup,Train.IN_MOVE_TO_TRACK_SECTION_PORT);
			addCoupling(currentScheduler,Train.IN_PASSENGER_LOAD_PORT,currentTrainGroup,Train.IN_PASSENGER_LOAD_PORT);
			addCoupling(currentScheduler,Scheduler.OUT_PASSENGER_LOAD_PORT,currentTrainGroup,Train.IN_PASSENGER_LOAD_PORT);

			// Connect the scheduler to the station group
			addCoupling(currentScheduler,Train.OUT_PASSENGER_UNLOAD_PORT,currentLoop,Train.OUT_PASSENGER_UNLOAD_PORT);
			addCoupling(currentLoop,Train.IN_PASSENGER_LOAD_PORT,currentScheduler,Train.IN_PASSENGER_LOAD_PORT);
			addCoupling(currentScheduler,TrackSection.IN_ACQUIRE_PORT,currentLoop,TrackSection.IN_ACQUIRE_PORT);
			addCoupling(currentScheduler,TrackSection.IN_RELEASE_PORT,currentLoop,TrackSection.IN_RELEASE_PORT);
			addCoupling(currentLoop,TrackSection.OUT_ACQUIRE_PORT,currentScheduler,TrackSection.OUT_ACQUIRE_PORT);
			addCoupling(currentLoop,TrackSection.OUT_RELEASE_PORT,currentScheduler,TrackSection.OUT_RELEASE_PORT);

			// Couple the scheduler to the transducer
			addCoupling(currentScheduler,Scheduler.OUT_N_PASSENGERS_DELIVERED_PORT,transducer,Scheduler.OUT_N_PASSENGERS_DELIVERED_PORT);
			addCoupling(currentTrainGroup,Train.OUT_WAIT_TIME_PORT,transducer,Train.OUT_WAIT_TIME_PORT);
		}

		initialize();

	}

}
