package Subway;

import java.util.Optional;
import java.util.Random;

public class ExpFrame6TrainsWithDelays extends BaseExpFrame {

    public ExpFrame6TrainsWithDelays() {
        super("Experimental Frame 6 Trains With Delays");

        SubwaySystemLoopConfig config = createScarboroughLoop(6)
                .with($ -> {
                    $.random = new Random(1000);
                    $.loadingTimeDistribution = new UniformRandom($.random, 15.0/60.0, 195.0/60.0);
                    $.breakdownGenerator = Optional.of(new CoupledBreakdownGenerator(
                            new UniformRandom($.random, 0.0, 3.0),
                            new UniformRandom($.random, 0.0, 10.0)));
                }).createSubwaySystemLoop();
        addSubwaySystemLoop(config);
    }
}
