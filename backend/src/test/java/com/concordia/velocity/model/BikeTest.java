package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.ArrayList;
import com.google.cloud.Timestamp;

import com.concordia.velocity.model.Bike;
import com.concordia.velocity.model.Station;

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
        LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
        bike.setReservationExpiryFromLocalDateTime(tomorrow);
        assertTrue(bike.isReservedActive());
    }

    @Test
    void startReservationExpiryTest() {
        LocalDateTime currentTime = LocalDateTime.now();
        Station station = new Station("S012", "Mackay Street Station", "full", "-29.03984", "-62.77431", "4050 Mackay St., Montreal Qc.", 10, 10, 15, new ArrayList<String>(Arrays.asList("D001", "D002", "D003", "D004", "D005", "D006", "D007", "D008", "D009", "D0010")), new ArrayList<String>(Arrays.asList("B001", "B002", "B003", "B004", "B005", "B006", "B007", "B008", "B009", "B0010")), 3, 7);
        assertTrue(bike.startReservationExpiry(station, "xxxxx").isAfter(currentTime));
    }

    @Test
    void clearReservationTest() {
        Station station = new Station("S012", "Mackay Street Station", "full", "-29.03984", "-62.77431", "4050 Mackay St., Montreal Qc.", 10, 10, 15, new ArrayList<String>(Arrays.asList("D001", "D002", "D003", "D004", "D005", "D006", "D007", "D008", "D009", "D0010")), new ArrayList<String>(Arrays.asList("B001", "B002", "B003", "B004", "B005", "B006", "B007", "B008", "B009", "B0010")), 3, 7);
        bike.startReservationExpiry(station, "xxxxx");
        bike.clearReservation();
        assertNull(bike.getReservationExpiry());
        assertNull(bike.getReservedByUserId());
    }

    @AfterEach
    void tearDown() {
        bike = null;
    }
}