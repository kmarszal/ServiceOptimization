package com.example.serviceoptimization;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class ReinforcementAgent implements Agent {
    private double learningRate = 1;
    private double discountFactor = 0.9;
    private double experimentRate = 0.3;
    private Map<Integer, Double> knowledge;
    private Random random;
    private boolean powerSavingMode = false;

    public ReinforcementAgent() {
        this.knowledge = new HashMap<>();
        this.random = new Random();
    }

    public ReinforcementAgent(double learningRate, double discountFactor, double experimentRate, boolean powerSavingMode) {
        this.learningRate = learningRate;
        this.discountFactor = discountFactor;
        this.experimentRate = experimentRate;
        this.knowledge = new HashMap<>();
        this.random = new Random();
        this.powerSavingMode = powerSavingMode;
    }

    public void setPowerSavingMode(boolean powerSavingMode) {
        this.powerSavingMode = powerSavingMode;
    }

    @Override
    public boolean shouldOffload(State state) {
        if(knowledge.containsKey(state.hashCode()) && knowledge.containsKey(state.hashCode() + 1000000)) {
            boolean optimalAction = knowledge.get(state.hashCode()) < knowledge.get(state.hashCode() + 1000000);
            if(random.nextDouble() < experimentRate) {
                return !optimalAction;
            }
            return optimalAction;
        }
        else if(knowledge.containsKey(state.hashCode())) {
            return random.nextDouble() < experimentRate;
        }
        else if(knowledge.containsKey(state.hashCode() + 1000000)) {
            return random.nextDouble() > experimentRate;
        }
        return random.nextBoolean();
    }

    @Override
    public void updateKnowledge(Data data) {
        if(knowledge.containsKey(data.hashCode())) {
            knowledge.put(data.hashCode(), (1 - learningRate) * knowledge.get(data.hashCode()) +
                    learningRate * (getReward(data) + (discountFactor * Math.max(getReward(data.asNotOffloaded()), getReward(data.asOffloaded())))));
        } else {
            knowledge.put(data.hashCode(), getReward(data) + discountFactor * Math.max(getReward(data.asNotOffloaded()), getReward(data.asOffloaded())));
        }
    }

    private double getReward(Data data) {
        if(powerSavingMode) {
            return - 1 / data.getBatteryLevel() * data.getBatteryConsumption() - 0.001 * data.getDuration();
        }
        return - data.getDuration();
    }
}
