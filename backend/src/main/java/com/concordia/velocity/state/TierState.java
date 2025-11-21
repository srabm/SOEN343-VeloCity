package com.concordia.velocity.state;
import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.RiderStats;

public interface TierState {
    public void evaluateTier(Rider rider, RiderStats riderStats);
    public double applyDiscount(double price);
    public int getExtraHoldMinutes();
}
