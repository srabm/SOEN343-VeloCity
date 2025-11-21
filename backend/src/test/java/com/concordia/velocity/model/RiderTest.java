package com.concordia.velocity.model;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.concordia.velocity.model.Rider;

class RiderTest {

    private Rider rider;

    @BeforeEach
    void setup() {
        rider = new Rider("     Michel", "Traore", "359 av. Barvave, Balboa, SD", "m.traore@gmail.com", "5149994583");
    }

    @Test
    void constructorTest() {
        assertEquals("     Michel", rider.getFirstName());
        assertEquals("Traore", rider.getLastName());
        assertEquals("359 av. Barvave, Balboa, SD", rider.getAddress());
        assertEquals("m.traore@gmail.com", rider.getEmail());
        assertEquals("5149994583", rider.getPhoneNumber());
    }

    @Test
    void getFullNameTest() {
        assertEquals("Michel Traore", rider.getFullName());
    }

    @Test
    void getRodeTest() {
        assertEquals("RIDER", rider.getRole());
    }

    @AfterEach
    void tearDown() {
        rider = null;
    }
}