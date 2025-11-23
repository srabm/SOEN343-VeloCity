package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.ArrayList;
import com.concordia.velocity.model.Station;
import com.concordia.velocity.model.Bike;

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
    void isValidStatusTest() {
        assertTrue(station.isValidStatus("empty"));
        assertTrue(station.isValidStatus("occupied"));
        assertTrue(station.isValidStatus("full"));
        assertTrue(station.isValidStatus("out_of_service"));
        assertFalse(station.isValidStatus("random_status"));
        assertFalse(station.isValidStatus(null));
    }

    @Test
    void changeStatusTest() {
        assertThrows(IllegalArgumentException.class, () -> station.changeStatus("random_status"));
        assertFalse(station.changeStatus("full"));
        assertTrue(station.changeStatus("empty"));
    }

    @Test
    void determineStatusFromCapacityTest() {
        assertEquals(station.STATUS_FULL, station.determineStatusFromCapacity());
        station.setNumDockedBikes(0);
        assertEquals(station.STATUS_EMPTY, station.determineStatusFromCapacity());
        station.setNumDockedBikes(5);
        assertEquals(station.STATUS_OCCUPIED, station.determineStatusFromCapacity());
    }

    @Test
    void hasAvailableSpaceTest() {
        assertFalse(station.hasAvailableSpace());
        station.setNumDockedBikes(5);
        assertTrue(station.hasAvailableSpace());
        station.changeStatus(station.STATUS_OUT_OF_SERVICE);
        assertFalse(station.hasAvailableSpace());
    }

    @Test
    void hasBikesAvailableTest() {
        assertTrue(station.hasBikesAvailable());
        station.setNumDockedBikes(0);
        assertFalse(station.hasBikesAvailable());
        station.setNumDockedBikes(5);
        station.changeStatus(station.STATUS_OUT_OF_SERVICE);
        assertFalse(station.hasBikesAvailable());
        station.changeStatus(station.STATUS_OUT_OF_SERVICE);
        assertFalse(station.hasBikesAvailable());
    }

    @Test
    void isOutOfServiceTest() {
        assertFalse(station.isOutOfService());
        station.changeStatus(station.STATUS_OUT_OF_SERVICE);
        assertTrue(station.isOutOfService());
    }

    @Test
    void isFullTest() {
        assertTrue(station.isFull());
        station.setNumDockedBikes(station.getCapacity() - 1);
        assertFalse(station.isFull());
    }

    @Test
    void removeBikeTest() {
        int initialElectricBikes = station.getNumElectricBikes();
        Bike bike = new Bike("B005", "available", "electric", "D005", "S002");
        station.removeBike(bike);
        assertEquals(initialElectricBikes - 1, station.getNumElectricBikes());
    }

    @Test
    void addBikeTest() {
        int initialElectricBikes = station.getNumElectricBikes();
        int initialStandardBikes = station.getNumStandardBikes();
        Bike bike1 = new Bike("B005", "available", "electric", "D005", "S002");
        Bike bike2 = new Bike("B018", "available", "standard", "D006", "S002");
        station.removeBike(bike1);
        station.addBike(bike2);
        assertEquals(initialElectricBikes - 1, station.getNumElectricBikes());
        assertEquals(initialStandardBikes + 1, station.getNumStandardBikes());
    }

    @AfterEach
    void tearDown() {
        station = null;
    }
}