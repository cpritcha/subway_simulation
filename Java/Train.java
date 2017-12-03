package Subway;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import GenCol.doubleEnt;

import model.modeling.content;
import model.modeling.message;
import view.modeling.ViewableAtomic;

public class Train extends ViewableAtomic {
    public static final String IN_MOVE_TO_STATION_PORT = "inMoveToStationResponse";
    public static final String IN_MOVE_TO_TRACK_SECTION_PORT = "inMoveToTrackSectionResponse";
    public static final String OUT_REQUEST_MOVE_TO_STATION_PORT = "outRequestMoveToStation";
    public static final String OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT = "outRequestMoveToSection";
    public static final String IN_PASSENGER_LOAD_PORT = "inPassengerLoad";
    public static final String OUT_PASSENGER_UNLOAD_PORT = "outPassengerUnload";
    public static final String IN_BREAKDOWN_PORT = "inBreakdown";
    public static final String OUT_WAIT_TIME_PORT = "outWaitTime";
    public static final String OUT_DELAY_TIME_PORT = "outDelayTime";
    public static final String IN_STOP = "Stop";

    // Possible phases
    private static final String REQUEST_MOVE_TO_STATION = "request move to station";
    private static final String AWAITING_STATION_GO_AHEAD = "awaiting station go ahead";
    private static final String REQUEST_MOVE_TO_SECTION = "request move to section";
    private static final String AWAITING_SECTION_GO_AHEAD = "awaiting section go ahead";
    private static final String BEGIN_LOAD_UNLOAD = "begin load unload";
    private static final String AT_STATION = "at station";
    private static final String IN_TRANSIT = "in transit";

    private static final int PASSENGER_TOTAL_CAPACITY = 85; // 30 sitting and 55 standing

    private final UUID _id;
    private PassengerList _passengers;
    private PassengerList _unloadingPassengers;

    private PassengerList _initialPassengers;

    private UniformRandom _loadingTimeDistribution;
    private UniformRandom _delayTimeDistribution;
    private double _delayProbability;
    private double _waitTime;
    private double _delayTime;

    public Train(String name, UUID id, PassengerList passengers, UniformRandom loadingTimeDistribution,
                 double delayProbability, UniformRandom delayTimeDistribution) {
        super(name);
        _id = id;
        _initialPassengers = passengers;
        _passengers = new PassengerList();
        _passengers.addAll(_initialPassengers);
        
        // These are the delay times for loading
        // and unloading passengers.  The time should
        // be given in minutes
        _loadingTimeDistribution = loadingTimeDistribution;

        // Delay time in transit (minutes)
        _delayTimeDistribution = delayTimeDistribution;
        _delayProbability = delayProbability;

        initialize();

        addInport(IN_MOVE_TO_STATION_PORT);
        addOutport(OUT_REQUEST_MOVE_TO_STATION_PORT);

        addInport(IN_MOVE_TO_TRACK_SECTION_PORT);
        addOutport(OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT);

        addInport(IN_PASSENGER_LOAD_PORT);
        addOutport(OUT_PASSENGER_UNLOAD_PORT);
        
        addInport(IN_BREAKDOWN_PORT);
        addInport(IN_STOP);

        if (passengers.size() > 0) {
            addTestInput(IN_MOVE_TO_STATION_PORT, new KeyValueEntity<>(getID(), passengers.get(0).getDestination()));
            addTestInput(IN_PASSENGER_LOAD_PORT, new KeyValueEntity<>(getID(), passengers));
        }
        addTestInput(IN_MOVE_TO_TRACK_SECTION_PORT, new KeyValueEntity<Double>(getID(), 7.0));
        addTestInput(IN_BREAKDOWN_PORT, new KeyValueEntity<>(getID(), 2.0));
    }

    public Train(String name) {
        this(name, UUID.randomUUID(), new PassengerList(), new UniformRandom(15.0/60.0, 15.0/60.0),
                0.0, new UniformRandom(0.0, 0.0));
    }
    
    public Train(String name, UniformRandom loadingTimeDistribution,
                 double delayProbability, UniformRandom delayTimeDistribution) {
        // All trains must have a unique name for UUID generation to work
    	this(name, UUID.randomUUID(),new PassengerList(), loadingTimeDistribution,
                delayProbability, delayTimeDistribution);
    }

    public Train(UUID id) {
        this("Train", id, new PassengerList() {{
            add(new Passenger(UUID.randomUUID(), id));
            add(new Passenger(UUID.randomUUID(), id));
        }}, new UniformRandom(15.0/60.0, 15.0/60.0), 0.0, new UniformRandom(0.0, 0.0));
    }

    public Train() {
        this(UUID.randomUUID());
    }

    public void initialize() {
        _passengers = new PassengerList();
        _passengers.addAll(_initialPassengers);
        _unloadingPassengers = new PassengerList();
        _waitTime = 0;
        holdIn(IN_TRANSIT, 0);
    }

    public UUID getID() {
        return _id;
    }

    private Stream<content> getRelevantContent(message m) {
        return MessageFilterer.getRelevantContent(m, getID());
    }

    private Optional<UUID> getMoveToStationResponse(message m) {
        return getRelevantContent(m)
                .filter(c -> c.getPortName().equals(IN_MOVE_TO_STATION_PORT))
                .map(c -> ((KeyValueEntity<UUID>) c.getValue()).getValue())
                .sorted()
                .findFirst();
    }

    private Optional<Double> getMoveToSectionResponse(message m) {
        return getRelevantContent(m)
                .filter(c -> c.getPortName().equals(IN_MOVE_TO_TRACK_SECTION_PORT))
                .map(c -> ((KeyValueEntity<Double>) c.getValue()).getValue())
                .reduce((a, b) -> a + b);
    }

    private Optional<PassengerList> getPassengerLoad(message m) {
        Stream<content> cs = getRelevantContent(m);
        Optional<PassengerList> loadingPassengers = cs.filter(c -> c.getPortName().equals(IN_PASSENGER_LOAD_PORT))
                .map(c -> ((KeyValueEntity<PassengerList>) c.getValue()).getValue())
                .reduce((pl_all, pl) -> {
                    pl_all.addAll(pl);
                    return pl_all;
                });
        loadingPassengers.ifPresent(lps -> {
            if (_passengers.size() + lps.size() > PASSENGER_TOTAL_CAPACITY) {
                String msg = String.format("Train %s does not have enough capacity to admit %d _passengers. " +
                        "Current number of _passengers is %d", getName(), lps.size(), _passengers.size());
                throw new RuntimeException(msg);
            }
        });
        return loadingPassengers;
    }

    private double getBreakdown(message m) {
        return getRelevantContent(m)
                .map(c -> ((KeyValueEntity<Double>) c.getValue()).getValue())
                .reduce(0.0, (a, b) -> a + b);
    }

    public void deltint() {
        switch (phase) {
            case IN_TRANSIT:
                holdIn(REQUEST_MOVE_TO_STATION, 0);
                _delayTime = 0;
                break;
            case REQUEST_MOVE_TO_STATION:
            	// Reset the wait time
            	_waitTime = 0;
                passivateIn(AWAITING_STATION_GO_AHEAD);
                break;
            case BEGIN_LOAD_UNLOAD:
                _unloadingPassengers.clear();
                passivateIn(AT_STATION);
                break;
            case REQUEST_MOVE_TO_SECTION:
            	// Reset the wait time
            	_waitTime = 0;
                passivateIn(AWAITING_SECTION_GO_AHEAD);
                break;
        }
    }

    public void deltext(double e, message x) {
    	Continue(e);
    	// First check for a stop signal
    	for (int k = 0; k < x.size(); k++) {
    		if (messageOnPort(x,IN_STOP,k)) {
    			passivate();
    		}
    	}
    	
    	// Else process accordingly
        switch (phase) {
            case IN_TRANSIT:
                double breakdownTime = getBreakdown(x);
                if (breakdownTime > 0) {
                    System.out.println(String.format("Train %s delayed: %.2f minutes", getName(), breakdownTime));
                }
                sigma = sigma - e + breakdownTime;
                break;
            case AWAITING_STATION_GO_AHEAD:
            	// Update the wait time
            	_waitTime += e;
                Optional<UUID> stationGoAhead = getMoveToStationResponse(x);
                if (stationGoAhead.isPresent()) {
                    UUID id = stationGoAhead.get();
                    _unloadingPassengers = _passengers.stream()
                            .filter(p -> p.getDestination().equals(id))
                            .collect(Collectors.toCollection(PassengerList::new));
                    _passengers.removeIf(p -> p.getDestination().equals(id));
                    
                    // Compute the time required to unload passengers
                    double unloadTime = _loadingTimeDistribution.draw();
                    holdIn(BEGIN_LOAD_UNLOAD, unloadTime);
                }
                break;
            case AT_STATION:
            	// Compute the time required to load passengers
                double loadTime = _loadingTimeDistribution.draw();
            	
                Optional<PassengerList> loadingPassengers = getPassengerLoad(x);
                loadingPassengers.ifPresent(lps -> {
                    _passengers.addAll(lps);
                    
                    holdIn(REQUEST_MOVE_TO_SECTION, loadTime);
                });
                break;
            case AWAITING_SECTION_GO_AHEAD:
            	// Update the wait time
            	_waitTime += e;
                Optional<Double> sectionGoAhead = getMoveToSectionResponse(x);
                sectionGoAhead.ifPresent(time -> {
                    _delayTime = _delayTimeDistribution.nextDouble() > _delayProbability ? 0.0 :
                            _delayTimeDistribution.draw();
                    holdIn(IN_TRANSIT, time + _delayTime);
                });
                break;
        }
    }

    public message out() {
        message m = new message();
        switch (phase) {
            case REQUEST_MOVE_TO_SECTION:
                KeyEntity moveToTrackSectionRequest = new KeyEntity(getID());
                moveToTrackSectionRequest.print();
                m.add(makeContent(OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT, moveToTrackSectionRequest));
                break;
            case REQUEST_MOVE_TO_STATION:
                KeyEntity moveToStationRequest = new KeyEntity(getID());
                moveToStationRequest.print();
                m.add(makeContent(OUT_REQUEST_MOVE_TO_STATION_PORT, moveToStationRequest));
                break;
            case BEGIN_LOAD_UNLOAD:
                KeyValueEntity<PassengerUnloadRequest> pur = new KeyValueEntity<>(getID(), new PassengerUnloadRequest(
                        PASSENGER_TOTAL_CAPACITY - _passengers.size(), (PassengerList)_unloadingPassengers.clone()));
                pur.print();
                m.add(makeContent(OUT_PASSENGER_UNLOAD_PORT, pur));
                
                // Also output the wait time for going from track to station
                m.add(makeContent(OUT_WAIT_TIME_PORT,new doubleEnt(_waitTime)));
                break;
            case IN_TRANSIT:
            	// Output the wait time for going from station to track
                if (_delayTime > 0) {
                    m.add(makeContent(OUT_DELAY_TIME_PORT, new doubleEnt(_delayTime)));
                }
            	m.add(makeContent(OUT_WAIT_TIME_PORT,new doubleEnt(_waitTime)));
            	break;
        }
        return m;
    }
}
