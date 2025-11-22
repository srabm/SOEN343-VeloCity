package com.concordia.velocity.controller;

import com.concordia.velocity.service.AbandonedTripService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * REST Controller for managing abandoned trips
 * Provides endpoints for checking and processing abandoned trips
 */
@RestController
@RequestMapping("/api/abandoned-trips")
public class AbandonedTripController {

    private final AbandonedTripService abandonedTripService;

    public AbandonedTripController(AbandonedTripService abandonedTripService) {
        this.abandonedTripService = abandonedTripService;
    }

    /**
     * Manually trigger a check for abandoned trips
     * POST /api/abandoned-trips/check
     *
     * @return number of trips processed and details
     */
    @PostMapping("/check")
    public ResponseEntity<Map<String, Object>> manualCheck() {
        Map<String, Object> response = new HashMap<>();

        try {
            int processedCount = abandonedTripService.manualCheckForAbandonedTrips();

            response.put("success", true);
            response.put("message", "Abandoned trip check completed");
            response.put("tripsProcessed", processedCount);
            response.put("abandonmentThresholdHours", abandonedTripService.getAbandonmentThresholdHours());
            response.put("abandonmentFee", abandonedTripService.getAbandonmentFee());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to check for abandoned trips: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Check if a specific trip is abandoned
     * GET /api/abandoned-trips/check/{tripId}
     *
     * @param tripId the trip ID to check
     * @return whether the trip is abandoned
     */
    @GetMapping("/check/{tripId}")
    public ResponseEntity<Map<String, Object>> checkTrip(@PathVariable String tripId) {
        Map<String, Object> response = new HashMap<>();

        try {
            boolean isAbandoned = abandonedTripService.isTripAbandoned(tripId);

            response.put("success", true);
            response.put("tripId", tripId);
            response.put("isAbandoned", isAbandoned);
            response.put("thresholdHours", abandonedTripService.getAbandonmentThresholdHours());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to check trip: " + e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Get abandonment policy information
     * GET /api/abandoned-trips/policy
     *
     * @return abandonment policy details
     */
    @GetMapping("/policy")
    public ResponseEntity<Map<String, Object>> getPolicy() {
        Map<String, Object> response = new HashMap<>();

        response.put("thresholdHours", abandonedTripService.getAbandonmentThresholdHours());
        response.put("abandonmentFee", abandonedTripService.getAbandonmentFee());
        response.put("description", "Trips active for more than " +
                abandonedTripService.getAbandonmentThresholdHours() +
                " hours will be marked as abandoned and charged $" +
                abandonedTripService.getAbandonmentFee());

        return ResponseEntity.ok(response);
    }
}