package com.concordia.velocity.state;
import com.concordia.velocity.model.RiderStats;
import com.concordia.velocity.model.Rider;

// CRITERIAS FOR GOLD TIER:
// - GL-001: Rider covers Silver tier eligibility.
// - GL-002: Rider surpasses 5 trips every week for the last 3 months.
// - GL-003: Rider gets a 15% discount on trips and an extra 5-minute reservation hold.

public class GoldTierState implements TierState {
    private final double discount = 0.15;
    private final int hold = 5;

  @Override
    public void evaluateTier(Rider rider, RiderStats riderStats) {
        boolean bronzeCriteriaMet = riderStats.hasNoMissedReservations() && riderStats.returnedAllBikes() && riderStats.hasTripsLastYear(11);
        boolean silverCriteriaMet = bronzeCriteriaMet && riderStats.hasSuccessfulClaims(5) && riderStats.hasTripsEveryMonth(5, 3);
        boolean goldCriteriaMet = silverCriteriaMet && riderStats.hasTripsEveryWeek(5, 12);

        if (!bronzeCriteriaMet) {
            rider.setTierState(new NoTierState());
        }
        else if (!silverCriteriaMet) {
            rider.setTierState(new BronzeTierState());
        }
        else if (!goldCriteriaMet) {
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
