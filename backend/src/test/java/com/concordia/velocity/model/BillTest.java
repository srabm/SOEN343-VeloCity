package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.cloud.Timestamp;
import com.concordia.velocity.model.Bill;

class BillTest {

    private Bill bill;

    @BeforeEach
    void setup() {
        bill = new Bill("01559657-7fe2-43a5-b74b-3a5b29503217", "5af553e4-98c7-4385-b4e5-7b9346535371", "tQfMbbo4rZPtiVVythjfDb583I93", 13.25, 0, 0);
    }

    @Test
    void constructorTest() {
        Timestamp currentTime = Timestamp.now();
        assertEquals("01559657-7fe2-43a5-b74b-3a5b29503217", bill.getBillId());
        assertEquals("5af553e4-98c7-4385-b4e5-7b9346535371", bill.getTripId());
        assertEquals("tQfMbbo4rZPtiVVythjfDb583I93", bill.getRiderId());
        assertEquals(13.25, bill.getCost());
        assertEquals(0, bill.getTax());
        assertEquals(0, bill.getTotal());
        assertEquals("pending", bill.getStatus());
        assertTrue(currentTime.compareTo(bill.getBillingDate()) >= 0);
    }

    @Test
    void calculateTotalTest() {}

    @Test
    void calculateTaxTest() {}

    @AfterEach
    void tearDown() {
        bill = null;
    }
}