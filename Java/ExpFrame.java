package Subway;

import javafx.util.Pair;
import view.modeling.ViewableDigraph;
import view.modeling.ViewableAtomic;
import view.modeling.ViewableComponent;

import java.awt.Dimension;
import java.awt.Point;
import java.util.*;
import java.util.stream.Collectors;

public class ExpFrame extends BaseExpFrame {

    public ExpFrame() {
        super("Experimental Frame");

        SubwaySystemLoopConfig config = new SubwaySystemLoopConfig.Builder().with($ -> {
            $.loopName = "Scarborough";
            $.trackLengths = new ArrayList<>(Arrays.asList(
                    // Kennedy (East) to McCowan (East) (includes track section from Kennedy (West) to Kennedy (East)
                    3, 2, 1, 1, 1, 1,
                    // McCowan (West) to Kennedy (West)
                    1, 1, 1, 2, 3, 1
            ));
            $.stationData = new Object[][]{
                    // Station Name, Passenger Creation Rate
                    {"Kennedy (East)", 13 /* 17,969 */},
                    {"Lawrence East (East)", 3 /* 4,326 */},
                    {"Ellesmere (East)", 1 /* 864 */},
                    {"Midland (East)", 1 /* 1,358 */},
                    {"Scarborough Centre (East)", 8 /* 10,979 */},
                    {"McCowan (East)", 0},
                    {"McCowan (West)", 2 /* 2,857 */},
                    {"Scarborough Centre (West)" , 8},
                    {"Midland (West)", 1},
                    {"Ellesmere (West)", 1},
                    {"Lawrence East (West)", 3},
                    {"Kennedy (West)", 0}
            };
            $.trainGroupName = "Trains";
            $.minLoadTime = 15.0 / 60.0; // Minimum load time in minutes
            $.maxLoadDisturbanceTime = 180.0 / 60.0; // Maximum additional load time in minutes (random uniform distribution)
            $.trainNames = new ArrayList<>(Arrays.asList("T1", "T2", "T3", "T4", "T5", "T6"));
            $.trainPositions = new ArrayList<>(Arrays.asList(1, 3, 5, 7, 9, 11));

            $.breakdownGenerator = Optional.empty();
        }).createSubwaySystemLoop();

        addSubwaySystemLoop(config);
    }

}
