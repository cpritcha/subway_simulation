package Subway;

import GenCol.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.UUID;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import view.modeling.ViewableAtomic;
import model.modeling.message;

public class Station extends ViewableAtomic {
	
	protected Queue passengers;
	protected PassengerList passengersToBoard;
	protected int currentTrainCapacity;
	protected final int _passengerCreationRate;
	protected final int _initialPassengerCount;
	protected int totalPassengersCreated;
	protected int totalPassengersArrived;
	
	// We need a clock so we can create
	// _passengers at a specific rate
	protected double clock;
	protected double timeOfLastPassengerCreation;
	
	// Store a list of destination stations
    protected ArrayList<UUID> _destinations;
    
    // Output port to report number of passengers waiting
    protected static final String OUT_PASSENGERS_LEFT_WAITING_PORT = "Passengers Waiting";

    // Unique ID
	private final UUID _id;
    
	public Station(String name, UUID id, int passengerCreationRate, int initialPassengerCount,
				   ArrayList<UUID> destinations) {
		super(name);
		
		_passengerCreationRate = passengerCreationRate;
		_initialPassengerCount = initialPassengerCount;

		_id = id;

		_destinations = destinations;

		// Add the input ports
		addInport(Train.OUT_PASSENGER_UNLOAD_PORT);
		addInport(Transducer.OUT_STOP);
		
		// Add the output ports
		addOutport(Train.IN_PASSENGER_LOAD_PORT);
		
	}

	// https://medium.com/beingprofessional/think-functional-advanced-builder-pattern-using-lambda-284714b85ed5
	public static class Builder {
		public String name;
		public UUID id;
		public int passengerCreationRate;
		public int initialPassengerCount;
		public ArrayList<UUID> destinations;
		public Random random;

		public Builder with(
				Consumer<Builder> builderFunction) {
			builderFunction.accept(this);
			return this;
		}

		public static ArrayList<Builder> fromData(Object[][] data, Random random) {
			// Assume each row of data has a Station Name (String) and a Passenger Creation Rate (int) in that order
			ArrayList<Station.Builder> stationBuilders = Arrays.stream(data)
					.map(row -> new Station.Builder().with($ -> {
						$.id = UUID.randomUUID();
						$.name = (String) row[0];
						$.initialPassengerCount = 10;
						$.passengerCreationRate = (int) row[1];
						$.random = random;
					})).collect(Collectors.toCollection(ArrayList::new));

			ArrayList<UUID> destinations = stationBuilders.stream().map(sb -> sb.id)
					.collect(Collectors.toCollection(ArrayList::new));

			stationBuilders.forEach($ -> {
				ArrayList<UUID> ds = new ArrayList<>();
				ds.addAll(destinations);
				ds.removeIf(id -> id == $.id);
				$.destinations = ds;
			});

			return stationBuilders;
		}

		public Station createStation() {
			return new Station(name, id, passengerCreationRate, initialPassengerCount, destinations);
		}
	}

	public Station() {
		this("Station", UUID.randomUUID(), 5, 10,
				new ArrayList<>(Arrays.asList(UUID.randomUUID(), UUID.randomUUID())));
	}
	
	public void initialize() {
		
		passivate();
		
		clock = 0.0;
		timeOfLastPassengerCreation = 0.0;
		totalPassengersCreated = 0;
		totalPassengersArrived = 0;
		
		// Start with an initial set of _passengers
		passengers = new Queue();
		if (_initialPassengerCount >0) {
			passengers.addAll(passengerFactory(_initialPassengerCount));
		}
		
		// Initialize the boarding _passengers to an empty list
		passengersToBoard = new PassengerList();
		
		super.initialize();
	}

	public UUID getID() {
		return _id;
	}

	public void deltext(double e, message x) {
		clock = clock + e;
		Continue(e);
		
		for (int k=0; k<x.size(); k++) {
			if (messageOnPort(x,"Stop",k)) {
				// Immediately stop
				holdIn("Stop",0);
			}
		}
		
		// First ensure we're starting from the
		// passive phase so we don't interrupt
		// another phase
		if (phaseIs("passive")) {
			for (int k=0; k<x.size(); k++) {
				if (messageOnPort(x, Train.OUT_PASSENGER_UNLOAD_PORT,k)) {
					// First check that the key value in the entity
					// passed in matches the current station
					// Get the train capacity from the request message
					KeyValueEntity ent = (KeyValueEntity)x.getValOnPort(Train.OUT_PASSENGER_UNLOAD_PORT, k);
					UUID trainID = ent.getID();
					PassengerUnloadRequest pur = (PassengerUnloadRequest)ent.getValue();
					
					if (trainID.equals(this.getID())) {
						currentTrainCapacity = pur.getRemainingCapacity();
						
						// Get the total time since the last passenger addition
						// and generate some new _passengers first
						int deltaTime = (int)Math.round(clock - timeOfLastPassengerCreation);
						passengers.addAll(passengerFactory(deltaTime* _passengerCreationRate));
						System.out.println(String.format("(Station) # of Passengers Waiting at %s: %d",
                                getName(), passengers.size()));

						// Figure out how many _passengers we can provide
						int numPassengers = Math.min(passengers.size(), currentTrainCapacity);
						
						// Put together a list of _passengers
						for (int kp=0; kp < numPassengers; kp++) {
							passengersToBoard.add((Passenger)passengers.remove());
						}
						
						// Move immediately to output
						holdIn("Unloading Passengers",0);
					}
				}
			}
		}
	}
	
	public void deltint() {
		clock = clock + sigma;
		if (phaseIs("Unloading Passengers")) {
			// Move immediately to generating passengers for the train to load
			holdIn("Boarding Passengers",0);
		}
		else if (phaseIs("Stop")) {
			// Print the number of waiting passengers remaining
			System.out.println("Station "+this.name+" created "+
					this.totalPassengersCreated+" passengers");
			System.out.println("Station "+this.name+" has "+
					this.passengers.size()+" waiting passengers remaining");
			passivate();
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

	private PassengerList passengerFactory(int passengerCount) {
		return this.passengerFactory(passengerCount,"random",0);
	}

	private PassengerList passengerFactory(int passengerCount, String destinationMode) {
		return this.passengerFactory(passengerCount, destinationMode, 0);
	}

	private PassengerList passengerFactory(int passengerCount, String destinationMode, int destinationIndex) {
		PassengerList passengers = new PassengerList();

		// Get a random integer to decide the destination
		Random random = new Random();
		int destinationNumber = 0;
		if (destinationMode.equals("fixed")) {
			destinationNumber = destinationIndex;
		}

		// Create the _passengers
		for (int k = 0; k < passengerCount; k++) {
			// Get a random destination
			if (destinationMode.equals("random")) {
				destinationNumber = random.nextInt(_destinations.size());
			}

			// Add the new passenger given the destination index
			passengers.add(new Passenger(getID(), _destinations.get(destinationNumber)));

			// For fixed loop increment after we use the destination
			// number so that we start with zero
			if (destinationMode.equals("fixedLoop")) {
				destinationNumber++;
				destinationNumber = destinationNumber% _destinations.size();
			}


		}
		// Update the time of last passenger creation
		timeOfLastPassengerCreation = clock;

		// Update the total number of _passengers created
		totalPassengersCreated += passengerCount;

		return passengers;
	}
	
	public message out() {
		message m = new message();

		if (phaseIs("Boarding Passengers")) {
			// Pass the passengers as a bag of inputs
			PassengerList boardingPassengers = passengersToBoard.copy();
			System.out.println(String.format("(Station) # of Passengers Boarding at %s: %d",
                    getName(), boardingPassengers.size()));
			m.add(makeContent(Train.IN_PASSENGER_LOAD_PORT, new KeyValueEntity<>(getID(), boardingPassengers)));
			
			// Passengers have been passed along, so we can clear the list
			passengersToBoard.clear();
		}
		else if (phaseIs("Stop")) {
			m.add(makeContent(Station.OUT_PASSENGERS_LEFT_WAITING_PORT, new intEnt(this.passengers.size())));
		}
		
		return m;
	}

}
