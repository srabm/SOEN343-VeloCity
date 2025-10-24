package com.concordia.velocity.controller;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.service.BikeService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/bikes")
@CrossOrigin(origins = "*")
public class BikeController {

    private final BikeService bikeService;

    public BikeController(BikeService bikeService) {
        this.bikeService = bikeService;
    }

    @PutMapping("/{bikeId}/status") // PUT endpoint to update bike status
    public ResponseEntity<Map<String, String>> updateBikeStatus(
            @PathVariable String bikeId,
            @RequestParam String newStatus) {

        Map<String, String> response = new HashMap<>();
        String message;

        try {
            message = bikeService.updateBikeStatus(bikeId, newStatus);
            response.put("bikeId", bikeId);
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

    @GetMapping("/{bikeId}") // GET endpoint to fetch bike's data
    public Bike getBike(@PathVariable String bikeId) throws ExecutionException, InterruptedException {
        return bikeService.getBikeById(bikeId);
    }
    
    @GetMapping // GET endpoint to fetch all bikes
    public List<Bike> getAllBikes() throws ExecutionException, InterruptedException {
        return bikeService.getAllBikes();
    }

   
    
}
