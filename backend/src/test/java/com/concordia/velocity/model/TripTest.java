package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

import com.google.cloud.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Arrays;
import java.util.ArrayList;
import com.concordia.velocity.model.Trip;
import com.concordia.velocity.model.Station;
import com.concordia.velocity.model.Dock;

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
    void completeTripTest() {
        Station station = new Station("S012", "Mackay Street Station", "full", "-29.03984", "-62.77431", "4050 Mackay St., Montreal Qc.", 10, 10, 15, new ArrayList<String>(Arrays.asList("D001", "D002", "D003", "D004", "D005", "D006", "D007", "D008", "D009", "D0010")), new ArrayList<String>(Arrays.asList("B001", "B002", "B003", "B004", "B005", "B006", "B007", "B008", "B009", "B0010")), 3, 7);
        Dock dock = new Dock("D003", "occupied", "B001", "S001");
        trip.completeTrip(station.getStationId(), station.getStationName(), dock.getDockId());
        assertEquals("S012", trip.getEndStationId());
        assertEquals("Mackay Street Station", trip.getEndStationName());
        assertEquals("D003", trip.getEndDockId());
        assertEquals(trip.STATUS_COMPLETED, trip.getStatus());
    }

    @Test
    void calculateDurationTest() {
        Station station = new Station("S012", "Mackay Street Station", "full", "-29.03984", "-62.77431", "4050 Mackay St., Montreal Qc.", 10, 10, 15, new ArrayList<String>(Arrays.asList("D001", "D002", "D003", "D004", "D005", "D006", "D007", "D008", "D009", "D0010")), new ArrayList<String>(Arrays.asList("B001", "B002", "B003", "B004", "B005", "B006", "B007", "B008", "B009", "B0010")), 3, 7);
        Dock dock = new Dock("D003", "occupied", "B001", "S001");
        LocalDateTime startDateTime = LocalDateTime.now().minusDays(3);
        Instant instant = startDateTime.atZone(ZoneId.systemDefault()).toInstant();
        Timestamp startDateTimeTimestamp = Timestamp.ofTimeSecondsAndNanos(
            instant.getEpochSecond(),
            instant.getNano()
        );
        trip.setStartTime(startDateTimeTimestamp);
        trip.completeTrip(station.getStationId(), station.getStationName(), dock.getDockId());
        assertTrue(trip.getDurationMinutes() > 0);
    }

    @Test
    void getCurrentDurationMinutesTest() {
        assertEquals(0L, trip.getCurrentDurationMinutes());;
    }

    @Test
    void isActiveTest() {
        assertTrue(trip.isActive());
        trip.setStatus(Trip.STATUS_COMPLETED);
        assertFalse(trip.isActive());
    }

    @Test
    void isCompletedTest() {
        assertFalse(trip.isCompleted());
        trip.setStatus(Trip.STATUS_COMPLETED);
        assertTrue(trip.isCompleted());
    }

    @Test
    void cancelTripTest() {
        trip.cancelTrip();
        Timestamp currentTime = Timestamp.now();
        assertEquals(Trip.STATUS_CANCELLED, trip.getStatus());
        assertTrue(currentTime.compareTo(trip.getEndTime()) >= 0);
    }

    @AfterEach
    void tearDown() {
        trip = null;
    }
}