package com.concordia.velocity.observer;

public class DashboardObserver implements Observer {
    @Override
    public void update(String message) {
        System.out.println("Dashboard updated: " + message);
    }
}