package Subway;

import model.modeling.Coupled;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SubwaySystemLoop {
    /* Includes all components necessary to build a full loop. Used for building loops for setting up experiments */

    public SubwayLoop loop;
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
        public Optional<CoupledBreakdownGenerator> cbg;

        public Builder with(
                Consumer<Builder> builderFunction) {
            builderFunction.accept(this);
            return this;
        }

        public SubwaySystemLoop createSubwaySystemLoop() {
            return new SubwaySystemLoop(loopName, trackLengths, stationData,
                    trainGroupName, trainNames, trainPositions, minLoadTime, maxLoadDisturbanceTime,
                    cbg);
        }
    }

    public SubwaySystemLoop(String loopName, List<Integer> trackLengths, Object[][] stationData,
                            String trainGroupName, ArrayList<String> trainNames, ArrayList<Integer> initialPositions,
                            double minLoadTime, double maxLoadDisturbanceTime, Optional<CoupledBreakdownGenerator> cbg) {
        ArrayList<TrackSection> trackSections = TrackSection.createTracks(trackLengths);
        ArrayList<Station> stations = Station.Builder.fromData(stationData).stream()
                .map(Station.Builder::createStation)
                .collect(Collectors.toCollection(ArrayList::new));
        loop = new SubwayLoop(loopName, trackSections, stations);
        trainGroup = new TrainGroup(trainGroupName, trainNames, minLoadTime, maxLoadDisturbanceTime);
        cbg.ifPresent(g -> trainGroup.addBreakdowns(g));
        this.initialPositions = initialPositions;
    }
}
