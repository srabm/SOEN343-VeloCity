package com.concordia.velocity.model;

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

    public Rider() {}

    public Rider(String firstName, String lastName, String address, String email, String phoneNumber) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
    }

    public String getId() {return id;}
    public void setId(String id) {this.id = id;}

    public String getFirstName() {return firstName;}
    public void setFirstName(String firstName) { this.firstName = firstName; }

    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getPhoneNumber() { return phoneNumber; }
    public void setPhoneNumber(String phoneNumber) { this.phoneNumber = phoneNumber; }

    public int getFlexDollars() { return flexDollars; }
    public void setFlexDollars(int flexDollars) { this.flexDollars = flexDollars; }

    public Boolean getIsOperator() { return isOperator; }
    public void setIsOperator(Boolean isOperator) { this.isOperator = isOperator; }

    public PaymentInfo getPaymentInfo() { return paymentInfo; }
    public void setPaymentInfo(PaymentInfo paymentInfo) { this.paymentInfo = paymentInfo; }

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

    public static class PaymentInfo {
        private Boolean hasPaymentMethod;
        private String cardNumber;
        private String cvc;
        private String cardholderName;
        private String expiryDate;

        public PaymentInfo() {}

        public Boolean getHasPaymentMethod() { return hasPaymentMethod; }
        public void setHasPaymentMethod(Boolean hasPaymentMethod) { this.hasPaymentMethod = hasPaymentMethod; }

        public String getCardNumber() { return cardNumber; }
        public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }

        public String getCvc() { return cvc; }
        public void setCvc(String cvc) { this.cvc = cvc; }

        public String getCardholderName() { return cardholderName; }
        public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }

        public String getExpiryDate() { return expiryDate; }
        public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }
    }
}