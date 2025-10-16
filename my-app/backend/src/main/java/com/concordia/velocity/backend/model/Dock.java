package com.concordia.velocity.backend.model;
import jakarta.persistence.*;

@Entity
@Table(name = "docks")
abstract class Dock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;
    private String state;

    public Dock(String dockId, String state) {
        this.dockId = dockId;
        this.state = state;
    }
}