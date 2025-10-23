package com.concordia.velocity.observer;

public class NotificationObserver implements Observer {
    @Override
    public void update(String stationId, String newStatus) {
        System.out.println("Notification sent: Station " + stationId + " status changed to " + newStatus);
    }
}