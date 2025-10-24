package com.concordia.velocity.model;

import com.concordia.velocity.observer.Observer;
import com.concordia.velocity.observer.Subject;
import java.util.ArrayList;
import java.util.List;


public class Dock implements Subject{
    private String dockId;
    private String status; // empty | occupied | out_of_service
    private String bikeId;
    private String stationId;

    private transient List<Observer> observers = new ArrayList<>();

    public Dock() {}


    public Dock(String dockId, String status, String bikeId, String stationId) {
        this.dockId = dockId;
        this.status = status;
        this.bikeId = bikeId;
        this.stationId = stationId;
    }

    
    public String getDockId() {return dockId;}
    public void setDockId(String dockId) {this.dockId = dockId;}

    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}

    public String getBikeId() {return bikeId;}
    public void setBikeId(String bikeId) {this.bikeId = bikeId;}

    public String getStationId() {return stationId;}
    public void setStationId(String stationId) {this.stationId = stationId;}

     @Override
    public void attach(Observer o) {
        if (observers == null) observers = new ArrayList<>();
        observers.add(o);
    }

    @Override
    public void detach(Observer o) {
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
}