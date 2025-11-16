// package com.concordia.velocity.seed;

// import com.concordia.velocity.model.Trip;
// import com.google.cloud.Timestamp;
// import com.google.cloud.firestore.Firestore;
// import com.google.firebase.cloud.FirestoreClient;
// import org.springframework.boot.CommandLineRunner;
// import org.springframework.stereotype.Component;

// import java.time.LocalDateTime;
// import java.time.ZoneId;
// import java.util.Date;
// import java.util.Random;
// import java.util.UUID;

// /**
//  * Seeds ONLY extra trips for 4 real riders:
//  *  - Entry  : Andrew  (FKBe9WL1VGPFQADp5a8UaWf6dKu2)
//  *  - Bronze : Jennifer(0R7jIrcXu9PLACdGOQcVGK7R48U2)
//  *  - Silver : Tri     (SusrVlPszETEze5baaFrkd4LQ333)
//  *  - Gold   : Linh    (j5HjgyTlgwVzFQGQ3wdSR4zLFOB3)
//  *
//  * Existing data (stations, docks, bikes, trips, riders) is NOT deleted.
//  * This seeder runs ONLY when env var SEED_LOYALTY_DEMO=true.
//  */
// @Component
// public class LoyaltyDemoSeeder implements CommandLineRunner {

//     private final Firestore db = FirestoreClient.getFirestore();
//     private final Random random = new Random();

//     // Rider IDs (from your Firestore)
//     private static final String ENTRY_RIDER_ID   = "FKBe9WL1VGPFQADp5a8UaWf6dKu2";   // Andrew
//     private static final String BRONZE_RIDER_ID  = "0R7jIrcXu9PLACdGOQcVGK7R48U2";   // Jennifer
//     private static final String SILVER_RIDER_ID  = "SusrVlPszETEze5baaFrkd4LQ333";  // Tri
//     private static final String GOLD_RIDER_ID    = "j5HjgyTlgwVzFQGQ3wdSR4zLFOB3";  // Linh

//     @Override
//     public void run(String... args) throws Exception {
//         // Only seed when explicitly enabled
//         if (!"true".equalsIgnoreCase(System.getenv("SEED_LOYALTY_DEMO"))) {
//             System.out.println("[LoyaltyDemoSeeder] SEED_LOYALTY_DEMO != true, skipping seeding.");
//             return;
//         }

//         System.out.println("[LoyaltyDemoSeeder] Starting loyalty demo seeding...");

//         seedEntryRiderTrips();
//         seedBronzeRiderTrips();
//         seedSilverRiderTrips();
//         seedGoldRiderTrips();

//         System.out.println("[LoyaltyDemoSeeder] Seeding complete.");
//     }

//     // ---------- Helpers ----------

//     private Timestamp toTimestamp(LocalDateTime ldt) {
//         return Timestamp.of(Date.from(ldt.atZone(ZoneId.systemDefault()).toInstant()));
//     }

//     private Trip createCompletedTrip(
//             String riderId,
//             LocalDateTime start,
//             int durationMinutes,
//             String bikeId,
//             String bikeType,
//             String startStationId, String startStationName, String startDockId,
//             String endStationId, String endStationName, String endDockId
//     ) {
//         Trip trip = new Trip();

//         String tripId = UUID.randomUUID().toString();
//         trip.setTripId(tripId);
//         trip.setRiderId(riderId);

//         trip.setBikeId(bikeId);
//         trip.setBikeType(bikeType);

//         trip.setStartStationId(startStationId);
//         trip.setStartStationName(startStationName);
//         trip.setStartDockId(startDockId);

//         trip.setEndStationId(endStationId);
//         trip.setEndStationName(endStationName);
//         trip.setEndDockId(endDockId);

//         Timestamp startTs = toTimestamp(start);
//         Timestamp endTs   = toTimestamp(start.plusMinutes(durationMinutes));

//         trip.setStartTime(startTs);
//         trip.setEndTime(endTs);
//         trip.setDurationMinutes((long) durationMinutes);
//         trip.setStatus(Trip.STATUS_COMPLETED);

//         // Bill can be null; your history view already checks for null
//         trip.setBill(null);

//         return trip;
//     }

//     private void saveTrip(Trip trip) throws Exception {
//         db.collection("trips")
//           .document(trip.getTripId())
//           .set(trip)
//           .get(); // wait for write
//     }

//     // ---------- Tier seeding ----------

//     /**
//      * Entry rider: very few trips (or none).
//      * We give Andrew 1 completed trip so he clearly does NOT satisfy Bronze conditions.
//      */
//     private void seedEntryRiderTrips() throws Exception {
//         System.out.println("[LoyaltyDemoSeeder] Seeding Entry rider (Andrew)...");

//         LocalDateTime now = LocalDateTime.now();

//         // One short trip about 2 months ago
//         LocalDateTime start = now.minusMonths(2).withDayOfMonth(10).withHour(9).withMinute(15);

//         Trip t = createCompletedTrip(
//                 ENTRY_RIDER_ID,
//                 start,
//                 8,
//                 "B029", "standard",          // Old Port bike
//                 "S002", "Old Port Station", "D011",
//                 "S006", "Place des Arts Station", "D060"
//         );

//         saveTrip(t);
//     }

//     /**
//      * Bronze rider: over 10 trips in the last year.
//      * We spread 10 trips over the past 10 months.
//      */
//     private void seedBronzeRiderTrips() throws Exception {
//         System.out.println("[LoyaltyDemoSeeder] Seeding Bronze rider (Jennifer)...");

//         LocalDateTime now = LocalDateTime.now();

//         for (int i = 1; i <= 11; i++) {
//             LocalDateTime start = now.minusMonths(i).withDayOfMonth(12).withHour(8).withMinute(30);

//             // Alternate routes between a few stations
//             String bikeId;
//             String startStationId, startStationName, startDockId;
//             String endStationId, endStationName, endDockId;

//             if (i % 2 == 0) {
//                 // S002 Old Port -> S006 Place des Arts
//                 bikeId = "B029"; // you have B029 at Old Port / Place des Arts
//                 startStationId = "S002";
//                 startStationName = "Old Port Station";
//                 startDockId = "D011";
//                 endStationId = "S006";
//                 endStationName = "Place des Arts Station";
//                 endDockId = "D060";
//             } else {
//                 // S003 McGill -> S009 Place Ville Marie
//                 bikeId = "B018"; // from McGill's range
//                 startStationId = "S003";
//                 startStationName = "McGill University Station";
//                 startDockId = "D021";
//                 endStationId = "S009";
//                 endStationName = "Place Ville Marie Station";
//                 endDockId = "D090";
//             }

//             Trip t = createCompletedTrip(
//                     BRONZE_RIDER_ID,
//                     start,
//                     12 + random.nextInt(10), // 12–21 min
//                     bikeId, "standard",
//                     startStationId, startStationName, startDockId,
//                     endStationId, endStationName, endDockId
//             );
//             saveTrip(t);
//         }
//     }

//     /**
//      * Silver rider: at least 5 trips per month for the last 3 months.
//      * We give Tri 6 trips per month for the last 3 months.
//      */
//     private void seedSilverRiderTrips() throws Exception {
//         System.out.println("[LoyaltyDemoSeeder] Seeding Silver rider (Tri)...");

//         LocalDateTime now = LocalDateTime.now();

//         // Last 3 months: 0 = current, 1 = last month, 2 = two months ago
//         for (int monthOffset = 0; monthOffset < 3; monthOffset++) {
//             for (int i = 0; i < 6; i++) {
//                 LocalDateTime base = now.minusMonths(monthOffset)
//                         .withDayOfMonth(5 + i * 3) // 5,8,11,14,17,20-ish
//                         .withHour(9 + (i % 3))
//                         .withMinute(10 + (i * 5) % 50);

//                 // Alternate between a few reasonable routes
//                 String bikeId;
//                 String startStationId, startStationName, startDockId;
//                 String endStationId, endStationName, endDockId;

//                 if (i % 3 == 0) {
//                     // S004 Atwater -> S002 Old Port
//                     bikeId = "B024";
//                     startStationId = "S004";
//                     startStationName = "Atwater Market Station";
//                     startDockId = "D031";
//                     endStationId = "S002";
//                     endStationName = "Old Port Station";
//                     endDockId = "D011";
//                 } else if (i % 3 == 1) {
//                     // S007 Quartier des Spectacles -> S008 Complexe Desjardins
//                     bikeId = "B041";
//                     startStationId = "S007";
//                     startStationName = "Quartier des Spectacles Station";
//                     startDockId = "D063";
//                     endStationId = "S008";
//                     endStationName = "Complexe Desjardins Station";
//                     endDockId = "D078";
//                 } else {
//                     // S003 McGill -> S006 Place des Arts
//                     bikeId = "B015";
//                     startStationId = "S003";
//                     startStationName = "McGill University Station";
//                     startDockId = "D021";
//                     endStationId = "S006";
//                     endStationName = "Place des Arts Station";
//                     endDockId = "D051";
//                 }

//                 Trip t = createCompletedTrip(
//                         SILVER_RIDER_ID,
//                         base,
//                         10 + random.nextInt(15), // 10–24 min
//                         bikeId,
//                         (i % 2 == 0) ? "standard" : "electric",
//                         startStationId, startStationName, startDockId,
//                         endStationId, endStationName, endDockId
//                 );
//                 saveTrip(t);
//             }
//         }
//     }

//     /**
//      * Gold rider: at least 5 trips per week for the last 12 weeks.
//      * We give Linh 5 trips per week for 12 weeks (60 trips).
//      */
//     private void seedGoldRiderTrips() throws Exception {
//         System.out.println("[LoyaltyDemoSeeder] Seeding Gold rider (Linh)...");

//         LocalDateTime now = LocalDateTime.now();

//         for (int weekOffset = 0; weekOffset < 12; weekOffset++) {
//             for (int i = 0; i < 5; i++) {
//                 LocalDateTime start = now
//                         .minusWeeks(weekOffset)
//                         .withHour(7 + i)        // 7,8,9,10,11 AM
//                         .withMinute(15 + i * 3);

//                 // Alternate routes for variety
//                 String bikeId;
//                 String startStationId, startStationName, startDockId;
//                 String endStationId, endStationName, endDockId;

//                 switch (i) {
//                     case 0:
//                         // S009 Place Ville Marie -> S003 McGill
//                         bikeId = "B058";
//                         startStationId = "S009";
//                         startStationName = "Place Ville Marie Station";
//                         startDockId = "D090";
//                         endStationId = "S003";
//                         endStationName = "McGill University Station";
//                         endDockId = "D021";
//                         break;
//                     case 1:
//                         // S010 Square Victoria -> S011 Chinatown
//                         bikeId = "B069";
//                         startStationId = "S010";
//                         startStationName = "Square Victoria Station";
//                         startDockId = "D105";
//                         endStationId = "S011";
//                         endStationName = "Chinatown Station";
//                         endDockId = "D115";
//                         break;
//                     case 2:
//                         // S002 Old Port -> S004 Atwater
//                         bikeId = "B035";
//                         startStationId = "S002";
//                         startStationName = "Old Port Station";
//                         startDockId = "D012";
//                         endStationId = "S004";
//                         endStationName = "Atwater Market Station";
//                         endDockId = "D031";
//                         break;
//                     case 3:
//                         // S006 Place des Arts -> S007 Quartier des Spectacles
//                         bikeId = "B036";
//                         startStationId = "S006";
//                         startStationName = "Place des Arts Station";
//                         startDockId = "D051";
//                         endStationId = "S007";
//                         endStationName = "Quartier des Spectacles Station";
//                         endDockId = "D063";
//                         break;
//                     default:
//                         // S008 Complexe Desjardins -> S009 Place Ville Marie
//                         bikeId = "B049";
//                         startStationId = "S008";
//                         startStationName = "Complexe Desjardins Station";
//                         startDockId = "D078";
//                         endStationId = "S009";
//                         endStationName = "Place Ville Marie Station";
//                         endDockId = "D090";
//                         break;
//                 }

//                 Trip t = createCompletedTrip(
//                         GOLD_RIDER_ID,
//                         start,
//                         8 + random.nextInt(20), // 8–27 min
//                         bikeId,
//                         (weekOffset % 2 == 0 ? "electric" : "standard"),
//                         startStationId, startStationName, startDockId,
//                         endStationId, endStationName, endDockId
//                 );
//                 saveTrip(t);
//             }
//         }
//     }
// }
