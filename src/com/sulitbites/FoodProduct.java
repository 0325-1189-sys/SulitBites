package com.sulitbites;

/** ABSTRACTION + ENCAPSULATION. Template method: calculateTotal() is final; discountRate() is what each category overrides. */
public abstract class FoodProduct implements Stockable {
    private final int id;
    private String name;
    private double price;
    private int stock;
    private boolean available = true;
    private String emoji;
    private String description;

    protected FoodProduct(int id, String name, double price, int stock, String emoji, String description) {
        this.id = id; setName(name); setPrice(price); setStock(stock); setEmoji(emoji); setDescription(description);
    }
    public final int getId() { return id; }
    public final String getName() { return name; }
    public final double getPrice() { return price; }
    public final int getStock() { return stock; }
    public final boolean isAvailable() { return available; }
    public final String getEmoji() { return emoji; }
    public final String getDescription() { return description; }

    public final void setName(String name) { if (name == null || name.isBlank()) throw new IllegalArgumentException("Product name is required."); this.name = name.trim(); }
    public final void setPrice(double price) { if (!(price > 0)) throw new IllegalArgumentException("Price must be greater than 0."); this.price = price; }
    public final void setStock(int stock) { if (stock < 0) throw new IllegalArgumentException("Stock cannot be negative."); this.stock = stock; }
    public final void setAvailable(boolean available) { this.available = available; }
    public final void setEmoji(String emoji) { this.emoji = emoji == null || emoji.isBlank() ? "\uD83C\uDF7D\uFE0F" : emoji.trim(); }
    public final void setDescription(String description) { this.description = description == null ? "" : description.trim(); }
    public final void reduceStock(int q) { setStock(stock - q); }
    public final void addStock(int q) { setStock(stock + q); }

    public abstract String getCategory();
    protected abstract double discountRate(int quantity);                           // POLYMORPHISM
    public final double calculateTotal(int quantity) { return price * quantity * (1 - discountRate(quantity)); }
    public String getPromo() { return ""; }

    @Override public final boolean isInStock() { return available && stock > 0; }
    @Override public final String getStatus() { return !isInStock() ? "Out of stock" : stock <= 5 ? "Low stock" : "In stock"; }

    /** Factory method used when loading and when the admin adds a product. */
    public static FoodProduct create(String category, int id, String name, double price, int stock, String emoji, String description) {
        switch (category) {
            case "Meal": return new Meal(id, name, price, stock, emoji, description);
            case "Drink": return new Drink(id, name, price, stock, emoji, description);
            case "Dessert": return new Dessert(id, name, price, stock, emoji, description);
            default: throw new IllegalArgumentException("Unknown category.");
        }
    }
}
