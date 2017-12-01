package Subway;

import GenCol.entity;
import model.modeling.message;
import view.modeling.ViewableAtomic;

public class BreakdownNotifier extends ViewableAtomic {
    public static final String IN_BREAKDOWN_PORT = "inBreakdown";
    public static final String OUT_ELAPSED_PORT = "outElapsed";

    public BreakdownNotifier() {
        super("Breakdown Notifier");
        addInport(IN_BREAKDOWN_PORT);
        addOutport(OUT_ELAPSED_PORT);
    }

    @Override
    public void deltext(double e, message x) {
        holdIn("notifiy", 0);
    }

    @Override
    public void deltint() {
        passivate();
    }

    @Override
    public message out() {
        message m = new message();
        m.add(makeContent(OUT_ELAPSED_PORT, new entity("Elapsed")));
        return m;
    }
}
