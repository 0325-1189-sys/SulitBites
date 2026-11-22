package com.sulitbites;

public final class Drink extends FoodProduct {
    public Drink(int id, String name, double price, int stock) { super(id, name, price, stock); }
    @Override public String getCategory() { return "Drink"; }
    @Override public double calculateTotal(int quantity) { return getPrice() * quantity; }
}
