package com.concordia.velocity.model;

import com.concordia.velocity.observer.Subject;
import com.concordia.velocity.observer.Observer;
import com.concordia.velocity.model.BikeStatus;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;


public class Bike implements Subject {
   
    private String bikeId;
    private String status;
    private String type; 
    private Date reservationExpiry;
    private String dockId; // i thought of using private Dock dock, but Firebase can't serialize nested custom objects because it leads to circular references (a bike contains a dock, which contains a station, which contains bikes)
    private String stationId;

    private transient List<Observer> observers = new ArrayList<>();

    public Bike() {}

    public Bike(String bikeId, String status, String type, String dockId, String stationId) {
        this.bikeId = bikeId;
        this.status = status;
        this.type = type;
        this.dockId = dockId;
        this.stationId = stationId;
        this.reservationExpiry = null;
    }

    @Override
    public void attach(Observer observer) {
        if (observers == null) observers = new ArrayList<>();
        observers.add(observer);
    }

    @Override
    public void detach(Observer observer) {
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

    public void reportProblem() {
        status = BikeStatus.valueOf("MAINTENANCE");
        notifyObservers();
//        System.out.print("Bike is now in maintenance")
    }

    // setters, getters
    public String getBikeId() {return bikeId;}
    public void setBikeId(String bikeId) {this.bikeId = bikeId;}
    
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}

    public String getType() {return type;}
    public void setType(String type) {this.type = type;}

    public Date getReservationExpiry() {return reservationExpiry;}
    public void setReservationExpiry(Date reservationExpiry) {this.reservationExpiry = reservationExpiry;}

    public String getDockId() {return dockId;}
    public void setDockId(String dockId) {this.dockId = dockId;}

    public String getStationId() {return stationId;}
    public void setStationId(String stationId) {this.stationId = stationId;}

    // Returns true if this bike is currently reserved and has not expired. 
    public boolean isReservedActive() {
        if (!BikeStatus.RESERVED.name().equalsIgnoreCase(this.status)) return false;
        if (reservationExpiry == null) return false;
        return reservationExpiry.after(new Date());
    }

    @Override
    public String toString() {
        return "Bike{" +
                "bikeId='" + bikeId + '\'' +
                ", status='" + status + '\'' +
                ", type='" + type + '\'' +
                ", dockId='" + dockId + '\'' +
                ", stationId='" + stationId + '\'' +
                ", reservationExpiry=" + reservationExpiry +
                '}';
    }

}