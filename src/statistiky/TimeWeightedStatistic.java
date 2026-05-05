package statistiky;

public class TimeWeightedStatistic {
    private final String name;
    private double weightedSum;
    private double startTime;
    private double lastChangeTime;
    private double currentValue;

    public TimeWeightedStatistic(String name, double startTime, double initialValue) {
        this.name = name;
        this.reset(startTime, initialValue);
    }

    public void reset(double startTime, double initialValue) {
        this.weightedSum = 0;
        this.startTime = startTime;
        this.lastChangeTime = startTime;
        this.currentValue = Math.max(0, initialValue);
    }

    public void update(double newValue, double currentTime) {
        double safeTime = Math.max(currentTime, this.lastChangeTime);
        this.weightedSum += this.currentValue * (safeTime - this.lastChangeTime);
        this.lastChangeTime = safeTime;
        this.currentValue = Math.max(0, newValue);
    }

    public double getAverage(double currentTime) {
        double safeTime = Math.max(currentTime, this.lastChangeTime);
        double observationTime = safeTime - this.startTime;
        if (observationTime <= 0) {
            return this.currentValue;
        }

        double finalSum = this.weightedSum + (this.currentValue * (safeTime - this.lastChangeTime));
        return finalSum / observationTime;
    }

    public String getName() {
        return name;
    }
}
