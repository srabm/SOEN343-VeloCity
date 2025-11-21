package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

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
    void isValidStatusTest() {}

    @Test
    void changeStatusTest() {}

    @Test
    void isAvailableTest() {}

    @Test
    void isOccupiedTest() {}

    @Test
    void isOutOfServiceTest() {}

    @AfterEach
    void tearDown() {
        dock = null;
    }
}