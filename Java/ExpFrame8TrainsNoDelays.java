package Subway;

import java.util.Random;

public class ExpFrame8TrainsNoDelays extends BaseExpFrame {

    public ExpFrame8TrainsNoDelays(boolean logResults) {
        super("Experimental Frame 8 Trains No Delays");

        SubwaySystemLoopConfig config = createScarboroughLoop(8)
                .with($ -> {
                    $.random = new Random();
                    $.loopName = "ExpFrame8TrainsNoDelays";
                    $.loadingTimeDistribution = new UniformRandom($.random, 15.0/60.0, 195.0/60.0);
                    $.delayTimeDistribution = new UniformRandom($.random, 0.0, 0.0);
                    $.delayProbability = 0.0;
                    $.logResults = logResults;
                }).createSubwaySystemLoop();
        addSubwaySystemLoop(config);
    }
}
