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
    private int _remainingCapacity;
    private Optional<content> _result = Optional.empty();

    private TrackSection _prevSection;
    private TrackSection _nextSection;

    TrackSection(int length) {
        _length = length;
        _capacity = 1;
        _remainingCapacity = _capacity;
        sigma = INFINITY;

        addInport(IN_ACQUIRE_PORT);
        addInport(IN_RELEASE_PORT);
        addOutport(OUT_ACQUIRE_PORT);
        addOutport(OUT_RELEASE_PORT);
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

    public void deltint() {}

    private TreeSet<entity> entitiesOnPort(message m, String portName) {
        TreeSet<entity> entities = new TreeSet<entity>();
        for (int i = 0; i < m.size(); i++) {
            if (messageOnPort(m, portName, i)) {
                entities.add(m.getValOnPort(portName, i));
            }
        }
        return entities;
    }

    private TreeSet<entity> inAcquireEntities(message m) {
        return entitiesOnPort(m, IN_ACQUIRE_PORT);
    }

    private TreeSet<entity> inReleaseEntities(message m) {
        return entitiesOnPort(m, IN_RELEASE_PORT);
    }

    public void deltext(double e,message x) {
        // A predictable iteration order is required so that the order of elements in the bag does not change whether
        // or not the semaphore can be acquired or released. Out messages include the train interface so that if only
        // some acquire or release requests are granted in a time frame we know who to give permission to.
        TreeSet<entity> inAcquired = inAcquireEntities(x);
        TreeSet<entity> inReleased = inReleaseEntities(x);

        for (entity acquireRequest: inAcquired) {
            String id = acquireRequest.getName();
            LockEntity le;
            if (_remainingCapacity > 0) {
                _remainingCapacity -= 1;
                le = new LockEntity(id, true);
            } else {
                le = new LockEntity(id, false);
            }
            _result = Optional.of(makeContent(OUT_ACQUIRE_PORT, le));
        }

        for (entity releaseRequest: inReleased) {
            String id = releaseRequest.getName();
            LockEntity le;
            if (_remainingCapacity < _capacity) {
                _remainingCapacity += 1;
                le = new LockEntity(id, true);
            } else {
                le = new LockEntity(id, false);
            }
            _result = Optional.of(makeContent(OUT_RELEASE_PORT, le));
        }
    }

    public message out() {
        message m = new message();
        if (_result.isPresent()) {
            m.add(_result.get());
        }
        _result = Optional.empty();
        return m;
    }
}
