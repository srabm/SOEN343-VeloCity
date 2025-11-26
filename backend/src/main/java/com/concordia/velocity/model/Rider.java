package com.concordia.velocity.model;

import com.concordia.velocity.state.*;
import com.google.cloud.Timestamp;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Rider {
    private String id;
    private String firstName;
    private String lastName;
    private String address;
    private String email;
    private String phoneNumber;
    private int flexDollars;
    private Boolean isOperator;
    private PaymentInfo paymentInfo;
    private List<Timestamp> missedReservationTimestamps = new ArrayList<>();

    private TierState tierState;
    private String tier; // For Firestore persistence to store "NoTier", "Bronze", "Silver", "Gold"

    public Rider() { // default constructor for Firestore
        this.tierState = new NoTierState(); // initially the rider has no tier
        this.tier = "NoTier";
    }


    public Rider(String firstName, String lastName, String address, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.tierState = new NoTierState(); // initially the rider has no tier
        this.tier = "NoTier";
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }
    public int getFlexDollars() { return flexDollars; }
    public void setFlexDollars(int flexDollars) { this.flexDollars = flexDollars; }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Boolean getIsOperator() {
        return isOperator;
    }

    public void setIsOperator(Boolean isOperator) {
        this.isOperator = isOperator;
    }

    public PaymentInfo getPaymentInfo() {
        return paymentInfo;
    }

    public void setPaymentInfo(PaymentInfo paymentInfo) {
        this.paymentInfo = paymentInfo;
    }

    public TierState getTierState() {
        return tierState;
    }

    public boolean redeemFlexDollar() {
        if (this.flexDollars > 0) {
            this.flexDollars = this.flexDollars - 1;
            return true;
        }
        return false;
    }

    public String getFullName() {
        String fn = firstName != null ? firstName : "";
        String ln = lastName != null ? lastName : "";
        return (fn + " " + ln).trim();
    }

    public String getRole() {
        return isOperator != null && isOperator ? "OPERATOR" : "RIDER";
    }

    public List<Timestamp> getMissedReservationTimestamps() {
        return missedReservationTimestamps;
    }

    public void setMissedReservationTimestamps(List<Timestamp> timestamps) {
        this.missedReservationTimestamps = timestamps;
    }

    // add a missed reservation timestamp
    public void addMissedReservation() {
        if (missedReservationTimestamps == null) {
            missedReservationTimestamps = new ArrayList<>();
        }
        missedReservationTimestamps.add(Timestamp.now());
    }

    // Helper to count only last year
    public int getMissedReservationsLastYear() {
        if (missedReservationTimestamps == null)
            return 0;

        LocalDate oneYearAgo = LocalDate.now().minusYears(1);
        int count = 0;

        for (Timestamp ts : missedReservationTimestamps) {
            LocalDate missedDate = ts.toSqlTimestamp().toLocalDateTime().toLocalDate();
            if (missedDate.isAfter(oneYearAgo)) {
                count++;
            }
        }
        return count;
    }

    public static class PaymentInfo {
        private Boolean hasPaymentMethod;
        private String cardNumber;
        private String cvc;
        private String cardholderName;
        private String expiryDate;

        public PaymentInfo() {
        }

        public Boolean getHasPaymentMethod() {
            return hasPaymentMethod;
        }

        public void setHasPaymentMethod(Boolean hasPaymentMethod) {
            this.hasPaymentMethod = hasPaymentMethod;
        }

        public String getCardNumber() {
            return cardNumber;
        }

        public void setCardNumber(String cardNumber) {
            this.cardNumber = cardNumber;
        }

        public String getCvc() {
            return cvc;
        }

        public void setCvc(String cvc) {
            this.cvc = cvc;
        }

        public String getCardholderName() {
            return cardholderName;
        }

        public void setCardholderName(String cardholderName) {
            this.cardholderName = cardholderName;
        }

        public String getExpiryDate() {
            return expiryDate;
        }

        public void setExpiryDate(String expiryDate) {
            this.expiryDate = expiryDate;
        }
    }

    // ==== TIER MANAGEMENT ====

    /**
     * Gets the tier tier string for Firestore persistence
     */
    public String getTier() {
        return tier;
    }

    /**
     * Sets tier from Firestore and reconstructs the TierState object
     */
    public void setTier(String tier) {
        this.tier = tier;
        // Reconstruct tierState from string when loading from Firestore
        switch (tier) {
            case "Bronze":
                this.tierState = new BronzeTierState();
                break;
            case "Silver":
                this.tierState = new SilverTierState();
                break;
            case "Gold":
                this.tierState = new GoldTierState();
                break;
            default:
                this.tierState = new NoTierState();
                break;
        }
    }

    /*  Evaluates tier based on rider stats and tracks changes
     * @return TierChange object containing old and new tier, or null if no change
     */
    public TierChange evaluateTier(RiderStats riderStats)  {
        String oldTier = this.tier;

        // Let the current state evaluate and potentially change the tier
        tierState.evaluateTier(this, riderStats);

        // Update tier string after evaluation
        this.tier = getTierName();

        // Log tier change for notification
        if (!oldTier.equals(this.tier)) {
            System.out.println("TIER CHANGE for " + getFullName() + ": " + oldTier + " → " + this.tier + "\n");
            return new TierChange(oldTier, this.tier);
        }

        System.out.println("\n" + getFullName() + " is in tier: " + this.tier);
        return null;
    }

    /**
     * Sets the tier state (called by TierState implementations)
     */
    public void setTierState(TierState tierState) {
        this.tierState = tierState;
        this.tier = getTierName();
    }

    public double applyDiscount(double price) {
        return tierState.applyDiscount(price);
    }

    public int getExtraHoldMinutes() {
        return tierState.getExtraHoldMinutes();
    }

    public String getTierName() {
        return tierState.getClass().getSimpleName().replace("TierState", "");
    }

    /**
     * Inner class to represent tier change information
     */
    public static class TierChange {
        private final String oldTier;
        private final String newTier;

        public TierChange(String oldTier, String newTier) {
            this.oldTier = oldTier;
            this.newTier = newTier;
        }

        public String getOldTier() {
            return oldTier;
        }

        public String getNewTier() {
            return newTier;
        }
    }
}