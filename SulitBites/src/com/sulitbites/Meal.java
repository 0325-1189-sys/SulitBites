package com.sulitbites;

public final class Meal extends FoodProduct {
    public Meal(int id, String name, double price, int stock) { super(id, name, price, stock); }
    @Override public String getCategory() { return "Meal"; }
    @Override public double calculateTotal(int quantity) { return getPrice() * quantity * (quantity >= 5 ? 0.90 : 1.00); }
}
