package generatory;

import java.util.ArrayList;
import java.util.Random;

public class DiscreteEmpiricGenerator extends EmpiricGenerator {
    public DiscreteEmpiricGenerator(Random seedGenerator, ArrayList<EmpiricData> empiricDataList) {
        super(seedGenerator, empiricDataList);
    }

    public int nextInt() {
        double p = probabilityGenerator.nextDouble();
        double cumulativeProbability = 0;

        for (EmpiricData data : empiricDataList) {
            cumulativeProbability += data.getProbability();
            if (p < cumulativeProbability) {
                return data.getRandom().nextInt(data.getMin(), data.getMax());
            }
        }
        throw new RuntimeException("Wrong cumulative probability");
    }
}
