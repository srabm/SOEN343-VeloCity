// This file handles HTTP requests. The controller handles the incoming requests, processes them by calling the service layer, which calls the repository.

package com.concordia.velocity.controller;
import com.concordia.velocity.model.Station;
import com.concordia.velocity.service.StationService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
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

    @PutMapping("/{stationId}/status") //PUT endpoint that changes a station's status
    public ResponseEntity<Map<String, String>> updateStationStatus(@PathVariable String stationId, @RequestParam String newStatus) throws ExecutionException, InterruptedException {
        String message = stationService.updateStationStatus(stationId, newStatus);
        Map<String, String> response = new HashMap<>();
        response.put("message", message);
        response.put("stationId", stationId);
        response.put("newStatus", newStatus);
        return ResponseEntity.ok(response);
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

