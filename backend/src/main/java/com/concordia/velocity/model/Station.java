package com.concordia.velocity.model;

import com.concordia.velocity.observer.Observer;
import com.concordia.velocity.observer.Subject;
import java.util.ArrayList;
import java.util.List;

public class Station implements Subject {

    // Firestore document ID 
    private String stationId;

    private String stationName;
    private String status; // "empty" | "occupied" | "full" | "out_of_service"
    private String latitude;
    private String longitude;
    private String streetAddress;
    private int capacity;           
    private int numDockedBikes;  
    private int reservationHoldTime; 
    private List<String> dockIds;
    private List<String> bikeIds;

    private transient final List<Observer> observers = new ArrayList<>(); // put transient here so that it doesn't serialize Firestore (Serialization error)

    public Station() {}


    public Station(String stationId,
                   String stationName,
                   String status,
                   String latitude,
                   String longitude,
                   String streetAddress,
                   int capacity,
                   int numDockedBikes,
                   int reservationHoldTime,
                   List<String> dockIds,
                   List<String> bikeIds) {

        this.stationId = stationId;
        this.stationName = stationName;
        this.status = status;
        this.latitude = latitude;
        this.longitude = longitude;
        this.streetAddress = streetAddress;
        this.capacity = capacity;
        this.numDockedBikes = numDockedBikes;
        this.reservationHoldTime = reservationHoldTime;
        this.dockIds = dockIds;
        this.bikeIds = bikeIds;
    }
    @Override
    public void attach (Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override 
    public void detach (Observer observer) {
        observers.remove(observer);
    }

    @Override public void notifyObservers() {
        for (Observer obs : observers) {
            obs.update("Station " + stationId + " status changed to " + status);
        }
    }

    // setters, getters
    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }

    public String getStationName() { return stationName; }
    public void setStationName(String stationName) { this.stationName = stationName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { 
        this.status = status;
        notifyObservers(); 
    }

    public String getLatitude() { return latitude; }
    public void setLatitude(String latitude) { this.latitude = latitude; }

    public String getLongitude() { return longitude; }
    public void setLongitude(String longitude) { this.longitude = longitude; }

    public String getStreetAddress() { return streetAddress; }
    public void setStreetAddress(String streetAddress) { this.streetAddress = streetAddress; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public int getNumDockedBikes() { return numDockedBikes; }
    public void setNumDockedBikes(int numDockedBikes) {
        this.numDockedBikes = numDockedBikes;
    }

    public int getReservationHoldTime() { return reservationHoldTime; }
    public void setReservationHoldTime(int reservationHoldTime) { this.reservationHoldTime = reservationHoldTime; }

    public List<String> getDockIds() { return dockIds; }
    public void setDockIds(List<String> dockIds) { this.dockIds = dockIds; }

    public List<String> getBikeIds() { return bikeIds; }
    public void setBikeIds(List<String> bikeIds) { this.bikeIds = bikeIds; }

    
}
