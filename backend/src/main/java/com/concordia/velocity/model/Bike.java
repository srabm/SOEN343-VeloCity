package com.concordia.velocity.model;

import com.concordia.velocity.observer.Observer;
import com.concordia.velocity.observer.StatusObserver;
import com.concordia.velocity.observer.Subject;
import com.concordia.velocity.reservation.ReservationManager;
import com.google.cloud.Timestamp;
import com.google.cloud.firestore.annotation.Exclude;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Bike model with proper Firestore serialization
 * Uses Timestamp for Firestore storage but provides LocalDateTime interface
 */
public class Bike implements Subject {

    // Valid bike statuses
    public static final String STATUS_AVAILABLE = "available";
    public static final String STATUS_RESERVED = "reserved";
    public static final String STATUS_MAINTENANCE = "maintenance";
    public static final String STATUS_ON_TRIP = "on_trip";

    private static final List<String> VALID_STATUSES = Arrays.asList(
            STATUS_AVAILABLE, STATUS_RESERVED, STATUS_MAINTENANCE, STATUS_ON_TRIP
    );

    private String bikeId;
    private String status;
    private String type;

    // Store as Timestamp for Firestore compatibility
    private Timestamp reservationExpiry;
    private String reservedByUserId;
    private String dockId;
    private String stationId;
    // private static final ScheduledExecutorService RESERVATION_SCHEDULER = Executors.newSingleThreadScheduledExecutor();
    private Rider reservationUser;

    private transient List<Observer> observers = new ArrayList<>();
    // private transient java.util.concurrent.ScheduledFuture<?> reservationTask;

    public Bike() {}

    public Bike(String bikeId, String status, String type, String dockId, String stationId) {
        this.bikeId = bikeId;
        this.status = status;
        this.type = type;
        this.dockId = dockId;
        this.stationId = stationId;
        this.reservationUser = null;
        this.reservationExpiry = null;
        attach(new StatusObserver()); //*remove: apparently we don't want to auto attach observers on bike creation 
                                     //since it does it in bike service
    }

    @Override
    public void attach(Observer observer) {
        if (observers == null)
            observers = new ArrayList<>();
        observers.add(observer);
    }

    @Override
    public void detach(Observer observer) {
        if (observers == null)
            observers = new ArrayList<>();
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        if (observers != null) {
            for (Observer o : observers) {
                o.update("Bike " + bikeId + " changed status to " + status);
            }
        }
    }

    // notifyObservers with a custom message so we can detect the event in StatusObserver 
    public void notifyObservers(String message) {
        if (observers != null) {
            for (Observer o : observers) {
                o.update(message);
            }
        }
    }

    public static boolean isValidStatus(String status) {
        if (status == null)
            return false;
        return VALID_STATUSES.contains(status);
    }

    public boolean changeStatus(String newStatus) {
        if (!isValidStatus(newStatus)) {
            throw new IllegalArgumentException("Invalid status: " + newStatus +
                    ". Valid options are: available, reserved, maintenance, on_trip");
        }

        String normalizedNewStatus = newStatus.toLowerCase();
        String normalizedCurrentStatus = this.status.toLowerCase();

        if (STATUS_ON_TRIP.equalsIgnoreCase(this.status)
                && !STATUS_AVAILABLE.equalsIgnoreCase(newStatus)) {
            throw new IllegalStateException(
                    "Cannot change status for a bike currently on a trip (Bike ID: " + bikeId + ")");
        }

        if (normalizedCurrentStatus.equals(normalizedNewStatus)) {
            return false;
        }

        this.status = normalizedNewStatus;
        notifyObservers();
        return true;
    }

    public boolean isReservedActive() {
        if (!STATUS_RESERVED.equalsIgnoreCase(this.status))
            return false;
        if (reservationExpiry == null)
            return false;

        LocalDateTime expiryTime = timestampToLocalDateTime(reservationExpiry);
        return expiryTime != null && expiryTime.isAfter(LocalDateTime.now());
    }

    public LocalDateTime startReservationExpiry(Station station, String userId) {
        LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(station.getReservationHoldTime());
        setReservationExpiryFromLocalDateTime(expiryTime);
        setReservedByUserId(userId);
        setStatus(STATUS_RESERVED);

        ReservationManager.schedule(
            bikeId,
            () -> {
                LocalDateTime expiry = getReservationExpiryAsLocalDateTime();

                if (STATUS_RESERVED.equalsIgnoreCase(getStatus())
                    && expiry != null
                    && !LocalDateTime.now().isBefore(expiry)) {

                    setStatus(STATUS_AVAILABLE);
                    setReservationExpiry(null);
                    notifyObservers("RESERVATION_EXPIRED userId=" + reservedByUserId);
                    setReservedByUserId(null);

                    System.out.println("Reservation expired, bike " + getBikeId() + " has been set to available.");
                }
            },
            station.getReservationHoldTime()
        );

        return expiryTime;
    }

    public void clearReservation() {
        this.reservationExpiry = null;
        this.reservedByUserId = null;
        ReservationManager.cancel(this.bikeId);
    }

    // ============ Conversion Helpers ============

    /**
     * Convert LocalDateTime to Timestamp
     */
    private Timestamp localDateTimeToTimestamp(LocalDateTime localDateTime) {
        if (localDateTime == null)
            return null;
        Instant instant = localDateTime.atZone(ZoneId.systemDefault()).toInstant();
        return Timestamp.ofTimeSecondsAndNanos(instant.getEpochSecond(), instant.getNano());
    }

    /**
     * Convert Timestamp to LocalDateTime
     */
    private LocalDateTime timestampToLocalDateTime(Timestamp timestamp) {
        if (timestamp == null)
            return null;
        return LocalDateTime.ofInstant(
                Instant.ofEpochSecond(timestamp.getSeconds(), timestamp.getNanos()),
                ZoneId.systemDefault());
    }

    // ============ Getters and Setters ============

    public String getBikeId() {
        return bikeId;
    }

    public void setBikeId(String bikeId) {
        this.bikeId = bikeId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    // Firestore uses this
    public Timestamp getReservationExpiry() {
        return reservationExpiry;
    }

    public void setReservationExpiry(Timestamp reservationExpiry) {
        this.reservationExpiry = reservationExpiry;
    }

    // Convenience methods for LocalDateTime (marked with @Exclude so Firestore
    // ignores them)
    @Exclude
    public LocalDateTime getReservationExpiryAsLocalDateTime() {
        return timestampToLocalDateTime(reservationExpiry);
    }

    @Exclude
    public void setReservationExpiryFromLocalDateTime(LocalDateTime localDateTime) {
        this.reservationExpiry = localDateTimeToTimestamp(localDateTime);
    }

    public String getReservedByUserId() {
        return reservedByUserId;
    }

    public void setReservedByUserId(String reservedByUserId) {
        this.reservedByUserId = reservedByUserId;
    }

    // public void setReservationTask(java.util.concurrent.ScheduledFuture<?> task) {
    //     this.reservationTask = task;
    // }

    // public void cancelReservationTimer() {
    //     if (reservationExpiry == null) return;

    //     if (reservationTask != null && !reservationTask.isDone()) {
    //         reservationTask.cancel(true);
    //         System.out.println("[DEBUG] Cancelled reservation timer for bike " + bikeId);
    //     }

    //     reservationTask = null; // important
    // }
    
    public String getDockId() {
        return dockId;
    }

    public void setDockId(String dockId) {
        this.dockId = dockId;
    }

    public String getStationId() {
        return stationId;
    }

    public void setStationId(String stationId) {
        this.stationId = stationId;
    }

    @Override
    public String toString() {
        return "Bike{" +
                "bikeId='" + bikeId + '\'' +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                ", dockId='" + dockId + '\'' +
                ", stationId='" + stationId + '\'' +
                ", reservationExpiry=" + (reservationExpiry != null ? getReservationExpiryAsLocalDateTime() : null) +
                ", reservedByUserId='" + reservedByUserId + '\'' +
                '}';
    }
}