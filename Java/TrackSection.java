package Subway;

import java.util.Optional;
import java.util.TreeSet;

import model.modeling.content;
import model.modeling.message;
import GenCol.entity;
import view.modeling.ViewableAtomic;

public class TrackSection extends ViewableAtomic {
    public static final String IN_ACQUIRE_PORT = "inAcquire";
    public static final String IN_RELEASE_PORT = "inRelease";
    public static final String OUT_RELEASE_PORT = "outRelease";
    public static final String OUT_ACQUIRE_PORT = "outAcquire";

    private final int _length;
    private final int _capacity;
    private TreeSet<String> _trainsOnSection = new TreeSet<>();
    private message _result = new message();

    private TrackSection _prevSection;
    private TrackSection _nextSection;

    public TrackSection(int length) {
        super("TrackSection: " + Integer.toString(length));
        _length = length;
        _capacity = 1;
        passivate();

        addInport(IN_ACQUIRE_PORT);
        addInport(IN_RELEASE_PORT);
        addOutport(OUT_ACQUIRE_PORT);
        addOutport(OUT_RELEASE_PORT);

        addTestInput(IN_ACQUIRE_PORT, new entity("1"), 0);
        addTestInput(IN_ACQUIRE_PORT, new entity("2"), 0);
        addTestInput(IN_RELEASE_PORT, new entity("1"), 0);
        addTestInput(IN_RELEASE_PORT, new entity("2"), 0);
    }

    public TrackSection() {
        this(5);
    }

    public int getLength() {
        return _length;
    }

    public void setPrevSection(TrackSection prevSection) {
        _prevSection = prevSection;
    }

    public void setNextSection(TrackSection nextSection) {
        _nextSection = nextSection;
    }

    public TrackSection getPrevSection() {
        return _prevSection;
    }

    public TrackSection getNextSection() {
        return _nextSection;
    }

    public void initialize() {

    }

    public void deltint() {
        _result = new message();
        passivate();
    }

    private TreeSet<String> entitiesOnPort(message m, String portName) {
        TreeSet<String> entities = new TreeSet<String>();
        for (int i = 0; i < m.size(); i++) {
            if (messageOnPort(m, portName, i)) {
                entities.add(m.getValOnPort(portName, i).getName());
            }
        }
        return entities;
    }

    private TreeSet<String> inAcquireEntities(message m) {
        return entitiesOnPort(m, IN_ACQUIRE_PORT);
    }

    private TreeSet<String> inReleaseEntities(message m) {
        return entitiesOnPort(m, IN_RELEASE_PORT);
    }

    public void deltext(double e,message x) {
        // A predictable iteration order is required so that the order of elements in the bag does not change whether
        // or not the semaphore can be acquired or released. Out messages include the train interface so that if only
        // some acquire or release requests are granted in a time frame we know who to give permission to.
        TreeSet<String> inAcquired = inAcquireEntities(x);
        TreeSet<String> inReleased = inReleaseEntities(x);

        System.out.println("inAquired: " + inAcquired.toString());
        for (String id: inAcquired) {
            LockEntity le;
            if (_trainsOnSection.size() < _capacity && !_trainsOnSection.contains(id)) {
                _trainsOnSection.add(id);
                le = new LockEntity(id, true);
            } else {
                le = new LockEntity(id, false);
            }
            _result.add(makeContent(OUT_ACQUIRE_PORT, le));
        }

        System.out.println("inReleased: " + inReleased.toString());
        for (String id: inReleased) {
            LockEntity le;
            if (_trainsOnSection.contains(id)) {
                _trainsOnSection.remove(id);
                le = new LockEntity(id, true);
            } else {
                le = new LockEntity(id, false);
            }
            _result.add(makeContent(OUT_RELEASE_PORT, le));
        }
        holdIn("busy", 0);
    }

    public message out() {
        passivate();
        System.err.println("Message: " + _result.toString());
        return _result;
    }
}
