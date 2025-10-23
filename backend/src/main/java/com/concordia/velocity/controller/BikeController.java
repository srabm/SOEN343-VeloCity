package com.concordia.velocity.controller;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.service.BikeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.ExecutionException;

@RestController
@RequestMapping("/api/bikes")
public class BikeController {

    private final BikeService bikeService;

    public BikeController(BikeService bikeService) {
        this.bikeService = bikeService;
    }

    @PostMapping
    public String createBike(@RequestBody Bike bike) throws ExecutionException, InterruptedException {
        return bikeService.saveBike(bike);
    }

    @GetMapping("/{bikeId}")
    public Bike getBike(@PathVariable String bikeId) throws ExecutionException, InterruptedException {
        return bikeService.getBikeById(bikeId);
    }

    @GetMapping
    public List<Bike> getAllBikes() throws ExecutionException, InterruptedException {
        return bikeService.getAllBikes();
    }

    @DeleteMapping("/{bikeId}")
    public String deleteBike(@PathVariable String bikeId) throws ExecutionException, InterruptedException {
        return bikeService.deleteBike(bikeId);
    }

    @PutMapping("/{bikeId}/status")
    public String updateBikeStatus(
            @PathVariable String bikeId,
            @RequestParam String status
    ) throws ExecutionException, InterruptedException {
        return bikeService.updateBikeStatus(bikeId, status);
    }
}
