package com.sulitbites;

public final class CashPayment extends Payment {
    public CashPayment(double amount) { super(amount); }
    @Override public String getMethod() { return "Cash"; }
    @Override public double process(String details) {
        double cash;
        try { cash = Double.parseDouble(details); } catch (NumberFormatException e) { throw new IllegalArgumentException("Enter a valid cash amount."); }
        if (cash < getAmount()) throw new IllegalArgumentException("Cash is less than the order total.");
        return cash - getAmount();
    }
}
