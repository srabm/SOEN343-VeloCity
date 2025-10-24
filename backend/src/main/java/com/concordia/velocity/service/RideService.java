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

    public String undock(String bikeId) throws ExecutionException, InterruptedException {
        Bike bike = this.bikeService.getBikeById(bikeId);
        String dockId = bike.getDockId();
        Dock dock = this.dockService.getDockById(dockId);
        String stationId = dock.getStationId();
        Station station = this.stationService.getStationById(stationId);

        if (!dock.getState().equals("occupied")) {
            return "Dock empty; undocking failed."
        } else if (!bike.getState().equals("available")) {
            return "Bike unavailable; undocking failed."
        } else if (station.getState().equals("out_of_service")) {
            return "Station out of service; undocking failed."
        }

        this.dockService.updateDockStatus(dockId, "empty")
        this.bikeService.updateBikeStatus(bikeId, "on_trip")
        this.stationService.update(stationId, -1);

        // TO-DO: Add ride history through database for future logging
        // TO-DO: Send context/notification (user - on - ride)

        return "Bike " + bikeId + " succesfully undocked. Starting ride...";
    }

    public String return(String bikeId, String dockId) throws ExecutionException, InterruptedException {
        Bike bike = this.bikeService.getBikeById(bikeId);
        Dock dock = this.dockService.getDockById(dockId);
        String stationId = dock.getStationId();
        Station station = this.stationService.getStationById(stationId);

        if (!dock.getState().equals("empty")) {
            return "Dock unavailable; docking failed."
        } else if (!bike.getState().equals("on_trip")) {
            return "Bike not on trip; docking failed."
        } else if (station.getState().equals("out_of_service")) {
            return "Station out of service; undocking failed."
        }

        this.dockService.updateDockStatus(dockId, "occupied")
        this.bikeService.updateBikeStatus(bikeId, "available")
        this.stationService.update(stationId, 1);

        // TO-DO: Send context/notification (user - not - on - ride)

        return "Bike " + bikeId + " succesfully docked. Ending ride...";
    }
}