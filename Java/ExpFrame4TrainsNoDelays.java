package Subway;

import java.util.Optional;
import java.util.Random;

public class ExpFrame4TrainsNoDelays extends BaseExpFrame {

    public ExpFrame4TrainsNoDelays() {
        super("Experimental Frame 4 Trains No Delays");

        SubwaySystemLoopConfig config = createScarboroughLoop(4)
                .with($ -> {
                    $.random = new Random(1000);
                    $.loadingTimeDistribution = new UniformRandom($.random, 15.0/60.0, 195.0/60.0);
                    $.breakdownGenerator = Optional.empty();
                }).createSubwaySystemLoop();
        addSubwaySystemLoop(config);
    }
}
