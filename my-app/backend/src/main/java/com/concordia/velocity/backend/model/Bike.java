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
    private String type;
    private LocalDateTime reservationExpiry;

    public Bike(String bikeId, String status, String type) {
        this.bikeId = bikeId;
        this.status = status;
        this.type = type;
        this.reservationExpiry = null;
    }
}