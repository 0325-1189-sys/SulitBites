package com.sulitbites;
public final class Drink extends FoodProduct {
    public Drink(int id, String name, double price, int stock, String emoji, String description) { super(id, name, price, stock, emoji, description); }
    @Override public String getCategory() { return "Drink"; }
    @Override protected double discountRate(int quantity) { return 0; }
}
