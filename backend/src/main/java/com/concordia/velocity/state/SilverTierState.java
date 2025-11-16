package com.concordia.velocity.state;
import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.RiderStats;

// CRITERIAS FOR SILVER TIER:
// - SL-001: Rider covers Bronze tier eligibility.
// - SL-002: Rider has to have at least 5 reservations of bikes that were
// successfully claimed within the last year.
// - SL-003: Rider has surpassed 5 trips per month for the last three months.
// - SL-004: Rider gets a 10% discount on trips and an extra 2-minute
// reservation hold.


public class SilverTierState implements TierState {
    private final double discount = 0.10;
    private final int hold = 2;
    private RiderStats riderStats;

    @Override
    public void evaluateTier(Rider rider, RiderStats riderStats) {
        boolean bronzeCriteriaMet = riderStats.hasNoMissedReservations() && riderStats.returnedAllBikes() && riderStats.hasTripsLastYear(11);
        boolean silverCriteriaMet = bronzeCriteriaMet && riderStats.hasSuccessfulClaims(5) && riderStats.hasTripsEveryMonth(5, 3);

        //off the bat, check if bronzeCriteria is still met, if not, downgrade to no tier
        if (!bronzeCriteriaMet) {
            rider.setTierState(new NoTierState());  
            return;
        }

        //since we know bronze criteria is still met, check if silver criteria is still met
        if (!silverCriteriaMet) {
            rider.setTierState(new BronzeTierState());
            return;
        }

        //since we know both bronze and silver criterias are met, we can test for gold criteria and either upgrade or stay
        if (silverCriteriaMet && riderStats.hasTripsEveryWeek(5, 12)){
            rider.setTierState(new GoldTierState());
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
