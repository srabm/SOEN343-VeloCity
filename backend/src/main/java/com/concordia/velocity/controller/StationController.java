// This file handles HTTP requests. The controller handles the incoming requests, processes them by calling the service layer, which calls the repository.

package com.concordia.velocity.controller;

import com.concordia.velocity.service.StationService;
import org.springframework.web.bind.annotation.*;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/stations")
@CrossOrigin(origins = "*") // allows frontend request when testing from Vue.js
public class StationController {
    private final StationService stationService;

    public StationController (StationService stationService) {
        this.stationService = stationService;
    }

    @PutMapping("/{stationId}/status") //PUT endpoint that changes a station's status
    public String updateStationStatus(@PathVariable String stationId, @RequestParam String newStatus) throws ExecutionException, InterruptedException {
        return stationService.updateStationStatus(stationId, newStatus);
    }

   
    @GetMapping("/{stationId}") // GET endoint to fetch station's data
    public Object getStationById(@PathVariable String stationId) throws ExecutionException, InterruptedException {
        return stationService.getStationById(stationId);
    }

    

}

