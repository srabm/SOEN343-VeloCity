package com.concordia.velocity.model;

public class Rider extends User {
    public Rider(String fullName, String address, String email, String phoneNumber, String username) {
        super(fullName, address, email, phoneNumber, username);
    }

    public String reserveBike(Bike bike, Station station) {
        bike.setStatus("reserved");
        bike.setReservationUser(this);
        bike.startReservationExpiry(station);
        return "You have successfully reserved bike " + bike.getBikeId() + " in dock " + bike.getDockId() + ". Your unlock code is \"1234\". You have until " + bike.getReservationExpiry() + " to unlock it.";
    }
}