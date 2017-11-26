package Subway;

import GenCol.entity;

import java.util.UUID;


/**
 * An entity that has an id from the sender and a payload
 */
public class KeyValueEntity<T> extends entity implements IWithUUID {
    private final T _value;
    private final UUID _id;

    public KeyValueEntity(UUID id, T value) {
        _value = value;
        _id = id;
    }

    public String getName() {
        return String.format("<KV id=%s value=%s>", _id.toString(), _value.toString());
    }

    public T getValue() {
        return _value;
    }

    public void print() {
        System.out.println(getName());
    }

    public UUID getID() {
        return _id;
    }
}
