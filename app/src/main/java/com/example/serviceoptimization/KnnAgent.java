package com.example.serviceoptimization;

import java.util.Random;

import weka.classifiers.lazy.IBk;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class KnnAgent implements Agent {
    private IBk iBk;
    private Instances structure;
    private Random random;

    public KnnAgent(int k) {
        FastVector attrs = Data.getAttributes();
        structure = new Instances("MyRelation", attrs, 0);
        structure.setClassIndex(structure.numAttributes() - 1);
        iBk = new IBk(k);
        try {
            iBk.buildClassifier(structure);
        } catch (Exception e) {
            e.printStackTrace();
        }
        random = new Random();
    }

    @Override
    public boolean shouldOffload(State state) {
        try {
            Data data = new Data(state, state);
            if(iBk.getNumTraining() < 10)
                return random.nextBoolean();
            Instance notOffloaded = data.asNotOffloaded().toWekaInstance();
            notOffloaded.setDataset(structure);
            Instance offloaded = data.asOffloaded().toWekaInstance();
            offloaded.setDataset(structure);
            double notOffloadedPrediction = iBk.classifyInstance(notOffloaded);
            double offloadedPrediction = iBk.classifyInstance(offloaded);
            return offloadedPrediction < notOffloadedPrediction;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void updateKnowledge(Data data) {
        try {
            Instance instance = data.toWekaInstance();
            instance.setDataset(structure);
            structure.add(instance);
            iBk.updateClassifier(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
