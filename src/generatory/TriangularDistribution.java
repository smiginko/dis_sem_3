package generatory;

import java.util.Random;

public class TriangularDistribution {
    private final Random random;
    private final double min;
    private final double max;
    private final double mod;
    private final double F_c;

    public TriangularDistribution(Random random, double min, double max, double mod) {
        if (!(min <= mod && mod <= max) || (min == max)) {
            throw new IllegalArgumentException("Musí platiť min <= mod <= max a zároveň min != max");
        }
        this.min = min;
        this.max = max;
        this.mod = mod;
        this.random = new Random(random.nextInt());
        this.F_c = (mod - min) / (max - min);
    }


    public double nextValue() {
        double u = random.nextDouble();
        if (u < F_c) {
            return min + Math.sqrt(u * (max - min) * (mod - min));
        } else {
            return max - Math.sqrt((1 - u) * (max - min) * (max - mod));
        }
    }


    public boolean runChiSquareTest(int nSamples) {
        int nBins = 10;
        int[] observed = new int[nBins];
        double expectedPerBin = (double) nSamples / nBins;

        for (int i = 0; i < nSamples; i++) {
            double x = this.nextValue();
            double p;

            // Distribučná funkcia F(x) pre trojuholníkové rozdelenie
            if (x <= mod) {
                p = Math.pow(x - min, 2) / ((max - min) * (mod - min));
            } else {
                p = 1 - Math.pow(max - x, 2) / ((max - min) * (max - mod));
            }

            int binIndex = (int) (p * nBins);
            if (binIndex >= nBins) binIndex = nBins - 1;
            if (binIndex < 0) binIndex = 0;
            observed[binIndex]++;
        }

        double chiStat = 0;
        for (int count : observed) {
            chiStat += Math.pow(count - expectedPerBin, 2) / expectedPerBin;
        }

        double criticalValue = 16.92; // Pre 9 stupňov voľnosti
        System.out.printf("--- Triangular Chi-Square (a=%.1f, b=%.1f, c=%.1f) ---\n", min, max, mod);
        System.out.printf("Statistika: %.4f (Kriticka: %.2f)\n", chiStat, criticalValue);

        return chiStat < criticalValue;
    }
}