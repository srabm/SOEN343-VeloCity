package com.concordia.velocity.seed;

import com.concordia.velocity.model.Trip;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.*;
import com.google.firebase.cloud.FirestoreClient;
import com.google.api.core.ApiFuture;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.ExecutionException;

// @Component
public class LoyaltyCleanupAndReseed implements CommandLineRunner {

    private final Firestore db = FirestoreClient.getFirestore();
    private final Random random = new Random();

    // Rider IDs
    private static final String ANDREW = "FKBe9WL1VGPFQADp5a8UaWf6dKu2";
    private static final String JENN   = "0R7jIrcXu9PLACdGOQcVGK7R48U2";
    private static final String TRI    = "SusrVlPszETEze5baaFrkd4LQ333";
    private static final String LINH   = "j5HjgyTlgwVzFQGQ3wdSR4zLFOB3";

    @Override
    public void run(String... args) throws Exception {
        if (!"true".equalsIgnoreCase(System.getenv("CLEANUP_LOYALTY_DEMO"))) {
            System.out.println("[LoyaltyCleanup] CLEANUP_LOYALTY_DEMO != true → Skipping cleanup/reseed.");
            return;
        }

        System.out.println("[LoyaltyCleanup] Starting cleanup & reseed...");

        deleteTripsFor(ANDREW);
        deleteTripsFor(JENN);
        deleteTripsFor(TRI);
        deleteTripsFor(LINH);

        seedAndrew();
        seedJennifer();
        seedTri();
        seedLinh();

        System.out.println("[LoyaltyCleanup] Cleanup & reseed complete.");
    }

    // ------------------- Helpers -------------------

    private Timestamp ts(LocalDateTime t) {
        return Timestamp.of(Date.from(t.atZone(ZoneId.systemDefault()).toInstant()));
    }

    private Trip trip(
            String riderId,
            LocalDateTime start,
            int mins,
            String bikeId,
            String startStationId, String startStationName, String startDockId,
            String endStationId, String endStationName, String endDockId
    ) {
        Trip t = new Trip();
        String id = UUID.randomUUID().toString();

        t.setTripId(id);
        t.setRiderId(riderId);
        t.setBikeId(bikeId);
        t.setBikeType("standard"); // you can edit if needed

        t.setStartStationId(startStationId);
        t.setStartStationName(startStationName);
        t.setStartDockId(startDockId);

        t.setEndStationId(endStationId);
        t.setEndStationName(endStationName);
        t.setEndDockId(endDockId);

        t.setStartTime(ts(start));
        t.setEndTime(ts(start.plusMinutes(mins)));
        t.setDurationMinutes((long) mins);
        t.setStatus(Trip.STATUS_COMPLETED);
        t.setBill(null);

        return t;
    }

    private void save(Trip t) throws ExecutionException, InterruptedException {
        db.collection("trips")
          .document(t.getTripId())
          .set(t)
          .get();
    }

    private void deleteTripsFor(String riderId) throws Exception {
        System.out.println("[LoyaltyCleanup] Deleting trips for rider " + riderId);

        ApiFuture<QuerySnapshot> query = db.collection("trips")
                .whereEqualTo("riderId", riderId)
                .get();

        QuerySnapshot snap = query.get();
        for (DocumentSnapshot doc : snap.getDocuments()) {
            doc.getReference().delete();
        }

        System.out.println("[LoyaltyCleanup] Deleted " + snap.size() + " trips.");
    }

    // ------------------- Reseed Logic -------------------

    /** Andrew: exactly 1 trip */
    private void seedAndrew() throws Exception {
        System.out.println("[LoyaltyCleanup] Reseeding Andrew (1 trip)");
        LocalDateTime now = LocalDateTime.now();

        Trip t = trip(
                ANDREW,
                now.minusMonths(2).withDayOfMonth(10).withHour(9).withMinute(15),
                8,
                "B029",
                "S002", "Old Port Station", "D011",
                "S006", "Place des Arts Station", "D060"
        );
        save(t);
    }

    /** Jennifer: 11 trips in last year */
    private void seedJennifer() throws Exception {
        System.out.println("[LoyaltyCleanup] Reseeding Jennifer (11 trips)");
        LocalDateTime now = LocalDateTime.now();

        for (int i = 1; i <= 11; i++) {
            LocalDateTime start = now.minusMonths(i)
                    .withDayOfMonth(12)
                    .withHour(8)
                    .withMinute(30);

            Trip t;
            if (i % 2 == 0) {
                // S002 → S006
                t = trip(JENN, start, 12 + random.nextInt(10),
                        "B029",
                        "S002", "Old Port Station", "D011",
                        "S006", "Place des Arts Station", "D060");
            } else {
                // S003 → S009
                t = trip(JENN, start, 12 + random.nextInt(10),
                        "B018",
                        "S003", "McGill University Station", "D021",
                        "S009", "Place Ville Marie Station", "D090");
            }
            save(t);
        }
    }

    /** Tri: 6 trips per month for last 3 months */
    private void seedTri() throws Exception {
        System.out.println("[LoyaltyCleanup] Reseeding Tri (18 trips)");
        LocalDateTime now = LocalDateTime.now();

        for (int month = 0; month < 3; month++) {
            for (int i = 0; i < 6; i++) {
                LocalDateTime start = now.minusMonths(month)
                        .withDayOfMonth(5 + i * 3)
                        .withHour(9 + (i % 3))
                        .withMinute(10 + (i * 5) % 50);

                Trip t;
                if (i % 3 == 0) {
                    t = trip(TRI, start, 10 + random.nextInt(15),
                            "B024",
                            "S004", "Atwater Market Station", "D031",
                            "S002", "Old Port Station", "D011");
                } else if (i % 3 == 1) {
                    t = trip(TRI, start, 10 + random.nextInt(15),
                            "B041",
                            "S007", "Quartier des Spectacles Station", "D063",
                            "S008", "Complexe Desjardins Station", "D078");
                } else {
                    t = trip(TRI, start, 10 + random.nextInt(15),
                            "B015",
                            "S003", "McGill University Station", "D021",
                            "S006", "Place des Arts Station", "D051");
                }

                save(t);
            }
        }
    }

    /** Linh: 5 trips per week for last 12 weeks */
    private void seedLinh() throws Exception {
        System.out.println("[LoyaltyCleanup] Reseeding Linh (60 trips)");
        LocalDateTime now = LocalDateTime.now();

        for (int week = 0; week < 12; week++) {
            for (int i = 0; i < 5; i++) {
                LocalDateTime start = now.minusWeeks(week)
                        .withHour(7 + i)
                        .withMinute(15 + i * 3);

                Trip t;
                switch (i) {
                    case 0:
                        t = trip(LINH, start, 8 + random.nextInt(20),
                                "B058",
                                "S009", "Place Ville Marie Station", "D090",
                                "S003", "McGill University Station", "D021");
                        break;
                    case 1:
                        t = trip(LINH, start, 8 + random.nextInt(20),
                                "B069",
                                "S010", "Square Victoria Station", "D105",
                                "S011", "Chinatown Station", "D115");
                        break;
                    case 2:
                        t = trip(LINH, start, 8 + random.nextInt(20),
                                "B035",
                                "S002", "Old Port Station", "D012",
                                "S004", "Atwater Market Station", "D031");
                        break;
                    case 3:
                        t = trip(LINH, start, 8 + random.nextInt(20),
                                "B036",
                                "S006", "Place des Arts Station", "D051",
                                "S007", "Quartier des Spectacles Station", "D063");
                        break;
                    default:
                        t = trip(LINH, start, 8 + random.nextInt(20),
                                "B049",
                                "S008", "Complexe Desjardins Station", "D078",
                                "S009", "Place Ville Marie Station", "D090");
                        break;
                }

                save(t);
            }
        }
    }
}
