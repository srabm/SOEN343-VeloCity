package com.concordia.velocity.seed;

import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.SetOptions;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

/**
 * Seeder for loyalty tier test riders + trips.
 *
 * Riders:
 *  - srab   (DaImh9saRcXoPtmLFBKB0qlwOCR2)  -> 1 completed trip
 *  - etienne (QaMWHOagIVfdo7Xr8dRb1iDep953) -> 8 completed trips in last year (aim: upgrade to Bronze after 3 more)
 *  - jake   (PRoLKTdC6ZNl5piSpMiO46M5FU42)  -> 4 / 6 / 6 trips in last 3 months (almost Silver)
 *  - mimi   (O8o2VaPPxNPP46XzG3VT0HfCsYN2)  -> 6 trips per week for the last 11 weeks, 5 trips this week, just short of Gold
 *  - sophie (haP0KBuS7oMdyExttjKWO0FBMkA3)  -> 6 trips per week for the last 12 weeks, should already qualify for Gold
 *
 * All trips:
 *  - startStationId = "S001", startStationName = "Downtown Ste-Catherine Station", startDockId = "D001"
 *  - endStationId   = "S002", endStationName   = "Old Port Station",           endDockId   = "D011"
 *  - bikeId = "B001", bikeType = "standard"
 *
 * NOTE: This seeder is idempotent: trip IDs are fixed, so rerunning just overwrites the same docs.
 */
// @Component
// public class LoyaltyTierTestSeeder implements CommandLineRunner {

//     private final Firestore db = FirestoreClient.getFirestore();
//     private final LocalTime DEFAULT_TIME = LocalTime.of(10, 0);

//     @Override
//     public void run(String... args) throws Exception {
//         System.out.println("=== LoyaltyTierTestSeeder: Seeding test riders + trips ===");

//         // Base reference date: now
//         LocalDate today = LocalDate.now();

//         // 1) Upsert riders into BOTH "riders" and "users" collections (so your UserService and Firestore UI both see them)
//         upsertTestRiders();

//         // 2) Seed trips for each rider
//         seedTripsForSrab(today);
//         seedTripsForEtienne(today);
//         seedTripsForJake(today);
//         seedTripsForMimi(today);
//         seedTripsForSophie(today);

//         System.out.println("=== LoyaltyTierTestSeeder: Done seeding loyalty test data ===");
//     }

//     // -------------------------------------------------------------------------
//     //  RIDERS
//     // -------------------------------------------------------------------------

//     private void upsertTestRiders() throws ExecutionException, InterruptedException {
//         // srab
//         upsertRiderInBothCollections(
//                 "DaImh9saRcXoPtmLFBKB0qlwOCR2",
//                 "srab@test.com",
//                 "srab",
//                 "m",
//                 "5144320987"
//         );

//         // etienne
//         upsertRiderInBothCollections(
//                 "QaMWHOagIVfdo7Xr8dRb1iDep953",
//                 "etienne@test.com",
//                 "etienne",
//                 "j",
//                 "5144325969"
//         );

//         // jake
//         upsertRiderInBothCollections(
//                 "PRoLKTdC6ZNl5piSpMiO46M5FU42",
//                 "jake@test.com",
//                 "jake",
//                 "c",
//                 "5142049586"
//         );

//         // mimi
//         upsertRiderInBothCollections(
//                 "O8o2VaPPxNPP46XzG3VT0HfCsYN2",
//                 "mimi@test.com",
//                 "mimi",
//                 "s",
//                 "5140987654"
//         );

//         // sophie
//         upsertRiderInBothCollections(
//                 "haP0KBuS7oMdyExttjKWO0FBMkA3",
//                 "sophie@test.com",
//                 "sophie",
//                 "m",
//                 "5141234567"
//         );
//     }

//     private void upsertRiderInBothCollections(String id,
//                                               String email,
//                                               String firstName,
//                                               String lastName,
//                                               String phoneNumber) throws ExecutionException, InterruptedException {

//         Map<String, Object> rider = new HashMap<>();
//         rider.put("id", id);
//         rider.put("email", email);
//         rider.put("firstName", firstName);
//         rider.put("lastName", lastName);
//         rider.put("phoneNumber", phoneNumber);
//         rider.put("isOperator", false);
//         rider.put("createdAt", Timestamp.now());

//         // Where your app UI probably looks
//         db.collection("riders").document(id).set(rider, SetOptions.merge()).get();
//         // Where UserService currently reads from
//         db.collection("users").document(id).set(rider, SetOptions.merge()).get();

//         System.out.println("Upserted rider " + firstName + " (" + id + ") into 'riders' and 'users'.");
//     }

//     // -------------------------------------------------------------------------
//     //  TRIPS – PER RIDER
//     // -------------------------------------------------------------------------

//     // srab: 1 completed trip in the last year
//     private void seedTripsForSrab(LocalDate today) throws ExecutionException, InterruptedException {
//         String riderId = "DaImh9saRcXoPtmLFBKB0qlwOCR2";

//         LocalDateTime start = today.minusMonths(6).withDayOfMonth(10).atTime(DEFAULT_TIME);
//         createCompletedTrip("srab-trip-1", riderId, start);

//         System.out.println("Seeded trips for srab (1 trip).");
//     }

//     // etienne: 8 completed trips in the last year (you'll simulate 3 more to reach Bronze)
//     private void seedTripsForEtienne(LocalDate today) throws ExecutionException, InterruptedException {
//         String riderId = "QaMWHOagIVfdo7Xr8dRb1iDep953";

//         // 8 trips, spaced every 2 weeks in the past
//         for (int i = 1; i <= 8; i++) {
//             LocalDateTime start = today.minusWeeks(i * 2L).atTime(DEFAULT_TIME.plusMinutes(i * 5L));
//             String tripId = "etienne-trip-" + i;
//             createCompletedTrip(tripId, riderId, start);
//         }

//         System.out.println("Seeded trips for etienne (8 trips).");
//     }

//     // jake:
//     //  - Bronze conditions satisfied (enough trips in last year, no missed, etc.)
//     //  - Almost Silver:
//     //      last 3 months tripsPerMonth = [thisMonth: 4, prev: 6, prev2: 6]
//     //    After you simulate a couple more trips this month, he should reach Silver.
//     private void seedTripsForJake(LocalDate today) throws ExecutionException, InterruptedException {
//         String riderId = "PRoLKTdC6ZNl5piSpMiO46M5FU42";

//         // Month indices relative to today:
//         //   0 = this month
//         //   1 = previous month
//         //   2 = 2 months ago
//         // We'll seed:
//         //   month 0 -> 4 trips
//         //   month 1 -> 6 trips
//         //   month 2 -> 6 trips

//         // helper to seed trips for a specific month offset
//         seedMonthlyTripsForJake(today, riderId, 0, 4, "jake-m0-trip-");
//         seedMonthlyTripsForJake(today, riderId, 1, 6, "jake-m1-trip-");
//         seedMonthlyTripsForJake(today, riderId, 2, 6, "jake-m2-trip-");

//         System.out.println("Seeded trips for jake (4 / 6 / 6 in last 3 months).");
//     }

//     private void seedMonthlyTripsForJake(LocalDate today,
//                                          String riderId,
//                                          int monthOffset,
//                                          int tripCount,
//                                          String tripPrefix) throws ExecutionException, InterruptedException {

//         LocalDate baseMonth = today.minusMonths(monthOffset);
//         int baseDay = Math.min(5, baseMonth.lengthOfMonth());

//         for (int i = 0; i < tripCount; i++) {
//             int day = Math.min(baseDay + (i * 2), baseMonth.lengthOfMonth());
//             LocalDateTime start = baseMonth.withDayOfMonth(day).atTime(DEFAULT_TIME.plusMinutes(i * 3L));
//             String tripId = tripPrefix + (i + 1);
//             createCompletedTrip(tripId, riderId, start);
//         }
//     }

//     // mimi:
//     //  - Covers Silver eligibility (many completed trips in last year, strong monthly distribution)
//     //  - Just short of Gold: last 12 weeks have:
//     //       week 0 (this week): 4 trips
//     //       weeks 1–11:        6 trips per week
//     //    So hasTripsEveryWeek(6, 12) fails only for the current week.
//     private void seedTripsForMimi(LocalDate today) throws ExecutionException, InterruptedException {
//         String riderId = "O8o2VaPPxNPP46XzG3VT0HfCsYN2";

//         for (int weekDiff = 0; weekDiff < 12; weekDiff++) {
//             LocalDate baseDate = today.minusWeeks(weekDiff);
//             int tripsThisWeek = (weekDiff == 0) ? 4 : 6;

//             for (int i = 0; i < tripsThisWeek; i++) {
//                 LocalDate date = baseDate.plusDays(Math.min(i, 3)); // stay within same week
//                 LocalDateTime start = date.atTime(DEFAULT_TIME.plusMinutes(i * 4L));
//                 String tripId = "mimi-w" + weekDiff + "-trip-" + (i + 1);
//                 createCompletedTrip(tripId, riderId, start);
//             }
//         }

//         System.out.println("Seeded trips for mimi (almost Gold: strong weeks, but this week < threshold).");
//     }

//     // sophie:
//     //  - Should ALREADY be Gold:
//     //      last 12 weeks all have 6 trips, no missed reservations, many completed trips.
//     private void seedTripsForSophie(LocalDate today) throws ExecutionException, InterruptedException {
//         String riderId = "haP0KBuS7oMdyExttjKWO0FBMkA3";

//         for (int weekDiff = 0; weekDiff < 12; weekDiff++) {
//             LocalDate baseDate = today.minusWeeks(weekDiff);

//             for (int i = 0; i < 6; i++) {
//                 LocalDate date = baseDate.plusDays(Math.min(i, 3));
//                 LocalDateTime start = date.atTime(DEFAULT_TIME.plusMinutes(i * 5L));
//                 String tripId = "sophie-w" + weekDiff + "-trip-" + (i + 1);
//                 createCompletedTrip(tripId, riderId, start);
//             }
//         }

//         System.out.println("Seeded trips for sophie (should satisfy Gold criteria).");
//     }

//     // -------------------------------------------------------------------------
//     //  TRIP CREATION (shared helper)
//     // -------------------------------------------------------------------------

//     /**
//      * Creates a completed trip document for the given rider at the specified start time.
//      * All trips:
//      *   - bikeId = B001, bikeType = standard
//      *   - S001/D001 -> S002/D011
//      */
//     private void createCompletedTrip(String tripId,
//                                      String riderId,
//                                      LocalDateTime startDateTime) throws ExecutionException, InterruptedException {

//         Map<String, Object> trip = new HashMap<>();

//         trip.put("tripId", tripId);
//         trip.put("riderId", riderId);
//         trip.put("bikeId", "B001");
//         trip.put("bikeType", "standard");

//         trip.put("startStationId", "S001");
//         trip.put("startStationName", "Downtown Ste-Catherine Station");
//         trip.put("startDockId", "D001");

//         trip.put("endStationId", "S002");
//         trip.put("endStationName", "Old Port Station");
//         trip.put("endDockId", "D011");

//         // Start & end times
//         Timestamp startTs = Timestamp.of(java.sql.Timestamp.valueOf(startDateTime));
//         Timestamp endTs = Timestamp.of(java.sql.Timestamp.valueOf(startDateTime.plusMinutes(15)));

//         trip.put("startTime", startTs);
//         trip.put("endTime", endTs);

//         // Simple fixed duration: 15 min
//         trip.put("durationMinutes", 15L);

//         // Status flags
//         trip.put("status", "completed");
//         trip.put("completed", true);
//         trip.put("active", false);

//         db.collection("trips").document(tripId).set(trip, SetOptions.merge()).get();
//     }
// }
