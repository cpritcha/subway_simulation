package Subway;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SubwaySystemLoopConfig {
    /* Includes all components necessary to build a full loop. Used for building loops for setting up experiments */

    public SubwayLoop loopLayout;
    public TrainGroup trainGroup;
    public ArrayList<Integer> initialPositions;
    public boolean logResults;

    public static class Builder {
        public String loopName;
        public ArrayList<Integer> trackLengths;
        public Object[][] stationData;
        public String trainGroupName;
        public ArrayList<String> trainNames;
        public ArrayList<Integer> trainPositions;
        public UniformRandom loadingTimeDistribution;
        public UniformRandom delayTimeDistribution;
        public double delayProbability;
        public Random random;
        public boolean logResults;

        public Builder with(
                Consumer<Builder> builderFunction) {
            builderFunction.accept(this);
            return this;
        }

        public SubwaySystemLoopConfig createSubwaySystemLoop() {
            return new SubwaySystemLoopConfig(loopName, trackLengths, stationData,
                    trainGroupName, trainNames, trainPositions, loadingTimeDistribution,
                    delayProbability, delayTimeDistribution, random, logResults);
        }
    }

    public SubwaySystemLoopConfig(String loopName, List<Integer> trackLengths, Object[][] stationData,
                                  String trainGroupName, ArrayList<String> trainNames, ArrayList<Integer> initialPositions,
                                  UniformRandom loadingTimeDistribution, double delayProbability,
                                  UniformRandom delayTimeDistribution, Random random, boolean logResults) {
        ArrayList<TrackSection> trackSections = TrackSection.createTracks(trackLengths);
        ArrayList<Station> stations = Station.Builder.fromData(stationData, random).stream()
                .map(Station.Builder::createStation)
                .collect(Collectors.toCollection(ArrayList::new));
        loopLayout = new SubwayLoop(loopName, trackSections, stations);
        trainGroup = new TrainGroup(trainGroupName, trainNames, loadingTimeDistribution,
                delayProbability, delayTimeDistribution);
        this.initialPositions = initialPositions;
        this.logResults = logResults;
    }
}
