package com.concordia.velocity.backend.model;

import java.time.LocalDateTime;
import jakarta.persistence.*;

@Entity
@Table(name = "bikes")

abstract class Bike {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String status;
    private String type; // should i keep this as a String type? or should I make the bike class abstract so that it can be extended to different bike types?
    private LocalDateTime reservationExpiry;

    @ManyToOne
    @JoinColumn(name = "station_id")
    private Station station;

    public Bike() {}

    public Bike(String status, String type) {
        this.status = status;
        this.type = type;
        this.reservationExpiry = null;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }
    public void setType(String type) {
        this.type = type;
    }

    public LocalDateTime getReservationExpiry() {
        return reservationExpiry;
    }
    public void setReservationExpiry(LocalDateTime reservationExpiry) {
        this.reservationExpiry = reservationExpiry;
    }
}