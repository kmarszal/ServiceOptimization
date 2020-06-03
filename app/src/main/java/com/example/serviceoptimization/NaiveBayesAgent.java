package com.example.serviceoptimization;

import java.util.Random;

import weka.classifiers.bayes.NaiveBayesUpdateable;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class NaiveBayesAgent implements Agent {
    private NaiveBayesUpdateable naiveBayes;
    private Instances structure;
    private Random random;

    public NaiveBayesAgent() {
        FastVector attrs = Data.getAttributesNominalDuration();
        structure = new Instances("MyRelation", attrs, 0);
        structure.setClassIndex(structure.numAttributes() - 1);
        naiveBayes = new NaiveBayesUpdateable();
        try {
            naiveBayes.buildClassifier(structure);
        } catch (Exception e) {
            e.printStackTrace();
        }
        random = new Random();
    }

    @Override
    public boolean shouldOffload(State state) {
        try {
            Data data = new Data(state, state);
            if(structure.numInstances() < 10)
                return random.nextBoolean();
            Instance notOffloaded = data.asNotOffloaded().toWekaInstanceNominalDuration();
            notOffloaded.setDataset(structure);
            Instance offloaded = data.asOffloaded().toWekaInstanceNominalDuration();
            offloaded.setDataset(structure);
            double notOffloadedPrediction = naiveBayes.classifyInstance(notOffloaded);
            double offloadedPrediction = naiveBayes.classifyInstance(offloaded);
            return offloadedPrediction < notOffloadedPrediction;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public void updateKnowledge(Data data) {
        try {
            Instance instance = data.toWekaInstanceNominalDuration();
            instance.setDataset(structure);
            structure.add(instance);
            if(structure.numInstances() == 10)
                naiveBayes.buildClassifier(structure);
            if(structure.numInstances() > 10)
                naiveBayes.updateClassifier(instance);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
