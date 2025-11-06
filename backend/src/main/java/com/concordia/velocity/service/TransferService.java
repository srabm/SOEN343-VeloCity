package com.concordia.velocity.service;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.Dock;
import com.concordia.velocity.model.Station;
import com.concordia.velocity.observer.DashboardObserver;
import com.concordia.velocity.observer.Observer;
import com.google.cloud.firestore.DocumentSnapshot;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.WriteBatch;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutionException;

// todo full review of this class / the move bike method

@Service
public class TransferService {

    private final Firestore db = FirestoreClient.getFirestore();

    /**
     * Moves a bike from one station to another
     *
     * This operation:
     * 1. Validates all entities exist
     * 2. Validates bike can be moved (not reserved or on trip)
     * 3. Validates destination station has capacity
     * 4. Validates destination dock is empty
     * 5. Updates bike location
     * 6. Updates source dock (set to empty)
     * 7. Updates destination dock (set to occupied)
     * 8. Updates station bike counts
     * 9. Uses Firestore batch to ensure atomicity
     *
     * @param bikeId the bike to move
     * @param sourceStationId the station where bike currently is
     * @param destinationStationId the station to move bike to
     * @param destinationDockId the specific dock to place bike in
     * @return confirmation message
     */
    public String moveBike(String bikeId, String sourceStationId, String destinationStationId, String destinationDockId)
            throws ExecutionException, InterruptedException {

        // ==================== Step 1: Fetch all entities ====================
        DocumentSnapshot bikeDoc = db.collection("bikes").document(bikeId).get().get();
        Bike bike = bikeDoc.toObject(Bike.class);

        DocumentSnapshot sourceStationDoc = db.collection("stations").document(sourceStationId).get().get();
        Station sourceStation = sourceStationDoc.toObject(Station.class);

        DocumentSnapshot destinationStationDoc = db.collection("stations").document(destinationStationId).get().get();
        Station destinationStation = destinationStationDoc.toObject(Station.class);

        DocumentSnapshot destinationDockDoc = db.collection("docks").document(destinationDockId).get().get();
        Dock destinationDock = destinationDockDoc.toObject(Dock.class);

        // Validate all entities exist
        if (bike == null) {
            throw new IllegalArgumentException("Bike not found: " + bikeId);
        }
        if (sourceStation == null) {
            throw new IllegalArgumentException("Source station not found: " + sourceStationId);
        }
        if (destinationStation == null) {
            throw new IllegalArgumentException("Destination station not found: " + destinationStationId);
        }
        if (destinationDock == null) {
            throw new IllegalArgumentException("Destination dock not found: " + destinationDockId);
        }

        // ==================== Step 2: Validate bike state ====================
        String currentBikeStatus = bike.getStatus();

        if (Bike.STATUS_RESERVED.equalsIgnoreCase(currentBikeStatus)) {
            throw new IllegalStateException(
                    "Cannot move bike " + bikeId + " - it is currently reserved by user " +
                            bike.getReservedByUserId()
            );
        }

        if (Bike.STATUS_ON_TRIP.equalsIgnoreCase(currentBikeStatus)) {
            throw new IllegalStateException(
                    "Cannot move bike " + bikeId + " - it is currently on a trip"
            );
        }

        // ==================== Step 3: Validate bike is at source station ====================
        String currentStationId = bike.getStationId();
        if (currentStationId == null || !currentStationId.equals(sourceStationId)) {
            throw new IllegalStateException(
                    "Bike " + bikeId + " is not at source station " + sourceStationId +
                            ". Current station: " + (currentStationId != null ? currentStationId : "none")
            );
        }

        String currentDockId = bike.getDockId();
        if (currentDockId == null) {
            throw new IllegalStateException(
                    "Bike " + bikeId + " has no dock assignment. Cannot determine source dock."
            );
        }

        // Fetch source dock
        DocumentSnapshot sourceDockDoc = db.collection("docks").document(currentDockId).get().get();
        Dock sourceDock = sourceDockDoc.toObject(Dock.class);

        if (sourceDock == null) {
            throw new IllegalArgumentException("Source dock not found: " + currentDockId);
        }

        // Validate source dock belongs to source station
        validateDockAtStation(sourceDock, sourceStationId);

        // ==================== Step 4: Validate destination station ====================
        if (Station.STATUS_OUT_OF_SERVICE.equalsIgnoreCase(destinationStation.getStatus())) {
            throw new IllegalStateException(
                    "Destination station " + destinationStationId + " (" +
                            destinationStation.getStationName() + ") is out of service"
            );
        }

        if (destinationStation.getNumDockedBikes() >= destinationStation.getCapacity()) {
            throw new IllegalStateException(
                    "Destination station " + destinationStationId + " (" +
                            destinationStation.getStationName() + ") is full. " +
                            "Capacity: " + destinationStation.getCapacity() +
                            ", Current bikes: " + destinationStation.getNumDockedBikes()
            );
        }

        // ==================== Step 5: Validate destination dock ====================
        // Validate destination dock belongs to destination station
        validateDockAtStation(destinationDock, destinationStationId);

        if (!Dock.STATUS_EMPTY.equalsIgnoreCase(destinationDock.getStatus())) {
            throw new IllegalStateException(
                    "Destination dock " + destinationDockId + " is not empty. " +
                            "Current status: " + destinationDock.getStatus() +
                            (destinationDock.getBikeId() != null ?
                                    ", occupied by bike: " + destinationDock.getBikeId() : "")
            );
        }

        // ==================== Step 6: Perform the move ====================
        // Attach observers
        Observer dashboardObserver = new DashboardObserver();

        bike.attach(dashboardObserver);
        sourceDock.attach(dashboardObserver);
        destinationDock.attach(dashboardObserver);
        sourceStation.attach(dashboardObserver);
        destinationStation.attach(dashboardObserver);

        // Update bike location
        bike.setStationId(destinationStationId);
        bike.setDockId(destinationDockId);

        // Update source dock (set to empty)
        sourceDock.setStatus(Dock.STATUS_EMPTY);
        sourceDock.setBikeId(null);
        sourceDock.notifyObservers();

        // Update destination dock (set to occupied)
        destinationDock.setStatus(Dock.STATUS_OCCUPIED);
        destinationDock.setBikeId(bikeId);
        destinationDock.notifyObservers();

        // Update station bike counts
        sourceStation.setNumDockedBikes(Math.max(0, sourceStation.getNumDockedBikes() - 1));
        if (bike.getType().equals("electric")) {
            sourceStation.setNumElectricBikes(Math.max(0, sourceStation.getNumElectricBikes() - 1));
        } else {
            sourceStation.setNumStandardBikes(Math.max(0, sourceStation.getNumStandardBikes() - 1));
        }

        sourceStation.removeBike(bikeId);


        destinationStation.setNumDockedBikes(destinationStation.getNumDockedBikes() + 1);
        if (bike.getType().equals("electric")) {
            destinationStation.setNumElectricBikes(Math.max(0, destinationStation.getNumElectricBikes() + 1));
        } else {
            destinationStation.setNumStandardBikes(Math.max(0, destinationStation.getNumStandardBikes() + 1));
        }

        destinationStation.addBike(bikeId);

        // Update station statuses based on new counts
        String newSourceStatus = sourceStation.determineStatusFromCapacity();
        if (!newSourceStatus.equals(sourceStation.getStatus()) &&
                !Station.STATUS_OUT_OF_SERVICE.equalsIgnoreCase(sourceStation.getStatus())) {
            sourceStation.setStatus(newSourceStatus);
        }

        String newDestinationStatus = destinationStation.determineStatusFromCapacity();
        if (!newDestinationStatus.equals(destinationStation.getStatus()) &&
                !Station.STATUS_OUT_OF_SERVICE.equalsIgnoreCase(destinationStation.getStatus())) {
            destinationStation.setStatus(newDestinationStatus);
        }

        // ==================== Step 7: Persist using Firestore Batch ====================
        // Using batch writes ensures all updates succeed or all fail (atomicity)
        WriteBatch batch = db.batch();

        batch.set(db.collection("bikes").document(bikeId), bike);
        batch.set(db.collection("docks").document(currentDockId), sourceDock);
        batch.set(db.collection("docks").document(destinationDockId), destinationDock);
        batch.set(db.collection("stations").document(sourceStationId), sourceStation);
        batch.set(db.collection("stations").document(destinationStationId), destinationStation);

        // Commit all changes atomically
        batch.commit().get();

        // Notify observers
        dashboardObserver.update("Bike " + bikeId + " moved from " + sourceStation.getStationName() +
                " to " + destinationStation.getStationName());

        return "Bike " + bikeId + " (" + bike.getType() + ") successfully moved from " +
                sourceStation.getStationName() + " (dock " + currentDockId + ") to " +
                destinationStation.getStationName() + " (dock " + destinationDockId + "). " +
                "Source station now has " + sourceStation.getNumDockedBikes() + "/" + sourceStation.getCapacity() + " bikes. " +
                "Destination station now has " + destinationStation.getNumDockedBikes() + "/" +
                destinationStation.getCapacity() + " bikes.";
    }

    // ==================== Validation Helper Methods ====================

    /**
     * Validates that a bike is assigned to the specified station
     */
    private void validateBikeAtStation(Bike bike, String expectedStationId) {
        if (!expectedStationId.equals(bike.getStationId())) {
            throw new IllegalStateException(
                    "Bike " + bike.getBikeId() + " is not at station " + expectedStationId +
                            ". Current station: " + bike.getStationId()
            );
        }
    }

    /**
     * Validates that a dock belongs to the specified station
     */
    private void validateDockAtStation(Dock dock, String expectedStationId) {
        if (!expectedStationId.equals(dock.getStationId())) {
            throw new IllegalStateException(
                    "Dock " + dock.getDockId() + " does not belong to station " + expectedStationId +
                            ". Current station: " + dock.getStationId()
            );
        }
    }

    /**
     * Validates that a bike is at a specific dock
     */
    private void validateBikeAtDock(Bike bike, String expectedDockId) {
        if (!expectedDockId.equals(bike.getDockId())) {
            throw new IllegalStateException(
                    "Bike " + bike.getBikeId() + " is not at dock " + expectedDockId +
                            ". Current dock: " + bike.getDockId()
            );
        }
    }
}