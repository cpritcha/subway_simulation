package Subway;

import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

import java.awt.Dimension;
import java.awt.Point;
import java.util.ArrayList;

public class TrainGroup extends ViewableDigraph {

    protected ArrayList<Train> trains;

    public TrainGroup(String name, ArrayList<String> TrainNames, double minLoadTime, double maxLoadDisturbanceTime) {
        super(name);

        // Instantiate each of the train names
        trains = new ArrayList<Train>(TrainNames.size());
        for (String tname : TrainNames) {
            trains.add(new Train(tname,minLoadTime,maxLoadDisturbanceTime));
        }

        // Add the inports
        addInport(Train.IN_PASSENGER_LOAD_PORT);
        addInport(Train.IN_MOVE_TO_STATION_PORT);
        addInport(Train.IN_MOVE_TO_TRACK_SECTION_PORT);

        // Add the outports
        addOutport(Train.OUT_PASSENGER_UNLOAD_PORT);
        addOutport(Train.OUT_REQUEST_MOVE_TO_STATION_PORT);
        addOutport(Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT);
        addOutport(Train.OUT_WAIT_TIME_PORT);

        // Couple all of the trains to the in and out ports
        for (Train train : trains) {
            // Add the train
            add(train);

            // Inports
            addCoupling(this, Train.IN_PASSENGER_LOAD_PORT, train, Train.IN_PASSENGER_LOAD_PORT);
            addCoupling(this, Train.IN_MOVE_TO_STATION_PORT, train, Train.IN_MOVE_TO_STATION_PORT);
            addCoupling(this, Train.IN_MOVE_TO_TRACK_SECTION_PORT, train, Train.IN_MOVE_TO_TRACK_SECTION_PORT);

            // Outports
            addCoupling(train, Train.OUT_PASSENGER_UNLOAD_PORT, this, Train.OUT_PASSENGER_UNLOAD_PORT);
            addCoupling(train, Train.OUT_REQUEST_MOVE_TO_STATION_PORT, this, Train.OUT_REQUEST_MOVE_TO_STATION_PORT);
            addCoupling(train, Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT, this, Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT);
            addCoupling(train, Train.OUT_WAIT_TIME_PORT,this,Train.OUT_WAIT_TIME_PORT);
        }

        initialize();
    }

    public void addBreakdowns(CoupledBreakdownGenerator cbg) {
        add(cbg);
        for (Train train: trains) {
            String outBreakdownPortName = cbg.register(train);
            addCoupling(cbg, outBreakdownPortName, train, Train.IN_BREAKDOWN_PORT);
        }
        cbg.initialize();
    }

    public Train get(int index) {
    	return this.trains.get(index);
    }
    
    public int size() {
    	return this.trains.size();
    }
}
