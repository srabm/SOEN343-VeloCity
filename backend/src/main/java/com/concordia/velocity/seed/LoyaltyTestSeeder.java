// package com.concordia.velocity.seed;

// import com.concordia.velocity.model.Trip;
// import com.google.cloud.Timestamp;
// import com.google.cloud.firestore.*;
// import com.google.firebase.cloud.FirestoreClient;
// import com.google.api.core.ApiFuture;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// import java.time.LocalDateTime;
// import java.time.ZoneId;
// import java.util.*;
// import java.util.concurrent.ExecutionException;

// @Component
// public class LoyaltyTestSeeder implements CommandLineRunner {

//     private final Firestore db = FirestoreClient.getFirestore();
//     private final Random random = new Random();

//     // -------- NEW RIDERS FROM YOUR FIREBASE --------
//     private static final String ALPHA   = "mGKdMnL37OgLn7tJpmvDhrsgVBa2";
//     private static final String BETA    = "872xPb5AMohP4OKpA7wGq0UlI7H3";
//     private static final String CHARLIE = "31oTzoYvjCTML1AellyPaC2cPMm1";
//     private static final String DELTA   = "tQfMbbo4rZPtiVVythjfDb583I93";
//     private static final String ECHO    = "o5W3UgYxjYWOWfs67hFgRrExs653";

//     @Override
//     public void run(String... args) throws Exception {
//         if (!"true".equalsIgnoreCase(System.getenv("CLEANUP_LOYALTY_DEMO"))) {
//             System.out.println("[LoyaltyCleanup] CLEANUP_LOYALTY_DEMO != true → Skipping cleanup/reseed.");
//             return;
//         }

//         System.out.println("[LoyaltyCleanup] Starting cleanup & reseed...");

//         deleteTripsFor(ALPHA);
//         deleteTripsFor(BETA);
//         deleteTripsFor(CHARLIE);
//         deleteTripsFor(DELTA);
//         deleteTripsFor(ECHO);

//         seedAlpha();
//         seedBeta();
//         seedCharlie();
//         seedDelta();
//         seedEcho();

//         updateTiers();

//         System.out.println("[LoyaltyCleanup] Cleanup & reseed complete.");
//     }

//     // ------------------- Helpers -------------------

//     private Timestamp ts(LocalDateTime t) {
//         return Timestamp.of(Date.from(t.atZone(ZoneId.systemDefault()).toInstant()));
//     }

//     private Trip trip(
//             String riderId,
//             LocalDateTime start,
//             int mins,
//             String bikeId,
//             String startStationId, String startStationName, String startDockId,
//             String endStationId, String endStationName, String endDockId
//     ) {
//         Trip t = new Trip();
//         String id = UUID.randomUUID().toString();

//         t.setTripId(id);
//         t.setRiderId(riderId);
//         t.setBikeId(bikeId);
//         t.setBikeType("standard");

//         t.setStartStationId(startStationId);
//         t.setStartStationName(startStationName);
//         t.setStartDockId(startDockId);

//         t.setEndStationId(endStationId);
//         t.setEndStationName(endStationName);
//         t.setEndDockId(endDockId);

//         t.setStartTime(ts(start));
//         t.setEndTime(ts(start.plusMinutes(mins)));
//         t.setDurationMinutes((long) mins);
//         t.setStatus(Trip.STATUS_COMPLETED);

//         return t;
//     }

//     private void save(Trip t) throws ExecutionException, InterruptedException {
//         db.collection("trips")
//                 .document(t.getTripId())
//                 .set(t)
//                 .get();
//     }

//     private void deleteTripsFor(String riderId) throws Exception {
//         System.out.println("[LoyaltyCleanup] Deleting trips for rider " + riderId);

//         ApiFuture<QuerySnapshot> query = db.collection("trips")
//                 .whereEqualTo("riderId", riderId)
//                 .get();

//         QuerySnapshot snap = query.get();
//         for (DocumentSnapshot doc : snap.getDocuments()) {
//             doc.getReference().delete();
//         }

//         System.out.println("[LoyaltyCleanup] Deleted " + snap.size() + " trips.");
//     }

//     // ------------------- Reseed Logic -------------------

//     /** Alpha: 1 trip */
//     private void seedAlpha() throws Exception {
//         System.out.println("[LoyaltyCleanup] Reseeding Alpha (1 trip)");
//         LocalDateTime now = LocalDateTime.now();

//         Trip t = trip(
//                 ALPHA,
//                 now.minusDays(5).withHour(9).withMinute(10),
//                 10,
//                 "B111",
//                 "S001", "Downtown Ste-Catherine Station", "D001",
//                 "S001", "Downtown Ste-Catherine Station", "D002"
//         );
//         save(t);
//     }

//     /** Beta: 8 trips in last year */
//     private void seedBeta() throws Exception {
//         System.out.println("[LoyaltyCleanup] Reseeding Beta (8 trips)");
//         LocalDateTime now = LocalDateTime.now();

//         for (int i = 1; i <= 8; i++) {
//             LocalDateTime start = now.minusDays(i * 7)
//                     .withHour(8)
//                     .withMinute(15);

//             Trip t = trip(
//                     BETA,
//                     start,
//                     12 + random.nextInt(10),
//                     "B222",
//                     "S002", "Old Port Station", "D011",
//                     "S002", "Old Port Station", "D012"
//             );
//             save(t);
//         }
//     }

//     /** Charlie: last 3 months → [6 trips, 6 trips, 4 trips] */
//     private void seedCharlie() throws Exception {
//         System.out.println("[LoyaltyCleanup] Reseeding Charlie (just below Silver)");

//         LocalDateTime now = LocalDateTime.now();

//         // This month → 4 trips
//         for (int i = 0; i < 4; i++) {
//             LocalDateTime start = now.minusDays(i * 3)
//                     .withHour(9)
//                     .withMinute(20 + i);

//             Trip t = trip(
//                     CHARLIE,
//                     start,
//                     10 + random.nextInt(12),
//                     "B333",
//                     "S003", "McGill University Station", "D021",
//                     "S003", "McGill University Station", "D022"
//             );
//             save(t);
//         }

//         // Last month → 6 trips
//         for (int i = 0; i < 6; i++) {
//             LocalDateTime start = now.minusMonths(1).minusDays(i * 3)
//                     .withHour(10)
//                     .withMinute(5 + i);

//             Trip t = trip(
//                     CHARLIE,
//                     start,
//                     10 + random.nextInt(12),
//                     "B334",
//                     "S004", "Atwater Market Station", "D031",
//                     "S004", "Atwater Market Station", "D032"
//             );
//             save(t);
//         }

//         // 2 months ago → 6 trips
//         for (int i = 0; i < 6; i++) {
//             LocalDateTime start = now.minusMonths(2).minusDays(i * 3)
//                     .withHour(11)
//                     .withMinute(10 + i);

//             Trip t = trip(
//                     CHARLIE,
//                     start,
//                     11 + random.nextInt(10),
//                     "B335",
//                     "S005", "Plateau Mont-Royal Station", "D041",
//                     "S005", "Plateau Mont-Royal Station", "D042"
//             );
//             save(t);
//         }
//     }

//     /** Delta: 6 trips for weeks 1–11, 4 trips for week 0 */
//     private void seedDelta() throws Exception {
//         System.out.println("[LoyaltyCleanup] Reseeding Delta (just below Gold)");

//         LocalDateTime now = LocalDateTime.now();

//         // Weeks 1–11 → 6 trips each
//         for (int week = 1; week <= 11; week++) {
//             for (int i = 0; i < 6; i++) {
//                 LocalDateTime start = now.minusWeeks(week)
//                         .withHour(9 + (i % 3))
//                         .withMinute(10 + i * 2);

//                 Trip t = trip(
//                         DELTA,
//                         start,
//                         10 + random.nextInt(15),
//                         "B444",
//                         "S006", "Place Des Arts Station", "D051",
//                         "S006", "Place Des Arts Station", "D052"
//                 );
//                 save(t);
//             }
//         }

//         // Week 0 → 4 trips
//         for (int i = 0; i < 4; i++) {
//             LocalDateTime start = now.minusDays(i)
//                     .withHour(8 + i)
//                     .withMinute(10 + i * 3);

//             Trip t = trip(
//                     DELTA,
//                     start,
//                     10 + random.nextInt(12),
//                     "B445",
//                     "S007", "Quartier des Spectacles Station", "D063",
//                     "S007", "Quartier des Spectacles Station", "D064"
//             );
//             save(t);
//         }
//     }

//     /** Echo: full Gold — 6 trips/week for 12 weeks */
//     private void seedEcho() throws Exception {
//         System.out.println("[LoyaltyCleanup] Reseeding Echo (Gold)");

//         LocalDateTime now = LocalDateTime.now();

//         for (int week = 0; week < 12; week++) {
//             for (int i = 0; i < 6; i++) {
//                 LocalDateTime start = now.minusWeeks(week)
//                         .withHour(11 + (i % 3))
//                         .withMinute(5 + i * 4);

//                 Trip t = trip(
//                         ECHO,
//                         start,
//                         12 + random.nextInt(10),
//                         "B555",
//                         "S011", "Chinatown Station", "D115",
//                         "S011", "Chinatown Station", "D116"
//                 );
//                 save(t);
//             }
//         }
//     }

//     /** Set tiers for riders already in tiers */
//     private void updateTiers() throws Exception {
//         setTier(CHARLIE, "Bronze");
//         setTier(DELTA, "Silver");
//         setTier(ECHO, "Gold");
//         setTier(ALPHA, "No");
//         setTier(BETA, "No");
//     }

//     private void setTier(String riderId, String tier) throws Exception {
//         db.collection("riders").document(riderId)
//                 .set(Collections.singletonMap("tier", tier), SetOptions.merge())
//                 .get();
//         db.collection("riders").document(riderId)
//                 .set(Collections.singletonMap("missedReservationsLastYear", 0), SetOptions.merge())
//                 .get();
//     }
// }
