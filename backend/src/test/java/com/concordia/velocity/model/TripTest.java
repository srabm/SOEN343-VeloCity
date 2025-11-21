package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.google.cloud.Timestamp;
import com.concordia.velocity.model.Trip;

class TripTest {

    private Trip trip;

    @BeforeEach
    void setup() {
        trip = new Trip("0069cd82-c775-4981-ab22-3a1c17002e67", "tQfMbbo4rZPtiVVythjfDb583I93", "B009", "standard", "S004", "Mgcill Metro Station", "D003");
    }

    @Test
    void constructorTest() {
        Timestamp currentTime = Timestamp.now();
        assertEquals("0069cd82-c775-4981-ab22-3a1c17002e67", trip.getTripId());
        assertEquals("tQfMbbo4rZPtiVVythjfDb583I93", trip.getRiderId());
        assertTrue(currentTime.compareTo(trip.getStartTime()) >= 0);
        assertNull(trip.getEndTime());
        assertEquals("S004", trip.getStartStationId());
        assertEquals("Mgcill Metro Station", trip.getStartStationName());
        assertNull(trip.getEndStationId());
        assertNull(trip.getEndStationName());
        assertEquals("D003", trip.getStartDockId());
        assertNull(trip.getEndDockId());
        assertEquals("B009", trip.getBikeId());
        assertEquals("standard", trip.getBikeType());
        assertEquals(Trip.STATUS_ACTIVE, trip.getStatus());
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