package Subway;

import java.util.Optional;
import java.util.Random;

public class ExpFrame4TrainsNoDelays extends BaseExpFrame {

    public ExpFrame4TrainsNoDelays(boolean logResults) {
        super("Experimental Frame 4 Trains No Delays");

        SubwaySystemLoopConfig config = createScarboroughLoop(4)
                .with($ -> {
                    $.random = new Random();
                    $.loopName = "ExpFrame4TrainsNoDelays";
                    $.loadingTimeDistribution = new UniformRandom($.random, 15.0/60.0, 195.0/60.0);
                    $.delayTimeDistribution = new UniformRandom($.random, 0.0, 0.0);
                    $.delayProbability = 0.0;
                    $.logResults = logResults;
                }).createSubwaySystemLoop();
        addSubwaySystemLoop(config);
    }

    public ExpFrame4TrainsNoDelays() {
        this(false);
    }
}
