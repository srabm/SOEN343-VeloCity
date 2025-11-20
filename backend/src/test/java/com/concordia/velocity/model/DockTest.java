package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.concordia.velocity.model.Dock;

class DockTest {

    private Dock dock;

    @BeforeEach
    void setup() {
        dock = new Dock();
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