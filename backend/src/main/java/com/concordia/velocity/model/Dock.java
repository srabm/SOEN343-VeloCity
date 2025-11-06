package com.concordia.velocity.model;

import com.concordia.velocity.observer.Observer;
import com.concordia.velocity.observer.Subject;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class Dock implements Subject {

    // Valid dock statuses
    public static final String STATUS_EMPTY = "empty";
    public static final String STATUS_OCCUPIED = "occupied";
    public static final String STATUS_OUT_OF_SERVICE = "out_of_service";

    private static final List<String> VALID_STATUSES = Arrays.asList(
            STATUS_EMPTY, STATUS_OCCUPIED, STATUS_OUT_OF_SERVICE
    );

    private String dockId;
    private String status;
    private String bikeId;
    private String stationId;
    private String dockCode;  // Code required to unlock the dock

    private transient List<Observer> observers = new ArrayList<>();

    public Dock() {}

    public Dock(String dockId, String status, String bikeId, String stationId) {
        this.dockId = dockId;
        this.status = status;
        this.bikeId = bikeId;
        this.stationId = stationId;
    }

    public Dock(String dockId, String status, String bikeId, String stationId, String dockCode) {
        this.dockId = dockId;
        this.status = status;
        this.bikeId = bikeId;
        this.stationId = stationId;
        this.dockCode = dockCode;
    }

    /**
     * Validates if a status string is valid
     */
    public static boolean isValidStatus(String status) {
        if (status == null) return false;
        return VALID_STATUSES.contains(status.toLowerCase());
    }

    /**
     * Changes the dock status with validation
     * @param newStatus the new status to set
     * @return true if status was changed, false otherwise
     * @throws IllegalArgumentException if the status is invalid
     */
    public boolean changeStatus(String newStatus) {
        if (!isValidStatus(newStatus)) {
            throw new IllegalArgumentException("Invalid dock status: " + newStatus +
                    ". Valid options are: empty, occupied, out_of_service");
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
     * Checks if dock is available for use (empty and not out of service)
     */
    public boolean isAvailable() {
        return STATUS_EMPTY.equalsIgnoreCase(this.status);
    }

    /**
     * Checks if dock is occupied
     */
    public boolean isOccupied() {
        return STATUS_OCCUPIED.equalsIgnoreCase(this.status);
    }

    /**
     * Checks if dock is out of service
     */
    public boolean isOutOfService() {
        return STATUS_OUT_OF_SERVICE.equalsIgnoreCase(this.status);
    }

    // Getters and setters
    public String getDockId() { return dockId; }
    public void setDockId(String dockId) { this.dockId = dockId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getBikeId() { return bikeId; }
    public void setBikeId(String bikeId) { this.bikeId = bikeId; }

    public String getStationId() { return stationId; }
    public void setStationId(String stationId) { this.stationId = stationId; }

    public String getDockCode() { return dockCode; }
    public void setDockCode(String dockCode) { this.dockCode = dockCode; }

    @Override
    public void attach(Observer o) {
        if (observers == null) observers = new ArrayList<>();
        observers.add(o);
    }

    @Override
    public void detach(Observer o) {
        if (observers == null) observers = new ArrayList<>();
        observers.remove(o);
    }

    @Override
    public void notifyObservers() {
        if (observers != null) {
            for (Observer o : observers) {
                o.update("Dock " + dockId + " status changed to " + status);
            }
        }
    }

    @Override
    public String toString() {
        return "Dock{" +
                "dockId='" + dockId + '\'' +
                ", status='" + status + '\'' +
                ", bikeId='" + bikeId + '\'' +
                ", stationId='" + stationId + '\'' +
                ", dockCode='" + (dockCode != null ? "***" : "null") + '\'' +
                '}';
    }
}