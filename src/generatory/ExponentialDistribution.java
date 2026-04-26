package generatory;

import java.util.Random;

public class ExponentialDistribution {
    private final Random random;
    private final double lambda;


    public ExponentialDistribution(Random random, double mean) {
        this.random = new Random(random.nextInt());
        this.lambda = 1 / mean;
    }

    public double nextValue() {
        return -Math.log(1.0 - this.random.nextDouble()) / this.lambda;
    }



    public boolean runChiSquareTest(int nSamples) {
        int nBins = 10;
        int[] observed = new int[nBins];
        double expectedPerBin = (double) nSamples / nBins;

        // 1. Nazbierame vzorky a rozdelíme ich do binov podľa teoretickej distribučnej funkcie
        for (int i = 0; i < nSamples; i++) {
            double x = this.nextValue();
            // p je hodnota distribučnej funkcie F(x) = 1 - e^(-lambda * x)
            // Hovorí nám, v ktorom percentile sa hodnota nachádza
            double p = 1.0 - Math.exp(-this.lambda * x);

            int binIndex = (int) (p * nBins);
            if (binIndex >= nBins) binIndex = nBins - 1;
            if (binIndex < 0) binIndex = 0;

            observed[binIndex]++;
        }
        // 2. Výpočet štatistiky Chi^2 = Sum( (O - E)^2 / E )
        double chiStat = 0;
        for (int count : observed) {
            chiStat += Math.pow(count - expectedPerBin, 2) / expectedPerBin;
        }
        // 3. Porovnanie s kritickou hodnotou (pre 9 stupňov voľnosti a alfa 0.05 je to 16.92)
        double criticalValue = 16.92;

        System.out.println("--- Chi-Square Test Report ---");
        System.out.printf("Lambda: %.4f | Vzoriek: %d\n", this.lambda, nSamples);
        System.out.printf("Chi-kvadrát štatistika: %.4f (Kritická: %.2f)\n", chiStat, criticalValue);

        boolean isOk = chiStat < criticalValue;
        System.out.println("Výsledok: " + (isOk ? "PASS" : "FAIL"));
        return isOk;
    }
}