package Subway;

import java.util.Optional;
import java.util.Random;

public class ExpFrame6TrainsWithDelays extends BaseExpFrame {

    public ExpFrame6TrainsWithDelays() {
        this(false);
    }

    public ExpFrame6TrainsWithDelays(boolean logResults) {
        super("Experimental Frame 6 Trains With Delays");

        SubwaySystemLoopConfig config = createScarboroughLoop(6)
                .with($ -> {
                    $.random = new Random();
                    $.loopName = "ExpFrame6TrainsWithDelays";
                    $.loadingTimeDistribution = new UniformRandom($.random, 15.0/60.0, 195.0/60.0);
                    $.delayTimeDistribution = new UniformRandom($.random, 0.0, 3.0);
                    $.delayProbability = 0.2;
                    $.logResults = logResults;
                }).createSubwaySystemLoop();
        addSubwaySystemLoop(config);
    }
}
