package Subway;

import GenCol.*;
import model.modeling.*;
import view.modeling.ViewableAtomic;
import java.util.HashMap;
import java.util.Map;

public class Transducer extends ViewableAtomic {

	protected double observationTime;
	protected double clock;
	protected double total_ta;
	private int totalPassengersDelivered;

	public Transducer() {
		this("Transducer", 500);
	}
	
	public Transducer(double ObservationTime) {
		this("Transducer",ObservationTime);
	}

	public Transducer(String name, double ObservationTime) {
		super(name);
		observationTime = ObservationTime;
		
		// Input ports
		addInport(Scheduler.OUT_N_PASSENGERS_DELIVERED_PORT);
		
		initialize();
	}

	public void initialize() {
		holdIn("active", observationTime);
		clock = 0;
		total_ta = 0;
		totalPassengersDelivered = 0;
		super.initialize();
	}

	public void deltext(double e, message x) {
		clock = clock + e;
		Continue(e);
		
		if (phaseIs("passive")) {
			for (int k = 0; k < x.size(); k++) {
				if (messageOnPort(x,Scheduler.OUT_N_PASSENGERS_DELIVERED_PORT,k)) {
					// Get the entity
					intEnt nDelivered = (intEnt)x.getValOnPort(Scheduler.OUT_N_PASSENGERS_DELIVERED_PORT, k);
					totalPassengersDelivered += nDelivered.getv();
				}
			}
		}
	}

	public void deltint() {
		clock = clock + sigma;
		passivate();
		show_state();
	}

	public message out() {
		message m = new message();

		return m;
	}

	public void show_state() {
		System.out.println("State of " + name + ": ");
		System.out.println("phase, sigma: " + phase + ", " + sigma + " ");

	}
}
