package com.concordia.velocity;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.Station;

// tests expiry of a reservation
public class RiderTest {
    public static void main(String[] args) {
        Bike bike = new Bike("B101", "available", "standard", "D101", "S001");
        Station station = new Station("S001", "TestStation", "occupied", "0", "0", "123 Test St", 10, 1, 3, null, null);
        Rider rider = new Rider("Test Rider", "123 Test St", "test@example.com", "1234567890", "testrider");

        System.out.println("Initial bike status: " + bike.getStatus());
        System.out.println("Initial bike reservation expiry: " + bike.getReservationExpiry());
        System.out.println("Station hold time: " + station.getReservationHoldTime());
        System.out.println("");

        String reserveResult = rider.reserveBike(bike, station);
        System.out.println("reserveBike result: " + reserveResult);
        System.out.println("Bike status after reservation: " + bike.getStatus());
        System.out.println("Bike reservation user: " + bike.getReservationUser());
        System.out.println("Bike reservation expiry: " + bike.getReservationExpiry());
        System.out.println("");
    }
}

