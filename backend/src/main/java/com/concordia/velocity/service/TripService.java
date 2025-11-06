package com.concordia.velocity.service;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.observer.DashboardObserver;
import com.concordia.velocity.observer.NotificationObserver;
import com.concordia.velocity.observer.Observer;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;

@Service
public class TripService {

    private final Firestore db = FirestoreClient.getFirestore();

    /**
     * Undocks a reserved bike and starts a trip
     * @param bikeId the bike to undock
     * @param riderId the rider undocking the bike
     * @return trip confirmation message
     */
    public String undockReservedBike(String bikeId, String riderId)
            throws ExecutionException, InterruptedException {

        // Retrieve bike from Firestore
        DocumentSnapshot doc = db.collection("bikes").document(bikeId).get().get();
        Bike bike = doc.toObject(Bike.class);

        if (bike == null) {
            throw new IllegalArgumentException("Bike not found: " + bikeId);
        }

        // Validate bike is reserved
        if (!Bike.STATUS_RESERVED.equalsIgnoreCase(bike.getStatus())) {
            throw new IllegalStateException("Bike is not reserved. Current status: " + bike.getStatus());
        }

        // Validate reservation is still active (not expired)
        if (!bike.isReservedActive()) {
            throw new IllegalStateException("Reservation has expired for bike: " + bikeId);
        }

        // Validate riderId matches the user who made the reservation
        if (!riderId.equals(bike.getReservedByUserId())) {
            throw new IllegalArgumentException("Rider " + riderId + " did not reserve this bike");
        }

        // Attach observers
        Observer dashboardObserver = new DashboardObserver();
        Observer notificationObserver = new NotificationObserver();
        bike.attach(dashboardObserver);
        bike.attach(notificationObserver);

        // Start trip - change status to ON_TRIP
        bike.changeStatus(Bike.STATUS_ON_TRIP);

        // Clear reservation data since trip has started
        bike.clearReservation();

        // Update bike location - bike is no longer at dock
        String previousDockId = bike.getDockId();
        bike.setDockId(null);

        // Persist to Firestore
        db.collection("bikes").document(bikeId).set(bike).get();

        // TODO: Create trip record in trips collection
        // createTripRecord(bikeId, riderId, previousDockId);

        return "Trip started successfully for bike " + bikeId + " by rider " + riderId;
    }

    /**
     * Undocks an available bike by entering dock code and starts a trip
     * @param dockId the dock where the bike is located
     * @param dockCode the code to unlock the dock
     * @param riderId the rider undocking the bike
     * @return trip confirmation message
     */
    public String undockAvailableBike(String dockId, String dockCode, String riderId)
            throws ExecutionException, InterruptedException {

        // TODO: Validate dock code
        // validateDockCode(dockId, dockCode);

        // Find bike at this dock
        var bikeQuery = db.collection("bikes")
                .whereEqualTo("dockId", dockId)
                .whereEqualTo("status", Bike.STATUS_AVAILABLE)
                .limit(1)
                .get()
                .get()
                .getDocuments();

        if (bikeQuery.isEmpty()) {
            throw new IllegalStateException("No available bike found at dock: " + dockId);
        }

        DocumentSnapshot doc = bikeQuery.get(0);
        Bike bike = doc.toObject(Bike.class);

        if (bike == null) {
            throw new IllegalStateException("Failed to retrieve bike from dock: " + dockId);
        }

        // Attach observers
        Observer dashboardObserver = new DashboardObserver();
        Observer notificationObserver = new NotificationObserver();
        bike.attach(dashboardObserver);
        bike.attach(notificationObserver);

        // Start trip - change status to ON_TRIP
        bike.changeStatus(Bike.STATUS_ON_TRIP);

        // Update bike location - bike is no longer at dock
        String previousDockId = bike.getDockId();
        bike.setDockId(null);

        // Persist to Firestore
        db.collection("bikes").document(bike.getBikeId()).set(bike).get();

        // TODO: Create trip record in trips collection
        // createTripRecord(bike.getBikeId(), riderId, previousDockId);

        return "Trip started successfully for bike " + bike.getBikeId() + " by rider " + riderId;
    }

    /**
     * Ends a trip and docks the bike at a station
     * @param bikeId the bike being docked
     * @param dockId the dock where bike is being returned
     * @param riderId the rider ending the trip
     * @return trip summary message
     */
    public String endTrip(String bikeId, String dockId, String riderId)
            throws ExecutionException, InterruptedException {

        // Retrieve bike from Firestore
        DocumentSnapshot doc = db.collection("bikes").document(bikeId).get().get();
        Bike bike = doc.toObject(Bike.class);

        if (bike == null) {
            throw new IllegalArgumentException("Bike not found: " + bikeId);
        }

        // Validate bike is on a trip
        if (!Bike.STATUS_ON_TRIP.equalsIgnoreCase(bike.getStatus())) {
            throw new IllegalStateException("Bike is not currently on a trip. Current status: " + bike.getStatus());
        }

        // TODO: Validate riderId matches the trip owner
        // TODO: Validate dock exists and is available

        // Attach observers
        Observer dashboardObserver = new DashboardObserver();
        Observer notificationObserver = new NotificationObserver();
        bike.attach(dashboardObserver);
        bike.attach(notificationObserver);

        // End trip - change status to AVAILABLE
        bike.changeStatus(Bike.STATUS_AVAILABLE);

        // Update bike location - bike is now at dock
        DocumentSnapshot dockDoc = db.collection("docks").document(dockId).get().get();
        if (!dockDoc.exists()) {
            throw new IllegalArgumentException("Dock not found: " + dockId);
        }

        bike.setDockId(dockId);

        // Set station ID from dock
        String stationId = (String) dockDoc.getData().get("stationId");
        bike.setStationId(stationId);

        // Persist to Firestore
        db.collection("bikes").document(bikeId).set(bike).get();

        // TODO: Update trip record - set end time, calculate duration/cost
        // endTripRecord(bikeId, riderId, dockId);

        return "Trip ended successfully. Bike " + bikeId + " docked at " + dockId;
    }

    // TODO: Implement helper methods
    // private void validateDockCode(String dockId, String dockCode) {}
    // private void createTripRecord(String bikeId, String riderId, String startDockId) {}
    // private void endTripRecord(String bikeId, String riderId, String endDockId) {}
}