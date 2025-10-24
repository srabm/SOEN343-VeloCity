package com.concordia.velocity.controller;

import com.concordia.velocity.service.TransferService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/transfer")
@CrossOrigin(origins = "*")
public class TransferController {

    private final TransferService transferService;

    public TransferController(TransferService transferService) {
        this.transferService = transferService;
    }

    @PutMapping("/bike")
    public ResponseEntity<Map<String, String>> moveBike(
            @RequestParam String bikeId,
            @RequestParam String sourceStationId,
            @RequestParam String destinationStationId,
            @RequestParam String destinationDockId)
            throws ExecutionException, InterruptedException {

        Map<String, String> response = new HashMap<>();

        try {
            String message = transferService.moveBike(bikeId, sourceStationId, destinationStationId, destinationDockId);
            response.put("message", message);
            response.put("bikeId", bikeId);
            response.put("sourceStation", sourceStationId);
            response.put("destinationStation", destinationStationId);
            response.put("destinationDock", destinationDockId);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(response);

        } catch (IllegalStateException e) {
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(response);

        } catch (Exception e) {
            response.put("error", "Unexpected error: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
