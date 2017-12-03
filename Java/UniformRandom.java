package Subway;

import java.util.Random;

public class UniformRandom {

    private final Random _random;
    private final double _min;
    private final double _max;

    public UniformRandom(Random r, double min, double max) {
        _random = r;
        _min = min;
        _max = max;
    }

    public UniformRandom(double min, double max) {
        this(new Random(), min , max);
    }

    public double nextDouble() { return _random.nextDouble(); }

    public double draw() {
        return (_max - _min) * _random.nextDouble() + _min;
    }
}
