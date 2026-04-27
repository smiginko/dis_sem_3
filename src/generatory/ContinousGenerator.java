package generatory;

import java.util.Random;

public class ContinousGenerator {
    private Random generator;
    private int min;
    private int max;

    public ContinousGenerator(int min, int max, Random seedGenerator) {
        this.min = min;
        this.max = max;
        this.generator = new Random(seedGenerator.nextInt());
    }

    public double nextDouble() {
        return generator.nextDouble(this.min, this.max);
    }
}