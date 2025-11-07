package com.concordia.velocity.model;

import com.google.cloud.Timestamp;

public class Bill {

    private String billId;
    private String tripId;
    private String riderId;
    private double cost;
    private double tax;
    private double total;
    private String paymentMethodLastFour;  // Last 4 digits of payment method
    private Timestamp billingDate;
    private String status;  // "pending", "paid", "failed"

    public Bill() {}

    public Bill(String billId, String tripId, String riderId, double cost, double tax, double total) {
        this.billId = billId;
        this.tripId = tripId;
        this.riderId = riderId;
        this.cost = cost;
        this.tax = tax;
        this.total = total;
        this.billingDate = Timestamp.now();
        this.status = "pending";
    }

    public Bill(String billId, String tripId, String riderId, double cost, double tax, double total,
                String paymentMethodLastFour, String status) {
        this.billId = billId;
        this.tripId = tripId;
        this.riderId = riderId;
        this.cost = cost;
        this.tax = tax;
        this.total = total;
        this.paymentMethodLastFour = paymentMethodLastFour;
        this.billingDate = Timestamp.now();
        this.status = status;
    }

    /**
     * Calculates total from cost and tax
     */
    public void calculateTotal() {
        this.total = this.cost + this.tax;
    }

    /**
     * Calculates tax based on cost and tax rate
     * @param taxRate the tax rate (e.g., 0.14975 for 14.975%)
     */
    public void calculateTax(double taxRate) {
        this.tax = this.cost * taxRate;
        calculateTotal();
    }

    // Getters and setters
    public String getBillId() { return billId; }
    public void setBillId(String billId) { this.billId = billId; }

    public String getTripId() { return tripId; }
    public void setTripId(String tripId) { this.tripId = tripId; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public double getTax() { return tax; }
    public void setTax(double tax) { this.tax = tax; }

    public double getTotal() { return total; }
    public void setTotal(double total) { this.total = total; }

    public String getPaymentMethodLastFour() { return paymentMethodLastFour; }
    public void setPaymentMethodLastFour(String paymentMethodLastFour) {
        this.paymentMethodLastFour = paymentMethodLastFour;
    }

    public Timestamp getBillingDate() { return billingDate; }
    public void setBillingDate(Timestamp billingDate) { this.billingDate = billingDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getRiderId() { return this.riderId; }

    @Override
    public String toString() {
        return "Bill{" +
                "billId='" + billId + '\'' +
                ", tripId='" + tripId + '\'' +
                ", riderId='" + riderId + '\'' +
                ", cost=" + cost +
                ", tax=" + tax +
                ", total=" + total +
                ", paymentMethodLastFour='" + (paymentMethodLastFour != null ? "****" + paymentMethodLastFour : "null") + '\'' +
                ", billingDate=" + billingDate +
                ", status='" + status + '\'' +
                '}';
    }
}