package com.concordia.velocity.model;

public class Rider extends User {
    public Rider(String fullName, String address, String email, String phoneNumber, String username) {
        super(fullName, address, email, phoneNumber, username);
    }

    // TODO either here or, more likely, in a service : report bike (turns status to maintenance)

}