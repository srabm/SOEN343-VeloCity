package com.concordia.velocity.model;

abstract class User {
    private String fullName;
    private String address;
    private String email;
    private String phoneNumber;
    private String username;

    public User(String fullName, String address, String email, String phoneNumber, String username) {
        this.fullName = fullName;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.username = username;
    }

    @Override
    public String toString() {
        return fullName;
    }
}