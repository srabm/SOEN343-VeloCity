package com.concordia.velocity.backend.model;

abstract class Station {
    private String name;
    private String status;
    private double latitude;
    private double longitude;
    private String streetAddress;
    private int capacity;
    private int numDockedBikes;
    private int reservationHoldTime;

    public Station(String name, String status, double latitude, double longitude, String streetAddress, int capacity, int numDockedBikes, int reservationHoldTime) {
        this.name = name;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.streetAddress = streetAddress;
        this.capacity = capacity;
        this.numDockedBikes = numDockedBikes;
        this.reservationHoldTime = reservationHoldTime;
    }
}