package com.concordia.velocity.observer;

public class DashboardObserver implements Observer {
    @Override
    public void update(String stationId, String newStatus) {
        System.out.println("Dashboard updated: Station " + stationId + " status changed to " + newStatus);
    }
}