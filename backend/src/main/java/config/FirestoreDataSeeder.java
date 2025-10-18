package com.concordia.velocity.backend.config;

import com.concordia.velocity.backend.model.Bike;
import com.concordia.velocity.backend.model.Dock;
import com.concordia.velocity.backend.model.Station;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Configuration
public class FirestoreDataSeeder {

    @Bean
    CommandLineRunner seedFirestoreData() {
        return args -> {
            Firestore db = FirestoreClient.getFirestore();

            try {
                // Seed sample stations
                Station station1 = new Station(
                        "S001",
                        "Downtown Ste-Catherine Station",
                        "occupied",
                        "45.5017",
                        "-73.5673",
                        "300 Ste-Catherine St, Montreal",
                        3,
                        2,
                        10,
                        Arrays.asList("D001", "D002", "D003"),
                        Arrays.asList("B001", "B002")
                );

                Station station2 = new Station(
                        "S002",
                        "Old Port Station",
                        "empty",
                        "45.5070",
                        "-73.5540",
                        "456 St-Paul St, Montreal",
                        2,
                        0,
                        5,
                        Arrays.asList("D004", "D005"),
                        Arrays.asList()
                );

                db.collection("stations").document(station1.getStationId()).set(station1).get();
                db.collection("stations").document(station2.getStationId()).set(station2).get();

                System.out.println("✅ Sample stations added to Firestore.");

                // Seed sample docks
                Dock dock1 = new Dock("D001", "occupied", "B001", "S001");
                Dock dock2 = new Dock("D002", "occupied", "B002", "S001");
                Dock dock3 = new Dock("D003", "empty", null, "S001");
                Dock dock4 = new Dock("D004", "empty", null, "S002");
                Dock dock5 = new Dock("D005", "out_of_service", null, "S002");

                List<Dock> docks = Arrays.asList(dock1, dock2, dock3, dock4, dock5);
                for (Dock d : docks) {
                    db.collection("docks").document(d.getDockId()).set(d).get();
                }

                System.out.println("✅ Sample docks added to Firestore.");

                // Seed sample bikes
                Bike bike1 = new Bike("B001", "available", "standard", "D001", "S001");
                Bike bike2 = new Bike("B002", "maintenance", "e-bike", "D002", "S001");
                Bike bike3 = new Bike("B003", "available", "standard", null, "S002");

                List<Bike> bikes = Arrays.asList(bike1, bike2, bike3);
                for (Bike b : bikes) {
                    db.collection("bikes").document(b.getBikeId()).set(b).get();
                }

                System.out.println("Sample bikes added to Firestore.");

            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        };
    }
}
