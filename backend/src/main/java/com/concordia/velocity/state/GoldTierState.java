package com.concordia.velocity.state;

import com.concordia.velocity.model.Rider;

public class GoldTierState implements TierState {
    private final double discount = 0;
    private final int hold = 0;
  @Override
    public void evaluateTier(Rider rider) {
        // No tier to evaluate
    }
    @Override
    public double applyDiscount(double price) {
        return discount;
    }
    @Override
    public int getExtraHoldMinutes() {
        return hold;
    }
}
