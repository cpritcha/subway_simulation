package Subway;

import view.modeling.ViewableDigraph;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.HashMap;

public class ExpFrame extends ViewableDigraph {

	public ExpFrame() {
		super("Experimental Frame");
		
		// Define the station names (in order)
		ArrayList<String> eastStationNames = new ArrayList<String>(Arrays.asList("Kennedy",
				"Lawrence East","Ellesmere","Midland","Scarborough Centre","McCowan"));
		ArrayList<String> westStationNames = new ArrayList<String>(Arrays.asList("McCowan","Scarborough Centre",
				"Midland","Ellesmere","Lawrence East","Kennedy"));
		
		// Define the track lengths from Kennedy to McCowan.
		// The last element is the long segment from McCowan to Kennedy
		ArrayList<Integer> trackLengths = new ArrayList<Integer>(Arrays.asList(3,2,1,1,1,9));
		ArrayList<TrackSection> tracks = new ArrayList<TrackSection>(trackLengths.size());
		for (int k=0; k<trackLengths.size(); k++) {
			tracks.add(k, new TrackSection(trackLengths.get(k)));
		}
		
		ArrayList<TrackSection> reversedTracks = new ArrayList<TrackSection>(tracks.size());
		// Build a reversed array from McCowan to Kennedy using the same object references
		for (int k=0; k<trackLengths.size()-1; k++) {
			reversedTracks.add(k, tracks.get(tracks.size()-2-k));
		}
		// The last track here should also be the last track in the
		// original order
		int lastIndex = tracks.size()-1;
		reversedTracks.add(lastIndex, tracks.get(lastIndex));
		
		// Found typical business day ridership here:
		// https://www1.toronto.ca/wps/portal/contentonly?vgnextoid=c077c316f16e8410VgnVCM10000071d60f89RCRD
		//
		// To compute rates below, take values from spreadsheet, divide riders over
		// 12 hours and then divide by 2 (since we have one 'station' per direction
		// for each stop)
		
		// Define the passenger creation rates
		HashMap<String,Integer> passengerCreationRates = new HashMap<String,Integer>();
		passengerCreationRates.put("Kennedy", 13); 				// 17,969
		passengerCreationRates.put("Lawrence East", 3);		// 4,326
		passengerCreationRates.put("Ellesmere", 1);			// 865
		passengerCreationRates.put("Midland", 1);				// 1,358
		passengerCreationRates.put("Scarborough Centre", 8);	// 10,979
		passengerCreationRates.put("McCowan", 2);				// 2,857
		
		// Define the initial passenger counts
		HashMap<String,Integer> initialPassengerCounts = new HashMap<String,Integer>();
		initialPassengerCounts.put("Kennedy", 10);
		initialPassengerCounts.put("Lawrence East", 10);
		initialPassengerCounts.put("Ellesmere", 10);
		initialPassengerCounts.put("Midland", 10);
		initialPassengerCounts.put("Scarborough Centre", 10);
		initialPassengerCounts.put("McCowan", 10);
		
		// Create an array of loops for passing to the scheduler
		SubwayLoop eastLoop = new SubwayLoop("Scarborough East",eastStationNames,
				tracks,passengerCreationRates,initialPassengerCounts);
		SubwayLoop westLoop = new SubwayLoop("Scarborough West",westStationNames,
				reversedTracks,passengerCreationRates,initialPassengerCounts);
		ArrayList<SubwayLoop> loops = new ArrayList<SubwayLoop>();
		loops.add(eastLoop);
		loops.add(westLoop);
		
		// Trains
		// Note: According to the wikipedia Line 3 pages, 6 four-car trains operate
		// on the route, though it is reduced to 5 for construction until 2018.
		// From here: https://en.wikipedia.org/wiki/S-series_(Toronto_subway) the
		// train capacity is 30 seated, 55 standing
		//
		// Add three trains for each direction
		ArrayList<String> eastTrainNames = new ArrayList<String>(Arrays.asList("ET1","ET2","ET3"));
		ArrayList<String> westTrainNames = new ArrayList<String>(Arrays.asList("WT1","WT2","WT3"));
		
		// Put the trains into groups
		TrainGroup eastTrainGroup = new TrainGroup("East Trains",eastTrainNames);
		TrainGroup westTrainGroup = new TrainGroup("West Trains",westTrainNames);
		
		// Add all the train groups into a list
		ArrayList<TrainGroup> trainGroups = new ArrayList<TrainGroup>();
		trainGroups.add(eastTrainGroup);
		trainGroups.add(westTrainGroup);
		
		// Specify the train starting positions
		ArrayList<Integer> eastTrainPositions = new ArrayList<Integer>(Arrays.asList(4,2,0));
		ArrayList<Integer> westTrainPositions = new ArrayList<Integer>(Arrays.asList(4,2,0));
		
		// Create an array of train starting positions for passing to the scheduler
		ArrayList<ArrayList<Integer>> initialTrainPositions = new ArrayList<ArrayList<Integer>>();
		initialTrainPositions.add(eastTrainPositions);
		initialTrainPositions.add(westTrainPositions);
		
		// For each of the train groups and Subway Loops, create scheduler
		TrainGroup currentTrainGroup;
		Scheduler currentScheduler;
		SubwayLoop currentLoop;
		ArrayList<Integer> currentInitialPositions;
		
		ArrayList<Scheduler> schedulers = new ArrayList<Scheduler>();
		for (int k=0; k<loops.size(); k++) {
			currentLoop = loops.get(k);
			currentTrainGroup = trainGroups.get(k);
			currentInitialPositions = initialTrainPositions.get(k);
			currentScheduler = new Scheduler(currentLoop,currentTrainGroup,currentInitialPositions);
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
			
		}
		
		initialize();
	
	}
	
}
