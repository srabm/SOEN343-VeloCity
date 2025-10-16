package com.concordia.velocity.backend.model;

abstract class Dock {
    private String dockId;
    private String state;

    public Dock(String dockId, String state) {
        this.dockId = dockId;
        this.state = state;
    }
}