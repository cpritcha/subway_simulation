package Subway;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SubwaySystemLoopConfig {
    /* Includes all components necessary to build a full loop. Used for building loops for setting up experiments */

    public SubwayLoop loopLayout;
    public TrainGroup trainGroup;
    public ArrayList<Integer> initialPositions;

    public static class Builder {
        public String loopName;
        public ArrayList<Integer> trackLengths;
        public Object[][] stationData;
        public String trainGroupName;
        public ArrayList<String> trainNames;
        public ArrayList<Integer> trainPositions;
        public double minLoadTime;
        public double maxLoadDisturbanceTime;
        public Optional<CoupledBreakdownGenerator> breakdownGenerator;

        public Builder with(
                Consumer<Builder> builderFunction) {
            builderFunction.accept(this);
            return this;
        }

        public SubwaySystemLoopConfig createSubwaySystemLoop() {
            return new SubwaySystemLoopConfig(loopName, trackLengths, stationData,
                    trainGroupName, trainNames, trainPositions, minLoadTime, maxLoadDisturbanceTime,
                    breakdownGenerator);
        }
    }

    public SubwaySystemLoopConfig(String loopName, List<Integer> trackLengths, Object[][] stationData,
                                  String trainGroupName, ArrayList<String> trainNames, ArrayList<Integer> initialPositions,
                                  double minLoadTime, double maxLoadDisturbanceTime, Optional<CoupledBreakdownGenerator> cbg) {
        ArrayList<TrackSection> trackSections = TrackSection.createTracks(trackLengths);
        ArrayList<Station> stations = Station.Builder.fromData(stationData).stream()
                .map(Station.Builder::createStation)
                .collect(Collectors.toCollection(ArrayList::new));
//        UniformRandom loadingsTimeDistribution = new UniformRandom(minLoadTime, minLoadTime);
        loopLayout = new SubwayLoop(loopName, trackSections, stations);
        trainGroup = new TrainGroup(trainGroupName, trainNames, minLoadTime, maxLoadDisturbanceTime);
        cbg.ifPresent(g -> trainGroup.addBreakdowns(g));
        this.initialPositions = initialPositions;
    }
}
