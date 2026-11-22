package com.sulitbites;

public final class OrderItem {
    private final FoodProduct product;
    private final int quantity;
    public OrderItem(FoodProduct product, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be positive.");
        this.product = product; this.quantity = quantity;
    }
    public FoodProduct getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getSubtotal() { return product.calculateTotal(quantity); }
}
