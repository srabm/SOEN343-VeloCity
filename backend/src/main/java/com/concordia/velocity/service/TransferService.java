package com.concordia.velocity.service;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.Dock;
import com.concordia.velocity.model.Station;
import com.google.api.core.ApiFuture;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Service for handling bike transfers between docks and stations
 */
@Service
public class TransferService {

    private static final String BIKES_COLLECTION = "bikes";
    private static final String DOCKS_COLLECTION = "docks";
    private static final String STATIONS_COLLECTION = "stations";

    /**
     * Get all stations from Firestore
     */
    public List<Station> getAllStations() throws ExecutionException, InterruptedException {
        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection(STATIONS_COLLECTION).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Station> stations = new ArrayList<>();
        for (QueryDocumentSnapshot document : documents) {
            Station station = document.toObject(Station.class);
            stations.add(station);
        }

        return stations;
    }

    /**
     * Get all available bikes at a specific station
     */
    public List<Bike> getAvailableBikesAtStation(String stationId)
            throws ExecutionException, InterruptedException {

        if (stationId == null || stationId.isEmpty()) {
            throw new IllegalArgumentException("Station ID cannot be null or empty");
        }

        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection(BIKES_COLLECTION)
                .whereEqualTo("stationId", stationId)
                .whereEqualTo("status", Bike.STATUS_AVAILABLE)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Bike> bikes = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            Bike bike = document.toObject(Bike.class);
            bikes.add(bike);
        }

        return bikes;
    }

    /**
     * Get all available (empty) docks at a specific station
     */
    public List<Dock> getAvailableDocksAtStation(String stationId)
            throws ExecutionException, InterruptedException {

        if (stationId == null || stationId.isEmpty()) {
            throw new IllegalArgumentException("Station ID cannot be null or empty");
        }

        Firestore db = FirestoreClient.getFirestore();

        ApiFuture<QuerySnapshot> future = db.collection(DOCKS_COLLECTION)
                .whereEqualTo("stationId", stationId)
                .whereEqualTo("status", Dock.STATUS_EMPTY)
                .get();

        List<QueryDocumentSnapshot> documents = future.get().getDocuments();
        List<Dock> docks = new ArrayList<>();

        for (QueryDocumentSnapshot document : documents) {
            Dock dock = document.toObject(Dock.class);
            docks.add(dock);
        }

        return docks;
    }

    /**
     * Transfer a bike from one dock to another
     */
    public Map<String, Object> transferBike(
            String bikeId,
            String sourceDockId,
            String destinationDockId,
            String sourceStationId,
            String destinationStationId
    ) throws ExecutionException, InterruptedException {

        Firestore db = FirestoreClient.getFirestore();

        // Start a transaction to ensure atomicity
        ApiFuture<Map<String, Object>> transaction = db.runTransaction(txn -> {

            // 1. Fetch the bike
            DocumentReference bikeRef = db.collection(BIKES_COLLECTION).document(bikeId);
            DocumentSnapshot bikeDoc = txn.get(bikeRef).get();

            if (!bikeDoc.exists()) {
                throw new IllegalArgumentException("Bike not found: " + bikeId);
            }

            Bike bike = bikeDoc.toObject(Bike.class);

            // Validate bike status
            if (!Bike.STATUS_AVAILABLE.equalsIgnoreCase(bike.getStatus())) {
                throw new IllegalStateException(
                        "Bike is not available for transfer. Current status: " + bike.getStatus()
                );
            }

            // Validate bike is at the source dock
            if (!sourceDockId.equals(bike.getDockId())) {
                throw new IllegalArgumentException(
                        "Bike is not at the specified source dock. Expected: " +
                                sourceDockId + ", Actual: " + bike.getDockId()
                );
            }

            // 2. Fetch source dock
            DocumentReference sourceDockRef = db.collection(DOCKS_COLLECTION).document(sourceDockId);
            DocumentSnapshot sourceDockDoc = txn.get(sourceDockRef).get();

            if (!sourceDockDoc.exists()) {
                throw new IllegalArgumentException("Source dock not found: " + sourceDockId);
            }

            Dock sourceDock = sourceDockDoc.toObject(Dock.class);

            if (!Dock.STATUS_OCCUPIED.equalsIgnoreCase(sourceDock.getStatus())) {
                throw new IllegalStateException(
                        "Source dock is not occupied. Status: " + sourceDock.getStatus()
                );
            }

            if (!bikeId.equals(sourceDock.getBikeId())) {
                throw new IllegalArgumentException(
                        "Source dock does not contain the specified bike"
                );
            }

            // 3. Fetch destination dock
            DocumentReference destDockRef = db.collection(DOCKS_COLLECTION).document(destinationDockId);
            DocumentSnapshot destDockDoc = txn.get(destDockRef).get();

            if (!destDockDoc.exists()) {
                throw new IllegalArgumentException(
                        "Destination dock not found: " + destinationDockId
                );
            }

            Dock destDock = destDockDoc.toObject(Dock.class);

            if (!Dock.STATUS_EMPTY.equalsIgnoreCase(destDock.getStatus())) {
                throw new IllegalStateException(
                        "Destination dock is not empty. Status: " + destDock.getStatus()
                );
            }

            // 4. Fetch source station
            DocumentReference sourceStationRef = db.collection(STATIONS_COLLECTION).document(sourceStationId);
            DocumentSnapshot sourceStationDoc = txn.get(sourceStationRef).get();

            if (!sourceStationDoc.exists()) {
                throw new IllegalArgumentException(
                        "Source station not found: " + sourceStationId
                );
            }

            Station sourceStation = sourceStationDoc.toObject(Station.class);

            // 5. Fetch destination station
            DocumentReference destStationRef = db.collection(STATIONS_COLLECTION).document(destinationStationId);
            DocumentSnapshot destStationDoc = txn.get(destStationRef).get();

            if (!destStationDoc.exists()) {
                throw new IllegalArgumentException(
                        "Destination station not found: " + destinationStationId
                );
            }

            Station destStation = destStationDoc.toObject(Station.class);

            if (!destStation.hasAvailableSpace()) {
                throw new IllegalStateException(
                        "Destination station has no available space or is out of service"
                );
            }

            // 6. Perform the transfer
            boolean isInterStationTransfer = !sourceStationId.equals(destinationStationId);

            // Update bike location
            bike.setDockId(destinationDockId);
            bike.setStationId(destinationStationId);

            // Clear any reservation data (important for operator transfers)
            // This also cancels any scheduled reservation expiry tasks
            bike.clearReservation();

            txn.set(bikeRef, bike);

            // Update source dock - make it empty
            sourceDock.changeStatus(Dock.STATUS_EMPTY);
            sourceDock.setBikeId(null);
            txn.set(sourceDockRef, sourceDock);

            // Update destination dock - make it occupied
            destDock.changeStatus(Dock.STATUS_OCCUPIED);
            destDock.setBikeId(bikeId);
            txn.set(destDockRef, destDock);

            // Update stations if inter-station transfer
            if (isInterStationTransfer) {
                sourceStation.removeBike(bike);
                txn.set(sourceStationRef, sourceStation);

                destStation.addBike(bike);
                txn.set(destStationRef, destStation);
            }

            // 7. Create response map
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "Bike successfully transferred");
            response.put("bikeId", bike.getBikeId());
            response.put("sourceDockId", sourceDockId);
            response.put("destinationDockId", destinationDockId);
            response.put("sourceStationId", sourceStationId);
            response.put("sourceStationName", sourceStation.getStationName());
            response.put("destinationStationId", destinationStationId);
            response.put("destinationStationName", destStation.getStationName());
            response.put("interStationTransfer", isInterStationTransfer);
            response.put("timestamp", java.time.Instant.now().toString());

            return response;
        });

        return transaction.get();
    }
}