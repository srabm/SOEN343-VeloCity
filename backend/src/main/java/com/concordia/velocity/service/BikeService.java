package com.concordia.velocity.service;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.BikeStatus;
import com.concordia.velocity.observer.DashboardObserver;
import com.concordia.velocity.observer.NotificationObserver;
import com.concordia.velocity.observer.Observer;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class BikeService {

    private final Firestore db = FirestoreClient.getFirestore();

    public String updateBikeStatus(String bikeId, String newStatus)
            throws ExecutionException, InterruptedException {

        // Retrieve bike from Firestore
        DocumentSnapshot doc = db.collection("bikes").document(bikeId).get().get();
        Bike bike = doc.toObject(Bike.class);

        if (bike == null) {
            throw new IllegalArgumentException("Bike not found: " + bikeId);
        }

        BikeStatus currentStatus = BikeStatus.valueOf(bike.getStatus().toUpperCase());
        BikeStatus targetStatus;

        try {
            targetStatus = BikeStatus.valueOf(newStatus.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid status: " + newStatus +
                    ". Valid options are: AVAILABLE, RESERVED, MAINTENANCE, ON_TRIP");
        }

        // // Business rule: cannot modify if bike is ON_TRIP
        // if (currentStatus == BikeStatus.ON_TRIP) {
        //     throw new IllegalStateException(
        //             "Cannot change status for a bike currently on a trip (Bike ID: " + bikeId + ")");
        // }

        // If bike is reserved, terminate reservation before proceeding
        if (currentStatus == BikeStatus.RESERVED) {
            terminateReservation(bikeId);
            bike.setStatus(BikeStatus.AVAILABLE.name());
            System.out.println("Reservation terminated for bike " + bikeId);
        }

        // Perform update
        bike.setStatus(targetStatus.name());
        db.collection("bikes").document(bikeId).set(bike);

        // Attach observers
        Observer dashboardObserver = new DashboardObserver();
        Observer notificationObserver = new NotificationObserver();

        bike.attach(dashboardObserver);
        bike.attach(notificationObserver);

        // Emit event
        bike.notifyObservers();

        return "Bike " + bikeId + " updated successfully to status: " + bike.getStatus();
    }

    public Bike getBikeById(String bikeId) throws ExecutionException, InterruptedException {
        return db.collection("bikes").document(bikeId)
                .get().get().toObject(Bike.class);
    }

    public List<Bike> getAllBikes() throws ExecutionException, InterruptedException {
        List<Bike> bikes = new ArrayList<>();
        for (DocumentSnapshot doc : db.collection("bikes").get().get().getDocuments()) {
            bikes.add(doc.toObject(Bike.class));
        }
        return bikes;
    }

    private void terminateReservation(String bikeId) throws ExecutionException, InterruptedException {
        // Example: find and delete any reservation document tied to this bike
        var reservations = db.collection("reservations")
                .whereEqualTo("bikeId", bikeId)
                .get()
                .get()
                .getDocuments();

        for (DocumentSnapshot res : reservations) {
            db.collection("reservations").document(res.getId()).delete();
        }

        System.out.println("Reservation entries for bike " + bikeId + " cleaned up.");
    }
}
