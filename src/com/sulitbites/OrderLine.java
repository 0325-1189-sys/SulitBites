package com.sulitbites;

/** One saved line of an order. Immutable, so a later price change never alters an old receipt. */
public final class OrderLine {
    private final int productId, quantity;
    private final String name, emoji;
    private final double unitPrice, subtotal;
    public OrderLine(int productId, String name, String emoji, int quantity, double unitPrice, double subtotal) {
        this.productId = productId; this.name = name; this.emoji = emoji; this.quantity = quantity; this.unitPrice = unitPrice; this.subtotal = subtotal;
    }
    public int getProductId() { return productId; }
    public String getName() { return name; }
    public String getEmoji() { return emoji; }
    public int getQuantity() { return quantity; }
    public double getUnitPrice() { return unitPrice; }
    public double getSubtotal() { return subtotal; }
}
