package com.concordia.velocity.backend.model;

import java.time.LocalDateTime;




public class Bike {
   
    private String bikeId;
    private String status;
    private String type; // should i keep this as a String type? or should I make the bike class abstract so that it can be extended to different bike types?
    private LocalDateTime reservationExpiry;
    private String dockId; // i thought of using private Dock dock, but Firebase can't serialize nested custom objects because it leads to circular references (a bike contains a dock, which contains a station, which contains bikes)
    private String stationId;

    public Bike() {}

    public Bike(String bikeId, String status, String type, String dockId, String stationId) {
        this.bikeId = bikeId;
        this.status = status;
        this.type = type;
        this.dockId = dockId;
        this.stationId = stationId;
    }
    public String getBikeId() {return bikeId;}
    public void setBikeId(String bikeId) {this.bikeId = bikeId;}
    
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}

    public String getType() {return type;}
    public void setType(String type) {this.type = type;}

    public LocalDateTime getReservationExpiry() {return reservationExpiry;}
    public void setReservationExpiry(LocalDateTime reservationExpiry) {this.reservationExpiry = reservationExpiry;}

    public String getDockId() {return dockId;}
    public void setDockId(String dockId) {this.dockId = dockId;}

    public String getStationId() {return stationId;}
    public void setStationId(String stationId) {this.stationId = stationId;}
}