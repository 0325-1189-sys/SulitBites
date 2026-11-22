package com.sulitbites;

public final class Dessert extends FoodProduct {
    public Dessert(int id, String name, double price, int stock) { super(id, name, price, stock); }
    @Override public String getCategory() { return "Dessert"; }
    @Override public double calculateTotal(int quantity) { return getPrice() * quantity * (quantity >= 3 ? 0.95 : 1.00); }
}
