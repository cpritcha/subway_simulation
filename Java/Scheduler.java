package Subway;

import GenCol.*;
import view.modeling.ViewableAtomic;
import model.modeling.message;

public class Scheduler extends ViewableAtomic {
	
	protected int numberOfTrains;
	protected int numberOfStations;
	protected Queue waitingTrains;
	
	private static final String REQUEST_MOVE_PORT = "Request Move";
	private static final String MOVE_RESPONSE_PORT = "Move Response";
	
	public Scheduler(int NumberOfTrains, int NumberOfStations) {
		super("Scheduler");
		
		numberOfTrains = NumberOfTrains;
		numberOfStations = NumberOfStations;
		waitingTrains = new Queue();
		
		addInport(REQUEST_MOVE_PORT);
		
		addOutport(MOVE_RESPONSE_PORT);
		
	}
	
	public void initialize() {
		passivate();
		super.initialize();
	}
	
	public void delext(double e, message x) {
		Continue(e);
		
		if (phaseIs("passivate")) {
			for (int k=0; k<x.size(); k++) {
				// Handle the move requests
				if (messageOnPort(x,"Request Move",k)) {
					
				}
			}
		}
	}
	
	public void deltint() {
		passivate();
	}
}
