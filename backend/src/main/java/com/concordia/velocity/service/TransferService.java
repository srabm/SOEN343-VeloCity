package com.concordia.velocity.service;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.Dock;
import com.concordia.velocity.model.Station;
import com.concordia.velocity.observer.DashboardObserver;
import com.concordia.velocity.observer.NotificationObserver;
import com.concordia.velocity.observer.Observer;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;
import java.util.List;

@Service
public class TransferService {

    private final Firestore db = FirestoreClient.getFirestore();

    public String moveBike(String bikeId, String sourceStationId, String destinationStationId, String destinationDockId)
            throws ExecutionException, InterruptedException {

        // Fetch Bike, Stations, Dock entities
        Bike bike = db.collection("bikes").document(bikeId).get().get().toObject(Bike.class);
        Station sourceStation = db.collection("stations").document(sourceStationId).get().get().toObject(Station.class);
        Station destinationStation = db.collection("stations").document(destinationStationId).get().get().toObject(Station.class);
        Dock destinationDock = db.collection("docks").document(destinationDockId).get().get().toObject(Dock.class);


        if (bike == null)
            throw new IllegalArgumentException("Bike not found: " + bikeId);
        if (sourceStation == null)
            throw new IllegalArgumentException("Source station not found: " + sourceStationId);
        if (destinationStation == null)
            throw new IllegalArgumentException("Destination station not found: " + destinationStationId);
        if (destinationDock == null)
            throw new IllegalArgumentException("Destination dock not found: " + destinationDockId);

        // --- Validate statuses ---
        if ("reserved".equalsIgnoreCase(bike.getStatus())) {
            throw new IllegalStateException("Cannot move Bike " + bikeId + " — it is currently reserved.");
        }
        if ("on_trip".equalsIgnoreCase(bike.getStatus())) {
            throw new IllegalStateException("Cannot move Bike " + bikeId + " — it is currently on a trip.");
        }
        if ("out_of_service".equalsIgnoreCase(destinationStation.getStatus())) {
            throw new IllegalStateException("Destination station " + destinationStationId + " is out of service.");
        }
        if (destinationStation.getNumDockedBikes() >= destinationStation.getCapacity()) {
            throw new IllegalStateException("Destination station " + destinationStationId + " is full.");
        }
        if (!"empty".equalsIgnoreCase(destinationDock.getStatus())) {
            throw new IllegalStateException("Destination dock " + destinationDockId + " is not empty.");
        }


        String oldDockId = bike.getDockId();

        // Update bike
        bike.setStationId(destinationStationId);
        bike.setDockId(destinationDockId);
        db.collection("bikes").document(bikeId).set(bike);

        // Update source dock (set empty)
        db.collection("docks").document(oldDockId)
                .update("status", "empty", "bikeId", null);

        // Update destination dock (set to occupied)
        destinationDock.setStatus("occupied");
        destinationDock.setBikeId(bikeId);
        db.collection("docks").document(destinationDockId).set(destinationDock);

        // Update stations
        sourceStation.setNumDockedBikes(Math.max(0, sourceStation.getNumDockedBikes() - 1));
        destinationStation.setNumDockedBikes(destinationStation.getNumDockedBikes() + 1);
        db.collection("stations").document(sourceStationId).set(sourceStation);
        db.collection("stations").document(destinationStationId).set(destinationStation);

        // Notify Observers
        Observer dashboard = new DashboardObserver();
        Observer notification = new NotificationObserver();
        dashboard.update("Bike " + bikeId + " moved from " + sourceStationId + " → " + destinationStationId);
        notification.update("Bike " + bikeId + " moved from " + sourceStationId + " → " + destinationStationId);

        return "Bike " + bikeId + " successfully moved from " + sourceStationId +
               " to " + destinationStationId + " (Dock " + destinationDockId + ")";
    }
}
