package Subway;

import GenCol.*;

import java.util.UUID;

public class Passenger extends entity {
	protected final UUID _origin;
	protected final UUID _destination;
	
	public Passenger (UUID origin, UUID destination) {
		_origin = origin;
		_destination = destination;
		
	}
	
	public UUID getOrigin() {
		return _origin;
	}
	
	public UUID getDestination() {
		return _destination;
	}

	public String toString() {
		return String.format("<Passenger origin=%s destination=%s>",
				_origin.toString(), _destination.toString());
	}
}
