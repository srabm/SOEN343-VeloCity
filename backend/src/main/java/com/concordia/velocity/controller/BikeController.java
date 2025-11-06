package com.concordia.velocity.controller;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.service.BikeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/bikes")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})  // ← Update this
public class BikeController {

    @Autowired
    private BikeService bikeService;

    /**
     * Reserve a bike at a station
     * POST /api/bikes/reserve
     * Body: { "bikeId": "...", "userId": "...", "stationId": "..." }
     */
    @PostMapping("/reserve")
    public ResponseEntity<Map<String, Object>> reserveBike(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String bikeId = request.get("bikeId");
            String userId = request.get("userId");
            String stationId = request.get("stationId");

            String message = bikeService.reserveBike(bikeId, userId, stationId);

            // Get updated bike details
            Bike bike = bikeService.getBikeById(bikeId);

            response.put("success", true);
            response.put("message", message);
            response.put("bike", bike);
            response.put("reservationExpiry", bike.getReservationExpiry());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get available bikes at a station
     * GET /api/bikes/available?stationId=...
     */
    @GetMapping("/available")
    public ResponseEntity<Map<String, Object>> getAvailableBikes(@RequestParam String stationId) {
        Map<String, Object> response = new HashMap<>();

        try {
            List<Bike> allBikes = bikeService.getAllBikes();

            // Filter bikes that are available at this station
            List<Bike> availableBikes = allBikes.stream()
                    .filter(bike -> stationId.equals(bike.getStationId()))
                    .filter(bike -> Bike.STATUS_AVAILABLE.equalsIgnoreCase(bike.getStatus()))
                    .toList();

            response.put("success", true);
            response.put("bikes", availableBikes);
            response.put("count", availableBikes.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to retrieve bikes: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get bike by ID
     * GET /api/bikes/{bikeId}
     */
    @GetMapping("/{bikeId}")
    public ResponseEntity<Map<String, Object>> getBikeById(@PathVariable String bikeId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Bike bike = bikeService.getBikeById(bikeId);

            if (bike == null) {
                response.put("success", false);
                response.put("error", "Bike not found");
                return ResponseEntity.notFound().build();
            }

            response.put("success", true);
            response.put("bike", bike);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to retrieve bike: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Update bike status
     * PATCH /api/bikes/{bikeId}/status
     * Body: { "status": "AVAILABLE|RESERVED|MAINTENANCE|ON_TRIP" }
     */
    @PatchMapping("/{bikeId}/status")
    public ResponseEntity<Map<String, Object>> updateBikeStatus(
            @PathVariable String bikeId,
            @RequestBody Map<String, String> request) {

        Map<String, Object> response = new HashMap<>();

        try {
            String newStatus = request.get("status");
            String message = bikeService.updateBikeStatus(bikeId, newStatus);

            response.put("success", true);
            response.put("message", message);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IllegalStateException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "An unexpected error occurred: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}