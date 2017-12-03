package Subway;

import view.modeling.ViewableAtomic;
import view.modeling.ViewableDigraph;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class BaseExpFrame extends ViewableDigraph{
    /* Common exp frame setup code for all classes goes here */

    protected Transducer _transducer;

    public BaseExpFrame(String name) {
        super(name);
        _transducer = new Transducer();
        add(_transducer);
    }

    public void addLoop(SubwayLoop currentLoop, TrainGroup currentTrainGroup, ArrayList<Integer> currentInitialPositions) {
        Scheduler currentScheduler = new Scheduler("Scheduler_" + currentLoop.getName().replaceAll(" ", ""), currentLoop, currentTrainGroup, currentInitialPositions);

        // Add the objects to the experimental frame
        add(currentLoop);
        add(currentTrainGroup);
        add(currentScheduler);

        // Connect the train group to the scheduler
        coupleTrainGroupAndScheduler(currentTrainGroup, currentScheduler);

        // Connect the scheduler to the station group
        coupleSchedulerAndLoop(currentLoop, currentScheduler);

        // Couple the scheduler to the transducer
        addCoupling(currentScheduler, Scheduler.OUT_N_PASSENGERS_DELIVERED_PORT, _transducer, Scheduler.OUT_N_PASSENGERS_DELIVERED_PORT);
        addCoupling(currentTrainGroup, Train.OUT_WAIT_TIME_PORT, _transducer, Train.OUT_WAIT_TIME_PORT);
    }

    public void addSubwaySystemLoop(SubwaySystemLoop subwaySystemLoop) {
        addLoop(subwaySystemLoop.loop, subwaySystemLoop.trainGroup, subwaySystemLoop.initialPositions);
    }

    protected void coupleTrainGroupAndScheduler(TrainGroup currentTrainGroup, Scheduler currentScheduler) {
        addCoupling(currentTrainGroup, Train.OUT_PASSENGER_UNLOAD_PORT, currentScheduler, Scheduler.IN_PASSENGER_UNLOAD_PORT);
        addCoupling(currentTrainGroup, Train.OUT_REQUEST_MOVE_TO_STATION_PORT, currentScheduler, Train.OUT_REQUEST_MOVE_TO_STATION_PORT);
        addCoupling(currentTrainGroup, Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT, currentScheduler, Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT);

        addCoupling(currentScheduler, Train.IN_BREAKDOWN_PORT, currentTrainGroup, Train.IN_BREAKDOWN_PORT);
        addCoupling(currentScheduler, Train.IN_MOVE_TO_STATION_PORT, currentTrainGroup, Train.IN_MOVE_TO_STATION_PORT);
        addCoupling(currentScheduler, Train.IN_MOVE_TO_TRACK_SECTION_PORT, currentTrainGroup, Train.IN_MOVE_TO_TRACK_SECTION_PORT);
        addCoupling(currentScheduler, Train.IN_PASSENGER_LOAD_PORT, currentTrainGroup, Train.IN_PASSENGER_LOAD_PORT);
        addCoupling(currentScheduler, Scheduler.OUT_PASSENGER_LOAD_PORT, currentTrainGroup, Train.IN_PASSENGER_LOAD_PORT);
    }

    protected void coupleSchedulerAndLoop(SubwayLoop currentLoop, Scheduler currentScheduler) {
        addCoupling(currentScheduler, Train.OUT_PASSENGER_UNLOAD_PORT, currentLoop, Train.OUT_PASSENGER_UNLOAD_PORT);
        addCoupling(currentLoop, Train.IN_PASSENGER_LOAD_PORT, currentScheduler, Train.IN_PASSENGER_LOAD_PORT);
        addCoupling(currentScheduler, TrackSection.IN_ACQUIRE_PORT, currentLoop, TrackSection.IN_ACQUIRE_PORT);
        addCoupling(currentScheduler, TrackSection.IN_RELEASE_PORT, currentLoop, TrackSection.IN_RELEASE_PORT);
        addCoupling(currentLoop, TrackSection.OUT_ACQUIRE_PORT, currentScheduler, TrackSection.OUT_ACQUIRE_PORT);
        addCoupling(currentLoop, TrackSection.OUT_RELEASE_PORT, currentScheduler, TrackSection.OUT_RELEASE_PORT);
    }
}
