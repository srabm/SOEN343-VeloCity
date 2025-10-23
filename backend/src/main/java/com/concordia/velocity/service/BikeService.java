package com.concordia.velocity.service;

import com.concordia.velocity.model.Bike;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ExecutionException;

@Service
public class BikeService {

    private static final String COLLECTION_NAME = "bikes";

    private Firestore getDb() {
        return FirestoreClient.getFirestore();
    }

    // Add or update bike
    public String saveBike(Bike bike) throws ExecutionException, InterruptedException {
        getDb().collection(COLLECTION_NAME)
                .document(bike.getBikeId())
                .set(bike)
                .get();
        return "Bike " + bike.getBikeId() + " saved successfully.";
    }

    // Get bike by ID
    public Bike getBikeById(String bikeId) throws ExecutionException, InterruptedException {
        DocumentSnapshot snapshot = getDb()
                .collection(COLLECTION_NAME)
                .document(bikeId)
                .get()
                .get();
        return snapshot.exists() ? snapshot.toObject(Bike.class) : null;
    }

    // Get all bikes
    public List<Bike> getAllBikes() throws ExecutionException, InterruptedException {
        List<QueryDocumentSnapshot> docs = getDb()
                .collection(COLLECTION_NAME)
                .get()
                .get()
                .getDocuments();
        List<Bike> bikes = new ArrayList<>();
        for (DocumentSnapshot doc : docs) {
            bikes.add(doc.toObject(Bike.class));
        }
        return bikes;
    }

    // Delete bike
    public String deleteBike(String bikeId) throws ExecutionException, InterruptedException {
        getDb().collection(COLLECTION_NAME).document(bikeId).delete().get();
        return "Bike " + bikeId + " deleted successfully.";
    }

    // Update bike status (Operator use case)
    public String updateBikeStatus(String bikeId, String newStatus)
            throws ExecutionException, InterruptedException {

        DocumentReference docRef = getDb().collection(COLLECTION_NAME).document(bikeId);
        DocumentSnapshot snapshot = docRef.get().get();

        if (!snapshot.exists()) {
            throw new RuntimeException("Bike not found");
        }

        Bike bike = snapshot.toObject(Bike.class);

        // Prevent updates while bike is on trip
        if ("on_trip".equalsIgnoreCase(bike.getStatus())) {
            return "Cannot change status — bike is currently on a trip.";
        }

        // If reserved, terminate reservation first
        if ("reserved".equalsIgnoreCase(bike.getStatus())) {
            bike.setStatus("available");
            bike.setReservationExpiry(null);
            System.out.println("Reservation terminated for bike " + bikeId);
        }

        bike.setStatus(newStatus.toLowerCase());
        bike.setReservationExpiry(null);
        docRef.set(bike).get();

        System.out.println("Bike " + bikeId + " status changed to " + newStatus.toUpperCase() +
                " at " + LocalDateTime.now());

        return "Bike " + bikeId + " updated to " + newStatus + " successfully.";
    }
}
