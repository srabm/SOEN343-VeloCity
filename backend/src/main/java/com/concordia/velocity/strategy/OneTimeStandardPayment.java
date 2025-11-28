package com.concordia.velocity.strategy;

import com.concordia.velocity.model.Bill;
import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.Trip;

import java.util.UUID;

/**
 * Payment strategy for standard (non-electric) bikes
 * Pricing: $1.11 base + $0.22 per minute
 */
public class OneTimeStandardPayment implements PaymentStrategy {

    private static final double BASE_PRICE = 1.11;
    private static final double PRICE_PER_MINUTE = 0.22;
    private static final double TAX_RATE = 0.14975; // 13% tax

    public Bill createBillAndProcessPayment(Trip trip, long durationMinutes, Rider rider) {
        // Calculate base cost BEFORE discount
        double baseCost = BASE_PRICE + (durationMinutes * PRICE_PER_MINUTE);
        baseCost = Math.round(baseCost * 100.0) / 100.0;

        // Apply tier discount
        double discount = 0.0;
        double operator_discount = 0.0;
        double finalCost = baseCost;

        if (rider != null) {
            double discountedCost = rider.applyDiscount(baseCost);
            discount = baseCost - discountedCost;
            finalCost = discountedCost;
        }

        if (rider != null) {
            if (rider.getIsOperator()) {
                operator_discount = baseCost * 0.1;
                finalCost -= operator_discount;
            }
        }

        // Round final cost and discount
        finalCost = Math.round(finalCost * 100.0) / 100.0;
        discount = Math.round(discount * 100.0) / 100.0;

        // Create bill
        String billId = UUID.randomUUID().toString();
        Bill bill = new Bill(billId, trip.getTripId(), trip.getRiderId(), baseCost, finalCost, discount, operator_discount,0, 0);

        // Calculate tax and total
        bill.calculateTax(TAX_RATE);

        // Round total and tax to 2 decimal places
        bill.setTotal(Math.round(bill.getTotal() * 100.0) / 100.0);
        bill.setTax(Math.round(bill.getTax() * 100.0) / 100.0);

        // process bill payment here --> get user credit card and charge
        bill.setStatus("paid");

        System.out.println(String.format("Applied %s tier discount: $%.2f off",
                rider != null ? rider.getTier() : "NoTier", discount));

        return bill;
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
        return String.format("OneTimeStandardPayment{base=$%.2f, perMinute=$%.2f, taxRate=%.2f%%}",
                BASE_PRICE, PRICE_PER_MINUTE, TAX_RATE * 100);
    }
}