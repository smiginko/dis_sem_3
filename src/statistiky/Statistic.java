package statistiky;

public class Statistic {
    private String name;
    private double sum;
    private double sumSq;
    private int count;

    public Statistic(String name) {
        this.name = name;
        this.reset();
    }

    public void reset() {
        this.sum = 0;
        this.sumSq = 0;
        this.count = 0;
    }

    public void addValue(double value) {
        double v = Math.max(0, value);
        this.sum += v;
        this.sumSq += v * v;
        this.count++;
    }

    public double getAverage() {
        return (count == 0) ? 0 : sum / count;
    }

    public int getCount() {
        return count;
    }

    public double[] getConfidenceInterval() {
        if (count <= 30) return null;
        
        double mean = getAverage();
        double variance = (sumSq - (sum * sum / count)) / (count - 1);
        if (variance < 0) variance = 0;
        double stdDev = Math.sqrt(variance);
        
        double zAlpha = 1.96; // 95% IS
        double halfWidth = (zAlpha * stdDev) / Math.sqrt(count);
        return new double[]{mean - halfWidth, mean + halfWidth};
    }

    public String getName() {
        return name;
    }
}
