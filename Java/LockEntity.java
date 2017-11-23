package Subway;

import GenCol.entity;


public class LockEntity extends entity {
    private String _trainName;
    private boolean _successful;

    public LockEntity(String name, boolean successful) {
        _trainName = name;
        _successful = successful;
    }

    public String getTrainName() {
        return _trainName;
    }

    public boolean getSuccessful() {
        return _successful;
    }

    @Override
    public String getName() {
        return _trainName + " " + Boolean.toString(_successful);
    }
}
