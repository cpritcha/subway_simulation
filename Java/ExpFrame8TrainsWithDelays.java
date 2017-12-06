package Subway;

import java.util.Random;

public class ExpFrame8TrainsWithDelays extends BaseExpFrame {

    public ExpFrame8TrainsWithDelays() {
        this(false);
    }

    public ExpFrame8TrainsWithDelays(boolean logResults) {
        super("Experimental Frame 8 Trains With Delays");

        SubwaySystemLoopConfig config = createScarboroughLoop(8)
                .with($ -> {
                    $.random = new Random();
                    $.loopName = "ExpFrame8TrainsWithDelays";
                    $.loadingTimeDistribution = new UniformRandom($.random, 15.0/60.0, 195.0/60.0);
                    $.delayTimeDistribution = new UniformRandom($.random, 0.0, 3.0);
                    $.delayProbability = 0.2;
                    $.logResults = logResults;
                }).createSubwaySystemLoop();
        addSubwaySystemLoop(config);
    }
}
