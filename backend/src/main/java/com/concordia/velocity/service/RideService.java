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
    private final DockService dockService;
    private final StationService stationService;

    public RideService(BikeService bikeService, DockService dockService, StationService stationService) {
        this.bikeService = bikeService;
        this.dockService = dockService;
        this.stationService = stationService;
    }

    public boolean undock(String bikeId) throws ExecutionException, InterruptedException {
        Bike bike = this.bikeService.getBikeById(bikeId);
        String dockId = bike.getDockId();
        Dock dock = this.dockService.getDockById(dockId);
        String stationId = dock.getStationId();
        Station station = this.stationService.getStationById(stationId);

        if (!dock.getState().equals("occupied") || !bike.getState().equals("available") || station.getState().equals("out_of_service")) {
            return false
        }

        this.stationService.decreaseBikeCount(stationId);
        this.dockService.updateDockStatus(dockId, "empty")
        // change bike status here

        // TO-DO: Add ride history through database for future logging

        return true;
    }
}