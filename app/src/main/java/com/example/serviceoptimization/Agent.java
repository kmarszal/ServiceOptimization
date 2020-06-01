package com.example.serviceoptimization;

public interface Agent {
    boolean shouldOffload(State state);
    void updateKnowledge(Data data);
}
