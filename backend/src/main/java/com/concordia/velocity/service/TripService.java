package com.concordia.velocity.service;

import com.concordia.velocity.model.*;
import com.concordia.velocity.observer.DashboardObserver;
import com.concordia.velocity.observer.StatusObserver;
import com.concordia.velocity.observer.Observer;
import com.concordia.velocity.strategy.OneTimeElectricPayment;
import com.concordia.velocity.strategy.OneTimeStandardPayment;
import com.concordia.velocity.strategy.PaymentStrategy;
import com.google.api.core.ApiFuture;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

@Service
public class TripService {

    private final Firestore db = FirestoreClient.getFirestore();

    /**
     * Wrapper method for docking bike and ending trip
     * This method signature matches what the TripController expects
     *
     * @param bikeId   the bike to dock
     * @param riderId  the rider ending the trip
     * @param dockId   the dock to return bike to
     * @param dockCode the code to lock the dock
     * @return trip completion message
     */
    public String dockBikeAndEndTrip(String bikeId, String riderId, String dockId, String dockCode)
            throws ExecutionException, InterruptedException {
        // Call the existing endTrip method with parameters in the correct order
        return endTrip(bikeId, dockId, dockCode, riderId);
    }

    /**
     * Undocks a reserved bike and starts a trip
     * Updated to include dockCode validation
     *
     * @param bikeId   the bike to undock
     * @param riderId  the rider undocking the bike
     * @param dockCode the code to unlock the dock
     * @return trip confirmation message
     */
    public String undockReservedBike(String bikeId, String riderId, String dockCode)
            throws ExecutionException, InterruptedException {

        // Retrieve bike from Firestore
        DocumentSnapshot bikeDoc = db.collection("bikes").document(bikeId).get().get();
        Bike bike = bikeDoc.toObject(Bike.class);

        if (bike == null) {
            throw new IllegalArgumentException("Bike not found: " + bikeId);
        }

        // Validate bike is reserved
        if (!Bike.STATUS_RESERVED.equalsIgnoreCase(bike.getStatus())) {
            throw new IllegalStateException("Bike is not reserved. Current status: " + bike.getStatus());
        }

        // Validate reservation is still active (not expired)
        if (!bike.isReservedActive()) {
            throw new IllegalStateException("Reservation has expired for bike: " + bikeId);
        }

        // Validate riderId matches the user who made the reservation
        if (!riderId.equals(bike.getReservedByUserId())) {
            throw new IllegalArgumentException("Rider " + riderId + " did not reserve this bike");
        }

        // Get dock and station info before removing bike
        String previousDockId = bike.getDockId();
        String stationId = bike.getStationId();

        if (previousDockId == null || stationId == null) {
            throw new IllegalStateException("Bike has invalid dock or station assignment");
        }

        // Retrieve dock
        DocumentSnapshot dockDoc = db.collection("docks").document(previousDockId).get().get();
        Dock dock = dockDoc.toObject(Dock.class);

        if (dock == null) {
            throw new IllegalArgumentException("Dock not found: " + previousDockId);
        }

        // Validate dock code before allowing bike to be undocked
        validateDockCode(dock, dockCode);

        // Validate bike is at the dock
        validateBikeAtDock(bike, previousDockId);

        // Retrieve station
        DocumentSnapshot stationDoc = db.collection("stations").document(stationId).get().get();
        Station station = stationDoc.toObject(Station.class);

        if (station == null) {
            throw new IllegalArgumentException("Station not found: " + stationId);
        }

        // Attach observers
        Observer dashboardObserver = new DashboardObserver();
        bike.attach(dashboardObserver);
        dock.attach(dashboardObserver);
        station.attach(dashboardObserver);

        // Start trip - change bike status to ON_TRIP
        bike.changeStatus(Bike.STATUS_ON_TRIP);

        // Clear reservation data since trip has started
        bike.clearReservation();

        // Update bike location - bike is no longer at dock
        bike.setDockId(null);
        bike.setStationId(null);

        // Update dock - set to empty and clear bike reference
        dock.setStatus(Dock.STATUS_EMPTY);
        dock.setBikeId(null);
        dock.notifyObservers();

        // Update station - decrement bike count
        station.setNumDockedBikes(Math.max(0, station.getNumDockedBikes() - 1));
        if (bike.getType().equals("electric")) {
            station.setNumElectricBikes(Math.max(0, station.getNumElectricBikes() - 1));
        } else {
            station.setNumStandardBikes(Math.max(0, station.getNumStandardBikes() - 1));
        }

        station.removeBike(bikeId);

        // Update station status based on new count
        String newStationStatus = station.determineStatusFromCapacity();
        if (!newStationStatus.equals(station.getStatus())) {
            station.setStatus(newStationStatus);
        }

        // Create trip record
        Trip trip = createTripRecord(bikeId, bike.getType(), riderId, previousDockId, stationId, station.getStationName());

        // Persist all changes to Firestore
        db.collection("bikes").document(bikeId).set(bike).get();
        db.collection("docks").document(previousDockId).set(dock).get();
        db.collection("stations").document(stationId).set(station).get();
        db.collection("trips").document(trip.getTripId()).set(trip).get();

        return "Trip started successfully for bike " + bikeId + " by rider " + riderId +
                ". Trip ID: " + trip.getTripId() + ". Bike undocked from dock " + previousDockId +
                " at station " + station.getStationName();
    }

    /**
     * Undocks an available bike by entering dock code and starts a trip
     *
     * @param dockId   the dock where the bike is located
     * @param dockCode the code to unlock the dock
     * @param riderId  the rider undocking the bike
     * @return trip confirmation message
     */
    public String undockAvailableBike(String dockId, String dockCode, String riderId)
            throws ExecutionException, InterruptedException {

        // Retrieve dock first to validate code
        DocumentSnapshot dockDoc = db.collection("docks").document(dockId).get().get();
        Dock dock = dockDoc.toObject(Dock.class);

        if (dock == null) {
            throw new IllegalArgumentException("Dock not found: " + dockId);
        }

        // Validate dock code
        validateDockCode(dock, dockCode);

        // Find bike at this dock
        var bikeQuery = db.collection("bikes")
                .whereEqualTo("dockId", dockId)
                .whereEqualTo("status", Bike.STATUS_AVAILABLE)
                .limit(1)
                .get()
                .get()
                .getDocuments();

        if (bikeQuery.isEmpty()) {
            throw new IllegalStateException("No available bike found at dock: " + dockId);
        }

        DocumentSnapshot bikeDoc = bikeQuery.get(0);
        Bike bike = bikeDoc.toObject(Bike.class);

        if (bike == null) {
            throw new IllegalStateException("Failed to retrieve bike from dock: " + dockId);
        }

        String bikeId = bike.getBikeId();
        String stationId = bike.getStationId();

        if (stationId == null) {
            throw new IllegalStateException("Bike has no station assignment");
        }

        // Validate bike is at the dock
        validateBikeAtDock(bike, dockId);

        // Retrieve station
        DocumentSnapshot stationDoc = db.collection("stations").document(stationId).get().get();
        Station station = stationDoc.toObject(Station.class);

        if (station == null) {
            throw new IllegalArgumentException("Station not found: " + stationId);
        }

        // Attach observers
        Observer dashboardObserver = new DashboardObserver();
        bike.attach(dashboardObserver);
        dock.attach(dashboardObserver);
        station.attach(dashboardObserver);

        // Start trip - change bike status to ON_TRIP
        bike.changeStatus(Bike.STATUS_ON_TRIP);

        // Update bike location - bike is no longer at dock
        bike.setDockId(null);
        bike.setStationId(null);

        // Update dock - set to empty and clear bike reference
        dock.setStatus(Dock.STATUS_EMPTY);
        dock.setBikeId(null);
        dock.notifyObservers();

        // Update station - decrement bike count
        station.setNumDockedBikes(Math.max(0, station.getNumDockedBikes() - 1));
        if (bike.getType().equals("electric")) {
            station.setNumElectricBikes(Math.max(0, station.getNumElectricBikes() - 1));
        } else {
            station.setNumStandardBikes(Math.max(0, station.getNumStandardBikes() - 1));
        }

        station.removeBike(bikeId);

        // Update station status based on new count
        String newStationStatus = station.determineStatusFromCapacity();
        if (!newStationStatus.equals(station.getStatus())) {
            station.setStatus(newStationStatus);
        }

        // Create trip record
        Trip trip = createTripRecord(bikeId, bike.getType(), riderId, dockId, stationId, station.getStationName());

        // Persist all changes to Firestore
        db.collection("bikes").document(bikeId).set(bike).get();
        db.collection("docks").document(dockId).set(dock).get();
        db.collection("stations").document(stationId).set(station).get();
        db.collection("trips").document(trip.getTripId()).set(trip).get();

        return "Trip started successfully for bike " + bikeId + " by rider " + riderId +
                ". Trip ID: " + trip.getTripId() + ". Bike undocked from dock " + dockId +
                " at station " + station.getStationName();
    }

    /**
     * Ends a trip by docking the bike
     *
     * @param bikeId   the bike being docked
     * @param dockId   the dock to return to
     * @param dockCode the code to lock the dock
     * @param riderId  the rider ending the trip
     * @return trip completion message
     */
    public String endTrip(String bikeId, String dockId, String dockCode, String riderId)
            throws ExecutionException, InterruptedException {

        // Retrieve bike from Firestore
        DocumentSnapshot bikeDoc = db.collection("bikes").document(bikeId).get().get();
        Bike bike = bikeDoc.toObject(Bike.class);

        if (bike == null) {
            throw new IllegalArgumentException("Bike not found: " + bikeId);
        }

        // Validate bike is on a trip
        if (!Bike.STATUS_ON_TRIP.equalsIgnoreCase(bike.getStatus())) {
            throw new IllegalStateException("Bike is not currently on a trip. Current status: " + bike.getStatus());
        }

        // Find active trip for this bike and rider
        Trip trip = getActiveTripForBike(bikeId, riderId);

        if (trip == null) {
            throw new IllegalStateException("No active trip found for bike " + bikeId + " and rider " + riderId);
        }

        // Retrieve dock
        DocumentSnapshot dockDoc = db.collection("docks").document(dockId).get().get();
        Dock dock = dockDoc.toObject(Dock.class);

        if (dock == null) {
            throw new IllegalArgumentException("Dock not found: " + dockId);
        }

        // Validate dock code before allowing bike return
        validateDockCode(dock, dockCode);

        // Validate dock is available
        if (!Dock.STATUS_EMPTY.equalsIgnoreCase(dock.getStatus())) {
            throw new IllegalStateException("Dock is not available. Current status: " + dock.getStatus());
        }

        // Get station from dock
        String stationId = dock.getStationId();
        if (stationId == null) {
            throw new IllegalStateException("Dock has no station assignment");
        }

        // Validate dock belongs to station
        validateDockAtStation(dock, stationId);

        // Retrieve station
        DocumentSnapshot stationDoc = db.collection("stations").document(stationId).get().get();
        Station station = stationDoc.toObject(Station.class);

        if (station == null) {
            throw new IllegalArgumentException("Station not found: " + stationId);
        }

        // Validate station can accept bikes
        if (station.isOutOfService()) {
            throw new IllegalStateException("Station is out of service: " + stationId);
        }

        if (station.isFull()) {
            throw new IllegalStateException("Station is full: " + stationId);
        }

        // Attach observers
        Observer dashboardObserver = new DashboardObserver();
        Observer notificationObserver = new StatusObserver();
        bike.attach(dashboardObserver);
        bike.attach(notificationObserver);
        dock.attach(dashboardObserver);
        dock.attach(notificationObserver);
        station.attach(dashboardObserver);
        station.attach(notificationObserver);

        // End trip - change bike status to AVAILABLE
        bike.changeStatus(Bike.STATUS_AVAILABLE);

        // Update bike location - bike is now at dock
        bike.setDockId(dockId);
        bike.setStationId(stationId);

        // Update dock - set to occupied and link to bike
        dock.setStatus(Dock.STATUS_OCCUPIED);
        dock.setBikeId(bikeId);
        dock.notifyObservers();

        // Update station - increment bike count
        station.setNumDockedBikes(station.getNumDockedBikes() + 1);
        if (bike.getType().equals("electric")) {
            station.setNumElectricBikes(Math.max(0, station.getNumElectricBikes() + 1));
        } else {
            station.setNumStandardBikes(Math.max(0, station.getNumStandardBikes() + 1));
        }

        station.addBike(bikeId);

        // Update station status based on new count
        String newStationStatus = station.determineStatusFromCapacity();
        if (!newStationStatus.equals(station.getStatus())) {
            station.setStatus(newStationStatus);
        }

        // Complete trip and calculate billing
        trip.completeTrip(stationId, station.getStationName(), dockId);
        Bill bill = calculateAndCreateBill(trip);
        trip.setBill(bill);

        // Persist all changes to Firestore
        db.collection("bikes").document(bikeId).set(bike).get();
        db.collection("docks").document(dockId).set(dock).get();
        db.collection("stations").document(stationId).set(station).get();
        db.collection("trips").document(trip.getTripId()).set(trip).get();
        db.collection("bills").document(bill.getBillId()).set(bill).get();

        return String.format(
                "Trip ended successfully. Bike %s docked at dock %s at station %s (%s). " +
                        "Trip duration: %d minutes. Total cost: $%.2f (including tax).",
                bikeId, dockId, stationId, station.getStationName(),
                trip.getDurationMinutes(), bill.getTotal()
        );
    }

    // ==================== Trip Management Helper Methods ====================

    /**
     * Creates a new trip record
     */
    private Trip createTripRecord(String bikeId, String bikeType, String riderId,
                                  String startDockId, String startStationId, String startStationName) {
        String tripId = UUID.randomUUID().toString();
        return new Trip(tripId, riderId, bikeId, bikeType, startStationId, startStationName, startDockId);
    }

    /**
     * Retrieves the active trip for a specific bike and rider
     */
    private Trip getActiveTripForBike(String bikeId, String riderId)
            throws ExecutionException, InterruptedException {

        var tripQuery = db.collection("trips")
                .whereEqualTo("bikeId", bikeId)
                .whereEqualTo("riderId", riderId)
                .whereEqualTo("status", Trip.STATUS_ACTIVE)
                .limit(1)
                .get()
                .get()
                .getDocuments();

        if (tripQuery.isEmpty()) {
            return null;
        }

        return tripQuery.get(0).toObject(Trip.class);
    }

    /**
     * Calculates billing for a completed trip and creates a bill
     * Uses Strategy pattern - delegates bill creation to the payment strategy
     */
    private Bill calculateAndCreateBill(Trip trip) {
        if (trip.getDurationMinutes() == null) {
            trip.calculateDuration();
        }

        long durationMinutes = trip.getDurationMinutes();

        // Select payment strategy based on bike type
        PaymentStrategy paymentStrategy = selectPaymentStrategy(trip.getBikeType());

        // Strategy creates the complete bill (cost + tax + total)
        return paymentStrategy.createBill(trip.getTripId(), durationMinutes);
    }

    /**
     * Selects the appropriate payment strategy based on bike type
     *
     * @param bikeType the type of bike used for the trip
     * @return the appropriate payment strategy
     */
    private PaymentStrategy selectPaymentStrategy(String bikeType) {
        if (bikeType != null && bikeType.toLowerCase().contains("electric")) {
            return new OneTimeElectricPayment();
        } else {
            return new OneTimeStandardPayment();
        }
    }

    /**
     * Updates payment information on a bill
     */
    public String updateBillPayment(String billId, String paymentMethodLastFour, String status)
            throws ExecutionException, InterruptedException {

        DocumentSnapshot billDoc = db.collection("bills").document(billId).get().get();
        Bill bill = billDoc.toObject(Bill.class);

        if (bill == null) {
            throw new IllegalArgumentException("Bill not found: " + billId);
        }

        bill.setPaymentMethodLastFour(paymentMethodLastFour);
        bill.setStatus(status);

        db.collection("bills").document(billId).set(bill).get();

        return "Bill " + billId + " updated successfully. Status: " + status;
    }

    /**
     * Retrieves a trip by ID
     */
    public Trip getTripById(String tripId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("trips").document(tripId).get().get();
        if (!doc.exists()) {
            throw new IllegalArgumentException("Trip not found: " + tripId);
        }
        return doc.toObject(Trip.class);
    }

    /**
     * Retrieves a bill by ID
     */
    public Bill getBillById(String billId) throws ExecutionException, InterruptedException {
        DocumentSnapshot doc = db.collection("bills").document(billId).get().get();
        if (!doc.exists()) {
            throw new IllegalArgumentException("Bill not found: " + billId);
        }
        return doc.toObject(Bill.class);
    }

    /**
     * Get all trips
     */
    public List<Trip> getAllTrips() throws ExecutionException, InterruptedException {
        CollectionReference tripsRef = db.collection("trips");
        ApiFuture<QuerySnapshot> future = tripsRef.get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Trip> trips = new ArrayList<>();

        for (QueryDocumentSnapshot doc : documents) {
            if (doc.exists()) {
                Trip trip = doc.toObject(Trip.class);
                if (trip != null) trips.add(trip);
            }
        }

        return trips;
    }

    /**
     * Get all trips for a specific rider (userId)
     */
    public List<Trip> getRiderTrips(String userId) throws ExecutionException, InterruptedException {
        CollectionReference tripsRef = db.collection("trips");
        ApiFuture<QuerySnapshot> future = tripsRef.whereArrayContains("riderId", userId).get();
        List<QueryDocumentSnapshot> documents = future.get().getDocuments();

        List<Trip> trips = new ArrayList<>();
        for (QueryDocumentSnapshot doc : documents) {
            if (doc.exists()) {
                Trip trip = doc.toObject(Trip.class);
                if (trip != null) trips.add(trip);
            }
        }

        return trips;
    }


    // ==================== Validation Helper Methods ====================

    /**
     * Validates the dock code entered by the user
     *
     * @param dock        the dock being accessed
     * @param enteredCode the code entered by the user
     * @throws IllegalArgumentException if the dock has no code set
     * @throws IllegalStateException    if the entered code doesn't match
     */
    private void validateDockCode(Dock dock, String enteredCode) {
        if (dock.getDockCode() == null || dock.getDockCode().isEmpty()) {
            throw new IllegalArgumentException(
                    "Dock " + dock.getDockId() + " has no access code configured. " +
                            "Please contact station support."
            );
        }

        if (enteredCode == null || enteredCode.isEmpty()) {
            throw new IllegalArgumentException("Dock code cannot be empty");
        }

        // Trim whitespace and compare
        String trimmedEnteredCode = enteredCode.trim();
        String trimmedDockCode = dock.getDockCode().trim();

        if (!trimmedDockCode.equals(trimmedEnteredCode)) {
            throw new IllegalStateException(
                    "Invalid dock code for dock " + dock.getDockId() + ". " +
                            "Please check the code and try again."
            );
        }

        // Code is valid - dock can be unlocked
    }

    /**
     * Validates that a bike is assigned to the specified dock
     */
    private void validateBikeAtDock(Bike bike, String expectedDockId) {
        if (!expectedDockId.equals(bike.getDockId())) {
            throw new IllegalStateException(
                    "Bike " + bike.getBikeId() + " is not at dock " + expectedDockId +
                            ". Current dock: " + bike.getDockId()
            );
        }
    }

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

    public String reportBike(String tripId, String issue) throws ExecutionException, InterruptedException, IOException {
        // Get trip document
        DocumentSnapshot tripDoc = db.collection("trips").document(tripId).get().get();
        if (!tripDoc.exists()) {
            throw new IllegalArgumentException("Trip not found: " + tripId);
        }

        // Extract bikeId from the trip
        String bikeId = tripDoc.getString("bikeId");
        if (bikeId == null || bikeId.isEmpty()) {
            throw new IllegalArgumentException("Trip " + tripId + " does not contain a valid bikeId");
        }

        // Get the corresponding bike document
        DocumentReference bikeRef = db.collection("bikes").document(bikeId);
        DocumentSnapshot bikeDoc = bikeRef.get().get();
        if (!bikeDoc.exists()) {
            throw new IllegalArgumentException("Bike not found: " + bikeId);
        }

        // Update bike status to maintenance
        Bike bike = bikeDoc.toObject(Bike.class);
        System.out.println(bike);
        if (bike == null)
            throw new NullPointerException("BikeId " + bikeId + " leads to null");

        bike.changeStatus(Bike.STATUS_MAINTENANCE);

        // persist changes
        db.collection("bikes").document(bikeId).set(bike).get();

        // Log the issue to a local file
        String logEntry = String.format(
                "[%s] TripID: %s | BikeID: %s | Issue: %s%n\n",
                Timestamp.now(),
                tripId,
                bikeId,
                issue
        );

        Path logFilePath = Paths.get("bike_issues.log");
        Files.write(logFilePath, logEntry.getBytes(StandardCharsets.UTF_8),
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);


        return "Bike " + bikeId + " marked as maintenance. Issue logged.";
    }
}