package Subway;

public class Passenger {
	protected String origin;
	protected String destination;
	
	public Passenger (String Origin, String Destination) {
		origin = Origin;
		destination = Destination;
	}
	
	public String getOrigin() {
		return origin;
	}
	
	public String getDestination() {
		return destination;
	}
	
	public void setDestination(String Destination) {
		destination = Destination;
	}
}
