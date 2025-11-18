package com.concordia.velocity.state;
import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.RiderStats;

// CRITERIAS FOR BRONZE TIER:
// - BR-001: Rider has to have no missed reservations within the last year.
// - BR-002: Rider returned all bikes that they ever took successfully.
// - BR-003: Rider has surpassed 10 trips in the last year.
// - BR-004: Rider gets 5% discount on trips.

public class BronzeTierState implements TierState {
    private final double discount = 0.05;
    private final int hold = 0;
    // private RiderStats riderStats; //*remove: how do i pass this? should i add it as a parameter in evaluateTier?

     @Override
    public void evaluateTier(Rider rider, RiderStats riderStats) {
        boolean bronzeCriteriaMet = riderStats.hasNoMissedReservations() && riderStats.returnedAllBikes() && riderStats.hasTripsLastYear(11);
        System.out.println("Evaluating Bronze Tier for rider " + rider.getId() + ": " +
                           "No missed reservations: " + riderStats.hasNoMissedReservations() +
                           ", Returned all bikes: " + riderStats.returnedAllBikes() +
                           ", Trips last year >= 11: " + riderStats.hasTripsLastYear(11) +
                           " => Bronze criteria met: " + bronzeCriteriaMet);

        //first check if bronze criteria is still met
        if (!bronzeCriteriaMet) {
            rider.setTierState(new NoTierState()); //*remove: is it gonna be a new object each time we evaluate?? 
            return;
        }

        //if the bronze criteria is still met, check for silver criteria 
        //*remove: i dont think i needed to check if the boolean bronzeCriteriaMet is true again, since if it was false it would have already returned, but better safe than sorry
        if (bronzeCriteriaMet && riderStats.hasSuccessfulClaims(5) && riderStats.hasTripsEveryMonth(6, 3)){
            rider.setTierState(new SilverTierState());
        }
    }

    @Override
    public double applyDiscount(double price) {
        return price - (price * discount);
    }
    @Override
    public int getExtraHoldMinutes() {
        return hold;
    }
}
