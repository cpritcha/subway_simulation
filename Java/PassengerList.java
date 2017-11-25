package Subway;

import GenCol.*;
import java.util.Collection;
import java.util.LinkedList;
import java.util.stream.Collectors;

public class PassengerList extends LinkedList<Passenger> {
    public PassengerList copy() {
        return (PassengerList)clone();
    }
}
