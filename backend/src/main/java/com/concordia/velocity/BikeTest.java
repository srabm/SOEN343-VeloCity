package com.concordia.velocity;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.Station;

import java.time.LocalDateTime;

public class BikeTest {
    public static void main(String[] args) throws Exception {
        Bike bike = new Bike("B101", "available", "standard", "D101", "S001");
        Station station = new Station("S001", "TestStation", "occupied", "0", "0", "123 Test St", 10, 1, 1, null, null);
        Rider rider = new Rider("Test Rider", "123 Test St", "test@example.com", "1234567890", "testrider");

        System.out.println("Initial bike status: " + bike.getStatus());
        System.out.println("Starting reservation for bike: " + bike.getBikeId());

        rider.reserveBike(bike, station);
        LocalDateTime expiry = bike.getReservationExpiry();

        System.out.println("Bike status after reservation: " + bike.getStatus());
        System.out.println("Sleeping thread until expiry...");

        while (LocalDateTime.now().isBefore(expiry)) {
            Thread.sleep(1000);
        }

        System.out.println("Bike status after expiry: " + bike.getStatus());

    }
}
