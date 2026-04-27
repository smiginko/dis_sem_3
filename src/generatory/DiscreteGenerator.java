package generatory;

import java.util.Random;

public class DiscreteGenerator {
    private Random generator;
    private int min;
    private int max;

    public DiscreteGenerator(int min, int max, Random seedGenerator) {
        this.min = min;
        this.max = max;
        this.generator = new Random(seedGenerator.nextInt());
    }

    public int nextInt() {
        return generator.nextInt(this.min, this.max + 1);
    }
}