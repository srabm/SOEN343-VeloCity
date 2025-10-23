// This file handles HTTP requests. The controller handles the incoming requests, processes them by calling the service layer, which calls the repository.

package com.concordia.velocity.controller;
import com.concordia.velocity.model.Station;
import com.concordia.velocity.service.StationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.concurrent.ExecutionException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stations")
@CrossOrigin(origins = "*") // allows frontend request when testing from Vue.js
public class StationController {
    private final StationService stationService;

    public StationController (StationService stationService) {
        this.stationService = stationService;
    }

    @PutMapping("/{stationId}/status")
    public ResponseEntity<Map<String, String>> updateStationStatus(
            @PathVariable String stationId,
            @RequestParam String newStatus) {

        Map<String, String> response = new HashMap<>();
        String message;

        try {
            
            message = stationService.updateStationStatus(stationId, newStatus);

            response.put("stationId", stationId);
            response.put("newStatus", newStatus);
            response.put("message", message);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);

        } catch (IllegalStateException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            response.put("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }



   
    @GetMapping("/{stationId}") // GET endpoint to fetch station's data
    public Object getStationById(@PathVariable String stationId) throws ExecutionException, InterruptedException {
        return stationService.getStationById(stationId);
    }

    @GetMapping // GET endpoint to fetch all stations' data
    public List<Station> getAllStations() throws ExecutionException, InterruptedException {
    return stationService.getAllStations();
}



}

