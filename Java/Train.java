package Subway;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private Optional<String> _stationName;

    public Train(String name) {
        super(name);
        _id = UUID.randomUUID();

        initialize();

        addInport(IN_MOVE_TO_STATION_PORT);
        addOutport(OUT_REQUEST_MOVE_TO_STATION_PORT);

        addInport(IN_MOVE_TO_TRACK_SECTION_PORT);
        addOutport(OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT);

        addInport(IN_PASSENGER_LOAD_PORT);
        addOutport(OUT_PASSENGER_UNLOAD_PORT);
        
        addInport(IN_BREAKDOWN_PORT);

        addTestInput(IN_MOVE_TO_STATION_PORT, new KeyEntity(getID()));
        PassengerList pl = new PassengerList();
        pl.add(new Passenger("other", getName()));
        addTestInput(IN_PASSENGER_LOAD_PORT, new KeyValueEntity<>(getID(), pl));
        addTestInput(IN_MOVE_TO_TRACK_SECTION_PORT, new KeyValueEntity<Double>(getID(), 7.0));
        addTestInput(IN_BREAKDOWN_PORT, new KeyValueEntity<>(getID(), 2.0));
    }

    public Train() {
        this("Train");
    }

    public void initialize() {
        _passengers = new PassengerList();
        _unloadingPassengers = new PassengerList();
        _stationName = Optional.empty();
        holdIn(IN_TRANSIT, 0);
    }

    public UUID getID() {
        return _id;
    }

    private Stream<content> getRelevantContent(message m) {
        return MessageFilterer.getRelevantContent(m, getID());
    }

    private boolean getMoveToStationResponse(message m) {
        return getRelevantContent(m)
                .anyMatch(c -> c.getPortName().equals(IN_MOVE_TO_STATION_PORT));
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
                String msg = String.format("Train %s does not have enough capacity to admit %d passengers. " +
                        "Current number of passengers is %d", getName(), lps.size(), _passengers.size());
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
                break;
            case REQUEST_MOVE_TO_STATION:
                passivateIn(AWAITING_STATION_GO_AHEAD);
                break;
            case BEGIN_LOAD_UNLOAD:
                _unloadingPassengers = _passengers.stream()
                        .filter(p -> p.getDestination().equals(_stationName.get()))
                        .collect(Collectors.toCollection(PassengerList::new));
                _passengers.removeIf(p -> p.getDestination().equals(_stationName.get()));
                passivateIn(AT_STATION);
                break;
            case REQUEST_MOVE_TO_SECTION:
                passivateIn(AWAITING_SECTION_GO_AHEAD);
                break;
        }
    }

    public void deltext(double e, message x) {
        switch (phase) {
            case IN_TRANSIT:
                double breakdownTime = getBreakdown(x);
                sigma = sigma - e + breakdownTime;
                break;
            case AWAITING_STATION_GO_AHEAD:
                boolean station_go_ahead = getMoveToStationResponse(x);
                if (station_go_ahead) {
                    holdIn(BEGIN_LOAD_UNLOAD, 0);
                }
                break;
            case AT_STATION:
                Optional<PassengerList> loadingPassengers = getPassengerLoad(x);
                loadingPassengers.ifPresent(lps -> {
                    _unloadingPassengers.clear();
                    _passengers.addAll(lps);
                    holdIn(REQUEST_MOVE_TO_SECTION, 0);
                });
                break;
            case AWAITING_SECTION_GO_AHEAD:
                _stationName = Optional.empty();
                Optional<Double> section_go_ahead = getMoveToSectionResponse(x);
                section_go_ahead.ifPresent(time -> holdIn(IN_TRANSIT, time));
                break;
        }
    }

    public message out() {
        message m = new message();
        switch (phase) {
            case REQUEST_MOVE_TO_SECTION:
                m.add(makeContent(OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT, new KeyEntity(getID())));
                break;
            case REQUEST_MOVE_TO_STATION:
                m.add(makeContent(OUT_REQUEST_MOVE_TO_STATION_PORT, new KeyEntity(getID())));
                break;
            case OUT_PASSENGER_UNLOAD_PORT:
                PassengerUnloadRequest pur = new PassengerUnloadRequest(
                        PASSENGER_TOTAL_CAPACITY - _passengers.size(), _unloadingPassengers);
                m.add(makeContent(OUT_PASSENGER_UNLOAD_PORT, new KeyValueEntity<>(getID(), pur)));
                break;
        }
        return m;
    }
}
