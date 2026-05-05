package generatory;

import java.util.Random;

public class ErlangDistribution {
    private final Random random;
    private final int k;
    private final double lambda; // rate = k / mean

    /**
     * Tento kód je vygenerovaný umelou inteligenciu - kapitola 3 dokumentácie AI
     *
     * @param random seed source
     * @param k      shape (počet fáz, kladné celé číslo)
     * @param mean   stredná hodnota celého rozdelenia (= k / lambda)
     */
    public ErlangDistribution(Random random, int k, double mean) {
        if (k < 1) throw new IllegalArgumentException("k musí byť >= 1");
        if (mean <= 0) throw new IllegalArgumentException("mean musí byť > 0");
        this.random = new Random(random.nextInt());
        this.k = k;
        this.lambda = k / mean;
    }

    public double nextValue() {
        // Erlang(k, λ) = súčet k nezávislých Exp(λ)
        // X = -1/λ * ln(U1 * U2 * ... * Uk)
        double product = 1.0;
        for (int i = 0; i < k; i++) {
            product *= random.nextDouble();
        }
        return -Math.log(product) / lambda;
    }

    // CDF: F(x) = 1 - e^(-λx) * sum_{n=0}^{k-1} (λx)^n / n!
    private double cdf(double x) {
        if (x <= 0) return 0.0;
        double lx = lambda * x;
        double eLx = Math.exp(-lx);
        double sum = 0.0;
        double term = 1.0; // (λx)^n / n!
        for (int n = 0; n < k; n++) {
            if (n > 0) term *= lx / n;
            sum += term;
        }
        return 1.0 - eLx * sum;
    }

    public boolean runChiSquareTest(int nSamples) {
        int nBins = 10;
        int[] observed = new int[nBins];
        double expectedPerBin = (double) nSamples / nBins;

        for (int i = 0; i < nSamples; i++) {
            double x = nextValue();
            double p = cdf(x);
            int binIndex = (int) (p * nBins);
            if (binIndex >= nBins) binIndex = nBins - 1;
            if (binIndex < 0) binIndex = 0;
            observed[binIndex]++;
        }

        double chiStat = 0;
        for (int count : observed) {
            chiStat += Math.pow(count - expectedPerBin, 2) / expectedPerBin;
        }

        double criticalValue = 16.92; // 9 stupňov voľnosti, α = 0.05
        System.out.printf("--- Erlang Chi-Square (k=%d, mean=%.2f) ---\n", k, (double) k / lambda);
        System.out.printf("Štatistika: %.4f (Kritická: %.2f)\n", chiStat, criticalValue);
        System.out.println("Výsledok: " + (chiStat < criticalValue ? "PASS" : "FAIL"));
        return chiStat < criticalValue;
    }
}
