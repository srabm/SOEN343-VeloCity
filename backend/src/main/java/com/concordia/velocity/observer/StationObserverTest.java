package com.concordia.velocity.observer;

import com.concordia.velocity.model.Station;

import java.util.Arrays;

public class StationObserverTest {
    public static void main(String[] args) {
        // Create a sample Station 
        Station testStation = new Station(
                "S999",
                "Test Station",
                "occupied",
                "45.5000",
                "-73.5600",
                "123 Test St, Montreal",
                10,  // capacity
                8,   // numDockedBikes
                10,  // reservationHoldTime
                Arrays.asList("D001","D002","D003","D004","D005","D006","D007","D008","D009","D010"),
                Arrays.asList("B001","B002","B003","B004","B005","B006","B007","B008"),
                2,
                8
        );

        // Create and attach observers 
        DashboardObserver dashboardObserver = new DashboardObserver();
        NotificationObserver notificationObserver = new NotificationObserver();

        testStation.attach(dashboardObserver);
        testStation.attach(notificationObserver);

        // Change status and trigger notifications
        System.out.println("Changing station status to FULL...");
        testStation.setStatus("full");

        System.out.println("\n Changing station status to OUT_OF_SERVICE...");
        testStation.setStatus("out_of_service");

        System.out.println("\n Changing station status to EMPTY...");
        testStation.setStatus("empty");

        // Detach one observer and test again
        System.out.println("\n Detaching DashboardObserver...");
        testStation.detach(dashboardObserver);

        System.out.println("Changing station status to OCCUPIED...");
        testStation.setStatus("occupied");

        System.out.println("\nTest complete.");
    }
}
