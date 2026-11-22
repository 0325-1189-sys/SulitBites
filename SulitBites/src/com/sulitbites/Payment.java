package com.sulitbites;

public abstract class Payment {
    private final double amount;
    protected Payment(double amount) { this.amount = amount; }
    public final double getAmount() { return amount; }
    public abstract String getMethod();
    public abstract double process(String details);
}
