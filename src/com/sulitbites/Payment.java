package com.sulitbites;

/** ABSTRACTION: each payment method checks its own details and returns the change. */
public abstract class Payment {
    private final double amount;
    protected Payment(double amount) { this.amount = amount; }
    public final double getAmount() { return amount; }
    public abstract String getMethod();
    /** Validates the payment details. Throws IllegalArgumentException with a clear message. Returns the change. */
    public abstract double process(String details);
}
