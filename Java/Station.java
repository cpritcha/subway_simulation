package Subway;

import GenCol.*;
import java.util.List;
import java.util.LinkedList;
import java.util.Random;
import view.modeling.ViewableAtomic;
import model.modeling.message;

public class Station extends ViewableAtomic {
	
	protected Queue passengers;
	protected PassengerList passengersToBoard;
	protected int currentTrainCapacity;
	protected int stationPassengerCapacity;
	protected int passengerCreationRate;
	protected int initialPassengerCount;
	
	// We need a clock so we can create
	// passengers at a specific rate
	protected double clock;
	protected double timeOfLastPassengerCreation;
	
	// Store a list of destination stations
    static protected List<String> destinations;
	
    // Available ports
    private static final String UNLOAD_PASSENGERS_PORT = "UnloadPassengers";
    private static final String REQUEST_PASSENGERS_PORT = "RequestPassengers";
    private static final String PASSENGERS_TO_BOARD_PORT = "PassengersToBoard";
    
	public Station(String name, int PassengerCapacity, int PassengerCreationRate, int InitialPassengerCount, List<String> Destinations) {
		super(name);
		
		stationPassengerCapacity = PassengerCapacity;
		passengerCreationRate = PassengerCreationRate;
		initialPassengerCount = InitialPassengerCount;
	
		destinations = Destinations;
		
		// Add the input ports
		addInport(UNLOAD_PASSENGERS_PORT);
		addInport(REQUEST_PASSENGERS_PORT);
		
		// Add the output ports
		addOutport(PASSENGERS_TO_BOARD_PORT);
		
	}
	
	public void initialize() {
		
		passivate();
		
		clock = 0.0;
		timeOfLastPassengerCreation = 0.0;
		
		// Start with an initial set of passengers
		passengers = new Queue();
		if (initialPassengerCount>0) {
			passengers.addAll(passengerFactory(initialPassengerCount));
		}
		
		// Initialize the boarding passengers to an empty list
		passengersToBoard = new PassengerList();
		
		super.initialize();
	}
	
	public void delext(double e, message x) {
		clock = clock + e;
		Continue(e);
		
		// First ensure we're starting from the
		// passive phase so we don't interrupt
		// another phase
		if (phaseIs("passive")) {
			for (int k=0; k<x.size(); k++) {
				// Add passengers
				if (messageOnPort(x,"RequestPassengers",k)) {
					// Get the total time since the last passenger addition
					// and generate some new passengers first
					int deltaTime = (int)Math.round(clock - timeOfLastPassengerCreation);
					passengers.addAll(passengerFactory(deltaTime*passengerCreationRate));
					
					// Get the train capacity from the request message
					intEnt ent = (intEnt)x.getValOnPort("RequestPassengers", k);
					currentTrainCapacity = ent.getv();
					
					// Figure out how many passengers we can provide
					int numPassengers = Math.min(passengers.size(), currentTrainCapacity);
					
					// Put together a list of passengers
					for (int kp=0; kp < numPassengers; kp++) {
						passengersToBoard.add((Passenger)passengers.remove());
					}
					
					// Move immediately to output
					holdIn("RequestPassengers",0);
				}
				
				else if (messageOnPort(x,"UnloadPassengers",k)) {
					// Unload the passengers.  If this is their final
					// destination, they can disappear into oblivion.
					// If it is not their final destination, they
					// will re-enter the passenger queue.
					//
					PassengerList P = (PassengerList)x.getValOnPort("UnloadPassengers", k);
					Passenger p;
					for (int kp=0; kp<P.size(); kp++) {
						p = P.get(kp);
						if (!p.getDestination().equals(name)) {
							// Add the passenger back into the queue
							// This compensates for deboarding due to
							// breakdowns
							passengers.add(p);
						}
					}
				}
			}
			
		}
	}
	
	public void deltint() {
		clock = clock + sigma;
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
	
	private LinkedList passengerFactory(int passengerCount) {
		LinkedList passengers = new LinkedList();
		
		// Get a random integer to decide the destination
		Random random = new Random();
		int randomNumber;
		
		// Create the passengers
		for (int k = 0; k < passengerCount; k++) {
			randomNumber = random.nextInt(destinations.size());
			passengers.add(new Passenger(name,destinations.get(randomNumber)));
		}
		// Update the time of last passenger creation
		timeOfLastPassengerCreation = clock;
		
		return passengers;
	}
	
	public message out() {
		message m = new message();
		
		if (phaseIs("RequestPassengers")) {
			// Pass the passengers as a bag of inputs
			PassengerList boardingPassengers = passengersToBoard.copy();
			m.add(makeContent("passengersToBoard",boardingPassengers));
			
			// Passengers have been passed along, so we can clear the list
			passengersToBoard.clear();
		}
		
		return m;
	}

}
