package com.example.serviceoptimization;

import java.util.HashMap;
import java.util.Map;

public class ReinforcementAgent {
    private double learningRate = 1;
    private double discountFactor = 0.9;
    private Map<State, Double> knowledge;

    public ReinforcementAgent() {
        this.knowledge = new HashMap<State, Double>();
    }

    public ReinforcementAgent(double learningRate, double discountFactor) {
        this.learningRate = learningRate;
        this.discountFactor = discountFactor;
        this.knowledge = new HashMap<State, Double>();
    }


}
