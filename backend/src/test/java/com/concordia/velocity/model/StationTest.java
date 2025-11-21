package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import java.util.Arrays;
import java.util.ArrayList;
import com.concordia.velocity.model.Station;

class StationTest {

    private Station station;

    @BeforeEach
    void setup() {
        station = new Station("S012", "Mackay Street Station", "full", "-29.03984", "-62.77431", "4050 Mackay St., Montreal Qc.", 10, 10, 15, new ArrayList<String>(Arrays.asList("D001", "D002", "D003", "D004", "D005", "D006", "D007", "D008", "D009", "D0010")), new ArrayList<String>(Arrays.asList("B001", "B002", "B003", "B004", "B005", "B006", "B007", "B008", "B009", "B0010")), 3, 7);
    }

    @Test
    void constructorTest() {
        assertEquals("S012", station.getStationId());
        assertEquals("Mackay Street Station", station.getStationName());
        assertEquals(Station.STATUS_FULL, station.getStatus());
        assertEquals("-29.03984", station.getLatitude());
        assertEquals("-62.77431", station.getLongitude());
        assertEquals("4050 Mackay St., Montreal Qc.", station.getStreetAddress());
        assertEquals(10, station.getCapacity());
        assertEquals(10, station.getNumDockedBikes());
        assertEquals(15, station.getReservationHoldTime());
        assertArrayEquals(new String[]{"D001", "D002", "D003", "D004", "D005", "D006", "D007", "D008", "D009", "D0010"}, station.getDockIds().toArray());
        assertArrayEquals(new String[]{"B001", "B002", "B003", "B004", "B005", "B006", "B007", "B008", "B009", "B0010"}, station.getBikeIds().toArray());
        assertEquals(7, station.getNumStandardBikes());
        assertEquals(3, station.getNumElectricBikes());
    }

    @Test
    void isValidStatusTest() {}

    @Test
    void changeStatusTest() {}

    @Test
    void determineStatusFromCapacityTest() {}

    @Test
    void hasAvailableSpaceTest() {}

    @Test
    void hasBikesAvailableTest() {}

    @Test
    void isOutOfServiceTest() {}

    @Test
    void isFullTest() {}

    @Test
    void removeBikeTest() {}

    @Test
    void addBikeTest() {}

    @AfterEach
    void tearDown() {
        station = null;
    }
}