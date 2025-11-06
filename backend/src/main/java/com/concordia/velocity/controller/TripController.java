package com.concordia.velocity.controller;

import com.concordia.velocity.model.Bill;
import com.concordia.velocity.model.Trip;
import com.concordia.velocity.service.TripService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:3000"})  // ← Update this
public class TripController {

    @Autowired
    private TripService tripService;

    /**
     * Start a trip by undocking a reserved bike
     * POST /api/trips/start/reserved
     * Body: { "bikeId": "...", "riderId": "...", "dockCode": "..." }
     */
    @PostMapping("/start/reserved")
    public ResponseEntity<Map<String, Object>> startTripFromReservation(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String bikeId = request.get("bikeId");
            String riderId = request.get("riderId");
            String dockCode = request.get("dockCode");

            String message = tripService.undockReservedBike(bikeId, riderId, dockCode);

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

    /**
     * Start a trip by undocking an available bike (no reservation)
     * POST /api/trips/start/direct
     * Body: { "dockId": "...", "dockCode": "...", "riderId": "..." }
     */
    @PostMapping("/start/direct")
    public ResponseEntity<Map<String, Object>> startTripDirect(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String dockId = request.get("dockId");
            String dockCode = request.get("dockCode");
            String riderId = request.get("riderId");

            String message = tripService.undockAvailableBike(dockId, dockCode, riderId);

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

    /**
     * End a trip by docking the bike
     * POST /api/trips/end
     * Body: { "bikeId": "...", "riderId": "...", "dockId": "...", "dockCode": "..." }
     */
    @PostMapping("/end")
    public ResponseEntity<Map<String, Object>> endTrip(@RequestBody Map<String, String> request) {
        Map<String, Object> response = new HashMap<>();

        try {
            String bikeId = request.get("bikeId");
            String riderId = request.get("riderId");
            String dockId = request.get("dockId");
            String dockCode = request.get("dockCode");

            String message = tripService.dockBikeAndEndTrip(bikeId, riderId, dockId, dockCode);

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

    /**
     * Get trip by ID
     * GET /api/trips/{tripId}
     */
    @GetMapping("/{tripId}")
    public ResponseEntity<Map<String, Object>> getTripById(@PathVariable String tripId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Trip trip = tripService.getTripById(tripId);

            response.put("success", true);
            response.put("trip", trip);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to retrieve trip: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    /**
     * Get bill by ID
     * GET /api/trips/bills/{billId}
     */
    @GetMapping("/bills/{billId}")
    public ResponseEntity<Map<String, Object>> getBillById(@PathVariable String billId) {
        Map<String, Object> response = new HashMap<>();

        try {
            Bill bill = tripService.getBillById(billId);

            response.put("success", true);
            response.put("bill", bill);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("success", false);
            response.put("error", e.getMessage());
            return ResponseEntity.notFound().build();

        } catch (Exception e) {
            response.put("success", false);
            response.put("error", "Failed to retrieve bill: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
}