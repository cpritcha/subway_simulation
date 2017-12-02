package Subway;

import view.modeling.ViewableComponent;

import java.awt.Dimension;
import java.awt.Point;
import java.util.*;
import java.util.stream.Collectors;

public class SimplExpFrame extends BaseExpFrame {

    public SimplExpFrame() {
        super("Simple Experimental Frame");

        // Define the track lengths from Kennedy to McCowan.
        // The last element is the long segment from McCowan to Kennedy
        ArrayList<Integer> trackLengths = new ArrayList<Integer>(Arrays.asList(3, 4));
        System.out.println("trackLengths.size = " + trackLengths.size());
        ArrayList<TrackSection> tracks = new ArrayList<TrackSection>(trackLengths.size());
        System.out.println("tracks.size = " + tracks.size());
        for (int k = 0; k < trackLengths.size(); k++) {
            System.out.println("k = " + k);
            tracks.add(k, new TrackSection(trackLengths.get(k)));
        }

        // Found typical business day ridership here:
        // https://www1.toronto.ca/wps/portal/contentonly?vgnextoid=c077c316f16e8410VgnVCM10000071d60f89RCRD
        //
        // To compute rates below, take values from spreadsheet, divide riders over
        // 12 hours and then divide by 2 (since we have one 'station' per direction
        // for each stop)

        Object[][] stationData = {
                // Station Name, Passenger Creation Rate
                {"Kennedy", 13 /* 17,969 */},
                {"Lawrence East", 3 /* 4,326 */},
        };

        ArrayList<Station.Builder> stationBuilders = Station.Builder.fromData(stationData);

        ArrayList<Station> stations = stationBuilders.stream()
                .map(Station.Builder::createStation)
                .collect(Collectors.toCollection(ArrayList::new));

        // Create an array of loops for passing to the scheduler
        SubwayLoop eastLoop = new SubwayLoop("Scarborough East", tracks, stations);
        ArrayList<SubwayLoop> loops = new ArrayList<SubwayLoop>();
        loops.add(eastLoop);

        // Trains
        // Note: According to the wikipedia Line 3 pages, 6 four-car trains operate
        // on the route, though it is reduced to 5 for construction until 2018.
        // From here: https://en.wikipedia.org/wiki/S-series_(Toronto_subway) the
        // train capacity is 30 seated, 55 standing
        //
        // Add three trains for each direction
        ArrayList<String> eastTrainNames = new ArrayList<String>(Arrays.asList("ET1"));

        // Put the trains into groups
        double minLoadTime = 15.0 / 60.0; // Minimum load time in minutes
        double maxLoadDisturbanceTime = 0.0 / 60.0; // Maximum additional load time in minutes (random uniform distribution)
        TrainGroup eastTrainGroup = new TrainGroup("East Trains", eastTrainNames, minLoadTime, maxLoadDisturbanceTime);

        // Add all the train groups into a list
        ArrayList<TrainGroup> trainGroups = new ArrayList<TrainGroup>();
        trainGroups.add(eastTrainGroup);

        // Specify the train starting positions
        ArrayList<Integer> eastTrainPositions = new ArrayList<Integer>(Arrays.asList(1));

        // Create an array of train starting positions for passing to the scheduler
        ArrayList<ArrayList<Integer>> initialTrainPositions = new ArrayList<>();
        initialTrainPositions.add(eastTrainPositions);

        setupExpFrame(loops, trainGroups, initialTrainPositions);
    }
}
