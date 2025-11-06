package com.concordia.velocity.model;

abstract class User {
    private String id;
    private String firstName;
    private String lastName;
    private String address;
    private String email;
    private String phoneNumber;
    private Boolean isOperator;
    private PaymentInfo paymentInfo;

    public User() {}

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

    public Boolean getIsOperator() { return isOperator; }
    public void setIsOperator(Boolean isOperator) { this.isOperator = isOperator; }

    public PaymentInfo getPaymentInfo() { return paymentInfo; }
    public void setPaymentInfo(PaymentInfo paymentInfo) { this.paymentInfo = paymentInfo; }

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
        private String cardBrand;
        private String last4;
        private String cardholderName;
        private String billingAddress;

        public PaymentInfo() {}

        public Boolean getHasPaymentMethod() { return hasPaymentMethod; }
        public void setHasPaymentMethod(Boolean hasPaymentMethod) { this.hasPaymentMethod = hasPaymentMethod; }

        public String getCardBrand() { return cardBrand; }
        public void setCardBrand(String cardBrand) { this.cardBrand = cardBrand; }

        public String getLast4() { return last4; }
        public void setLast4(String last4) { this.last4 = last4; }

        public String getCardholderName() { return cardholderName; }
        public void setCardholderName(String cardholderName) { this.cardholderName = cardholderName; }

        public String getBillingAddress() { return billingAddress; }
        public void setBillingAddress(String billingAddress) { this.billingAddress = billingAddress; }
    }