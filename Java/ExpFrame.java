package Subway;

import javafx.util.Pair;
import view.modeling.ViewableDigraph;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;

import java.awt.Dimension;
import java.awt.Point;
import java.util.*;
import java.util.stream.Collectors;

public class ExpFrame extends ViewableDigraph {

	public ExpFrame() {
		super("Experimental Frame");
		
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

        Object[][] stationData = {
                // Station Name, Passenger Creation Rate
                {"Kennedy", 13 /* 17,969 */},
                {"Lawrence East", 3 /* 4,326 */},
                {"Ellesmere", 1 /* 864 */},
                {"Midland", 1 /* 1,358 */},
                {"Scarborough Centre", 8 /* 10,979 */},
                {"McCowan", 2 /* 2,857 */}
        };

        ArrayList<Station.Builder> stationBuilders = Station.Builder.fromData(stationData);

        ArrayList<Station.Builder> reverseStationBuilders = new ArrayList<>();
        reverseStationBuilders.addAll(stationBuilders);
        Collections.reverse(reverseStationBuilders);

        ArrayList<Station> stations = stationBuilders.stream()
                .map(Station.Builder::createStation)
                .collect(Collectors.toCollection(ArrayList::new));

        ArrayList<Station> reversedStations = reverseStationBuilders.stream()
                .map(Station.Builder::createStation)
                .collect(Collectors.toCollection(ArrayList::new));

        // Create an array of loops for passing to the scheduler
        SubwayLoop eastLoop = new SubwayLoop("Scarborough East", tracks, stations);
        SubwayLoop westLoop = new SubwayLoop("Scarborough West", reversedTracks, reversedStations);
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
		ArrayList<Integer> eastTrainPositions = new ArrayList<Integer>(Arrays.asList(5,3,1));
		ArrayList<Integer> westTrainPositions = new ArrayList<Integer>(Arrays.asList(5,3,1));
		
		// Create an array of train starting positions for passing to the scheduler
		ArrayList<ArrayList<Integer>> initialTrainPositions = new ArrayList<ArrayList<Integer>>();
		initialTrainPositions.add(eastTrainPositions);
		initialTrainPositions.add(westTrainPositions);
		
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
