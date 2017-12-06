package Subway;

import javafx.util.Pair;
import view.modeling.ViewableDigraph;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;

import java.awt.Dimension;
import java.awt.Point;
import java.util.*;
import java.util.stream.Collectors;

public class ExpFrame6TrainsNoDelays extends BaseExpFrame {

    public ExpFrame6TrainsNoDelays(boolean logResults) {
        super("Experimental Frame 6 Trains No Delays");

        SubwaySystemLoopConfig config = createScarboroughLoop(6)
                .with($ -> {
                    $.random = new Random();
                    $.loopName = "ExpFrame6TrainsNoDelays";
                    $.loadingTimeDistribution = new UniformRandom($.random, 15.0/60.0, 195.0/60.0);
                    $.delayTimeDistribution = new UniformRandom($.random, 0.0, 0.0);
                    $.delayProbability = 0.0;
                    $.logResults = logResults;
                }).createSubwaySystemLoop();
        addSubwaySystemLoop(config);
    }
}
