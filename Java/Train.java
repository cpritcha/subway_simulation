package Subway;

import java.util.Optional;

import model.modeling.message;
import GenCol.entity;
import view.modeling.ViewableAtomic;


public class Train extends ViewableAtomic {
    public static final String IN_MOVE_RESPONSE_PORT = "inMoveResponse";
    public static final String OUT_REQUEST_MOVE_PORT = "outRequestMove";
    public static final String IN_PASSENGER_LOAD_PORT = "inPassengerLoad";
    public static final String OUT_PASSENGER_UNLOAD_PORT = "outPassengerUnload";
    public static final String IN_BREAKDOWN_PORT = "inBreakdown";

    private static final String AT_STATION = "at_station";
    private static final String IN_TRANSIT = "in_transit";

    private PassengerList _passengerList = new PassengerList();
    private message _result = new message();
    private TrackSection _trackSection;
    private Station _station;

    public Train(String name) {
        super(name);
        holdIn(IN_TRANSIT, 0);

        addInport(IN_MOVE_RESPONSE_PORT);
        addOutport(OUT_REQUEST_MOVE_PORT);

        addInport(IN_PASSENGER_LOAD_PORT);
        addOutport(OUT_PASSENGER_UNLOAD_PORT);
    }

    private Optional<Boolean> getMoveResponse(message m) {
        Optional<Boolean> result = Optional.empty();
        for (int i = 0; i < m.size(); i++) {
            if (messageOnPort(m, IN_MOVE_RESPONSE_PORT, i)) {
                ValueEntity<Boolean> v = (ValueEntity<Boolean>)m.getValOnPort(IN_MOVE_RESPONSE_PORT, i);
                if (v.getValue()) {
                    return Optional.of(true);
                } else {
                    result = Optional.of(false);
                }
            }
        }
        return result;
    }

    private Optional<PassengerList> getPassengerLoad(message m) {
        PassengerList pl = new PassengerList();
        for (int i = 0; i < m.size(); i++) {
            if (messageOnPort(m, IN_PASSENGER_LOAD_PORT, i)) {
                pl.addAll(((PassengerList) m.getValOnPort(IN_PASSENGER_LOAD_PORT, i)).passengers);
            }
        }
        if (pl.size() > 0) {
            return Optional.of(pl);
        } else {
            return Optional.empty();
        }
    }

    private Optional<Double> getBreakdown(message m) {
        double totalBreakDownTime = 0;
        for (int i = 0; i < m.size(); i++) {
            if (messageOnPort(m, IN_BREAKDOWN_PORT, i)) {
                ValueEntity<Double> v = (ValueEntity<Double>)m.getValOnPort(IN_MOVE_RESPONSE_PORT, i);
                totalBreakDownTime += v.getValue();
            }
        }
        if (totalBreakDownTime > 0) {
            return Optional.of(totalBreakDownTime);
        }
        return Optional.empty();
    }

    public void deltint() {
        switch (phase) {
            case IN_TRANSIT:
                passivateIn(AT_STATION); break;
            // Since at station waits for forever there should never be an internal transition on
        }
    }

    public void deltext(double e, message x) {
        Optional<Boolean> moveResponse = getMoveResponse(x);
        Optional<PassengerList> loadingPassengers = getPassengerLoad(x);
        Optional<Double> breakdownTime = getBreakdown(x);

        sigma = sigma - e + breakdownTime.orElse(0.0);
        if (moveResponse.isPresent()) {
            _trackSection = _trackSection.getNextSection();
            sigma = _trackSection.getLength();
        }

        loadingPassengers.ifPresent(lp -> _passengerList.addAll(lp.passengers));
    }

    public message out() {
        message m = new message();
        if (sigma == 0 && phaseIs(IN_TRANSIT)) {
            PassengerList departingPassengers =
                    _passengerList.filterByDestination(_station.getName());
            _passengerList.passengers.removeIf(p -> p.getDestination().equals(_station.getName()));
            m.add(makeContent(OUT_PASSENGER_UNLOAD_PORT, departingPassengers));
        }
        // Add out move request response
        return m;
    }
}
