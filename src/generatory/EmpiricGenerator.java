package generatory;

import java.util.ArrayList;
import java.util.Random;

abstract class EmpiricGenerator {
    protected  Random seedGenerator;
    protected  ArrayList<EmpiricData> empiricDataList;
    protected  Random probabilityGenerator;

    public EmpiricGenerator(Random seedGenerator, ArrayList<EmpiricData> empiricDataList) {
        if (seedGenerator == null || empiricDataList == null || empiricDataList.isEmpty()) { throw new IllegalArgumentException("Wrong parameters"); }
        this.seedGenerator = seedGenerator;
        this.empiricDataList = empiricDataList;
        this.probabilityGenerator = new Random(this.seedGenerator.nextInt());
        this.inicializeRandoms();
    }


    private void inicializeRandoms(){
        double cumul = 0;
        for (EmpiricData empiricData : empiricDataList) {
            empiricData.assignRandom(this.seedGenerator.nextInt());
            cumul += empiricData.getProbability();
        }
        if (cumul < 0.999999 ){ throw  new RuntimeException("Cumulative probability have to equal 1!"); }
    }
}
