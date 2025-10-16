package com.concordia.velocity.backend.model;

java.time.LocalDateTime;

abstract class Bike {
    private String bikeId;
    private String status;
    private String type;
    private LocalDateTime reservationExpiry;

    public Bike(String bikeId, String status, String type) {
        this.bikeId = bikeId;
        this.status = status;
        this.type = type;
        this.reservationExpiry = null;
    }
}