package com.concordia.velocity.controller;

import com.concordia.velocity.model.Dock;
import com.concordia.velocity.service.DockService;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;
import java.util.concurrent.ExecutionException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/docks")
@CrossOrigin(origins = "*")
public class DockController {

    private final DockService dockService;

    public DockController(DockService dockService) {
        this.dockService = dockService;
    }

    @PutMapping("/{dockId}/status")
    public ResponseEntity<Map<String, String>> updateDockStatus(
            @PathVariable String dockId,
            @RequestParam String newStatus) throws ExecutionException, InterruptedException {

        Map<String, String> response = new HashMap<>();

        try {
            String message = dockService.updateDockStatus(dockId, newStatus);
            response.put("message", message);
            response.put("dockId", dockId);
            response.put("newStatus", newStatus);
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

    @GetMapping("/{dockId}")
    public Dock getDockById(@PathVariable String dockId)
            throws ExecutionException, InterruptedException {
        return dockService.getDockById(dockId);
    }

    @GetMapping
    public List<Dock> getAllDocks()
            throws ExecutionException, InterruptedException {
        return dockService.getAllDocks();
    }
}
