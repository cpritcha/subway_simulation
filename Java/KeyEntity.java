package Subway;

import GenCol.entity;

import java.util.UUID;

/**
 * An entity that has an id from the sender
 */
public class KeyEntity extends entity implements IWithUUID {
    private final UUID _id;

    public KeyEntity(UUID id) {
        _id = id;
    }

    public UUID getID() {
        return _id;
    }
}
