package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.concordia.velocity.model.Dock;

class DockTest {

    private Dock dock;

    @BeforeEach
    void setup() {
        dock = new Dock("D003", "occupied", "B001", "S001");
    }

    @Test
    void constructorTest() {
        assertEquals("D003", dock.getDockId());
        assertEquals(Dock.STATUS_OCCUPIED, dock.getStatus());
        assertEquals("B001", dock.getBikeId());
        assertEquals("S001", dock.getStationId());
        assertNull(dock.getDockCode());
    }

    @Test
    void isValidStatusTest() {
        assertTrue(dock.isValidStatus("empty"));
        assertTrue(dock.isValidStatus("occupied"));
        assertTrue(dock.isValidStatus("out_of_service"));
        assertFalse(dock.isValidStatus("random_status"));
    }

    @Test
    void changeStatusTest() {
        assertThrows(IllegalArgumentException.class, () -> dock.changeStatus("random_status"));
        assertFalse(dock.changeStatus(dock.STATUS_OCCUPIED));
        assertTrue(dock.changeStatus(dock.STATUS_OUT_OF_SERVICE));
    }

    @Test
    void isAvailableTest() {
        assertFalse(dock.isAvailable());
        dock.changeStatus(dock.STATUS_EMPTY);
        assertTrue(dock.isAvailable());
    }

    @Test
    void isOccupiedTest() {
        assertTrue(dock.isOccupied());
        dock.changeStatus(dock.STATUS_EMPTY);
        assertFalse(dock.isOccupied());
    }

    @Test
    void isOutOfServiceTest() {
        assertFalse(dock.isOutOfService());
        dock.changeStatus(dock.STATUS_OUT_OF_SERVICE);
        assertTrue(dock.isOutOfService());
    }

    @AfterEach
    void tearDown() {
        dock = null;
    }
}