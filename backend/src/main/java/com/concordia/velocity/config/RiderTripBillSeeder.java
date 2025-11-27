package com.concordia.velocity.config;

import com.concordia.velocity.model.Bill;
import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.RiderStats;
import com.concordia.velocity.model.Trip;
import com.concordia.velocity.service.IdGeneratorService;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserRecord;
import com.google.firebase.cloud.FirestoreClient;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * Comprehensive Data Seeder for Riders, Trips, and Bills
 * Creates Firebase Auth users + Firestore rider documents
 * Tests all tier transitions and scenarios
 * Uses IdGeneratorService for sequential Trip and Bill IDs
 *
 * Password for ALL accounts: 123456
 * Operator: cimon.sofia@gmail.com
 *
 * To run: ./gradlew bootRun --args='--seed-riders'
 */
// @Component
public class RiderTripBillSeeder {

    private static final String DEFAULT_PASSWORD = "123456";
    private final IdGeneratorService idGeneratorService;

    // Constructor injection for IdGeneratorService
    public RiderTripBillSeeder(IdGeneratorService idGeneratorService) {
        this.idGeneratorService = idGeneratorService;
    }

    @Bean
    CommandLineRunner seedRiderTripBillData() {
        return args -> {
            if (Arrays.asList(args).contains("--seed-riders")) {
                System.out.println("🌱 Starting Rider/Trip/Bill seeding with Firebase Auth...");
                Firestore db = FirestoreClient.getFirestore();
                seedRidersTripsAndBills(db);
                System.out.println("✅ Rider/Trip/Bill seeding complete!");
            }
        };
    }

    private void seedRidersTripsAndBills(Firestore db) throws Exception {
        System.out.println("\n👥 Seeding riders with Firebase Auth + trip history and bills...\n");

        // Seed each test scenario
        seedOperator(db);
        seedNoTierRiders(db);
        seedBronzeQualifiedRiders(db);
        seedBronzeToSilverTransitionRiders(db);
        seedSilverQualifiedRiders(db);
        seedSilverToGoldTransitionRiders(db);
        seedGoldQualifiedRiders(db);
        seedDowngradeScenarios(db);
        seedEdgeCases(db);

        System.out.println("\n📊 Seeding Summary:");
        System.out.println("   • 15+ riders with Firebase Auth accounts");
        System.out.println("   • 200+ trips spanning last 12 months");
        System.out.println("   • 200+ bills with realistic pricing");
        System.out.println("   • All tier transitions testable");
        System.out.println("   • Password for all accounts: " + DEFAULT_PASSWORD);
        System.out.println("   • Operator account: cimon.sofia@gmail.com");
    }

    // ==================== OPERATOR ====================

    private void seedOperator(Firestore db) throws Exception {
        System.out.println("🔧 Creating operator account...");

        Rider operator = createRiderWithAuth(
                "cimon.sofia@gmail.com",
                "Sofia",
                "Cimon",
                "123 Admin Street, Montreal",
                "514-555-0001",
                db
        );
        operator.setIsOperator(true);

        saveRider(db, operator);
        System.out.println("   ✓ Operator: cimon.sofia@gmail.com (password: 123456)");
        System.out.println("   ✓ isOperator = true set in Firestore\n");
    }

    // ==================== NO TIER SCENARIOS ====================

    private void seedNoTierRiders(Firestore db) throws Exception {
        System.out.println("🆕 Creating NoTier riders...");

        // New rider with no trips
        Rider newRider = createRiderWithAuth(
                "new.user@test.com",
                "New",
                "User",
                "456 New St, Montreal",
                "514-555-0100",
                db
        );
        System.out.println("   ✓ new.user@test.com - Brand new user (0 trips)");

        // Rider with only 5 trips (not enough for Bronze)
        Rider fewTrips = createRiderWithAuth(
                "few.trips@test.com",
                "Few",
                "Trips",
                "789 Trial St, Montreal",
                "514-555-0101",
                db
        );
        createCompletedTrips(db, fewTrips, 5, 30, 60);
        System.out.println("   ✓ few.trips@test.com - Only 5 trips (needs 11+ for Bronze)");

        // Rider with missed reservation (blocks Bronze)
        Rider missedRes = createRiderWithAuth(
                "missed.reservation@test.com",
                "Missed",
                "Reservation",
                "101 Miss St, Montreal",
                "514-555-0102",
                db
        );
        createCompletedTrips(db, missedRes, 15, 90, 180);
        addMissedReservation(missedRes);
        saveRider(db, missedRes);
        System.out.println("   ✓ missed.reservation@test.com - 15 trips but 1 missed reservation (blocks Bronze)\n");
    }

    // ==================== BRONZE TIER ====================

    private void seedBronzeQualifiedRiders(Firestore db) throws Exception {
        System.out.println("🥉 Creating Bronze-qualified riders...");

        // Perfect Bronze: exactly meets requirements
        Rider bronze1 = createRiderWithAuth(
                "bronze.basic@test.com",
                "Bronze",
                "Basic",
                "200 Bronze Ave, Montreal",
                "514-555-0200",
                db
        );
        createCompletedTrips(db, bronze1, 11, 180, 365);
        System.out.println("   ✓ bronze.basic@test.com - Exactly 11 trips (just qualified)");

        // Solid Bronze: well above minimum
        Rider bronze2 = createRiderWithAuth(
                "bronze.solid@test.com",
                "Bronze",
                "Solid",
                "201 Bronze Ave, Montreal",
                "514-555-0201",
                db
        );
        createCompletedTrips(db, bronze2, 20, 180, 365);
        System.out.println("   ✓ bronze.solid@test.com - 20 trips (solid Bronze)\n");
    }

    private void seedBronzeToSilverTransitionRiders(Firestore db) throws Exception {
        System.out.println("🥈 Creating Bronze→Silver transition riders...");

        // Almost Silver: needs 1 more trip this month
        Rider almostSilver = createRiderWithAuth(
                "almost.silver@test.com",
                "Almost",
                "Silver",
                "300 Silver Blvd, Montreal",
                "514-555-0300",
                db
        );
        createMonthlyTrips(db, almostSilver, new int[]{5, 6, 6, 3, 2, 1, 0, 0, 0, 0, 0, 0});
        System.out.println("   ✓ almost.silver@test.com - Needs 1 more trip THIS month for Silver");

        // Ready for Silver: will upgrade on next evaluation
        Rider readySilver = createRiderWithAuth(
                "ready.silver@test.com",
                "Ready",
                "Silver",
                "301 Silver Blvd, Montreal",
                "514-555-0301",
                db
        );
        createMonthlyTrips(db, readySilver, new int[]{6, 6, 6, 4, 3, 2, 1, 0, 0, 0, 0, 0});
        System.out.println("   ✓ ready.silver@test.com - Will upgrade to Silver (6/6/6 trips)\n");
    }

    // ==================== SILVER TIER ====================

    private void seedSilverQualifiedRiders(Firestore db) throws Exception {
        System.out.println("🥈 Creating Silver-qualified riders...");

        // Perfect Silver
        Rider silver1 = createRiderWithAuth(
                "silver.perfect@test.com",
                "Silver",
                "Perfect",
                "400 Silver St, Montreal",
                "514-555-0400",
                db
        );
        createMonthlyTrips(db, silver1, new int[]{6, 6, 6, 5, 4, 3, 2, 1, 0, 0, 0, 0});
        System.out.println("   ✓ silver.perfect@test.com - Exactly meets Silver (6/6/6 trips)");

        // Strong Silver
        Rider silver2 = createRiderWithAuth(
                "silver.strong@test.com",
                "Silver",
                "Strong",
                "401 Silver St, Montreal",
                "514-555-0401",
                db
        );
        createMonthlyTrips(db, silver2, new int[]{8, 8, 7, 6, 5, 4, 3, 2, 1, 0, 0, 0});
        System.out.println("   ✓ silver.strong@test.com - Strong Silver (8/8/7 trips)\n");
    }

    private void seedSilverToGoldTransitionRiders(Firestore db) throws Exception {
        System.out.println("🥇 Creating Silver→Gold transition riders...");

        // Almost Gold: needs consistency
        Rider almostGold = createRiderWithAuth(
                "almost.gold@test.com",
                "Almost",
                "Gold",
                "500 Gold Dr, Montreal",
                "514-555-0500",
                db
        );
        // Weeks: 5,6,6,6,6,6,6,6,6,6,6,6 (needs 6+ per week for 12 weeks)
        createWeeklyTrips(db, almostGold, new int[]{5, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6});
        System.out.println("   ✓ almost.gold@test.com - Needs 1 more trip THIS week for Gold");

        // Ready for Gold
        Rider readyGold = createRiderWithAuth(
                "ready.gold@test.com",
                "Ready",
                "Gold",
                "501 Gold Dr, Montreal",
                "514-555-0501",
                db
        );
        createWeeklyTrips(db, readyGold, new int[]{6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6});
        System.out.println("   ✓ ready.gold@test.com - Will upgrade to Gold (6 trips/week for 12 weeks)\n");
    }

    // ==================== GOLD TIER ====================

    private void seedGoldQualifiedRiders(Firestore db) throws Exception {
        System.out.println("🥇 Creating Gold-qualified riders...");

        // Perfect Gold
        Rider gold1 = createRiderWithAuth(
                "gold.perfect@test.com",
                "Gold",
                "Perfect",
                "600 Gold Blvd, Montreal",
                "514-555-0600",
                db
        );
        createWeeklyTrips(db, gold1, new int[]{6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6});
        System.out.println("   ✓ gold.perfect@test.com - Exactly meets Gold (6/week × 12)");

        // Elite Gold
        Rider gold2 = createRiderWithAuth(
                "gold.elite@test.com",
                "Gold",
                "Elite",
                "601 Gold Blvd, Montreal",
                "514-555-0601",
                db
        );
        createWeeklyTrips(db, gold2, new int[]{10, 10, 9, 8, 8, 7, 7, 7, 6, 6, 6, 6});
        System.out.println("   ✓ gold.elite@test.com - Elite Gold (10+ trips some weeks)\n");
    }

    // ==================== DOWNGRADE SCENARIOS ====================

    private void seedDowngradeScenarios(Firestore db) throws Exception {
        System.out.println("⬇️ Creating downgrade scenario riders...");

        // Gold→Silver (stopped weekly consistency)
        Rider goldToSilver = createRiderWithAuth(
                "downgrade.goldtosilver@test.com",
                "Downgrade",
                "GoldToSilver",
                "700 Down St, Montreal",
                "514-555-0700",
                db
        );
        // Was doing 6/week, but last 2 weeks only 4/week
        createWeeklyTrips(db, goldToSilver, new int[]{4, 4, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6});
        System.out.println("   ✓ downgrade.goldtosilver@test.com - Was Gold, will drop to Silver");

        // Silver→Bronze (inconsistent monthly trips)
        Rider silverToBronze = createRiderWithAuth(
                "downgrade.silvertobronze@test.com",
                "Downgrade",
                "SilverToBronze",
                "701 Down St, Montreal",
                "514-555-0701",
                db
        );
        // This month only 3 trips (needs 6+ for 3 months)
        createMonthlyTrips(db, silverToBronze, new int[]{3, 6, 6, 5, 4, 3, 2, 1, 0, 0, 0, 0});
        System.out.println("   ✓ downgrade.silvertobronze@test.com - Was Silver, will drop to Bronze");

        // Any→NoTier (missed reservation)
        Rider tierToNone = createRiderWithAuth(
                "downgrade.tonone@test.com",
                "Downgrade",
                "ToNone",
                "702 Down St, Montreal",
                "514-555-0702",
                db
        );
        createWeeklyTrips(db, tierToNone, new int[]{6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6, 6});
        addMissedReservation(tierToNone);
        saveRider(db, tierToNone);
        System.out.println("   ✓ downgrade.tonone@test.com - Was Gold, will lose all tiers (missed reservation)\n");
    }

    // ==================== EDGE CASES ====================

    private void seedEdgeCases(Firestore db) throws Exception {
        System.out.println("🎯 Creating edge case riders...");

        // Exactly 10 trips (doesn't surpass 10, stays NoTier)
        Rider exactly10 = createRiderWithAuth(
                "edge.exactly10@test.com",
                "Edge",
                "Exactly10",
                "800 Edge Ave, Montreal",
                "514-555-0800",
                db
        );
        createCompletedTrips(db, exactly10, 10, 180, 365);
        System.out.println("   ✓ edge.exactly10@test.com - Exactly 10 trips (needs >10, stays NoTier)");

        // Has unreturned bike (BR-002 fails)
        Rider unreturnedBike = createRiderWithAuth(
                "edge.unreturned@test.com",
                "Edge",
                "Unreturned",
                "801 Edge Ave, Montreal",
                "514-555-0801",
                db
        );
        createCompletedTrips(db, unreturnedBike, 15, 60, 180);
        createActiveTrip(db, unreturnedBike);
        System.out.println("   ✓ edge.unreturned@test.com - 15 trips but has unreturned bike (blocks Bronze)");

        // Inactive user (old trips)
        Rider inactive = createRiderWithAuth(
                "edge.inactive@test.com",
                "Edge",
                "Inactive",
                "802 Edge Ave, Montreal",
                "514-555-0802",
                db
        );
        createCompletedTripsAtOffset(db, inactive, 20, 400, 500);
        System.out.println("   ✓ edge.inactive@test.com - 20 trips but all >13 months old (NoTier)\n");
    }

    // ==================== HELPER METHODS ====================

    /**
     * Creates a Firebase Auth user and corresponding Firestore rider document
     */
    private Rider createRiderWithAuth(String email, String firstName, String lastName,
                                      String address, String phone, Firestore db) throws Exception {
        try {
            // Create Firebase Auth user
            UserRecord.CreateRequest request = new UserRecord.CreateRequest()
                    .setEmail(email)
                    .setPassword(DEFAULT_PASSWORD)
                    .setDisplayName(firstName + " " + lastName)
                    .setEmailVerified(true);

            UserRecord userRecord = FirebaseAuth.getInstance().createUser(request);
            String uid = userRecord.getUid();

            System.out.println("   🔐 Created Auth user: " + email + " (UID: " + uid + ")");

            // Create Firestore rider document
            Rider rider = new Rider(firstName, lastName, address, email, phone);
            rider.setId(uid); // Use Firebase UID as document ID
            rider.setFlexDollars(0);
            rider.setIsOperator(false);

            // Set payment info
            Rider.PaymentInfo paymentInfo = new Rider.PaymentInfo();
            paymentInfo.setHasPaymentMethod(true);
            paymentInfo.setCardNumber("4532123456789000");
            paymentInfo.setCardholderName(firstName + " " + lastName);
            paymentInfo.setExpiryDate("12/25");
            paymentInfo.setCvc("123");
            rider.setPaymentInfo(paymentInfo);

            // Save to Firestore with UID as document ID
            saveRider(db, rider);

            return rider;

        } catch (Exception e) {
            if (e.getMessage().contains("EMAIL_EXISTS") || e.getMessage().contains("already exists")) {
                // User already exists, fetch from Auth
                System.out.println("   ⚠️  Auth user already exists: " + email);
                UserRecord existingUser = FirebaseAuth.getInstance().getUserByEmail(email);

                // Create/update Firestore document
                Rider rider = new Rider(firstName, lastName, address, email, phone);
                rider.setId(existingUser.getUid());
                rider.setFlexDollars(0);
                rider.setIsOperator(false);

                Rider.PaymentInfo paymentInfo = new Rider.PaymentInfo();
                paymentInfo.setHasPaymentMethod(true);
                paymentInfo.setCardNumber("4532123456789000");
                paymentInfo.setCardholderName(firstName + " " + lastName);
                paymentInfo.setExpiryDate("12/25");
                paymentInfo.setCvc("123");
                rider.setPaymentInfo(paymentInfo);

                saveRider(db, rider);
                return rider;
            }
            throw e;
        }
    }

    private void saveRider(Firestore db, Rider rider) throws ExecutionException, InterruptedException {
        Map<String, Object> riderData = new HashMap<>();
        riderData.put("id", rider.getId());
        riderData.put("firstName", rider.getFirstName());
        riderData.put("lastName", rider.getLastName());
        riderData.put("address", rider.getAddress());
        riderData.put("email", rider.getEmail());
        riderData.put("phoneNumber", rider.getPhoneNumber());
        riderData.put("flexDollars", rider.getFlexDollars());
        riderData.put("isOperator", rider.getIsOperator() != null ? rider.getIsOperator() : false);
        riderData.put("isOperatorView", false);
        riderData.put("tier", rider.getTier());
        riderData.put("createdAt", Timestamp.now());

        // Add payment info
        if (rider.getPaymentInfo() != null) {
            Map<String, Object> paymentInfo = new HashMap<>();
            paymentInfo.put("hasPaymentMethod", rider.getPaymentInfo().getHasPaymentMethod());
            paymentInfo.put("cardNumber", rider.getPaymentInfo().getCardNumber());
            paymentInfo.put("cardholderName", rider.getPaymentInfo().getCardholderName());
            paymentInfo.put("expiryDate", rider.getPaymentInfo().getExpiryDate());
            paymentInfo.put("cvc", rider.getPaymentInfo().getCvc());
            riderData.put("paymentInfo", paymentInfo);
        }

        // Add missed reservations if any
        if (rider.getMissedReservationTimestamps() != null && !rider.getMissedReservationTimestamps().isEmpty()) {
            riderData.put("missedReservationTimestamps", rider.getMissedReservationTimestamps());
        }

        db.collection("riders").document(rider.getId()).set(riderData).get();
    }

    private void createCompletedTrips(Firestore db, Rider rider, int count, int minDaysAgo, int maxDaysAgo)
            throws ExecutionException, InterruptedException {
        for (int i = 0; i < count; i++) {
            int daysAgo = minDaysAgo + (int)((maxDaysAgo - minDaysAgo) * Math.random());
            createCompletedTripAtDaysAgo(db, rider, daysAgo);
        }
        // Evaluate tier after creating trips
        evaluateAndSetTier(db, rider);
        saveRider(db, rider);
    }

    private void createCompletedTripsAtOffset(Firestore db, Rider rider, int count, int minDaysAgo, int maxDaysAgo)
            throws ExecutionException, InterruptedException {
        for (int i = 0; i < count; i++) {
            int daysAgo = minDaysAgo + (int)((maxDaysAgo - minDaysAgo) * Math.random());
            createCompletedTripAtDaysAgo(db, rider, daysAgo);
        }
        // Evaluate tier after creating trips
        evaluateAndSetTier(db, rider);
        saveRider(db, rider);
    }

    private void createMonthlyTrips(Firestore db, Rider rider, int[] tripsPerMonth)
            throws ExecutionException, InterruptedException {
        // tripsPerMonth[0] = this month, [1] = last month, etc.
        for (int monthIndex = 0; monthIndex < tripsPerMonth.length && monthIndex < 12; monthIndex++) {
            int trips = tripsPerMonth[monthIndex];
            for (int i = 0; i < trips; i++) {
                // Create trip in that month
                int daysAgo = (monthIndex * 30) + (int)(Math.random() * 30);
                createCompletedTripAtDaysAgo(db, rider, daysAgo);
            }
        }
        // Evaluate tier after creating trips
        evaluateAndSetTier(db, rider);
        saveRider(db, rider);
    }

    private void createWeeklyTrips(Firestore db, Rider rider, int[] tripsPerWeek)
            throws ExecutionException, InterruptedException {
        // tripsPerWeek[0] = this week, [1] = last week, etc.
        for (int weekIndex = 0; weekIndex < tripsPerWeek.length && weekIndex < 12; weekIndex++) {
            int trips = tripsPerWeek[weekIndex];
            for (int i = 0; i < trips; i++) {
                // Create trip in that week
                int daysAgo = (weekIndex * 7) + (int)(Math.random() * 7);
                createCompletedTripAtDaysAgo(db, rider, daysAgo);
            }
        }
        // Evaluate tier after creating trips
        evaluateAndSetTier(db, rider);
        saveRider(db, rider);
    }

    private void createCompletedTripAtDaysAgo(Firestore db, Rider rider, int daysAgo)
            throws ExecutionException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.now().minusDays(daysAgo).minusHours(1);
        LocalDateTime endTime = startTime.plusMinutes(15 + (int)(Math.random() * 45)); // 15-60 min trip

        Trip trip = new Trip();
        trip.setTripId(idGeneratorService.generateTripId());  // Use IdGeneratorService for sequential IDs
        trip.setRiderId(rider.getId());
        trip.setBikeId("B001"); // Use existing bike
        trip.setBikeType(Math.random() > 0.5 ? "standard" : "electric");
        trip.setStartStationId("S001");
        trip.setStartStationName("Downtown Ste-Catherine Station");
        trip.setStartDockId("D001");
        trip.setEndStationId("S003");
        trip.setEndStationName("McGill University Station");
        trip.setEndDockId("D021");
        trip.setStartTime(toTimestamp(startTime));
        trip.setEndTime(toTimestamp(endTime));
        trip.setStatus(Trip.STATUS_COMPLETED);

        long duration = ChronoUnit.MINUTES.between(startTime, endTime);
        trip.setDurationMinutes(duration);

        // Create bill
        Bill bill = createBill(trip, duration);
        trip.setBill(bill);

        // Save
        db.collection("trips").document(trip.getTripId()).set(trip).get();
        db.collection("bills").document(bill.getBillId()).set(bill).get();
    }

    private void createActiveTrip(Firestore db, Rider rider)
            throws ExecutionException, InterruptedException {
        LocalDateTime startTime = LocalDateTime.now().minusHours(2);

        Trip trip = new Trip();
        trip.setTripId(idGeneratorService.generateTripId());  // Use IdGeneratorService for sequential IDs
        trip.setRiderId(rider.getId());
        trip.setBikeId("B002");
        trip.setBikeType("standard");
        trip.setStartStationId("S001");
        trip.setStartStationName("Downtown Ste-Catherine Station");
        trip.setStartDockId("D002");
        trip.setStartTime(toTimestamp(startTime));
        trip.setStatus(Trip.STATUS_ACTIVE); // Not completed = unreturned bike

        db.collection("trips").document(trip.getTripId()).set(trip).get();
    }

    private Bill createBill(Trip trip, long durationMinutes) throws ExecutionException, InterruptedException {
        boolean isElectric = "electric".equals(trip.getBikeType());
        double basePrice = 1.11;
        double perMinute = isElectric ? 0.33 : 0.22;
        double cost = basePrice + (durationMinutes * perMinute);

        Bill bill = new Bill();
        bill.setBillId(idGeneratorService.generateBillId());  // Use IdGeneratorService for sequential IDs
        bill.setTripId(trip.getTripId());
        bill.setRiderId(trip.getRiderId());
        bill.setBaseCost(Math.round(cost * 100.0) / 100.0);
        bill.setCost(Math.round(cost * 100.0) / 100.0);
        bill.setDiscount(0.0);
        bill.calculateTax(0.14975);
        bill.setTotal(Math.round(bill.getTotal() * 100.0) / 100.0);
        bill.setTax(Math.round(bill.getTax() * 100.0) / 100.0);
        bill.setStatus("paid");
        bill.setBillingDate(trip.getEndTime());

        return bill;
    }

    private void addMissedReservation(Rider rider) {
        rider.addMissedReservation();
    }

    private Timestamp toTimestamp(LocalDateTime ldt) {
        return Timestamp.of(java.sql.Timestamp.valueOf(ldt));
    }

    /**
     * Evaluates and sets the tier for a rider based on their trip history
     * This duplicates the logic from LoyaltyStatsService and Rider.evaluateTier()
     * to avoid dependency injection issues in the seeder
     */
    private void evaluateAndSetTier(Firestore db, Rider rider) throws ExecutionException, InterruptedException {
        // Compute stats manually (similar to LoyaltyStatsService.computeStats)
        RiderStats stats = computeRiderStats(db, rider);

        // Evaluate tier (similar to Rider.evaluateTier)
        String oldTier = rider.getTier();
        String newTier = determineEligibleTier(rider, stats);

        if (!oldTier.equals(newTier)) {
            rider.setTier(newTier);
            System.out.println("   📊 Tier evaluated: " + rider.getEmail() + " → " + newTier);
        }
    }

    /**
     * Computes rider statistics from trip history
     */
    private RiderStats computeRiderStats(Firestore db, Rider rider) throws ExecutionException, InterruptedException {
        // Get all trips for this rider
        List<Trip> trips = new ArrayList<>();
        var tripDocs = db.collection("trips")
                .whereEqualTo("riderId", rider.getId())
                .get()
                .get()
                .getDocuments();

        for (var doc : tripDocs) {
            Trip trip = doc.toObject(Trip.class);
            if (trip != null) {
                trips.add(trip);
            }
        }

        RiderStats stats = new RiderStats();

        // Count trips last year
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        int tripsLastYear = 0;
        int successfulClaims = 0;

        for (Trip trip : trips) {
            if (trip.getStartTime() != null) {
                LocalDate tripDate = trip.getStartTime().toSqlTimestamp().toLocalDateTime().toLocalDate();
                String status = trip.getStatus();

                boolean claimed = "completed".equalsIgnoreCase(status) || "active".equalsIgnoreCase(status);

                if (!tripDate.isBefore(oneYearAgo) && claimed) {
                    tripsLastYear++;
                    successfulClaims++;
                }
            }
        }

        stats.setTripsLastYear(tripsLastYear);
        stats.setSuccessfulClaims(successfulClaims);
        stats.setMissedReservations(rider.getMissedReservationsLastYear());
        stats.setReturnedAllBikes(hasReturnedAllBikes(trips));
        stats.setTripsPerMonth(computeTripsPerMonth(trips));
        stats.setTripsPerWeek(computeTripsPerWeek(trips));

        return stats;
    }

    /**
     * Determines the highest tier the rider qualifies for
     */
    private String determineEligibleTier(Rider rider, RiderStats stats) {
        // Check Gold first (highest tier)
        if (qualifiesForGold(rider, stats)) {
            return "Gold";
        }

        // Check Silver
        if (qualifiesForSilver(rider, stats)) {
            return "Silver";
        }

        // Check Bronze
        if (qualifiesForBronze(rider, stats)) {
            return "Bronze";
        }

        // Default to NoTier
        return "NoTier";
    }

    private boolean qualifiesForBronze(Rider rider, RiderStats stats) {
        // BR-001: No missed reservations
        if (stats.getMissedReservations() > 0) {
            return false;
        }

        // BR-002: All bikes returned
        if (!stats.isReturnedAllBikes()) {
            return false;
        }

        // BR-003: >10 trips (i.e., 11+)
        if (stats.getTripsLastYear() <= 10) {
            return false;
        }

        return true;
    }

    private boolean qualifiesForSilver(Rider rider, RiderStats stats) {
        // Must have Bronze first
        if (!qualifiesForBronze(rider, stats)) {
            return false;
        }

        // SL-002: ≥5 successful claims
        if (stats.getSuccessfulClaims() < 5) {
            return false;
        }

        // SL-003: ≥6 trips per month for last 3 months
        List<Integer> tripsPerMonth = stats.getTripsPerMonth();
        if (tripsPerMonth == null || tripsPerMonth.size() < 3) {
            return false;
        }

        for (int i = 0; i < 3; i++) {
            if (tripsPerMonth.get(i) < 6) {
                return false;
            }
        }

        return true;
    }

    private boolean qualifiesForGold(Rider rider, RiderStats stats) {
        // Must have Silver first
        if (!qualifiesForSilver(rider, stats)) {
            return false;
        }

        // GL-002: ≥6 trips per week for last 12 weeks
        List<Integer> tripsPerWeek = stats.getTripsPerWeek();
        if (tripsPerWeek == null || tripsPerWeek.size() < 12) {
            return false;
        }

        for (int i = 0; i < 12; i++) {
            if (tripsPerWeek.get(i) < 6) {
                return false;
            }
        }

        return true;
    }

    private boolean hasReturnedAllBikes(List<Trip> trips) {
        for (Trip trip : trips) {
            if ("active".equalsIgnoreCase(trip.getStatus())) {
                return false;
            }
        }
        return true;
    }

    private List<Integer> computeTripsPerMonth(List<Trip> trips) {
        List<Integer> months = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            months.add(0);
        }

        LocalDate today = LocalDate.now();

        for (Trip trip : trips) {
            String status = trip.getStatus();
            if (!"completed".equalsIgnoreCase(status) && !"active".equalsIgnoreCase(status)) {
                continue;
            }

            if (trip.getStartTime() == null) {
                continue;
            }

            LocalDate date = trip.getStartTime().toSqlTimestamp().toLocalDateTime().toLocalDate();
            int diff = (today.getYear() - date.getYear()) * 12 + (today.getMonthValue() - date.getMonthValue());

            if (diff >= 0 && diff < 12) {
                months.set(diff, months.get(diff) + 1);
            }
        }

        return months;
    }

    private List<Integer> computeTripsPerWeek(List<Trip> trips) {
        List<Integer> weeks = new ArrayList<>();
        for (int i = 0; i < 12; i++) {
            weeks.add(0);
        }

        LocalDate today = LocalDate.now();
        java.time.temporal.WeekFields wf = java.time.temporal.WeekFields.of(java.util.Locale.getDefault());
        int currentWeek = today.get(wf.weekOfWeekBasedYear());
        int currentYear = today.getYear();

        for (Trip trip : trips) {
            String status = trip.getStatus();
            if (!"completed".equalsIgnoreCase(status) && !"active".equalsIgnoreCase(status)) {
                continue;
            }

            if (trip.getStartTime() == null) {
                continue;
            }

            LocalDate date = trip.getStartTime().toSqlTimestamp().toLocalDateTime().toLocalDate();
            int tripWeek = date.get(wf.weekOfWeekBasedYear());
            int tripYear = date.getYear();

            int diff = computeWeekDifference(currentYear, currentWeek, tripYear, tripWeek);

            if (diff >= 0 && diff < 12) {
                weeks.set(diff, weeks.get(diff) + 1);
            }
        }

        return weeks;
    }

    private int computeWeekDifference(int currentYear, int currentWeek, int tripYear, int tripWeek) {
        if (tripYear == currentYear) {
            return currentWeek - tripWeek;
        }
        if (tripYear == currentYear - 1) {
            return currentWeek + (52 - tripWeek);
        }
        return Integer.MAX_VALUE;
    }
}