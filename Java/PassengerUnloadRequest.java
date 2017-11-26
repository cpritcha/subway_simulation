package Subway;

public class PassengerUnloadRequest {
    private final int _remainingCapacity;
    private final PassengerList _passengers;

    public PassengerUnloadRequest(int remainingCapacity, PassengerList passengers) {
        _remainingCapacity = remainingCapacity;
        _passengers = passengers;
    }

    public int getRemainingCapacity() {
        return _remainingCapacity;
    }

    public PassengerList getPassengers() {
        return _passengers;
    }

    public String toString() {
        return String.format("<PassengerUnloadRequest nPassengers=%d nRemainingPassengers=%d",
                _passengers.size(), _remainingCapacity);
    }
}
