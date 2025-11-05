// This file is supposed to handle business logic related to updating station status

package com.concordia.velocity.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import com.concordia.velocity.model.Station;
import com.concordia.velocity.observer.DashboardObserver;
import com.concordia.velocity.observer.NotificationObserver;
import com.concordia.velocity.observer.Observer;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;
import com.google.cloud.firestore.DocumentSnapshot;
import java.util.concurrent.ExecutionException;

@Service
public class StationService {
        ///////
    private final Firestore db = FirestoreClient.getFirestore();

    // This handles the logic for updating a station's status by validating status changes, enforing reservation rules, and emitting observer events
    public String updateStationStatus(String stationId, String newStatus) throws ExecutionException, InterruptedException {
        Station station = db.collection("stations").document(stationId)
                .get().get().toObject(Station.class);

        if (station == null) {
            return "Station not found.";
        }

        // Attaches observers
        Observer dashboardObserver = new DashboardObserver();
        Observer notificationObserver = new NotificationObserver();

        station.attach(dashboardObserver);
        station.attach(notificationObserver);

        
        if (newStatus.equals("out_of_service")) {
            station.setStatus("out_of_service");
        } else if (Arrays.asList("empty", "occupied", "full").contains(newStatus)) {
            if (noReservedBikes(station)) {
                // No reservations, so activates normally
                String computedStatus = determineActiveStatus(station);
                station.setStatus(computedStatus);

            } else {
                // Has reservations, so terminate and then activate
                terminateReservations(station);
                String computedStatus = determineActiveStatus(station);
                station.setStatus(computedStatus);
            }
        } else {
            throw new IllegalArgumentException("Invalid station status: " + newStatus + "\n");
        }
        station.notifyObservers();
        db.collection("stations").document(stationId).set(station);
        return "Station " + stationId + " updated to status:  " + station.getStatus();
    }

    // Checks if any bikes at the station are still reserved
    private boolean noReservedBikes(Station station) throws ExecutionException, InterruptedException {
        List<String> bikeIds = station.getBikeIds();
        if (bikeIds == null || bikeIds.isEmpty()) {
            return true; // No bikes = ok to change status
        }

        for (String bikeId : bikeIds) {
            var bikeDoc = db.collection("bikes").document(bikeId).get().get();
            if (bikeDoc.exists()) {
                String bikeStatus = bikeDoc.getString("status");
                if ("reserved".equalsIgnoreCase(bikeStatus)) {
                    System.out.println("Bike " + bikeId + " is reserved. The station cannot be activated.");
                    return false;
                }
            }
        }
        return true;
    }
    // This determines the correct active state based on number of docked bikes (so empty if there's 0 bikes, full when it's at max cap, and occupied in any other case)
    private String determineActiveStatus(Station station) {
        int docked = station.getNumDockedBikes();
        int capacity = station.getCapacity();

        if (docked == 0) {
            return "empty";
        } else if (docked == capacity) {
            return "full";
        } else {
            return "occupied";
        }
    }

    // This terminates reservations and sets the reserved bikes back to "available" before reactivatnig
    private void terminateReservations(Station station) throws ExecutionException, InterruptedException {
        List<String> bikeIds = station.getBikeIds();
        if (bikeIds == null || bikeIds.isEmpty()) return;

        for (String bikeId : bikeIds) {
            var bikeDoc = db.collection("bikes").document(bikeId).get().get();
            if (bikeDoc.exists() && "reserved".equalsIgnoreCase(bikeDoc.getString("status"))) {
                bikeDoc.getReference().update("status", "available");
                System.out.println("Reservation terminated for bike " + bikeId);
            }
        }
    }

    public Station getStationById(String stationId) throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        return  db.collection("stations").document(stationId)
                .get().get().toObject(Station.class);
    }

    public List<Station> getAllStations() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();
        List<Station> stations = new ArrayList<>();
        for (DocumentSnapshot doc : db.collection("stations").get().get().getDocuments()) {
        stations.add(doc.toObject(Station.class));
    }
        return stations;
}

}
