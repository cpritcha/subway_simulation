package Subway;

import GenCol.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class PassengerList extends entity {
	
	LinkedList<Passenger> passengers;
	
	public PassengerList() {
		passengers = new LinkedList<Passenger>();
	}
	
	public void add(Passenger P) {
		passengers.add(P);
	}
	
	public void remove() {
		passengers.remove();
	}
	
	public void remove(int k) {
		passengers.remove(k);
	}
	
	public void addAll(Collection C) {
		passengers.addAll(C);
	}
	
	public void clear() {
		passengers.clear();
	}
	
	public int size() {
		return passengers.size();
	}
	
	public Passenger get(int k) {
		return passengers.get(k);
	}
	
	public PassengerList copy() {
		PassengerList pCopy = new PassengerList();
		pCopy.addAll((LinkedList<Passenger>)passengers.clone());
		return pCopy;
	}

	public PassengerList filterByDestination(String destination) {
		PassengerList passengerList = new PassengerList();
		passengerList.addAll(passengers.stream()
				.filter(p -> p.getDestination().equals(destination))
				.collect(Collectors.toList()));
		return passengerList;
	}
}
