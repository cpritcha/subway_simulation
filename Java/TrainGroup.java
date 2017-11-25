package Subway;

import view.modeling.ViewableDigraph;

import java.util.ArrayList;

public class TrainGroup extends ViewableDigraph {

    ArrayList<Train> trains;

    public TrainGroup(String name, ArrayList<String> TrainNames) {
        super(name);

        // Instantiate each of the train names
        trains = new ArrayList<Train>(TrainNames.size());
        for (String tname : TrainNames) {
            trains.add(new Train(tname));
        }

        // Add the inports
        addInport(Train.IN_PASSENGER_LOAD_PORT);
        addInport(Train.IN_BREAKDOWN_PORT);
        addInport(Train.IN_MOVE_TO_STATION_PORT);
        addInport(Train.IN_MOVE_TO_TRACK_SECTION_PORT);

        // Add the outports
        addOutport(Train.OUT_PASSENGER_UNLOAD_PORT);
        addOutport(Train.OUT_REQUEST_MOVE_TO_STATION_PORT);
        addOutport(Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT);

        // Couple all of the trains to the in and out ports
        for (Train train : trains) {
            // Add the train
            add(train);

            // Inports
            addCoupling(this, Train.IN_PASSENGER_LOAD_PORT, train, Train.IN_PASSENGER_LOAD_PORT);
            addCoupling(this, Train.IN_BREAKDOWN_PORT, train, Train.IN_BREAKDOWN_PORT);
            addCoupling(this, Train.IN_MOVE_TO_STATION_PORT, train, Train.IN_MOVE_TO_STATION_PORT);
            addCoupling(this, Train.IN_MOVE_TO_TRACK_SECTION_PORT, train, Train.IN_MOVE_TO_TRACK_SECTION_PORT);

            // Outports
            addCoupling(train, Train.OUT_PASSENGER_UNLOAD_PORT, this, Train.OUT_PASSENGER_UNLOAD_PORT);
            addCoupling(train, Train.OUT_REQUEST_MOVE_TO_STATION_PORT, this, Train.OUT_REQUEST_MOVE_TO_STATION_PORT);
            addCoupling(train, Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT, this, Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT);
        }

        initialize();
    }
}
