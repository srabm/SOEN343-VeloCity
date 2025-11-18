package com.concordia.velocity.controller;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.Dock;
import com.concordia.velocity.model.Station;
import com.concordia.velocity.service.TransferService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * REST Controller for bike transfer operations
 */
@RestController
@RequestMapping("/api/transfer")
@CrossOrigin(origins = "*")
public class TransferController {

    @Autowired
    private TransferService transferService;

    @GetMapping("/stations")
    public ResponseEntity<?> getAllStations() {
        try {
            List<Station> stations = transferService.getAllStations();
            return ResponseEntity.ok(stations);
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching stations: " + e.getMessage()));
        }
    }

    @GetMapping("/stations/{stationId}/bikes")
    public ResponseEntity<?> getAvailableBikesAtStation(@PathVariable String stationId) {
        try {
            List<Bike> bikes = transferService.getAvailableBikesAtStation(stationId);
            return ResponseEntity.ok(bikes);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching bikes: " + e.getMessage()));
        }
    }

    @GetMapping("/stations/{stationId}/docks")
    public ResponseEntity<?> getAvailableDocksAtStation(@PathVariable String stationId) {
        try {
            List<Dock> docks = transferService.getAvailableDocksAtStation(stationId);
            return ResponseEntity.ok(docks);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(createErrorResponse(e.getMessage()));
        } catch (ExecutionException | InterruptedException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error fetching docks: " + e.getMessage()));
        }
    }

    @PostMapping("/bike")
    public ResponseEntity<?> transferBike(@RequestBody Map<String, String> request) {
        try {
            // Extract and validate fields
            String bikeId = request.get("bikeId");
            String sourceDockId = request.get("sourceDockId");
            String destinationDockId = request.get("destinationDockId");
            String sourceStationId = request.get("sourceStationId");
            String destinationStationId = request.get("destinationStationId");

            if (bikeId == null || bikeId.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Bike ID is required"));
            }
            if (sourceDockId == null || sourceDockId.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Source dock ID is required"));
            }
            if (destinationDockId == null || destinationDockId.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Destination dock ID is required"));
            }
            if (sourceStationId == null || sourceStationId.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Source station ID is required"));
            }
            if (destinationStationId == null || destinationStationId.isEmpty()) {
                return ResponseEntity.badRequest().body(createErrorResponse("Destination station ID is required"));
            }

            // Perform transfer
            Map<String, Object> response = transferService.transferBike(
                    bikeId, sourceDockId, destinationDockId, sourceStationId, destinationStationId
            );

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(createErrorResponse(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(createErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(createErrorResponse("Error: " + e.getMessage()));
        }
    }

    private Map<String, Object> createErrorResponse(String message) {
        Map<String, Object> error = new HashMap<>();
        error.put("success", false);
        error.put("message", message);
        error.put("timestamp", java.time.Instant.now().toString());
        return error;
    }
}