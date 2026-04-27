package generatory;

import java.util.ArrayList;
import java.util.Random;

public class ContinousEmpiricGenerator extends EmpiricGenerator {
    public ContinousEmpiricGenerator(Random seedGenerator, ArrayList<EmpiricData> empiricDataList) {
        super(seedGenerator, empiricDataList);
    }

    public double nextDouble() {
        double p = probabilityGenerator.nextDouble();
        double cumulativeProbability = 0;

        for (EmpiricData data : empiricDataList) {
            cumulativeProbability += data.getProbability();
            if (p < cumulativeProbability) {
                return data.getRandom().nextDouble(data.getMin(), data.getMax());
            }
        }
        throw new RuntimeException("Wrong cumulative probability");
    }
}
