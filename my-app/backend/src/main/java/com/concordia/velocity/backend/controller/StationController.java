// This file handles HTTP requests. The controller handles the incoming requests, processes them by calling the service layer, which calls the repository.

package com.concordia.velocity.backend.controller;

import com.concordia.velocity.backend.model.Station;
import com.concordia.velocity.backend.service.StationService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/stations")
public class StationController {

    private final StationService stationService;

    public StationController(StationService stationService) {
        this.stationService = stationService;
    }

    @PostMapping
    public String createStation(@RequestBody Station station) throws ExecutionException, InterruptedException {
        return stationService.saveStation(station);
    }

    @GetMapping("/{stationId}")
    public Station getStation(@PathVariable String stationId) throws ExecutionException, InterruptedException {
        return stationService.getStationById(stationId);
    }

    @GetMapping
    public List<Station> getAllStations() throws ExecutionException, InterruptedException {
        return stationService.getAllStations();
    }

    @DeleteMapping("/{stationId}")
    public String deleteStation(@PathVariable String stationId) throws ExecutionException, InterruptedException {
        return stationService.deleteStation(stationId);
    }

    @PutMapping("/{stationId}/status")
    public String updateStationStatus(
            @PathVariable String stationId,
            @RequestParam String status
    ) throws ExecutionException, InterruptedException {
        return stationService.updateStationStatus(stationId, status);
    }
}
