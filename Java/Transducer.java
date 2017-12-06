package Subway;

import GenCol.*;
import model.modeling.*;
import view.modeling.ViewableAtomic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;

public class Transducer extends ViewableAtomic {

	protected double observationTime;
	protected double clock;
	protected double total_ta;
	private int totalPassengersDelivered;
	private int totalLoadUnloadDelayTime;
	private int delayLoadUnloadTimeCount;
	private int nonzeroLoadUnloadDelayTimeCount;

	private double totalTransitDelayTime;
	private int delayTransitTimeCount;

	protected static final String OUT_STOP = "Stop";


	public Transducer() {
		this("Transducer", 120);
	}
	
	public Transducer(double ObservationTime) {
		this("Transducer",ObservationTime);
	}

	public Transducer(String name, double ObservationTime) {
		super(name);
		observationTime = ObservationTime;
		
		// Input ports
		addInport(Scheduler.OUT_N_PASSENGERS_DELIVERED_PORT);
		addInport(Train.OUT_WAIT_TIME_PORT);
		
		// Output ports
		addOutport(OUT_STOP);
		
		initialize();
	}

	public void initialize() {
		holdIn("active", observationTime);
		clock = 0;
		total_ta = 0;
		totalPassengersDelivered = 0;

		totalLoadUnloadDelayTime = 0;
		delayLoadUnloadTimeCount = 0;
		nonzeroLoadUnloadDelayTimeCount = 0;

		totalTransitDelayTime = 0;
		delayTransitTimeCount = 0;

		super.initialize();
	}

	public void deltext(double e, message x) {
		clock = clock + e;
		Continue(e);
		
		for (int k = 0; k < x.size(); k++) {
			if (messageOnPort(x,Scheduler.OUT_N_PASSENGERS_DELIVERED_PORT,k)) {
				
				// Get the entity
				intEnt nDelivered = (intEnt)x.getValOnPort(Scheduler.OUT_N_PASSENGERS_DELIVERED_PORT, k);
				System.out.println("Passengers delivered: "+nDelivered.getv());
				totalPassengersDelivered += nDelivered.getv();
			}
			if (messageOnPort(x,Train.OUT_WAIT_TIME_PORT,k)) {
				// Get the entity
				doubleEnt delayEnt = (doubleEnt)x.getValOnPort(Train.OUT_WAIT_TIME_PORT, k);
				double delay = delayEnt.getv();
				totalLoadUnloadDelayTime += delay;
				delayLoadUnloadTimeCount += 1;
				if (delay>0) {
					nonzeroLoadUnloadDelayTimeCount += 1;
				}
			}
			if (messageOnPort(x,Train.OUT_DELAY_TIME_PORT,k)) {
				doubleEnt delayEnt = (doubleEnt)x.getValOnPort(Train.OUT_DELAY_TIME_PORT, k);
				double delay = delayEnt.getv();
				totalTransitDelayTime += delay;
				delayTransitTimeCount += 1;
			}
		}
	}

	public void deltint() {
		clock = clock + sigma;
		passivate();
		show_state();
		saveResultsToFile();
	}

	public message out() {
		message m = new message();
		if (phaseIs("active")) {
			m.add(makeContent("Stop", new entity("Stop")));
		}
		return m;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void saveResultsToFile() {
		File f = new File("data/" + this.name + ".log");
		boolean exists = f.exists();
		try (FileWriter logWriter = new FileWriter(f, true);
			 BufferedWriter bw = new BufferedWriter(logWriter);
			 PrintWriter out = new PrintWriter(bw)) {
			if (!exists) {
				out.print(String.format("%s,%s,%s,%s,%s,%s,%s\n",
						"Name", "Passengers Carried",
						"Accumulated Load Unload Delays", "Average Load Unload Delay", "Average Load Unload Nonzero Delay",
						"Total Transit Delay", "Average Transit Delay"));
			}
			out.print(String.format("%s,%d,%d,%.4f,%.4f,%.4f,%.4f\n",
					getName(), totalPassengersDelivered, totalLoadUnloadDelayTime,
					totalLoadUnloadDelayTime /(double) delayLoadUnloadTimeCount,
					totalLoadUnloadDelayTime /(double) nonzeroLoadUnloadDelayTimeCount,
					totalTransitDelayTime, totalTransitDelayTime /(double) delayTransitTimeCount));
		} catch (java.io.IOException ex) {
			ex.printStackTrace();
		}
	}

	public void show_state() {
		System.out.println("State of " + name + ": ");
		System.out.println("phase, sigma: " + phase + ", " + sigma + " ");
		System.out.println("Total Passengers Carried: "+totalPassengersDelivered);
		System.out.println("Total Accumulated Loading and Unloading Delay Time: "+ totalLoadUnloadDelayTime +" minutes");
		System.out.println("Overall Average Loading and Unloading Delay Time: "+ totalLoadUnloadDelayTime /(double) delayLoadUnloadTimeCount);
		System.out.println("Average Loading and Unloading Non-zero Delay Time: "+ totalLoadUnloadDelayTime /(double) nonzeroLoadUnloadDelayTimeCount);
		System.out.println(String.format("Total Accumulated Transit Delay Time: %.4f minutes", totalTransitDelayTime));
		System.out.println(String.format("Average Transit Delay Time: %.4f minutes", totalTransitDelayTime /(double) delayTransitTimeCount));
	}
}
