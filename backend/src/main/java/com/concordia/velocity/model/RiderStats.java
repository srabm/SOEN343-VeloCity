package com.concordia.velocity.model;

import java.util.List;

/**
 * RiderStats is a computed snapshot of a rider’s historical activity.
 *
 * It is NOT stored in Firestore.
 * It is generated dynamically by LoyaltyStatsService based on:
 * - trip history
 * - reservation history
 * - timestamps
 *
 * TierState implementations (Entry/Bronze/Silver/Gold) use this object
 * to check whether the rider meets the requirements for that tier.
 */
public class RiderStats {

    private int tripsLastYear; // total number of completed trips in the past year, used for Bronze tier (>=10
                               // trips) and inherited in Silver/Gold
    private int missedReservations; // number of reservations the rider did not claim before expiry, used for Bronze
                                    // (must be 0) and inherited in Silver/Gold
    private int successfulClaims; // number of reservations that were successfully claimed, used for Silver (>=5)
                                  // and inherited in Gold

    private List<Integer> tripsPerMonth; // size 12: last 12 months, index = 0 for most recent month, index = 11 for 12
                                         // months ago, used for Silver (5 trips per month for the last 3 months)
    private List<Integer> tripsPerWeek; // size 12: last ~12 weeks, index = 0 for most recent week, index = 11 for 12
                                        // weeks ago, used for Gold (5 trips per week for the last 12 weeks)

    private boolean returnedAllBikes;

    // getters and setters
    public int getTripsLastYear() {
        return tripsLastYear;
    }

    public void setTripsLastYear(int tripsLastYear) {
        this.tripsLastYear = tripsLastYear;
    }

    public int getMissedReservations() {
        return missedReservations;
    }

    public void setMissedReservations(int missedReservations) {
        this.missedReservations = missedReservations;
    }

    public int getSuccessfulClaims() {
        return successfulClaims;
    }

    public void setSuccessfulClaims(int successfulClaims) {
        this.successfulClaims = successfulClaims;
    }

    public List<Integer> getTripsPerMonth() {
        return tripsPerMonth;
    }

    public void setTripsPerMonth(List<Integer> tripsPerMonth) {
        this.tripsPerMonth = tripsPerMonth;
    }

    public List<Integer> getTripsPerWeek() {
        return tripsPerWeek;
    }

    public void setTripsPerWeek(List<Integer> tripsPerWeek) {
        this.tripsPerWeek = tripsPerWeek;
    }

    public boolean isReturnedAllBikes() {
        return returnedAllBikes;
    }

    public void setReturnedAllBikes(boolean returnedAllBikes) {
        this.returnedAllBikes = returnedAllBikes;
    }

    // === Tier evaluation helper methods used by the TierState pattern ===

    public boolean hasNoMissedReservations() { // for Bronze, no missed reservations in the last year
        return missedReservations == 0;
    }

    public boolean returnedAllBikes() { // for Bronze, the rider must have returned all bikes.
        // need to update this method once i define logic in TripService for bike
        // returns
        /**
         *
         * TODO: Implement properly in LoyaltyStatsService:
         * - Check if trip.getStatus() == "completed"
         * - Check that endStationId/endDockId are valid
         */
        return true;
    }

    public boolean hasTripsLastYear(int threshold) { // checks whether the rider meets a minimum number of trips in the
                                                     // last year, for Bronze > 10 (surpasses 10 trips so 11 and more in
                                                     // the last year)
        return tripsLastYear >= threshold;
    }

    public boolean hasSuccessfulClaims(int threshold) { // checks whether rider has enough successful claims, for Silver
                                                        // >=5
        return successfulClaims >= threshold;
    }

    public boolean hasTripsEveryMonth(int minTrips, int months) {
        /**
         * Silver requirement:
         * tripsPerMonth >= minTrips for the last 'months' months.
         * Example:
         * hasTripsEveryMonth(5, 3) → last 3 months have 5 or more trips
         */
        for (int i = 0; i < months; i++) {
            if (tripsPerMonth.get(i) < minTrips)
                return false;
        }
        return true;
    }

    public boolean hasTripsEveryWeek(int minTrips, int weeks) {
        /**
         * Gold requirement:
         * tripsPerWeek >= minTrips for the last 'weeks' weeks.
         * Example:
         * hasTripsEveryWeek(5, 12) → last 12 weeks have 5 or more trips each
         */
        for (int i = 0; i < weeks; i++) {
            if (tripsPerWeek.get(i) < minTrips)
                return false;
        }
        return true;
    }
}
