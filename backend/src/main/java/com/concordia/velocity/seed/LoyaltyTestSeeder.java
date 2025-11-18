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



// =================================================
// SEEDER FOR 16 RIDERS (4 OF EACH TIER)
// =================================================
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

//     // -------- RIDERS FROM YOUR FIREBASE --------
//     // No-tier (4)
//     private static final String NO1 = "WOHK4G6cHxS6JVx1D03qwCK495j1";
//     private static final String NO2 = "xRjPap1F1LRQT2swa3dWEOIdoVs1";
//     private static final String NO3 = "JHjHWlU7mwVNXS5i3gLB93nEvTG2";
//     private static final String NO4 = "EY3pkv5phdaj7e6wBthcsdNvTDi1";

//     // Bronze (4)
//     private static final String BR1 = "CFifBRO1eYbA5iKpNe3IuPUsqBJ3";
//     private static final String BR2 = "c0lEEmbr3LftZWd6dfE2kW0jUX73";
//     private static final String BR3 = "FibXYsiELZZptKBcdSlS5aUTbpB3";
//     private static final String BR4 = "EidizLFMt3Xn40rMkk0FMdtDdqk1";

//     // Silver (4)
//     private static final String SI1 = "4bvgpVPgYJdWy3DaQfHgERk3PU62";
//     private static final String SI2 = "MlvtJ0be0BPrU6f1y7VHEwkuA6X2";
//     private static final String SI3 = "tY2klmBYt0Uk3CMKUXD5WgqBE533";
//     private static final String SI4 = "WgXbAfTfHEcVFuQFgFitV6bS5i02";

//     // Gold (4)
//     private static final String GO1 = "NYUyjxrnR3T9bNxmxCejGZL21t13";
//     private static final String GO2 = "sk4A6fpiwrYAyPCEwz0pq52i5wl2";
//     private static final String GO3 = "BMQ9jfJbM5MwmFkhvDumIy9adD13";
//     private static final String GO4 = "CrBQNa0NLpc4Ljrr4XZKvMVuNvp1";

//     @Override
//     public void run(String... args) throws Exception {
//         if (!"true".equalsIgnoreCase(System.getenv("CLEANUP_LOYALTY_DEMO"))) {
//             System.out.println("[LoyaltyTestSeeder] CLEANUP_LOYALTY_DEMO != true → Skipping loyalty test reseed.");
//             return;
//         }

//         System.out.println("[LoyaltyTestSeeder] Starting loyalty test reseed...");

//         // Delete previous trips for all 16 riders
//         deleteTripsFor(NO1);
//         deleteTripsFor(NO2);
//         deleteTripsFor(NO3);
//         deleteTripsFor(NO4);

//         deleteTripsFor(BR1);
//         deleteTripsFor(BR2);
//         deleteTripsFor(BR3);
//         deleteTripsFor(BR4);

//         deleteTripsFor(SI1);
//         deleteTripsFor(SI2);
//         deleteTripsFor(SI3);
//         deleteTripsFor(SI4);

//         deleteTripsFor(GO1);
//         deleteTripsFor(GO2);
//         deleteTripsFor(GO3);
//         deleteTripsFor(GO4);

//         // Seed according to tier patterns
//         seedNoTierRiders();
//         seedBronzeRiders();
//         seedSilverRiders();
//         seedGoldRiders();

//         // Explicitly assign tiers in Firestore
//         updateTiers();

//         System.out.println("[LoyaltyTestSeeder] Loyalty test reseed complete.");
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
//           .document(t.getTripId())
//           .set(t)
//           .get();
//     }

//     private void deleteTripsFor(String riderId) throws Exception {
//         System.out.println("[LoyaltyTestSeeder] Deleting trips for rider " + riderId);

//         ApiFuture<QuerySnapshot> query = db.collection("trips")
//                 .whereEqualTo("riderId", riderId)
//                 .get();

//         QuerySnapshot snap = query.get();
//         for (DocumentSnapshot doc : snap.getDocuments()) {
//             doc.getReference().delete();
//         }

//         System.out.println("[LoyaltyTestSeeder] Deleted " + snap.size() + " trips.");
//     }

//     // ------------------- Seeding Logic -------------------

//     /**
//      * No-tier: < 10 trips last year for each rider.
//      */
//     private void seedNoTierRiders() throws Exception {
//         System.out.println("[LoyaltyTestSeeder] Seeding No-tier riders (<10 trips)");

//         LocalDateTime now = LocalDateTime.now();

//         // NO1: 3 trips
//         seedNoTierPattern(NO1, now, 3, "B003");

//         // NO2: 5 trips
//         seedNoTierPattern(NO2, now, 5, "B007");

//         // NO3: 7 trips
//         seedNoTierPattern(NO3, now, 7, "B012");

//         // NO4: 9 trips
//         seedNoTierPattern(NO4, now, 9, "B018");
//     }

//     private void seedNoTierPattern(String riderId, LocalDateTime now, int trips, String bikeId) throws Exception {
//         for (int i = 0; i < trips; i++) {
//             LocalDateTime start = now.minusDays(10 + i * 6)
//                     .withHour(9 + (i % 3))
//                     .withMinute(10 + (i * 3) % 50);

//             // Rotate among a few station pairs, all valid docks:
//             Trip t;
//             switch (i % 3) {
//                 case 0:
//                     t = trip(
//                             riderId,
//                             start,
//                             10 + random.nextInt(10),
//                             bikeId,
//                             "S001", "Downtown Ste-Catherine Station", "D001",
//                             "S002", "Old Port Station", "D011"
//                     );
//                     break;
//                 case 1:
//                     t = trip(
//                             riderId,
//                             start,
//                             10 + random.nextInt(10),
//                             bikeId,
//                             "S003", "McGill University Station", "D021",
//                             "S004", "Atwater Market Station", "D031"
//                     );
//                     break;
//                 default:
//                     t = trip(
//                             riderId,
//                             start,
//                             10 + random.nextInt(10),
//                             bikeId,
//                             "S005", "Plateau Mont-Royal Station", "D041",
//                             "S001", "Downtown Ste-Catherine Station", "D002"
//                     );
//                     break;
//             }
//             save(t);
//         }
//     }


//     /**
//      * Bronze riders:
//      * - 6 trips/month for last 2 months
//      * - 4 trips in current month
//      */
//     private void seedBronzeRiders() throws Exception {
//         System.out.println("[LoyaltyTestSeeder] Seeding Bronze riders (6,6,4 monthly pattern)");

//         LocalDateTime now = LocalDateTime.now();

//         seedBronzePattern(BR1, now, "B024");
//         seedBronzePattern(BR2, now, "B029");
//         seedBronzePattern(BR3, now, "B035");
//         seedBronzePattern(BR4, now, "B041");
//     }

//     private void seedBronzePattern(String riderId, LocalDateTime now, String bikeId) throws Exception {

//         // Current month → 4 trips
//         for (int i = 0; i < 4; i++) {
//             LocalDateTime start = now.minusDays(i * 4)
//                     .withHour(9 + (i % 2))
//                     .withMinute(15 + i * 4);

//             Trip t = trip(
//                     riderId,
//                     start,
//                     10 + random.nextInt(10),
//                     bikeId,
//                     "S002", "Old Port Station", "D012",
//                     "S003", "McGill University Station", "D021"
//             );
//             save(t);
//         }

//         // Previous month → 6 trips
//         for (int i = 0; i < 6; i++) {
//             LocalDateTime start = now.minusMonths(1).minusDays(i * 4)
//                     .withHour(10 + (i % 2))
//                     .withMinute(5 + i * 3);

//             Trip t = trip(
//                     riderId,
//                     start,
//                     10 + random.nextInt(10),
//                     bikeId,
//                     "S004", "Atwater Market Station", "D031",
//                     "S005", "Plateau Mont-Royal Station", "D042"
//             );
//             save(t);
//         }

//         // 2 months ago → 6 trips
//         for (int i = 0; i < 6; i++) {
//             LocalDateTime start = now.minusMonths(2).minusDays(i * 4)
//                     .withHour(11 + (i % 2))
//                     .withMinute(10 + i * 2);

//             Trip t = trip(
//                     riderId,
//                     start,
//                     11 + random.nextInt(10),
//                     bikeId,
//                     "S006", "Place des Arts Station", "D051",
//                     "S007", "Quartier des Spectacles Station", "D063"
//             );
//             save(t);
//         }
//     }


//     /**
//      * Silver riders:
//      * - 6 trips/week for last 11 weeks
//      * - 4 trips in current week
//      */
//     private void seedSilverRiders() throws Exception {
//         System.out.println("[LoyaltyTestSeeder] Seeding Silver riders (6 trips/week × 11 weeks + 4 this week)");

//         LocalDateTime now = LocalDateTime.now();

//         seedSilverPattern(SI1, now, "B049");
//         seedSilverPattern(SI2, now, "B056");
//         seedSilverPattern(SI3, now, "B063");
//         seedSilverPattern(SI4, now, "B072");
//     }

//     private void seedSilverPattern(String riderId, LocalDateTime now, String bikeId) throws Exception {

//         // Current week → 4 trips
//         for (int i = 0; i < 4; i++) {
//             LocalDateTime start = now.minusDays(i)
//                     .withHour(9 + (i % 2))
//                     .withMinute(10 + i * 3);

//             Trip t = trip(
//                     riderId,
//                     start,
//                     10 + random.nextInt(10),
//                     bikeId,
//                     "S007", "Quartier des Spectacles Station", "D063",
//                     "S008", "Complexe Desjardins Station", "D078"
//             );
//             save(t);
//         }

//         // Weeks 1–11 → 6 trips/week
//         for (int week = 1; week <= 11; week++) {
//             for (int i = 0; i < 6; i++) {
//                 LocalDateTime start = now.minusWeeks(week)
//                         .withHour(8 + (i % 3))
//                         .withMinute(15 + i * 4);

//                 Trip t;

//                 switch (i % 3) {
//                     case 0:
//                         t = trip(
//                                 riderId,
//                                 start,
//                                 10 + random.nextInt(10),
//                                 bikeId,
//                                 "S008", "Complexe Desjardins Station", "D079",
//                                 "S009", "Place Ville Marie Station", "D090"
//                         );
//                         break;
//                     case 1:
//                         t = trip(
//                                 riderId,
//                                 start,
//                                 10 + random.nextInt(10),
//                                 bikeId,
//                                 "S009", "Place Ville Marie Station", "D095",
//                                 "S010", "Square Victoria Station", "D105"
//                         );
//                         break;
//                     default:
//                         t = trip(
//                                 riderId,
//                                 start,
//                                 10 + random.nextInt(10),
//                                 bikeId,
//                                 "S011", "Chinatown Station", "D115",
//                                 "S007", "Quartier des Spectacles Station", "D063"
//                         );
//                         break;
//                 }

//                 save(t);
//             }
//         }
//     }


//     /**
//      * Gold riders:
//      * - 6 trips/week for last 12 weeks
//      * → satisfies Gold's 6 trips per week for 12 weeks.
//      */
//     private void seedGoldRiders() throws Exception {
//         System.out.println("[LoyaltyTestSeeder] Seeding Gold riders (6 trips/week × 12 weeks)");

//         LocalDateTime now = LocalDateTime.now();

//         seedGoldPattern(GO1, now, "B001");
//         seedGoldPattern(GO2, now, "B010");
//         seedGoldPattern(GO3, now, "B020");
//         seedGoldPattern(GO4, now, "B030");
//     }

//     private void seedGoldPattern(String riderId, LocalDateTime now, String bikeId) throws Exception {
//         for (int week = 0; week < 12; week++) {
//             for (int i = 0; i < 6; i++) {
//                 LocalDateTime start = now.minusWeeks(week)
//                         .withHour(7 + (i % 4))
//                         .withMinute(10 + i * 4);

//                 Trip t;
//                 switch (i % 4) {
//                     case 0:
//                         t = trip(
//                                 riderId,
//                                 start,
//                                 10 + random.nextInt(12),
//                                 bikeId,
//                                 "S001", "Downtown Ste-Catherine Station", "D003",
//                                 "S002", "Old Port Station", "D013"
//                         );
//                         break;
//                     case 1:
//                         t = trip(
//                                 riderId,
//                                 start,
//                                 10 + random.nextInt(12),
//                                 bikeId,
//                                 "S003", "McGill University Station", "D022",
//                                 "S006", "Place des Arts Station", "D052"
//                         );
//                         break;
//                     case 2:
//                         t = trip(
//                                 riderId,
//                                 start,
//                                 10 + random.nextInt(12),
//                                 bikeId,
//                                 "S007", "Quartier des Spectacles Station", "D070",
//                                 "S008", "Complexe Desjardins Station", "D085"
//                         );
//                         break;
//                     case 3:
//                         t = trip(
//                                 riderId,
//                                 start,
//                                 10 + random.nextInt(12),
//                                 bikeId,
//                                 "S009", "Place Ville Marie Station", "D095",
//                                 "S010", "Square Victoria Station", "D110"
//                         );
//                         break;
//                     default:
//                         t = trip(
//                                 riderId,
//                                 start,
//                                 10 + random.nextInt(12),
//                                 bikeId,
//                                 "S011", "Chinatown Station", "D120",
//                                 "S001", "Downtown Ste-Catherine Station", "D004"
//                         );
//                         break;
//                 }
//                 save(t);
//             }
//         }
//     }

//     // ------------------- Explicit Tier Assignment -------------------

//     private void updateTiers() throws Exception {
//         System.out.println("[LoyaltyTestSeeder] Setting explicit tiers in Firestore...");

//         // No-tier
//         setTier(NO1, "No");
//         setTier(NO2, "No");
//         setTier(NO3, "No");
//         setTier(NO4, "No");

//         // Bronze
//         setTier(BR1, "Bronze");
//         setTier(BR2, "Bronze");
//         setTier(BR3, "Bronze");
//         setTier(BR4, "Bronze");

//         // Silver
//         setTier(SI1, "Silver");
//         setTier(SI2, "Silver");
//         setTier(SI3, "Silver");
//         setTier(SI4, "Silver");

//         // Gold
//         setTier(GO1, "Gold");
//         setTier(GO2, "Gold");
//         setTier(GO3, "Gold");
//         setTier(GO4, "Gold");
//     }

//     private void setTier(String riderId, String tier) throws Exception {
//         db.collection("riders").document(riderId)
//                 .set(Collections.singletonMap("tier", tier), SetOptions.merge())
//                 .get();
//     }
// }
