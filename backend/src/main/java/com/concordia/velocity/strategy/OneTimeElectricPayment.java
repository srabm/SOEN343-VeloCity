package com.concordia.velocity.strategy;

import com.concordia.velocity.model.Bill;
import com.concordia.velocity.model.Trip;

import java.util.UUID;

/**
 * Payment strategy for electric bikes
 * Pricing: $1.11 base + $0.33 per minute
 */
public class OneTimeElectricPayment implements PaymentStrategy {

    private static final double BASE_PRICE = 1.11;
    private static final double PRICE_PER_MINUTE = 0.33;
    private static final double TAX_RATE = 0.14975;  // 13% tax

    public Bill createBillAndProcessPayment(Trip trip, long durationMinutes) {
        // Calculate cost
        double cost = BASE_PRICE + (durationMinutes * PRICE_PER_MINUTE);
        cost = Math.round(cost * 100.0) / 100.0;

        // Create bill
        String billId = UUID.randomUUID().toString();
        Bill bill = new Bill(billId, trip.getTripId(), trip.getRiderId(), cost, 0, 0);

        // Calculate tax and total
        bill.calculateTax(TAX_RATE);

        // Round total and tax to 2 decimal places
        bill.setTotal(Math.round(bill.getTotal() * 100.0) / 100.0);
        bill.setTax(Math.round(bill.getTax() * 100.0) / 100.0);

        // process bill payment here --> get user credit card and charge
        bill.setStatus("paid");

        return bill;
    }


    @Override
    public String getStrategyName() {
        return "One-Time Electric Payment";
    }

    /**
     * Gets the base price for this strategy
     */
    public double getBasePrice() {
        return BASE_PRICE;
    }

    /**
     * Gets the per-minute rate for this strategy
     */
    public double getPricePerMinute() {
        return PRICE_PER_MINUTE;
    }

    /**
     * Gets the tax rate for this strategy
     */
    public double getTaxRate() {
        return TAX_RATE;
    }

    @Override
    public String toString() {
        return String.format("OneTimeElectricPayment{base=$%.2f, perMinute=$%.2f, taxRate=%.2f%%}",
                BASE_PRICE, PRICE_PER_MINUTE, TAX_RATE * 100);
    }
}