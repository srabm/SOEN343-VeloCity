package com.concordia.velocity.state;

import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.RiderStats;

public class NoTierState implements TierState {
    private final double discount = 0;
    private final int hold = 0;
    
    @Override
    public void evaluateTier(Rider rider, RiderStats riderStats) {
        //Check for bronze tier eligibility
         if (riderStats.hasNoMissedReservations() && riderStats.returnedAllBikes() && riderStats.hasTripsLastYear(11)) {
            rider.setTierState(new BronzeTierState()); //is it gonna be a new bronzetierstate each time we evaluate?? 
        }
    }
    @Override
    public double applyDiscount(double price) {
        return discount; //there's no discount in this tier state
    }
    @Override
    public int getExtraHoldMinutes() {
        return hold; //no extra hold minutes in this tier state
    }
}
