package com.concordia.velocity.model;

import java.time.LocalDateTime;
import java.time.Duration;

public class Trip {

    // Trip status constants
    public static final String STATUS_ACTIVE = "active";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_CANCELLED = "cancelled";

    private String tripId;
    private String riderId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String startStationId;      // Store station ID for Firestore
    private String startStationName;    // Store name for quick reference
    private String endStationId;        // Store station ID for Firestore
    private String endStationName;      // Store name for quick reference
    private String startDockId;         // Track which dock bike was taken from
    private String endDockId;           // Track which dock bike was returned to
    private String bikeId;
    private String bikeType;
    private String status;              // active, completed, cancelled
    private Long durationMinutes;       // Trip duration in minutes
    private Bill bill;

    public Trip() {}

    /**
     * Constructor for starting a new trip
     */
    public Trip(String tripId, String riderId, String bikeId, String bikeType,
                String startStationId, String startStationName, String startDockId) {
        this.tripId = tripId;
        this.riderId = riderId;
        this.bikeId = bikeId;
        this.bikeType = bikeType;
        this.startStationId = startStationId;
        this.startStationName = startStationName;
        this.startDockId = startDockId;
        this.startTime = LocalDateTime.now();
        this.status = STATUS_ACTIVE;
    }

    /**
     * Completes the trip by setting end information
     */
    public void completeTrip(String endStationId, String endStationName, String endDockId) {
        this.endTime = LocalDateTime.now();
        this.endStationId = endStationId;
        this.endStationName = endStationName;
        this.endDockId = endDockId;
        this.status = STATUS_COMPLETED;
        calculateDuration();
    }

    /**
     * Calculates the trip duration in minutes
     */
    public void calculateDuration() {
        if (startTime != null && endTime != null) {
            Duration duration = Duration.between(startTime, endTime);
            this.durationMinutes = duration.toMinutes();
        }
    }

    /**
     * Gets duration in minutes, calculating if needed
     */
    public Long getDurationMinutes() {
        if (durationMinutes == null && startTime != null && endTime != null) {
            calculateDuration();
        }
        return durationMinutes;
    }

    /**
     * Gets current trip duration (for active trips)
     */
    public Long getCurrentDurationMinutes() {
        if (startTime == null) return 0L;
        LocalDateTime now = endTime != null ? endTime : LocalDateTime.now();
        return Duration.between(startTime, now).toMinutes();
    }

    /**
     * Checks if trip is currently active
     */
    public boolean isActive() {
        return STATUS_ACTIVE.equalsIgnoreCase(this.status);
    }

    /**
     * Checks if trip is completed
     */
    public boolean isCompleted() {
        return STATUS_COMPLETED.equalsIgnoreCase(this.status);
    }

    /**
     * Cancels the trip
     */
    public void cancelTrip() {
        this.status = STATUS_CANCELLED;
        this.endTime = LocalDateTime.now();
    }

    // Getters and setters
    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public String getRiderId() { return riderId; }
    public void setRiderId(String riderId) { this.riderId = riderId; }

    public LocalDateTime getStartTime() { return startTime; }
    public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }

    public LocalDateTime getEndTime() { return endTime; }
    public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }

    public String getStartStationId() { return startStationId; }
    public void setStartStationId(String startStationId) { this.startStationId = startStationId; }

    public String getStartStationName() { return startStationName; }
    public void setStartStationName(String startStationName) { this.startStationName = startStationName; }

    public String getEndStationId() { return endStationId; }
    public void setEndStationId(String endStationId) { this.endStationId = endStationId; }

    public String getEndStationName() { return endStationName; }
    public void setEndStationName(String endStationName) { this.endStationName = endStationName; }

    public String getStartDockId() { return startDockId; }
    public void setStartDockId(String startDockId) { this.startDockId = startDockId; }

    public String getEndDockId() { return endDockId; }
    public void setEndDockId(String endDockId) { this.endDockId = endDockId; }

    public String getBikeId() { return bikeId; }
    public void setBikeId(String bikeId) { this.bikeId = bikeId; }

    public String getBikeType() { return bikeType; }
    public void setBikeType(String bikeType) { this.bikeType = bikeType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public void setDurationMinutes(Long durationMinutes) { this.durationMinutes = durationMinutes; }

    public Bill getBill() { return bill; }
    public void setBill(Bill bill) { this.bill = bill; }

    @Override
    public String toString() {
        return "Trip{" +
                "tripId='" + tripId + '\'' +
                ", riderId='" + riderId + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", startStation='" + startStationName + "' (" + startStationId + ")" +
                ", endStation='" + endStationName + "' (" + endStationId + ")" +
                ", bikeId='" + bikeId + '\'' +
                ", bikeType='" + bikeType + '\'' +
                ", status='" + status + '\'' +
                ", durationMinutes=" + durationMinutes +
                '}';
    }
}