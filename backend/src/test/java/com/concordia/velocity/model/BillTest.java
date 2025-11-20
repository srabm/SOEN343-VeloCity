package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.concordia.velocity.model.Bill;

class BillTest {

    private Bill bill;

    @BeforeEach
    void setup() {
        bill = new Bill();
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