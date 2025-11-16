package com.concordia.velocity.observer;

import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.RiderStats;
import com.concordia.velocity.service.LoyaltyStatsService;
import com.concordia.velocity.service.UserService;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.FieldValue;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class ReservationObserver implements Observer {
    private final UserService userService;
    private final LoyaltyStatsService loyaltyStatsService;
    private final Firestore db = FirestoreClient.getFirestore();

    public ReservationObserver(UserService userService, LoyaltyStatsService loyaltyStatsService) {
        this.userService = userService;
        this.loyaltyStatsService = loyaltyStatsService;
    }

    @Override
    public void update(String message) {
        if (message.startsWith("RESERVATION_EXPIRED")) {
            String userId = extractUserId(message);
            if (userId != null) {
                try {
                    // atomic array union to add timestamp (thread-safe)
                    Map<String, Object> updates = new HashMap<>();
                    updates.put("missedReservationTimestamps", 
                               FieldValue.arrayUnion(Timestamp.now()));
                    
                    db.collection("riders").document(userId)
                      .update(updates).get();
                    
                    System.out.println("Added missed reservation timestamp for rider " + userId);
                    
                    // reload and evaluate
                    Rider rider = userService.getUserById(userId);
                    if (rider != null) {
                        RiderStats stats = loyaltyStatsService.computeStats(userId);
                        System.out.println("Computed stats: " + stats);
                        
                        rider.evaluateTier(stats);
                        
                        Map<String, Object> tierUpdates = new HashMap<>();
                        tierUpdates.put("tier", rider.getTier());
                        userService.updateUser(rider.getId(), tierUpdates);
                        
                        System.out.println("Evaluated rider " + userId + ". New tier: " + rider.getTier());
                    }
                } catch (ExecutionException | InterruptedException e) {
                    System.err.println("Error: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }
    }

    private String extractUserId(String message) {
        if (message.contains("userId=")) {
            return message.substring(message.indexOf("userId=") + 7).trim();
        }
        return null;
    }
}