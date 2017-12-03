package Subway;

import javafx.util.Pair;
import view.modeling.ViewableDigraph;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;

import java.awt.Dimension;
import java.awt.Point;
import java.util.*;
import java.util.stream.Collectors;

public class ExpFrame extends BaseExpFrame {

    public ExpFrame() {
        super("Experimental Frame");


        Random random = new Random(1000);
        SubwaySystemLoopConfig config = createScarboroughLoop(6)
                .with($ -> {
                    $.random = new Random(1000);
                    $.loadingTimeDistribution = new UniformRandom($.random, 15.0/60.0, 195.0/60.0);
                    $.breakdownGenerator = Optional.empty();
//                    new CoupledBreakdownGenerator(
//                            new UniformRandom($.random, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY),
//                            new UniformRandom($.random, 0, 0));
                }).createSubwaySystemLoop();
        addSubwaySystemLoop(config);
    }
}
