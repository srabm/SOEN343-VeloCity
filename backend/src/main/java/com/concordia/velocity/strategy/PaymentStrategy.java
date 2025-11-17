package com.concordia.velocity.strategy;

import com.concordia.velocity.model.Bill;
import com.concordia.velocity.model.Rider;
import com.concordia.velocity.model.Trip;

/**
 * Strategy interface for calculating trip costs and creating bills
 * Allows different pricing strategies based on bike type and payment plans
 */
public interface PaymentStrategy {

    /**
     * Calculates the cost for a trip and creates a complete Bill object
     * @param trip the ID of the trip being billed
     * @param durationMinutes the trip duration in minutes
     * @return a complete Bill object with cost, tax, and total calculated
     */
    Bill createBillAndProcessPayment(Trip trip, long durationMinutes, Rider rider);
}