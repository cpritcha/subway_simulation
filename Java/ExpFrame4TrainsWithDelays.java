package Subway;

import java.util.Optional;
import java.util.Random;

public class ExpFrame4TrainsWithDelays extends BaseExpFrame {

    public ExpFrame4TrainsWithDelays(boolean logScheduler) {
        super("Experimental Frame 4 Trains With Delays");

        SubwaySystemLoopConfig config = createScarboroughLoop(4)
                .with($ -> {
                    $.random = new Random();
                    $.loopName = "ExpFrame4TrainsWithDelays";
                    $.loadingTimeDistribution = new UniformRandom($.random, 15.0/60.0, 195.0/60.0);
                    $.delayTimeDistribution = new UniformRandom($.random, 0.0, 3.0);
                    $.delayProbability = 0.2;
                    $.logResults = logScheduler;
                }).createSubwaySystemLoop();
        addSubwaySystemLoop(config);
    }

    public ExpFrame4TrainsWithDelays() {
        this(false);
    }
}
