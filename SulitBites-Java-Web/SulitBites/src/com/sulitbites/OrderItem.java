package com.sulitbites;

/** A cart entry: a live product and a quantity. */
public final class OrderItem {
    private final FoodProduct product;
    private final int quantity;
    public OrderItem(FoodProduct product, int quantity) {
        if (quantity <= 0) throw new IllegalArgumentException("Quantity must be at least 1.");
        this.product = product; this.quantity = quantity;
    }
    public FoodProduct getProduct() { return product; }
    public int getQuantity() { return quantity; }
    public double getSubtotal() { return product.calculateTotal(quantity); }   // polymorphic call
    public OrderLine toLine() { return new OrderLine(product.getId(), product.getName(), product.getEmoji(), quantity, product.getPrice(), getSubtotal()); }
}
