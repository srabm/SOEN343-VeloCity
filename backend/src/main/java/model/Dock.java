package model;

public class Dock {
    private String dockId;
    private String state; // empty | occupied | out_of_service
    private String bikeId;
    private String stationId;

    public Dock() {}


    public Dock(String dockId, String state, String bikeId, String stationId) {
        this.dockId = dockId;
        this.state = state;
        this.bikeId = bikeId;
        this.stationId = stationId;
    }

    public String getDockId() {return dockId;}
    public void setDockId(String dockId) {this.dockId = dockId;}

    public String getState() {return state;}
    public void setState(String state) {this.state = state;}

    public String getBikeId() {return bikeId;}
    public void setBikeId(String bikeId) {this.bikeId = bikeId;}

    public String getStationId() {return stationId;}
    public void setStationId(String stationId) {this.stationId = stationId;}

}