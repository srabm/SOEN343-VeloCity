package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.concordia.velocity.model.Trip;

class TripTest {

    private Trip trip;

    @BeforeEach
    void setup() {
        trip = new Trip();
    }

    @Test
    void completeTripTest() {}

    @Test
    void calculateDurationTest() {}

    @Test
    void getDurationMinutesTest() {}

    @Test
    void getCurrentDurationMinutesTest() {}

    @Test
    void isActiveTest() {}

    @Test
    void isCompletedTest() {}

    @Test
    void cancelTripTest() {}

    @AfterEach
    void tearDown() {
        trip = null;
    }
}