package generatory;

import java.util.Random;

public class EmpiricData {
    private final int min;
    private final int max;
    private final double probability;
    private Random random = null;

    public EmpiricData(int min, int max, double probability) {
        this.min = min;
        this.max = max;
        this.probability = probability;
    }

    public int getMin() {
        return min;
    }

    public int getMax() {
        return max;
    }

    public double getProbability() {
        return probability;
    }

    public Random getRandom() {
        return random;
    }

    public void assignRandom(int seed) {
        if (this.random == null) { this.random = new Random(seed); }
        else { throw new IllegalStateException("Random already assigned"); }
    }
}
