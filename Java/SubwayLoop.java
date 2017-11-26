package Subway;

import view.modeling.ViewableAtomic;
import view.modeling.ViewableDigraph;

import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;

public class SubwayLoop extends ViewableDigraph {
	
	private CircularList<ViewableAtomic> subwayLoop;
	
	// Here we leave the tracks as an input already instantiated
	// rather than creating them locally.  This allows us to use
	// references to track instances, which effectively means
	// we can share track instances among multiple loops.
	public SubwayLoop(String name, ArrayList<TrackSection> trackSections, ArrayList<Station> stations) {
		
		super(name);
		
		// Add the inports
		addInport(Train.OUT_PASSENGER_UNLOAD_PORT);
		addInport(TrackSection.IN_ACQUIRE_PORT);
		addInport(TrackSection.IN_RELEASE_PORT);
		
		// Add the outports
		addOutport(Train.IN_PASSENGER_LOAD_PORT);
		addOutport(TrackSection.OUT_ACQUIRE_PORT);
		addOutport(TrackSection.OUT_RELEASE_PORT);

		// Build the loop composed of stations and tracks.
		// We start with a station and end with a track.
		// It is assumed the final track links the final station
		// and the first station.
        subwayLoop = new CircularList(stations.size() * 2);
        int stationIndex, trackIndex;
        Station currentStation;
        TrackSection currentTrack;
        for (int k = 0; k < trackSections.size(); k++) {
            stationIndex = 2 * k;
            trackIndex = 2 * k + 1;

            currentTrack = trackSections.get(k);
            currentStation = stations.get(k);

            subwayLoop.add(stationIndex, currentStation);
            subwayLoop.add(trackIndex, currentTrack);
			
			subwayLoop.add(stationIndex, currentStation);
			subwayLoop.add(trackIndex, currentTrack);
			
			// Couple the inports
			addCoupling(this,Train.OUT_PASSENGER_UNLOAD_PORT,currentStation,Train.OUT_PASSENGER_UNLOAD_PORT);
			addCoupling(this,TrackSection.IN_ACQUIRE_PORT,currentTrack,TrackSection.IN_ACQUIRE_PORT);
			addCoupling(this,TrackSection.IN_RELEASE_PORT,currentTrack,TrackSection.IN_RELEASE_PORT);
			
			// Couple the outports
			addCoupling(currentStation,Train.IN_PASSENGER_LOAD_PORT,this,Train.IN_PASSENGER_LOAD_PORT);
			addCoupling(currentTrack,TrackSection.OUT_ACQUIRE_PORT,this,TrackSection.OUT_ACQUIRE_PORT);
			addCoupling(currentTrack,TrackSection.OUT_RELEASE_PORT,this,TrackSection.OUT_RELEASE_PORT);
			
			add(currentStation);
			add(currentTrack);
		}
		
		initialize();
		
	}
	
	public int size() {
		return this.subwayLoop.size();
	}
	
	public ViewableAtomic get(int index) {
		return this.subwayLoop.get(index);
	}
}
