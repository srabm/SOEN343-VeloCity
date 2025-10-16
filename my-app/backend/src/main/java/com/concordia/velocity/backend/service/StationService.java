// This file is supposed to handle business logic related to stations. 
// It's the service layer that keeps the logic testable and reusable, so that it keeps the controller clean.

package com.concordia.velocity.backend.service;

import com.concordia.velocity.backend.model.Bike;
import com.concordia.velocity.backend.model.Station;
import com.concordia.velocity.backend.repository.StationRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StationService {

    private final StationRepository stationRepository;

    public StationService(StationRepository stationRepository) {
        this.stationRepository = stationRepository;
    }

    // Operator changes station status
    public Station updateStationStatus(Long id, String newStatus) {
        Station station = stationRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Station not found"));

        if (newStatus.equalsIgnoreCase("out_of_service")) {
            handleOutOfService(station);
        } else if (newStatus.equalsIgnoreCase("active")) {
            handleReactivate(station);
        } else {
            throw new IllegalArgumentException("Invalid status. Use 'active' or 'out_of_service'");
        }

        return stationRepository.save(station);
    }

    private void handleOutOfService(Station station) {
        station.setStatus("out_of_service");

        // End all bike reservations
        List<Bike> bikes = station.getDockedBikes();
        for (Bike bike : bikes) {
            if (bike.getStatus().equalsIgnoreCase("reserved")) {
                bike.setStatus("available");
                bike.setReservationExpiry(null);
            }
        }

        System.out.println("Station " + station.getStationName() + " is out of service.");
    }

    private void handleReactivate(Station station) {
        int num = station.getNumDockedBikes();
        if (num == 0) station.setStatus("empty");
        else if (num == station.getCapacity()) station.setStatus("full");
        else station.setStatus("occupied");

        System.out.println("Station " + station.getStationName() + " reactivated as " + station.getStatus());
    }

    public List<Station> getAllStations() {
        return stationRepository.findAll();
    }
}

