package com.sulitbites;
public final class Meal extends FoodProduct {
    public Meal(int id, String name, double price, int stock, String emoji, String description) { super(id, name, price, stock, emoji, description); }
    @Override public String getCategory() { return "Meal"; }
    @Override protected double discountRate(int quantity) { return quantity >= 5 ? 0.10 : 0; }
    @Override public String getPromo() { return "Buy 5 or more, get 10% off"; }
}
