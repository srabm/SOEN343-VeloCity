package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.concordia.velocity.model.Station;

class StationTest {

    private Station station;

    @BeforeEach
    void setup() {
        station = new Station();
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