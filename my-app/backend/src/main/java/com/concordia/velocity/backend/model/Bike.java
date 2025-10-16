package com.concordia.velocity.backend.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "bikes")

abstract class Bike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String bikeId;
    private String status;
    private String type; // should i keep this as a String type? or should I make the bike class abstract so that it can be extended to different bike types?
    private LocalDateTime reservationExpiry;

    public Bike(String bikeId, String status, String type) {
        this.bikeId = bikeId;
        this.status = status;
        this.type = type;
        this.reservationExpiry = null;
    }

    public String getBikeId() {
        return bikeId;
    } 
    public String getStatus() {
        return status;
    }
    public String getType() {
        return type;
    }
    public LocalDateTime getReservationExpiry() {
        return reservationExpiry;
    }
    public void setStatus(String status) {
        this.status = status;
    }
    public void setReservationExpiry(LocalDateTime reservationExpiry) {
        this.reservationExpiry = reservationExpiry;
    }
}