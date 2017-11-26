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

    // Unique ID
	private final UUID _id;
    
	public Station(String name, int PassengerCreationRate, int InitialPassengerCount, ArrayList<String> Destinations) {
		super(name);
		
		passengerCreationRate = PassengerCreationRate;
		initialPassengerCount = InitialPassengerCount;
	
		destinations = Destinations;

		_id = UUID.randomUUID();
		
		// Add the input ports
		addInport(Train.OUT_PASSENGER_UNLOAD_PORT);
		
		// Add the output ports
		addOutport(Train.IN_PASSENGER_LOAD_PORT);
		
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
				if (messageOnPort(x, Train.OUT_PASSENGER_UNLOAD_PORT,k)) {
					// First check that the key value in the entity
					// passed in matches the current station
					// Get the train capacity from the request message
					entity ent = x.getValOnPort(Train.OUT_PASSENGER_UNLOAD_PORT, k);
					Pair pair = (Pair)ent;
					String key = (String)pair.getKey();
					
					if (key.equals(this.name)) {
						// Unload the passengers.  If this is their final
						// destination, they can disappear into oblivion.
						// If it is not their final destination, they
						// will re-enter the passenger queue.
						//
						PassengerList unloadingPassengers = ((KeyValueEntity<PassengerList>)x.getValOnPort(Train.OUT_PASSENGER_UNLOAD_PORT, k)).getValue();
						for (Passenger p: unloadingPassengers) {
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
					
					holdIn("Unloading Passengers",0);
				}
			}
			
		}
	}
	
	public void deltint() {
		clock = clock + sigma;
		if (phaseIs("Unloading Passengers")) {
			// Move immediately to generating passengers for the train to load
			holdIn("Boarding Passenger",0);
		}
		else {
			passivate();
		}
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
		
		if (phaseIs("Boarding Passengers")) {
			// Pass the passengers as a bag of inputs
			PassengerList boardingPassengers = passengersToBoard.copy();
			m.add(makeContent(Train.IN_PASSENGER_LOAD_PORT, new KeyValueEntity<>(getID(), boardingPassengers)));
			
			// Passengers have been passed along, so we can clear the list
			passengersToBoard.clear();
		}
		
		return m;
	}

}
