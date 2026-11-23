package com.sulitbites;
public final class Dessert extends FoodProduct {
    public Dessert(int id, String name, double price, int stock, String emoji, String description) { super(id, name, price, stock, emoji, description); }
    @Override public String getCategory() { return "Dessert"; }
    @Override protected double discountRate(int quantity) { return quantity >= 3 ? 0.05 : 0; }
    @Override public String getPromo() { return "Buy 3 or more, get 5% off"; }
}
