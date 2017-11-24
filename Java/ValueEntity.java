package Subway;

import GenCol.entity;

public class ValueEntity<T> extends entity {
    private final T _value;

    public ValueEntity(T value) {
        _value = value;
    }

    public String getName() {
        return _value.toString();
    }

    public T getValue() {
        return _value;
    }
}
