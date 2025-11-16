package com.concordia.velocity.observer;

import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.RiderStats;
import com.concordia.velocity.service.LoyaltyStatsService;
import com.concordia.velocity.service.UserService;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.checkerframework.checker.units.qual.t;

public class ReservationObserver implements Observer {
    private final UserService userService;
    private final LoyaltyStatsService loyaltyStatsService;

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
                    Rider rider = userService.getUserById(userId);
                    if (rider != null) {
                        // Increment counter
                        rider.incrementMissedReservations();

                        // Create update map with CORRECT field name
                        Map<String, Object> updates = new HashMap<>();
                        updates.put("missedReservationsCount", rider.getMissedReservationsCount());

                        // call update to save missed reservations count
                        userService.updateUser(rider.getId(), updates);
                        System.out.println("Saved missed reservations count: " + rider.getMissedReservationsCount());

                        // compute stats
                        RiderStats stats = loyaltyStatsService.computeStats(userId);
                        System.out.println("Computed stats: " + stats);

                        // evaluate tier
                        rider.evaluateTier(stats);

                        // save tier changes
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
        // Parse "RESERVATION_EXPIRED userId=123"
        if (message.contains("userId=")) {
            return message.substring(message.indexOf("userId=") + 7).trim();
        }
        return null;
    }
}