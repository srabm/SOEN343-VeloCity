package com.concordia.velocity.backend.model;
import java.util.ArrayList;
import java.util.List;

public class Station {
    private String stationName;
    private String status;
    private String latitude;
    private String longitude;
    private String streetAddress;
    private int capacity;
    private int numDockedBikes;
    private int reservationHoldTime
    private List<Bike> dockedBikes;

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
        this.reservationHoldTime = reservationHoldTime;
        this.dockedBikes = new ArrayList<>();
    }

    public void addBike(Bike bike) {
        if (dockedBikes.size() < capacity) {
            dockedBikes.add(bike);
            numDockedBikes++;
        } else {
            throw new IllegalStateException("Station is at full capacity");
        }
    }

    public void removeBike(Bike bike) {
        if (dockedBikes.remove(bike)) {
            numDockedBikes--;
        } else {
            throw new IllegalArgumentException("Bike not found in station");
        }
    }

    private void updateStatus() {
        if (numDockedBikes == 0) {
            status = "Empty";
        } else if (numDockedBikes == capacity) {
            status = "Full";
        } else {
            status = "Occupied";
        }
    }

    public void setReservationHoldTime(int minutes) {
        this.reservationHoldTime = minutes;
    }

    public List<Bike> getDockedBikes() {
        return dockedBikes;
    }

    public String getStationName() {
        return stationName;
    }
    public String getStatus() {
        return status;
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

}