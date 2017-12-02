package Subway;

import view.modeling.ViewableComponent;
import view.modeling.ViewableDigraph;

import java.awt.*;

public class CoupledBreakdownGenerator extends ViewableDigraph {
    private UniformRandom _breakdownLengthGenerator;
    private UniformRandom _timeBetweenBreakdownGenerator;

    public CoupledBreakdownGenerator(UniformRandom breakdownLengthGenerator,
                                     UniformRandom timeBetweenBreakdownGenerator) {
        super("Breakdown Generator");
        _breakdownLengthGenerator = breakdownLengthGenerator;
        _timeBetweenBreakdownGenerator = timeBetweenBreakdownGenerator;
    }

    public String register(Train t) {
        // Add couplings for a particular train
        RandomBreakdownGenerator breakdownGenerator =
                new RandomBreakdownGenerator(
                        _breakdownLengthGenerator,
                        _timeBetweenBreakdownGenerator,
                        t);
        add(breakdownGenerator);
        String outPortName = t.getName();
        addOutport(outPortName);
        addCoupling(breakdownGenerator, RandomBreakdownGenerator.OUT_BREAKDOWN_PORT, this, outPortName);
        return outPortName;
    }
}
