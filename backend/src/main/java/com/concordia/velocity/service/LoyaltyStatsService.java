package com.concordia.velocity.service;

import com.concordia.velocity.model.RiderStats;
import com.concordia.velocity.model.Trip;
import org.springframework.stereotype.Service;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.Firestore;
import com.google.cloud.firestore.QueryDocumentSnapshot;
import com.google.firebase.cloud.FirestoreClient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.lang.InterruptedException;

/**
 * LoyaltyStatsService:
 * ---------------------
 * This service computes ALL historical metrics required by the Loyalty Tier
 * System.
 *
 * It does NOT store anything in Firestore.
 * It simply reads:
 * - all trips belonging to a given rider
 * and transforms this raw trip data into a RiderStats object.
 *
 * TierState classes (EntryState, BronzeState, SilverState, GoldState)
 * will call RiderStats helper methods to validate eligibility.
 */
@Service
public class LoyaltyStatsService {

    private final Firestore db = FirestoreClient.getFirestore();

    public LoyaltyStatsService() {
    }

    public RiderStats computeStats(String riderId) throws ExecutionException, InterruptedException {
        List<Trip> trips = loadRiderTrips(riderId);

        RiderStats stats = new RiderStats();

        stats.setTripsLastYear(countTripsLastYear(trips));
        stats.setMissedReservations(countMissedReservations(trips));
        stats.setSuccessfulClaims(countSuccessfulClaims(trips));
        stats.setReturnedAllBikes(hasReturnedAllBikes(trips));

        stats.setTripsPerMonth(computeTripsPerMonth(trips));
        stats.setTripsPerWeek(computeTripsPerWeek(trips));

        return stats;
    }

    private boolean hasReturnedAllBikes(List<Trip> trips) {
        // Check if rider has any active (unreturned) trips
        for (Trip trip : trips) {
            if ("active".equalsIgnoreCase(trip.getStatus())) {
                return false; // Has unreturned bike
            }
        }
        return true; // All bikes returned
    }

    // load trips directly from Firestore
    private List<Trip> loadRiderTrips(String riderId) throws ExecutionException, InterruptedException {
        var docs = db.collection("trips")
                .whereEqualTo("riderId", riderId)
                .get()
                .get()
                .getDocuments();

        List<Trip> trips = new ArrayList<>();
        for (QueryDocumentSnapshot doc : docs) {
            Trip t = doc.toObject(Trip.class);
            if (t != null)
                trips.add(t);
        }
        return trips;
    }

    // ---------------------------------------------------------------------
    // TIMESTAMP HELPERS
    // ---------------------------------------------------------------------

    private LocalDate getDate(Timestamp ts) { // converts Firestore timestamp to LocalDate
        if (ts == null)
            return null;
        return ts.toSqlTimestamp().toLocalDateTime().toLocalDate();
    }

    private LocalDateTime getDateTime(Timestamp ts) { // converts Firestore timestamp to LocalDateTime
        if (ts == null)
            return null;
        return ts.toSqlTimestamp().toLocalDateTime();
    }

    // ---------------------------------------------------------------------
    // STAT COUNTERS
    // ---------------------------------------------------------------------

    private int countTripsLastYear(List<Trip> trips) {
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        int count = 0;

        for (Trip trip : trips) {
            LocalDate date = getDate(trip.getStartTime());
            // Only count completed trips
            if (date != null && date.isAfter(oneYearAgo)
                    && "completed".equalsIgnoreCase(trip.getStatus())) {
                count++;
            }
        }
        return count;
    }

    private int countMissedReservations(List<Trip> trips) {
        int count = 0;
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);

        for (Trip trip : trips) {
            String s = trip.getStatus();
            if ("missed".equalsIgnoreCase(s) || "expired".equalsIgnoreCase(s)) {
                LocalDate date = getDate(trip.getStartTime());
                if (date != null && date.isAfter(oneYearAgo)) {
                    count++;
                }
            }
        }
        return count;
    }

    private int countSuccessfulClaims(List<Trip> trips) {
        int count = 0;
        LocalDate oneYearAgo = LocalDate.now().minusYears(1);

        for (Trip trip : trips) {
            LocalDate date = getDate(trip.getStartTime());
            String status = trip.getStatus();

            // Count trips that were started (claimed) within last year
            // Exclude missed/expired reservations
            if (date != null && date.isAfter(oneYearAgo)
                    && !"missed".equalsIgnoreCase(status)
                    && !"expired".equalsIgnoreCase(status)) {
                count++;
            }
        }
        return count;
    }

    // ---------------------------------------------------------------------
    // MONTHLY TRIP DISTRIBUTION (12 LAST MONTHS)
    // ---------------------------------------------------------------------

    /**
     * Returns list of size 12: tripsPerMonth[i]
     * i = 0 means THIS MONTH
     * i = 1 means last month
     * i = 11 means 12 months ago
     *
     * Used for Silver requirement:
     * - “5 trips per month for last 3 months”
     */
    private List<Integer> computeTripsPerMonth(List<Trip> trips) {
        List<Integer> months = new ArrayList<>();
        for (int i = 0; i < 12; i++)
            months.add(0);

        LocalDate today = LocalDate.now();

        for (Trip trip : trips) {
            // Only count completed trips
            if (!"completed".equalsIgnoreCase(trip.getStatus()))
                continue;

            LocalDate date = getDate(trip.getStartTime());
            if (date == null)
                continue;

            int diff = (today.getYear() - date.getYear()) * 12 +
                    (today.getMonthValue() - date.getMonthValue());

            if (diff >= 0 && diff < 12) {
                months.set(diff, months.get(diff) + 1);
            }
        }
        return months;
    }

    // ---------------------------------------------------------------------
    // WEEKLY TRIP DISTRIBUTION (LAST 12 WEEKS)
    // ---------------------------------------------------------------------

    /**
     * Computes trips for last 12 weeks.
     * Used for Gold requirement:
     * - “5 trips per week for last 12 weeks”
     */
    private List<Integer> computeTripsPerWeek(List<Trip> trips) {
        List<Integer> weeks = new ArrayList<>();
        for (int i = 0; i < 12; i++)
            weeks.add(0);

        LocalDate today = LocalDate.now();
        WeekFields wf = WeekFields.of(Locale.getDefault());
        int currentWeek = today.get(wf.weekOfWeekBasedYear());
        int currentYear = today.getYear();

        for (Trip trip : trips) {
            // Only count completed trips
            if (!"completed".equalsIgnoreCase(trip.getStatus()))
                continue;

            LocalDate date = getDate(trip.getStartTime());
            if (date == null)
                continue;

            int tripWeek = date.get(wf.weekOfWeekBasedYear());
            int tripYear = date.getYear();

            int diff = computeWeekDifference(currentYear, currentWeek, tripYear, tripWeek);

            if (diff >= 0 && diff < 12) {
                weeks.set(diff, weeks.get(diff) + 1);
            }
        }
        return weeks;
    }

    /**
     * Computes week difference between current week and trip week because we need
     * to know how many weeks ago a trip happened. For gold tier,
     * it requires that the user has 5 trips every week for the last 12 weeks. It
     * handles the case where the trip was in this current year, in
     * the previous year, or older than that.
     * Returns:
     * 0 → this week
     * 1 → last week
     * ...
     * 11 → 11 weeks ago
     * MAX_INT → older than 12 weeks
     * For example, storing tripsPerWeek[0] = number of trips this week,
     * tripsPerWeek[1] = number of trips last week, etc.
     */
    private int computeWeekDifference(int currentYear, int currentWeek, int tripYear, int tripWeek) {
        if (tripYear == currentYear) { // Same year
            return currentWeek - tripWeek;
        }
        if (tripYear == currentYear - 1) { // Previous year
            return currentWeek + (52 - tripWeek);
        }
        return Integer.MAX_VALUE; // Older than 1 year, ignore
    }
}
