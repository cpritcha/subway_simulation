package Subway;

import view.modeling.ViewableDigraph;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;

public class ExpFrame extends ViewableDigraph {
	
	ArrayList<String> eastStationNames;
	ArrayList<String> westStationNames;
	ArrayList<Train> eastTrains;
	ArrayList<Train> westTrains;
	CircularList<ViewableAtomic> eastLoop;
	CircularList<ViewableAtomic> westLoop;
	ArrayList<Integer> trackLengths;
	ArrayList<TrackSection> tracks;
	Map<String,Integer> passengerCreationRates;
	Map<String,Integer> initialPassengerCounts;

	public ExpFrame() {
		super("Experimental Frame");
		
		// Add the scheduler
		Scheduler sch = new Scheduler();
		add(sch);
		
		// Define the station names (in order)
		eastStationNames = new ArrayList<String>(Arrays.asList("Kennedy",
				"Lawrence East","Ellesmere","Midland","Scarborough Centre","McCowan"));
		westStationNames = new ArrayList<String>(Arrays.asList("McCowan","Scarborough Centre",
				"Midland","Ellesmere","Lawrence East","Kennedy"));
		
		// Define the track lengths from Kennedy to McCowan.
		// The last element is the long segment from McCowan to Kennedy
		trackLengths = new ArrayList<Integer>(Arrays.asList(3,2,1,1,1,9));
		tracks = new ArrayList<TrackSection>(trackLengths.size());
		for (int k=0; k<trackLengths.size(); k++) {
			tracks.set(k, new TrackSection(trackLengths.get(k)));
		}
		
		// Found typical business day ridership here:
		// https://www1.toronto.ca/wps/portal/contentonly?vgnextoid=c077c316f16e8410VgnVCM10000071d60f89RCRD
		//
		// To compute rates below, take values from spreadsheet, divide riders over
		// 12 hours and then divide by 2 (since we have one 'station' per direction
		// for each stop)
		
		// Define the passenger creation rates
		passengerCreationRates.put("Kennedy", 13); 				// 17,969
		passengerCreationRates.put("Lawrence East", 3);		// 4,326
		passengerCreationRates.put("Ellesmere", 1);			// 865
		passengerCreationRates.put("Midland", 1);				// 1,358
		passengerCreationRates.put("Scarborough Centre", 8);	// 10,979
		passengerCreationRates.put("McCowan", 2);				// 2,857
		
		// Define the initial passenger counts
		initialPassengerCounts.put("Kennedy", 10);
		initialPassengerCounts.put("Lawrence East", 10);
		initialPassengerCounts.put("Ellesmere", 10);
		initialPassengerCounts.put("Midland", 10);
		initialPassengerCounts.put("Scarborough Centre", 10);
		initialPassengerCounts.put("McCowan", 10);
		
		// Instantiate all of the stations
		// We use size*2 so we can include the tracks as well.
		// If index%2==0, then we have a station, else if
		// index%2==1, then we have tracks
		eastLoop = new CircularList(eastStationNames.size()*2);
		int stationIndex, trackIndex;
		String cName;
		for (int k=0; k<eastStationNames.size(); k++) {
			stationIndex = 2*k;
			trackIndex = 2*k+1;
			cName = eastStationNames.get(k);
			
			// Make a list of possible destinations
			ArrayList<String> possibleDestinations = new ArrayList<String>(eastStationNames);
			// Remove the current station
			possibleDestinations.remove(cName);
			
			eastLoop.set(stationIndex, new Station(cName,passengerCreationRates.get(cName),
					initialPassengerCounts.get(cName),possibleDestinations));
			eastLoop.set(trackIndex, tracks.get(k));
		}
		
		westLoop = new CircularList(westStationNames.size()*2);
		int trackReverseIndex;
		for (int k=0; k<westStationNames.size(); k++) {
			stationIndex = 2*k;
			trackIndex = 2*k+1;
			cName = westStationNames.get(k);
			
			// Make a list of possible destinations
			ArrayList<String> possibleDestinations = new ArrayList<String>(westStationNames);
			// Remove the current station
			possibleDestinations.remove(cName);
			
			// Remember to add the tracks in reverse order,
			// but we have to add the last track element last, so start
			// from the second index from the end
			if (k<tracks.size()-1) {
				trackReverseIndex = tracks.size()-2-k;
			}
			else {
				trackReverseIndex = k;
			}
			westLoop.set(stationIndex, new Station(cName,passengerCreationRates.get(cName),
					initialPassengerCounts.get(cName),possibleDestinations));
			westLoop.set(trackIndex, tracks.get(tracks.size()-1-k));
		}
		
		// Trains
		// Note: According to the wikipedia Line 3 pages, 6 four-car trains operate
		// on the route, though it is reduced to 5 for construction until 2018.
		// From here: https://en.wikipedia.org/wiki/S-series_(Toronto_subway) the
		// train capacity is 30 seated, 55 standing
		//
		// Add three trains for each direction
		eastTrains = new ArrayList<Train>(3);
		eastTrains.add(new Train("ET1"));
		eastTrains.add(new Train("ET2"));
		eastTrains.add(new Train("ET3"));
		
		westTrains = new ArrayList<Train>(3);
		westTrains.add(new Train("WT1"));
		westTrains.add(new Train("WT2"));
		westTrains.add(new Train("WT3"));
		
		// Specify the train starting positions
		ArrayList<Integer> eastTrainPositions = new ArrayList<Integer>(Arrays.asList(4,2,0));
		ArrayList<Integer> westTrainPositions = new ArrayList<Integer>(Arrays.asList(4,2,0));
		
		// Instantiate the scheduler
		
	}
	
}
