package Subway;

import controller.Governor;
import facade.modeling.FModel;
import facade.simulation.FCoupledSimulator;

import view.simView.SimView;


public class RunSimulation {
    public static void main(String[] args) {
        for (int i = 0; i < 60; i++) {
            runInstance(ExpFrame4TrainsNoDelays.class);
            runInstance(ExpFrame4TrainsWithDelays.class);
            runInstance(ExpFrame6TrainsNoDelays.class);
            runInstance(ExpFrame6TrainsWithDelays.class);
        }
    }

    public static <T extends BaseExpFrame> void runInstance(Class<T> c) {
        try {
            BaseExpFrame exp = c.newInstance();
            FCoupledSimulator cs = new FCoupledSimulator(exp, SimView.modelView, FModel.COUPLED);
            cs.setRTMultiplier(1e-4);
            Governor.setTV(20);
            cs.run();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
