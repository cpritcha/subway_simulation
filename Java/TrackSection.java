package Subway;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.UUID;
import java.util.stream.Collectors;

import model.modeling.message;
import view.modeling.ViewableAtomic;

public class TrackSection extends ViewableAtomic implements IWithUUID {
    public static final String IN_ACQUIRE_PORT = "inAcquire";
    public static final String IN_RELEASE_PORT = "inRelease";
    public static final String OUT_RELEASE_PORT = "outRelease";
    public static final String OUT_ACQUIRE_PORT = "outAcquire";

    // How many trains can occupy this section at once
    private final int _capacity;
    // How long it takes to travel through this track section
    private final int _travelTime;
    private final UUID _id;

    private TreeSet<UUID> _trainsOnSection;
    private message _result;

    public TrackSection(int travelTime, int capacity) {
        super("TrackSection: " + Integer.toString(travelTime));
        _id = UUID.randomUUID();
        _capacity = capacity;
        _travelTime = travelTime;

        initialize();

        addInport(IN_ACQUIRE_PORT);
        addInport(IN_RELEASE_PORT);
        addOutport(OUT_ACQUIRE_PORT);
        addOutport(OUT_RELEASE_PORT);

        UUID trainID1 = UUID.randomUUID();
        UUID trainID2 = UUID.randomUUID();
        addTestInput(IN_ACQUIRE_PORT, new KeyValueEntity<>(getID(), trainID1));
        addTestInput(IN_ACQUIRE_PORT, new KeyValueEntity<>(getID(), trainID2));
        addTestInput(IN_RELEASE_PORT, new KeyValueEntity<>(getID(), trainID1));
        addTestInput(IN_RELEASE_PORT, new KeyValueEntity<>(getID(), trainID2));
    }

    public TrackSection(int travelTime) {
        this(travelTime, 1);
    }

    public TrackSection() {
        this(5, 1);
    }

    protected static ArrayList<TrackSection> createTracks(List<Integer> trackLengths) {
        return trackLengths.stream().map(TrackSection::new).collect(Collectors.toCollection(ArrayList::new));
    }

    public UUID getID() {
        return _id;
    }

    public int getTravelTime() {
        return _travelTime;
    }

    public void initialize() {
        _result = new message();
        _trainsOnSection = new TreeSet<>();
        passivate();
    }

    public void deltint() {
        _result = new message();
        passivate();
    }

    private TreeSet<UUID> entitiesOnPort(message m, String portName) {
        return MessageFilterer.getRelevantContent(m, getID())
                .filter(c -> c.getPortName().equals(portName))
                .map(c -> ((KeyValueEntity<UUID>)c.getValue()).getValue()) // Extracting the train's UUID
                .collect(Collectors.toCollection(TreeSet::new));
    }

    private TreeSet<UUID> getAcquireRequests(message m) {
        return entitiesOnPort(m, IN_ACQUIRE_PORT);
    }

    private TreeSet<UUID> getReleaseRequests(message m) {
        return entitiesOnPort(m, IN_RELEASE_PORT);
    }

    public void deltext(double e,message x) {
        // A predictable iteration order is required so that the order of elements in the bag does not change whether
        // or not the semaphore can be acquired or released. Out messages include the train interface so that if only
        // some acquire or release requests are granted in a time frame we know who to give permission to.
        TreeSet<UUID> inAcquired = getAcquireRequests(x);
        TreeSet<UUID> inReleased = getReleaseRequests(x);

        System.out.println("inAquired: " + inAcquired.toString());
        for (UUID id: inAcquired) {
            KeyValueEntity<Boolean> le;
            if (_trainsOnSection.size() < _capacity && !_trainsOnSection.contains(id)) {
                _trainsOnSection.add(id);
                le = new KeyValueEntity<>(id, true);
            } else {
                le = new KeyValueEntity<>(id, false);
            }
            _result.add(makeContent(OUT_ACQUIRE_PORT, le));
        }

        System.out.println("inReleased: " + inReleased.toString());
        for (UUID id: inReleased) {
            KeyValueEntity<Boolean> le;
            if (_trainsOnSection.contains(id)) {
                _trainsOnSection.remove(id);
                le = new KeyValueEntity<>(id, true);
            } else {
                le = new KeyValueEntity<>(id, false);
            }
            _result.add(makeContent(OUT_RELEASE_PORT, le));
        }
        holdIn("busy", 0);
    }

    public message out() {
        System.err.println("Message: " + _result.toString());
        return _result;
    }
}
