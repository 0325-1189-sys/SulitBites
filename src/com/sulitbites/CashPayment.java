package com.sulitbites;
public final class CashPayment extends Payment {
    public CashPayment(double amount) { super(amount); }
    @Override public String getMethod() { return "Cash"; }
    @Override public double process(String details) {
        double cash;
        try { cash = Double.parseDouble(details == null ? "" : details.trim()); } catch (NumberFormatException e) { throw new IllegalArgumentException("Enter the cash amount you are handing over."); }
        if (cash < getAmount()) throw new IllegalArgumentException(String.format("Cash is less than the total. Enter at least %.2f.", getAmount()));
        return cash - getAmount();
    }
}
