package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
    void isValidStatusTest() {
        assertTrue(bike.isValidStatus("available"));
        assertTrue(bike.isValidStatus("reserved"));
        assertTrue(bike.isValidStatus("maintenance"));
        assertTrue(bike.isValidStatus("on_trip"));
        assertFalse(bike.isValidStatus("random_status"));
    }

    @Test
    void changeStatusTest() {
        assertThrows(IllegalArgumentException.class, () -> bike.changeStatus("random_status"));
        assertTrue(bike.changeStatus("reserved"));
        assertFalse(bike.changeStatus("reserved"));
        bike.changeStatus("on_trip");
        assertThrows(IllegalStateException.class, () -> bike.changeStatus("maintenance"));
    }

    @Test
    void isReservedActiveTest() {
        assertFalse(bike.isReservedActive());
        bike.changeStatus("reserved");
        assertFalse(bike.isReservedActive());
        // bike.setReservationExpiry();
        // set date and run assert true
    }

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