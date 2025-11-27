package com.concordia.velocity.strategy;

import com.concordia.velocity.model.Bill;
import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.Trip;

import java.util.UUID;

/**
 * Payment strategy for abandoned trips (trips active >12 hours)
 * Pricing: Fixed $333.00 fee (no per-minute charges)
 * Note: Tier discounts do NOT apply to abandonment fees
 */
public class AbandonedPayment implements PaymentStrategy {

    private static final double ABANDONMENT_FEE = 333.00;
    private static final double TAX_RATE = 0.14975; // 14.975% tax

    @Override
    public Bill createBillAndProcessPayment(Trip trip, long durationMinutes, Rider rider) {
        // Fixed abandonment fee - no discounts apply
        double cost = ABANDONMENT_FEE; // cost = base cost
        double discount = 0.0; // No tier discounts for abandonment
        double operatorDiscount = 0.0;

        // Round to 2 decimal places
        cost = Math.round(cost * 100.0) / 100.0;

        // Create bill with abandonment fee
        String billId = UUID.randomUUID().toString();
        Bill bill = new Bill(billId, trip.getTripId(), trip.getRiderId(), cost, cost, discount, operatorDiscount, 0, 0);

        // Calculate tax and total
        bill.calculateTax(TAX_RATE);

        // Round total and tax to 2 decimal places
        bill.setTotal(Math.round(bill.getTotal() * 100.0) / 100.0);
        bill.setTax(Math.round(bill.getTax() * 100.0) / 100.0);

        // Mark as paid (charge the user's payment method)
        bill.setStatus("paid");

        System.out.println(String.format(
                "[ABANDONED PAYMENT] Created abandonment bill for rider %s: " +
                        "Fee=$%.2f, Tax=$%.2f, Total=$%.2f (Duration: %d minutes)",
                rider != null ? rider.getFullName() : trip.getRiderId(),
                ABANDONMENT_FEE,
                bill.getTax(),
                bill.getTotal(),
                durationMinutes
        ));

        return bill;
    }

    /**
     * Gets the abandonment fee
     */
    public double getAbandonmentFee() {
        return ABANDONMENT_FEE;
    }

    /**
     * Gets the tax rate
     */
    public double getTaxRate() {
        return TAX_RATE;
    }

    @Override
    public String toString() {
        return String.format("AbandonedPayment{fee=$%.2f, taxRate=%.2f%%}",
                ABANDONMENT_FEE, TAX_RATE * 100);
    }
}