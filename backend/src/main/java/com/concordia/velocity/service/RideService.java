package com.concordia.velocity.service;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.Dock;
import com.concordia.velocity.model.Station;
import org.springframework.stereotype.Service;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.InterruptedException;

@Service
public class RideService {
    private final BikeService bikeService;

    public RideService(BikeService bikeService) {
        this.bikeService = bikeService;
    }

    public boolean undock(String bikeId) throws ExecutionException, InterruptedException {
        Bike bike = this.bikeService.getBikeById(bikeId);
    }
}