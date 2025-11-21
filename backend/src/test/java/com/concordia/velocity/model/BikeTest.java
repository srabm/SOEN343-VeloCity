package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.concordia.velocity.model.Bike;

class BikeTest {

    private Bike bike;

    @BeforeEach
    void setup() {
        bike = new Bike("B012", "available", "electric", "D005", "S002");
    }

    @Test
    void constructorTest() {
        assertEquals("B012", bike.getBikeId());
        assertEquals(Bike.STATUS_AVAILABLE, bike.getStatus());
        assertEquals("electric", bike.getType());
        assertEquals("D005", bike.getDockId());
        assertEquals("S002", bike.getStationId());
        assertNull(bike.getReservedByUserId());
        assertNull(bike.getReservationExpiry());
    }

    @Test
    void isValidStatusTest() {}

    @Test
    void changeStatusTest() {}

    @Test
    void isReservedActiveTest() {}

    @Test
    void startReservationExpiryTest() {}

    @Test
    void clearReservationTest() {}

    @Test
    void localDateTimeToTimestampTest() {}

    @Test
    void timestampToLocalDateTimeTest() {}

    @AfterEach
    void tearDown() {
        bike = null;
    }
}