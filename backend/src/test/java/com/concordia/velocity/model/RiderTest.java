package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.concordia.velocity.model.Bike;

class RiderTest {

    private Rider rider;

    @BeforeEach
    void setup() {
        rider = new Rider();
    }

    @Test
    void getFullNameTest() {}

    @AfterEach
    void tearDown() {
        rider = null;
    }
}