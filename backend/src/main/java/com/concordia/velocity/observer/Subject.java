package com.concordia.velocity.observer;

public interface Subject {
    void attach(Observer observer);
    void detach(Observer observer);
    void reportProblem(Observer observer);
    void notifyObservers();
}