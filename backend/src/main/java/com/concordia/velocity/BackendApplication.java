package com.concordia.velocity;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.Station;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.r2dbc.R2dbcAutoConfiguration;

import java.time.LocalDateTime;

@SpringBootApplication(
		
		exclude = {R2dbcAutoConfiguration.class}
)

public class BackendApplication {
	public static void main(String[] args) throws InterruptedException {
		System.out.println("Starting VeloCity Backend...");
		SpringApplication.run(BackendApplication.class, args);
		System.out.println("VeloCity Backend started successfully!");

		Bike bike = new Bike("B101", "available", "standard", "D101", "S001");
		Station station = new Station("S001", "TestStation", "occupied", "0", "0", "123 Test St", 10, 1, 1, null, null);
		Rider rider = new Rider("Test Rider", "123 Test St", "test@example.com", "1234567890", "testrider");

		System.out.println("Initial bike status: " + bike.getStatus());
		System.out.println("Starting reservation for bike: " + bike.getBikeId());

		rider.reserveBike(bike, station);
		LocalDateTime expiry = bike.getReservationExpiry();

		System.out.println("Bike status after reservation: " + bike.getStatus());
		System.out.println("Sleeping thread until expiry...");


	}
}