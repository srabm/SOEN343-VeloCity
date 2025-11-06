package com.concordia.velocity.config;

import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.cloud.Timestamp;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Configuration
public class RideDataSeeder {

    @Bean
    CommandLineRunner seedRideData() {
        return args -> {
            Firestore db = FirestoreClient.getFirestore();

            try {
                // Fetch all existing riders
                ApiFuture<QuerySnapshot> future = db.collection("riders").get();
                List<QueryDocumentSnapshot> riderDocs = future.get().getDocuments();

                if (riderDocs.isEmpty()) {
                    System.out.println("No riders found in Firestore. Seed aborted.");
                    return;
                }

                for (QueryDocumentSnapshot riderDoc : riderDocs) {
                    String riderId = riderDoc.getId();
                    System.out.println("Seeding rides for rider: " + riderId);

                    CollectionReference ridesCol = db.collection("riders")
                            .document(riderId)
                            .collection("rides");

                    // Create a few sample rides per rider
                    for (int i = 1; i <= 3; i++) {
                        String rideId = String.format("RIDE%04d", i); // RIDE0001, RIDE0002, etc.
                        String bikeId = String.format("B%03d", new Random().nextInt(50) + 1);

                        // --- BILL OBJECT ---
                        Map<String, Object> bill = new HashMap<>();
                        double price = 3.0 + (Math.random() * 2); // 3–5$
                        double tax = Math.round(price * 0.15 * 100.0) / 100.0;
                        double total = Math.round((price + tax) * 100.0) / 100.0;

                        bill.put("price", price);
                        bill.put("tax", tax);
                        bill.put("total", total);

                        // Alternate payment statuses (every other ride paid)
                        if (i % 2 == 0) {
                            bill.put("paymentStatus", "PAID");
                            bill.put("transactionId", "TXN-" + System.currentTimeMillis());
                        } else {
                            bill.put("paymentStatus", "NOT PAID");
                        }

                        // --- RIDE OBJECT ---
                        Map<String, Object> ride = new HashMap<>();
                        ride.put("rideId", rideId);
                        ride.put("riderName", riderDoc.getString("firstName") + " " + riderDoc.getString("lastName"));
                        ride.put("startTime", Timestamp.ofTimeSecondsAndNanos(Instant.now().getEpochSecond() - (i * 3600), 0));
                        ride.put("endTime", Timestamp.ofTimeSecondsAndNanos(Instant.now().getEpochSecond() - (i * 3500), 0));
                        ride.put("originStationName", "Station " + (char) ('A' + new Random().nextInt(5)));
                        ride.put("arrivalStationName", "Station " + (char) ('F' + new Random().nextInt(5)));
                        ride.put("bikeId", bikeId);
                        ride.put("bikeType", i % 2 == 0 ? "E-BIKE" : "BIKE");
                        ride.put("bill", bill);

                        // Save ride under each rider
                        ridesCol.document(rideId).set(ride, SetOptions.merge()).get();
                    }
                }

                System.out.println("Ride data seeded successfully for all riders!");

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        };
    }
}
