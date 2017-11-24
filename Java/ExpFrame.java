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
	CircularList<ViewableAtomic> eastLoop;
	CircularList<ViewableAtomic> westLoop;
	ArrayList<String> tracks;
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
		
		// Define the tracks from Kennedy to McCowan
		
		
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
		eastLoop = new CircularList(eastStationNames.size()*2);
		westLoop = new CircularList(westStationNames.size()*2);
		
		// Trains
		// Note: According to the wikipedia Line 3 pages, 6 four-car trains operate
		// on the route, though it is reduced to 5 for construction until 2018.
		// From here: https://en.wikipedia.org/wiki/S-series_(Toronto_subway) the
		// train capacity is 30 seated, 55 standing
		
		// Instantiate the scheduler
	}
	
}
