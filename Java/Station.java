package Subway;

import java.util.Queue;

import view.modeling.ViewableAtomic;
import model.modeling.message;

public class Station extends ViewableAtomic {
	
	protected Queue<Passenger> passengers;
	protected int currentTrainCapacity;
	protected int stationPassengerCapacity;
	
	public Station(String name, int passengerCapacity) {
		super(name);
		
		stationPassengerCapacity = passengerCapacity;
		
		// Add the input ports
		addInport("UnloadPassengers");
		addInport("RemainingCapacity");
		addInport("AddPassenger");
		
		// Add the output ports
		addOutport("RequestCapacity");
		addOutport("PassengersToLoad");
		
	}
	
	public void initialize() {
		
		passivate();
		
		super.initialize();
	}
	
	public void delext(double e, message x) {
		
	}
	
	public void deltint() {
		passivate();
	}
	
	/*
	 * Confluent function generates output and executes internal transition
	 * function prior to executing external transition function.
	 */
	public void deltcon(double e, message x) {
		deltint();
		deltext(0, x);
	}

}
