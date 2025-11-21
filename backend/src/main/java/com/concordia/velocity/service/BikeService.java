package com.concordia.velocity.service;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.Station;
import com.concordia.velocity.observer.DashboardObserver;
import com.concordia.velocity.observer.Observer;
import com.concordia.velocity.observer.ReservationObserver;
import com.concordia.velocity.observer.StatusObserver;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

@Service
public class BikeService {

    private final Firestore db = FirestoreClient.getFirestore();
    private final UserService userService;
    private final LoyaltyStatsService loyaltyStatsService;

    // Constructor injection for dependencies
    public BikeService(UserService userService, LoyaltyStatsService loyaltyStatsService) {
        this.userService = userService;
        this.loyaltyStatsService = loyaltyStatsService;
    }

    /**
     * Helper method to attach all necessary observers to a bike
     */
    private void attachObservers(Bike bike) {
        if (bike != null) {
            bike.attach(new StatusObserver());
            bike.attach(new DashboardObserver());
            bike.attach(new ReservationObserver(userService, loyaltyStatsService));
        }
    }

    /**
     * Creates a reservation for a bike
     * @param bikeId the bike to reserve
     * @param userId the user making the reservation
     * @param stationId the station where bike is located
     * @return confirmation message with expiry time
     */
    public String reserveBike(String bikeId, String userId, String stationId)
            throws ExecutionException, InterruptedException {

        // Retrieve bike from Firestore
        DocumentSnapshot doc = db.collection("bikes").document(bikeId).get().get();
        Bike bike = doc.toObject(Bike.class);

        if (bike == null) {
            throw new IllegalArgumentException("Bike not found: " + bikeId);
        }

        // Validate bike is available
        if (!Bike.STATUS_AVAILABLE.equalsIgnoreCase(bike.getStatus())) {
            throw new IllegalStateException("Bike is not available for reservation. Current status: " + bike.getStatus());
        }

        // Get station to determine hold time
        DocumentSnapshot stationDoc = db.collection("stations").document(stationId).get().get();
        com.concordia.velocity.model.Station station = stationDoc.toObject(com.concordia.velocity.model.Station.class);

        if (station == null) {
            throw new IllegalArgumentException("Station not found: " + stationId);
        }

        // Attach all observers
        attachObservers(bike);

        // Start reservation (sets status to RESERVED and schedules auto-expiry)
        Rider rider = userService.getUserById(userId);
        LocalDateTime expiryTime = bike.startReservationExpiry(station, rider);

        // Persist to Firestore
        db.collection("bikes").document(bikeId).set(bike).get();

        return "Bike " + bikeId + " reserved successfully for user " + rider.getFullName() +
                ". Reservation expires at " + expiryTime;
    }

    /**
     * Updates bike status with validation and business rules
     */
    public String updateBikeStatus(String bikeId, String newStatus)
            throws ExecutionException, InterruptedException {

        // Retrieve bike from Firestore
        DocumentSnapshot doc = db.collection("bikes").document(bikeId).get().get();
        Bike bike = doc.toObject(Bike.class);

        if (bike == null) {
            throw new IllegalArgumentException("Bike not found: " + bikeId);
        }

        // Attach all observers before making changes
        attachObservers(bike);

        // Perform status change (this will validate and notify observers)
        bike.changeStatus(newStatus);

        // Persist to Firestore
        db.collection("bikes").document(bikeId).set(bike).get();

        return "Bike " + bikeId + " updated successfully to status: " + bike.getStatus();
    }

    public Bike getBikeById(String bikeId) throws ExecutionException, InterruptedException {
        Bike bike = db.collection("bikes").document(bikeId)
                .get().get().toObject(Bike.class);
        
        // Attach observers when retrieving bikes
        attachObservers(bike);
        
        return bike;
    }

    public List<Bike> getAllBikes() throws ExecutionException, InterruptedException {
        List<Bike> bikes = new ArrayList<>();
        for (DocumentSnapshot doc : db.collection("bikes").get().get().getDocuments()) {
            Bike bike = doc.toObject(Bike.class);
            if (bike != null) {
                // Attach observers to each bike
                attachObservers(bike);
                bikes.add(bike);
            }
        }
        return bikes;
    }
}