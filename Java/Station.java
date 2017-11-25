package Subway;

import GenCol.*;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Random;
import java.util.UUID;

import view.modeling.ViewableAtomic;
import model.modeling.message;

public class Station extends ViewableAtomic {
	
	protected Queue passengers;
	protected PassengerList passengersToBoard;
	protected int currentTrainCapacity;
	protected int passengerCreationRate;
	protected int initialPassengerCount;
	protected int totalPassengersCreated;
	protected int totalPassengersArrived;
	
	// We need a clock so we can create
	// passengers at a specific rate
	protected double clock;
	protected double timeOfLastPassengerCreation;
	
	// Store a list of destination stations
    static protected ArrayList<String> destinations;
	
    // Available ports
    protected static final String UNLOAD_PASSENGERS_PORT = "UnloadPassengers";
    protected static final String REQUEST_PASSENGERS_PORT = "RequestPassengers";
    protected static final String PASSENGERS_TO_BOARD_PORT = "PassengersToBoard";

    // Unique ID
	private final UUID _id;
    
	public Station(String name, int PassengerCreationRate, int InitialPassengerCount, ArrayList<String> Destinations) {
		super(name);
		
		passengerCreationRate = PassengerCreationRate;
		initialPassengerCount = InitialPassengerCount;
	
		destinations = Destinations;

		_id = UUID.randomUUID();
		
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
		totalPassengersCreated = 0;
		totalPassengersArrived = 0;
		
		// Start with an initial set of passengers
		passengers = new Queue();
		if (initialPassengerCount>0) {
			passengers.addAll(passengerFactory(initialPassengerCount));
		}
		
		// Initialize the boarding passengers to an empty list
		passengersToBoard = new PassengerList();
		
		super.initialize();
	}

	public UUID getID() {
		return _id;
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
					// First check that the key value in the entity
					// passed in matches the current station
					// Get the train capacity from the request message
					entity ent = x.getValOnPort("RequestPassengers", k);
					Pair pair = (Pair)ent;
					String key = (String)pair.getKey();
					
					if (key.equals(this.name)) {
						currentTrainCapacity = (int)pair.getValue();
						
						// Get the total time since the last passenger addition
						// and generate some new passengers first
						int deltaTime = (int)Math.round(clock - timeOfLastPassengerCreation);
						passengers.addAll(passengerFactory(deltaTime*passengerCreationRate));
						
						// Figure out how many passengers we can provide
						int numPassengers = Math.min(passengers.size(), currentTrainCapacity);
						
						// Put together a list of passengers
						for (int kp=0; kp < numPassengers; kp++) {
							passengersToBoard.add((Passenger)passengers.remove());
						}
						
						// Move immediately to output
						holdIn("RequestPassengers",0);
					}
				}
				
				else if (messageOnPort(x,"UnloadPassengers",k)) {
					// First check that the key value in the entity
					// passed in matches the current station
					// Get the train capacity from the request message
					entity ent = x.getValOnPort("UnloadPassengers", k);
					Pair pair = (Pair)ent;
					String key = (String)pair.getKey();
					
					if (key.equals(this.name)) {
						// Unload the passengers.  If this is their final
						// destination, they can disappear into oblivion.
						// If it is not their final destination, they
						// will re-enter the passenger queue.
						//
						PassengerList P = ((KeyValueEntity<PassengerList>)x.getValOnPort("UnloadPassengers", k)).getValue();
						Passenger p;
						for (int kp=0; kp<P.size(); kp++) {
							p = P.get(kp);
							if (!p.getDestination().equals(name)) {
								// Add the passenger back into the queue
								// This compensates for deboarding due to
								// breakdowns
								passengers.add(p);
							}
							else {
								// Update the total number of passengers arrived
								totalPassengersArrived += 1;
							}
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
		return this.passengerFactory(passengerCount,"random",0);
	}
	
	private LinkedList passengerFactory(int passengerCount, String destinationMode) {
		return this.passengerFactory(passengerCount, destinationMode, 0);
	}
	
	private LinkedList passengerFactory(int passengerCount, String destinationMode, int destinationIndex) {
		LinkedList passengers = new LinkedList();
		
		// Get a random integer to decide the destination
		Random random = new Random();
		int destinationNumber = 0;
		if (destinationMode.equals("fixed")) {
			destinationNumber = destinationIndex;
		}
		
		// Create the passengers
		for (int k = 0; k < passengerCount; k++) {
			// Get a random destination
			if (destinationMode.equals("random")) {
				destinationNumber = random.nextInt(destinations.size());
			}
			
			// Add the new passenger given the destination index
			passengers.add(new Passenger(name,destinations.get(destinationNumber)));
			
			// For fixed loop increment after we use the destination
			// number so that we start with zero
			if (destinationMode.equals("fixedLoop")) {
				destinationNumber++;
				destinationNumber = destinationNumber%destinations.size();
			}
			
			
		}
		// Update the time of last passenger creation
		timeOfLastPassengerCreation = clock;
		
		// Update the total number of passengers created
		totalPassengersCreated += passengerCount;
		
		return passengers;
	}
	
	public message out() {
		message m = new message();
		
		if (phaseIs("RequestPassengers")) {
			// Pass the passengers as a bag of inputs
			PassengerList boardingPassengers = passengersToBoard.copy();
			m.add(makeContent("passengersToBoard", new KeyValueEntity<>(boardingPassengers, getID())));
			
			// Passengers have been passed along, so we can clear the list
			passengersToBoard.clear();
		}
		
		return m;
	}

}
