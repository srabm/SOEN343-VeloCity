package com.concordia.velocity.controller;

import com.concordia.velocity.service.RideService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.InterruptedException;

@RestController
@RequestMapping("/api/ride")
public class RideController {
    private final RideService rideService;

    public RideController(RideService rideService) {
        this.rideService = rideService;
    }

    /**
     * UNDOCKING
     * 1. find an ACTIVE station with OCCUPIED dock and AVAILABLE or RESERVED bike
     * 2. Select bike (or enter code if bike was RESERVED)
     * ^ above steps happen outside of controller ^ verifications for availability can still be ran
     * 3. Send context to dashboard (user currently on trip)
     * 4. Bike, Dock and Station status or capacity must change
     */

    @GetMapping("/{bikeId}")
    public boolean getDock(@PathVariable String bikeId) throws ExecutionException, InterruptedException {
        return false;
    }
}