package com.sulitbites;

public final class GCashPayment extends Payment {
    public GCashPayment(double amount) { super(amount); }
    @Override public String getMethod() { return "GCash"; }
    @Override public double process(String details) {
        if (!details.matches("09\\d{9}")) throw new IllegalArgumentException("GCash number must be 11 digits and start with 09.");
        return 0;
    }
}
