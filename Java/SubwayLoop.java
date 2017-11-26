package Subway;

import view.modeling.ViewableAtomic;
import view.modeling.ViewableDigraph;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Map;

public class SubwayLoop extends ViewableDigraph {
	
	CircularList<ViewableAtomic> subwayLoop;
	
	// Here we leave the tracks as an input already instantiated
	// rather than creating them locally.  This allows us to use
	// references to track instances, which effectively means
	// we can share track instances among multiple loops.
	public SubwayLoop(String name, ArrayList<String> StationNames,
			ArrayList<TrackSection> TrackSections, Map<String,Integer> PassengerCreationRates,
			Map<String,Integer> InitialPassengerCounts) {
		
		super(name);
		
		// Add the inports
		addInport(Train.OUT_PASSENGER_UNLOAD_PORT);
		
		// Add the outports
		addOutport(Train.IN_PASSENGER_LOAD_PORT);

		// Build the loop composed of stations and tracks.
		// We start with a station and end with a track.
		// It is assumed the final track links the final station
		// and the first station.
		subwayLoop = new CircularList(StationNames.size()*2);
		int stationIndex, trackIndex;
		String cName;
		Station currentStation;
		TrackSection currentTrack;
		for (int k=0; k<StationNames.size(); k++) {
			stationIndex = 2*k;
			trackIndex = 2*k+1;
			cName = StationNames.get(k);
			
			// Make a list of possible destinations
			ArrayList<String> possibleDestinations = new ArrayList<String>(StationNames);
			// Remove the current station
			possibleDestinations.remove(cName);
			
			
			currentStation = new Station(cName,PassengerCreationRates.get(cName),
					InitialPassengerCounts.get(cName),possibleDestinations);
			currentTrack = TrackSections.get(k);
			
			subwayLoop.add(stationIndex, currentStation);
			subwayLoop.add(trackIndex, currentTrack);
			
			// Couple the inports
			addCoupling(this,Train.OUT_PASSENGER_UNLOAD_PORT,currentStation,Train.OUT_PASSENGER_UNLOAD_PORT);
			
			// Couple the outports
			addCoupling(currentStation,Train.IN_PASSENGER_LOAD_PORT,this,Train.IN_PASSENGER_LOAD_PORT);
			
			add(currentStation);
			add(currentTrack);
		}
		
		initialize();
		
	}

}
