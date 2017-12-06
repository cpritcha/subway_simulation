package Subway;

import controller.Governor;
import facade.modeling.FModel;
import facade.simulation.FCoupledSimulator;

import view.simView.SimView;


public class RunSimulation {
    public static void main(String[] args) {
        boolean logResults = false;
        for (int i = 0; i < 50; i++) {
            runInstance(new ExpFrame4TrainsNoDelays(logResults));
            runInstance(new ExpFrame6TrainsNoDelays(logResults));
            runInstance(new ExpFrame8TrainsNoDelays(logResults));

            runInstance(new ExpFrame4TrainsWithDelays(logResults));
            runInstance(new ExpFrame6TrainsWithDelays(logResults));
            runInstance(new ExpFrame8TrainsWithDelays(logResults));
        }
        logResults = true;

        runInstance(new ExpFrame4TrainsNoDelays(logResults));
        runInstance(new ExpFrame6TrainsNoDelays(logResults));
        runInstance(new ExpFrame8TrainsNoDelays(logResults));

        runInstance(new ExpFrame4TrainsWithDelays(logResults));
        runInstance(new ExpFrame6TrainsWithDelays(logResults));
        runInstance(new ExpFrame8TrainsWithDelays(logResults));
    }

    public static <T extends BaseExpFrame> void runInstance(T exp) {
        try {
            FCoupledSimulator cs = new FCoupledSimulator(exp, SimView.modelView, FModel.COUPLED);
            cs.setRTMultiplier(1e-4);
            Governor.setTV(20);
            cs.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
