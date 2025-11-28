package com.concordia.velocity.service;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.Bill;
import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.Trip;
import com.concordia.velocity.strategy.AbandonedPayment;
import com.concordia.velocity.strategy.PaymentStrategy;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Service to detect and handle abandoned trips
 * A trip is considered abandoned if it has been active for more than 12 hours
 */
@Service
public class AbandonedTripService {

    private static final long ABANDONMENT_THRESHOLD_HOURS = 4;
    public static final String STATUS_ABANDONED = "abandoned";

    private final Firestore db = FirestoreClient.getFirestore();
    private final UserService userService;
    private final BikeService bikeService;
    private final PaymentStrategy abandonedPaymentStrategy;
    private final IdGeneratorService idGeneratorService;

    public AbandonedTripService(UserService userService, BikeService bikeService, IdGeneratorService idGeneratorService) {
        this.userService = userService;
        this.bikeService = bikeService;
        this.abandonedPaymentStrategy = new AbandonedPayment();
        this.idGeneratorService = idGeneratorService;
    }

    /**
     * Scheduled task that runs every hour to check for abandoned trips
     * Uses Spring's @Scheduled annotation with cron expression
     * Cron: "0 0 * * * *" means run at the top of every hour
     */
    @Scheduled(cron = "0 0 * * * *")
    public void checkForAbandonedTrips() {
        System.out.println("[ABANDONED TRIP CHECK] Starting scheduled check at " + LocalDateTime.now());

        try {
            List<Trip> abandonedTrips = findAbandonedTrips();

            if (abandonedTrips.isEmpty()) {
                System.out.println("[ABANDONED TRIP CHECK] No abandoned trips found");
                return;
            }

            System.out.println("[ABANDONED TRIP CHECK] Found " + abandonedTrips.size() + " abandoned trip(s)");

            for (Trip trip : abandonedTrips) {
                processAbandonedTrip(trip);
            }

        } catch (Exception e) {
            System.err.println("[ABANDONED TRIP CHECK] Error during abandoned trip check: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Manually trigger abandoned trip check (useful for testing or admin operations)
     * @return number of abandoned trips processed
     */
    public int manualCheckForAbandonedTrips() throws ExecutionException, InterruptedException {
        System.out.println("[ABANDONED TRIP CHECK] Manual check initiated");

        List<Trip> abandonedTrips = findAbandonedTrips();

        for (Trip trip : abandonedTrips) {
            processAbandonedTrip(trip);
        }

        return abandonedTrips.size();
    }

    /**
     * Finds all active trips that have exceeded the 12-hour threshold
     * @return list of abandoned trips
     */
    private List<Trip> findAbandonedTrips() throws ExecutionException, InterruptedException {
        // Query all active trips
        var querySnapshot = db.collection("trips")
                .whereEqualTo("status", Trip.STATUS_ACTIVE)
                .get()
                .get();

        List<Trip> abandonedTrips = new java.util.ArrayList<>();
        LocalDateTime now = LocalDateTime.now();

        for (QueryDocumentSnapshot doc : querySnapshot.getDocuments()) {
            Trip trip = doc.toObject(Trip.class);

            if (trip.getStartTime() != null) {
                LocalDateTime startTime = trip.getStartTime().toSqlTimestamp().toLocalDateTime();
                Duration duration = Duration.between(startTime, now);

                if (duration.toHours() >= ABANDONMENT_THRESHOLD_HOURS) {
                    abandonedTrips.add(trip);
                    System.out.println("[ABANDONED TRIP] Found abandoned trip: " + trip.getTripId() +
                            " (active for " + duration.toHours() + " hours)");
                }
            }
        }

        return abandonedTrips;
    }

    /**
     * Processes an abandoned trip by:
     * 1. Changing trip status to "abandoned"
     * 2. Creating a bill using AbandonedPayment strategy ($333 fee)
     * 3. Setting end time to now
     * 4. Updating the bike status to "abandoned" (lost/never returned)
     */
    private void processAbandonedTrip(Trip trip) throws ExecutionException, InterruptedException {
        System.out.println("[ABANDONED TRIP] Processing abandoned trip: " + trip.getTripId());

        // Calculate actual duration
        LocalDateTime startTime = trip.getStartTime().toSqlTimestamp().toLocalDateTime();
        LocalDateTime endTime = LocalDateTime.now();
        Duration duration = Duration.between(startTime, endTime);
        long durationMinutes = duration.toMinutes();

        // Update trip status and end time
        trip.setStatus(STATUS_ABANDONED);
        trip.setEndTime(Timestamp.now());
        trip.setDurationMinutes(durationMinutes);

        // Get rider for bill creation
        Rider rider = userService.getUserById(trip.getRiderId());

        // Create abandonment bill using payment strategy
        Bill abandonmentBill = abandonedPaymentStrategy.createBillAndProcessPayment(trip, durationMinutes, rider);

        String billId = idGeneratorService.generateBillId();  // Creates BILL0001, BILL0002, etc.
        abandonmentBill.setBillId(billId);

        trip.setBill(abandonmentBill);

        // Save updated trip to Firestore
        db.collection("trips").document(trip.getTripId()).set(trip).get();

        // Save bill to Firestore
        db.collection("bills").document(abandonmentBill.getBillId()).set(abandonmentBill).get();

        // Log the action
        System.out.println(String.format(
                "[ABANDONED TRIP] Trip %s marked as abandoned. Duration: %d hours, %d minutes. Bill: $%.2f (includes tax)",
                trip.getTripId(),
                duration.toHours(),
                duration.toMinutes() % 60,
                abandonmentBill.getTotal()
        ));

        // Update bike status to ABANDONED (lost/never returned to dock)
        if (trip.getBikeId() != null && !trip.getBikeId().isEmpty()) {
            try {
                var bike = bikeService.getBikeById(trip.getBikeId());
                if (bike != null) {
                    String oldBikeStatus = bike.getStatus();
                    bike.setStatus(Bike.STATUS_ABANDONED);
                    db.collection("bikes").document(bike.getBikeId()).set(bike).get();
                    System.out.println(String.format(
                            "[ABANDONED TRIP] Bike %s status changed: %s → abandoned (bike is lost/not returned to dock)",
                            bike.getBikeId(), oldBikeStatus
                    ));
                }
            } catch (Exception e) {
                System.err.println("[ABANDONED TRIP] Could not update bike status: " + e.getMessage());
            }
        }
    }

    /**
     * Check if a specific trip is abandoned
     * @param tripId the trip ID to check
     * @return true if the trip is abandoned (>12 hours active)
     */
    public boolean isTripAbandoned(String tripId) throws ExecutionException, InterruptedException {
        var doc = db.collection("trips").document(tripId).get().get();

        if (!doc.exists()) {
            return false;
        }

        Trip trip = doc.toObject(Trip.class);

        if (trip == null || !Trip.STATUS_ACTIVE.equalsIgnoreCase(trip.getStatus())) {
            return false;
        }

        if (trip.getStartTime() == null) {
            return false;
        }

        LocalDateTime startTime = trip.getStartTime().toSqlTimestamp().toLocalDateTime();
        LocalDateTime now = LocalDateTime.now();
        Duration duration = Duration.between(startTime, now);

        return duration.toHours() >= ABANDONMENT_THRESHOLD_HOURS;
    }

    /**
     * Get abandonment threshold in hours
     */
    public long getAbandonmentThresholdHours() {
        return ABANDONMENT_THRESHOLD_HOURS;
    }

    /**
     * Get abandonment fee amount
     */
    public double getAbandonmentFee() {
        return ((AbandonedPayment) abandonedPaymentStrategy).getAbandonmentFee();
    }
}