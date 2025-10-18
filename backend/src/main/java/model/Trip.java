package com.concordia.velocity.backend.model;

import java.util.Date;
public class Trip {
    private String tripId;
    private Date date;
    private Station startStation;
    private Station endStation;
    private String bikeType;
    private double cost;

    public Trip(String tripId, Date date, Station startStation, Station endStation, String bikeType, double cost) {
        this.tripId = tripId;
        this.date = date;
        this.startStation = startStation;
        this.endStation = endStation;
        this.bikeType = bikeType;
        this.cost = cost;
    }
}