package com.concordia.velocity.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.concordia.velocity.observer.Observer;
import com.concordia.velocity.observer.StatusObserver;
import com.concordia.velocity.observer.Subject;

public class Station implements Subject {

    // Valid station statuses
    public static final String STATUS_EMPTY = "empty";
    public static final String STATUS_OCCUPIED = "occupied";
    public static final String STATUS_FULL = "full";
    public static final String STATUS_OUT_OF_SERVICE = "out_of_service";

    private static final List<String> VALID_STATUSES = Arrays.asList(
            STATUS_EMPTY, STATUS_OCCUPIED, STATUS_FULL, STATUS_OUT_OF_SERVICE
    );

    // Firestore document ID
    private String stationId;

    private String stationName;
    private String status;
    private String latitude;
    private String longitude;
    private String streetAddress;
    private int capacity;
    private int numDockedBikes;
    private int reservationHoldTime;
    private List<String> dockIds;
    private List<String> bikeIds;

    private int numElectricBikes;
    private int numStandardBikes;

    private transient final List<Observer> observers = new ArrayList<>();

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
                   List<String> bikeIds,
                   int numElectricBikes,
                   int numStandardBikes) {

        this.stationId = stationId;
        this.stationName = stationName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.streetAddress = streetAddress;
        this.capacity = capacity;
        this.numDockedBikes = numDockedBikes;
        this.reservationHoldTime = reservationHoldTime;
        this.dockIds = dockIds;
        this.bikeIds = bikeIds;
        this.numStandardBikes = numStandardBikes;
        this.numElectricBikes = numElectricBikes;

        this.status = determineStatusFromCapacity();
        attach(new StatusObserver());
    }

    /**
     * Validates if a status string is valid
     */
    public static boolean isValidStatus(String status) {
        if (status == null) return false;
        return VALID_STATUSES.contains(status.toLowerCase());
    }

    /**
     * Changes the station status with validation
     * @param newStatus the new status to set
     * @return true if status was changed, false otherwise
     * @throws IllegalArgumentException if the status is invalid
     */
    public boolean changeStatus(String newStatus) {
        if (!isValidStatus(newStatus)) {
            throw new IllegalArgumentException("Invalid station status: " + newStatus +
                    ". Valid options are: empty, occupied, full, out_of_service");
        }

        String normalizedNewStatus = newStatus.toLowerCase();
        String normalizedCurrentStatus = this.status != null ? this.status.toLowerCase() : "";

        // If already at target status, no change needed
        if (normalizedCurrentStatus.equals(normalizedNewStatus)) {
            return false;
        }

        this.status = normalizedNewStatus;
        notifyObservers();
        return true;
    }

    /**
     * Determines the correct active status based on number of docked bikes
     */
    public String determineStatusFromCapacity() {
        if (numDockedBikes == 0) {
            return STATUS_EMPTY;
        } else if (numDockedBikes >= capacity) {
            return STATUS_FULL;
        } else {
            return STATUS_OCCUPIED;
        }
    }

    /**
     * Checks if station can accept more bikes
     */
    public boolean hasAvailableSpace() {
        return numDockedBikes < capacity && !STATUS_OUT_OF_SERVICE.equalsIgnoreCase(this.status);
    }

    /**
     * Checks if station has bikes available
     */
    public boolean hasBikesAvailable() {
        return numDockedBikes > 0 && !STATUS_OUT_OF_SERVICE.equalsIgnoreCase(this.status);
    }

    /**
     * Checks if station is out of service
     */
    public boolean isOutOfService() {
        return STATUS_OUT_OF_SERVICE.equalsIgnoreCase(this.status);
    }

    /**
     * Checks if station is at full capacity
     */
    public boolean isFull() {
        return numDockedBikes >= capacity;
    }

    @Override
    public void attach(Observer observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer obs : observers) {
            obs.update("Station " + stationId + " status changed to " + status);
        }
    }

    // Getters and setters
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
    public void setCapacity(int capacity) { 
        this.capacity = capacity;
        this.status = determineStatusFromCapacity(); 
    }

    public int getNumDockedBikes() { return numDockedBikes; }
    public void setNumDockedBikes(int numDockedBikes) {
        this.numDockedBikes = numDockedBikes;
        this.status = determineStatusFromCapacity();
    }

    public int getReservationHoldTime() { return reservationHoldTime; }
    public void setReservationHoldTime(int reservationHoldTime) { this.reservationHoldTime = reservationHoldTime; }

    public List<String> getDockIds() { return dockIds; }
    public void setDockIds(List<String> dockIds) { this.dockIds = dockIds; }

    public List<String> getBikeIds() { return bikeIds; }
    public void setBikeIds(List<String> bikeIds) { this.bikeIds = bikeIds; }

    public int getNumStandardBikes() { return numStandardBikes; }
    public void setNumStandardBikes(int numStandardBikes) {
        this.numStandardBikes = numStandardBikes;
    }

    public int getNumElectricBikes() { return numElectricBikes; }
    public void setNumElectricBikes(int numElectricBikes) {
        this.numElectricBikes = numElectricBikes;
    }


    // we could probably integrate the incrementing with this... so incrementing code does not repeat + maintained simultaneously
    public void removeBike(Bike bike) {
        String bikeId = bike.getBikeId();
        this.bikeIds.remove(bikeId);
        setNumDockedBikes(Math.max(0, getNumDockedBikes() - 1));

        if (bike.getType().equals("electric")) {
            setNumElectricBikes(Math.max(0, getNumElectricBikes() - 1));
        } else {
            setNumStandardBikes(Math.max(0, getNumStandardBikes() - 1));
        }

        String newStationStatus = determineStatusFromCapacity();
        if (!newStationStatus.equals(getStatus())) {
            setStatus(newStationStatus);
        }
    }

    public void addBike(Bike bike) {
        String bikeId = bike.getBikeId();
        this.bikeIds.add(bikeId);

        setNumDockedBikes(Math.max(0, getNumDockedBikes() + 1));

        if (bike.getType().equals("electric")) {
            setNumElectricBikes(Math.max(0, getNumElectricBikes() + 1));
        } else {
            setNumStandardBikes(Math.max(0, getNumStandardBikes() + 1));
        }

        String newStationStatus = determineStatusFromCapacity();
        if (!newStationStatus.equals(getStatus())) {
            setStatus(newStationStatus);
        }
    }


    @Override
    public String toString() {
        return "Station{" +
                "stationId='" + stationId + '\'' +
                ", stationName='" + stationName + '\'' +
                ", status='" + status + '\'' +
                ", capacity=" + capacity +
                ", numDockedBikes=" + numDockedBikes +
                ", streetAddress='" + streetAddress + '\'' +
                '}';
    }
}