package com.concordia.velocity.observer;

public class NotificationObserver implements Observer {
    @Override
    public void update(String message) {
        System.out.println("Notification sent: " + message);
    }
}