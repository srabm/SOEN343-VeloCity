// This file handles HTTP requests. The controller handles the incoming requests, processes them by calling the service layer, which calls the repository.

package com.concordia.velocity.backend.controller;

import com.concordia.velocity.backend.model.Station;
import com.concordia.velocity.backend.service.StationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    // Get all stations
    @GetMapping
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }

    // Update a station's status (PUT /api/stations/{id}/status?status=out_of_service)
    @PutMapping("/{id}/status")
    public ResponseEntity<Station> updateStationStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        Station updated = stationService.updateStationStatus(id, status);
        return ResponseEntity.ok(updated);
    }
}
