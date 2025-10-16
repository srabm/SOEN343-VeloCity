package com.concordia.velocity.backend.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "stations") // not sure about this

public class Station {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String stationName;
    private String status; // empty | occupied | full | out_of_service
    private String latitude;
    private String longitude;
    private String streetAddress;
    private int capacity;
    private int numDockedBikes;
    private int reservationHoldTime

    // List of bikes docked at this station
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "station_id")
    private List<Bike> dockedBikes = new ArrayList<>();

    public Station() {}

    public Station(String stationName, String status, String latitude, 
                   String longitude, String streetAddress, int capacity, 
                   int numDockedBikes) {
        this.stationName = stationName;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.streetAddress = streetAddress;
        this.capacity = capacity;
        this.numDockedBikes = numDockedBikes;
        // this.reservationHoldTime = reservationHoldTime;
        // this.dockedBikes = new ArrayList<>();
    }

    public void addBike(Bike bike) {
        if (dockedBikes.size() < capacity) {
            dockedBikes.add(bike);
            numDockedBikes = dockedBikes.size();
            updateStatus();
        } else {
            throw new IllegalStateException("Station is at full capacity");
        }
    }

    public void removeBike(Bike bike) {
        if (dockedBikes.remove(bike)) {
            numDockedBikes = dockedBikes.size();
            updateStatus();
        } else {
            throw new IllegalArgumentException("Bike not found in station");
        }
    }

    private void updateStatus() {
        if ("out_of_service".equalsIgnoreCase(status)) return; // don't auto-update if inactive
        if (numDockedBikes == 0) status = "empty";
        else if (numDockedBikes == capacity) status = "full";
        else status = "occupied";
    }

    public long getId() {
        return id;
    }
    public List<Bike> getDockedBikes() {
        return dockedBikes;
    }

    public String getStationName() {
        return stationName;
    }
    public void setStationName(String stationName) {
        this.stationName = stationName;
    }

    public String getStatus() {
        return status;
    }
    public void setStatus(String status) {
        this.status = status;
    }


    public String getLatitude() {
        return latitude;
    }   

    public String getLongitude() {
        return longitude;
    }  

    public String getStreetAddress() {
        return streetAddress;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getNumDockedBikes() {
        return numDockedBikes;
    } 

    public int getReservationHoldTime() {
        return reservationHoldTime;
    }
    public void setReservationHoldTime(int reservationHoldTime) {
        this.reservationHoldTime = reservationHoldTime;
    }
    
    

}