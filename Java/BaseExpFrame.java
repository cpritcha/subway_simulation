package Subway;

import view.modeling.ViewableDigraph;

import java.util.ArrayList;

public class BaseExpFrame extends ViewableDigraph{
    /* Common exp frame setup code for all classes goes here */

    protected Transducer _transducer;

    public BaseExpFrame(String name) {
        super(name);
        _transducer = new Transducer();
        add(_transducer);
    }

    public void addLoop(SubwayLoop loopLayout, TrainGroup trainGroup, ArrayList<Integer> initialPositions) {
        Scheduler scheduler = new Scheduler("Scheduler_" + loopLayout.getName().replaceAll(" ", ""), loopLayout, trainGroup, initialPositions);

        // Add the objects to the experimental frame
        add(loopLayout);
        add(trainGroup);
        add(scheduler);

        // Connect the train group to the scheduler
        coupleTrainGroupAndScheduler(trainGroup, scheduler);

        // Connect the scheduler to the station group
        coupleSchedulerAndLoop(loopLayout, scheduler);

        // Couple the scheduler to the transducer
        addCoupling(scheduler, Scheduler.OUT_N_PASSENGERS_DELIVERED_PORT, _transducer, Scheduler.OUT_N_PASSENGERS_DELIVERED_PORT);
        addCoupling(trainGroup, Train.OUT_WAIT_TIME_PORT, _transducer, Train.OUT_WAIT_TIME_PORT);
        
        // Couple the transducer to the train group
        addCoupling(_transducer, Transducer.OUT_STOP, trainGroup, Train.IN_STOP);
    }

    public void addSubwaySystemLoop(SubwaySystemLoopConfig config) {
        addLoop(config.loopLayout, config.trainGroup, config.initialPositions);
    }

    protected void coupleTrainGroupAndScheduler(TrainGroup trainGroup, Scheduler scheduler) {
        addCoupling(trainGroup, Train.OUT_PASSENGER_UNLOAD_PORT, scheduler, Scheduler.IN_PASSENGER_UNLOAD_PORT);
        addCoupling(trainGroup, Train.OUT_REQUEST_MOVE_TO_STATION_PORT, scheduler, Train.OUT_REQUEST_MOVE_TO_STATION_PORT);
        addCoupling(trainGroup, Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT, scheduler, Train.OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT);

        addCoupling(scheduler, Train.IN_BREAKDOWN_PORT, trainGroup, Train.IN_BREAKDOWN_PORT);
        addCoupling(scheduler, Train.IN_MOVE_TO_STATION_PORT, trainGroup, Train.IN_MOVE_TO_STATION_PORT);
        addCoupling(scheduler, Train.IN_MOVE_TO_TRACK_SECTION_PORT, trainGroup, Train.IN_MOVE_TO_TRACK_SECTION_PORT);
        addCoupling(scheduler, Train.IN_PASSENGER_LOAD_PORT, trainGroup, Train.IN_PASSENGER_LOAD_PORT);
        addCoupling(scheduler, Scheduler.OUT_PASSENGER_LOAD_PORT, trainGroup, Train.IN_PASSENGER_LOAD_PORT);
    }

    protected void coupleSchedulerAndLoop(SubwayLoop loopLayout, Scheduler scheduler) {
        addCoupling(scheduler, Train.OUT_PASSENGER_UNLOAD_PORT, loopLayout, Train.OUT_PASSENGER_UNLOAD_PORT);
        addCoupling(loopLayout, Train.IN_PASSENGER_LOAD_PORT, scheduler, Train.IN_PASSENGER_LOAD_PORT);
        addCoupling(scheduler, TrackSection.IN_ACQUIRE_PORT, loopLayout, TrackSection.IN_ACQUIRE_PORT);
        addCoupling(scheduler, TrackSection.IN_RELEASE_PORT, loopLayout, TrackSection.IN_RELEASE_PORT);
        addCoupling(loopLayout, TrackSection.OUT_ACQUIRE_PORT, scheduler, TrackSection.OUT_ACQUIRE_PORT);
        addCoupling(loopLayout, TrackSection.OUT_RELEASE_PORT, scheduler, TrackSection.OUT_RELEASE_PORT);
    }
}
