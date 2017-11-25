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
    private PassengerList _passengers = new PassengerList();
    private PassengerList _unloadingPassengers = new PassengerList();
    private message _result = new message();
    private Optional<String> _stationName = Optional.empty();

    public Train(String name) {
        super(name);
        _id = UUID.randomUUID();
        holdIn(IN_TRANSIT, 0);

        addInport(IN_MOVE_TO_STATION_PORT);
        addOutport(OUT_REQUEST_MOVE_TO_STATION_PORT);

        addInport(IN_MOVE_TO_TRACK_SECTION_PORT);
        addOutport(OUT_REQUEST_MOVE_TO_TRACK_SECTION_PORT);

        addInport(IN_PASSENGER_LOAD_PORT);
        addOutport(OUT_PASSENGER_UNLOAD_PORT);
    }


    public UUID getID() {
        return _id;
    }

    private Stream<content> getRelevantMessages(message m) {
        return m.stream().filter(c -> ((IWithUUID) ((content) c).getValue()).getID() == getID());
    }

    private boolean getMoveResponse(message m, String portName) {
        return m.stream().anyMatch(c -> ((content) c).getPortName().equals(portName));
    }

    private boolean getMoveToStationResponse(message m) {
        return getRelevantMessages(m)
                .anyMatch(c -> c.getPortName().equals(IN_MOVE_TO_STATION_PORT));
    }

    private Optional<Double> getMoveToSectionResponse(message m) {
        double timeToNextSection = getRelevantMessages(m)
                .filter(c -> c.getPortName().equals(IN_MOVE_TO_TRACK_SECTION_PORT))
                .map(c -> ((KeyValueEntity<Double>)c.getValue()).getValue())
                .reduce(0.0, (a, b) -> a + b);
        if (timeToNextSection == 0) {
            return Optional.empty();
        } else {
            return Optional.of(timeToNextSection);
        }
    }

    private Optional<PassengerList> getPassengerLoad(message m) {
        Stream<content> cs = getRelevantMessages(m);
        PassengerList passengers = cs.filter(c -> c.getPortName().equals(IN_PASSENGER_LOAD_PORT))
                .map(c -> ((KeyValueEntity<PassengerList>) c.getValue()).getValue())
                .reduce(new PassengerList(), (pl_all, pl) -> {
                    pl_all.addAll(pl);
                    return pl_all;
                });
        if (passengers.size() > 0) {
            return Optional.of(passengers);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Double> getBreakdown(message m) {
        double totalBreakDownTime = getRelevantMessages(m)
                .map(c -> ((KeyValueEntity<Double>) c.getValue()).getValue())
                .reduce(0.0, (a, b) -> a + b);
        if (totalBreakDownTime > 0) {
            return Optional.of(totalBreakDownTime);
        }
        return Optional.empty();
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
            case REQUEST_MOVE_TO_SECTION:
                passivateIn(AWAITING_SECTION_GO_AHEAD);
                break;
        }
    }

    public void deltext(double e, message x) {
        switch (phase) {
            case IN_TRANSIT:
                Optional<Double> breakdownTime = getBreakdown(x);
                sigma = sigma - e + breakdownTime.orElse(0.0);
                break;
            case AWAITING_STATION_GO_AHEAD:
                boolean station_go_ahead = getMoveToStationResponse(x);
                if (station_go_ahead) {
                    holdIn(BEGIN_LOAD_UNLOAD, 0);
                }
                break;
            case AT_STATION:
                _unloadingPassengers.clear();
                Optional<PassengerList> loadingPassengers = getPassengerLoad(x);
                loadingPassengers.ifPresent(lps -> _passengers.addAll(lps));
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
                m.add(makeContent(OUT_PASSENGER_UNLOAD_PORT, new KeyValueEntity<>(pur, getID())));
                break;
        }
        return m;
    }
}