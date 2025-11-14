package com.concordia.velocity.state;
import com.concordia.velocity.model.Rider;

public interface TierState {
    public void evaluateTier(Rider rider);
    public double applyDiscount(double price);
    public int getExtraHoldMinutes();
}
