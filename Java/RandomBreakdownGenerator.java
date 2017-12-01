package Subway;

import GenCol.doubleEnt;
import model.modeling.message;
import view.modeling.ViewableAtomic;

import java.util.UUID;

public class RandomBreakdownGenerator extends ViewableAtomic {
    public static final String OUT_BREAKDOWN_PORT = "outBreakdown";

    private static final String PASSIVE_PHASE = "passive";

    private UniformRandom _lengthOfNextGenerator, _timeToNextBreakdownGenerator;
    private UUID _trainId;

    public RandomBreakdownGenerator(UniformRandom breakdownLengthGenerator,
                                    UniformRandom timeBetweenBreakdownGenerator,
                                    Train train) {
        super("Breakdowns: " + train.getName()); // coupling have to have unique names
        _lengthOfNextGenerator = breakdownLengthGenerator;
        _timeToNextBreakdownGenerator = timeBetweenBreakdownGenerator;
        _trainId = train.getID();
        addOutport(OUT_BREAKDOWN_PORT);
    }

    @Override
    public void initialize() {
        holdIn(PASSIVE_PHASE, _timeToNextBreakdownGenerator.draw());
    }

    @Override
    public void deltext(double e, message x) {
        Continue(e);
    }

    @Override
    public void deltint() {
        if (phaseIs("emit")) {
            holdIn(PASSIVE_PHASE, _timeToNextBreakdownGenerator.draw());
        } else {
            holdIn("emit", 0);
        }
    }

    @Override
    public message out() {
        message m = new message();
        if (phaseIs("emit")) {
            m.add(makeContent(OUT_BREAKDOWN_PORT, new KeyValueEntity<>(_trainId, _lengthOfNextGenerator.draw())));
        }
        return m;
    }
}
