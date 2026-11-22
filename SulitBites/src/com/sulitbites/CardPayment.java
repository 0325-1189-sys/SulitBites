package com.sulitbites;

public final class CardPayment extends Payment {
    public CardPayment(double amount) { super(amount); }
    @Override public String getMethod() { return "Card"; }
    @Override public double process(String details) {
        if (!details.replaceAll("\\s", "").matches("\\d{16}")) throw new IllegalArgumentException("Card number must contain 16 digits.");
        return 0;
    }
}
