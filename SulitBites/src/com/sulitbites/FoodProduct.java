package com.sulitbites;

public abstract class FoodProduct {
    private final int id;
    private final String name;
    private final double price;
    private int stock;
    private boolean available;

    protected FoodProduct(int id, String name, double price, int stock) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name is required.");
        if (price <= 0) throw new IllegalArgumentException("Price must be greater than zero.");
        if (stock < 0) throw new IllegalArgumentException("Stock cannot be negative.");
        this.id = id;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.available = true;
    }

    public final int getId() { return id; }
    public final String getName() { return name; }
    public final double getPrice() { return price; }
    public final int getStock() { return stock; }
    public final void setStock(int stock) {
        if (stock < 0) throw new IllegalArgumentException("Stock cannot be negative.");
        this.stock = stock;
    }
    public final boolean isAvailable() { return available; }
    public final void setAvailable(boolean available) { this.available = available; }
    public final boolean isInStock() { return available && stock > 0; }
    public final String getStatus() { return !isInStock() ? "Out of stock" : stock <= 5 ? "Low stock" : "In stock"; }
    public abstract String getCategory();
    public abstract double calculateTotal(int quantity);

    @Override public String toString() {
        return String.format("%-3d %-22s %-9s PHP %7.2f  Stock: %-3d %-13s", id, name, getCategory(), price, stock, getStatus());
    }
}
