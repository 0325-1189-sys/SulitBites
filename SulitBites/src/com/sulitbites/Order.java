package com.sulitbites;

import java.time.LocalDateTime;
import java.util.List;

public final class Order {
    public enum Status { PENDING, PREPARING, READY, COMPLETED, CANCELLED }
    private final int id;
    private final User customer;
    private final List<OrderItem> items;
    private final String paymentMethod;
    private final double change;
    private final LocalDateTime date;
    private Status status;

    public Order(int id, User customer, List<OrderItem> items, String paymentMethod, double change) {
        this.id = id; this.customer = customer; this.items = List.copyOf(items);
        this.paymentMethod = paymentMethod; this.change = change; this.date = LocalDateTime.now(); this.status = Status.PENDING;
    }
    public int getId() { return id; }
    public User getCustomer() { return customer; }
    public List<OrderItem> getItems() { return items; }
    public String getPaymentMethod() { return paymentMethod; }
    public double getChange() { return change; }
    public LocalDateTime getDate() { return date; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public double getTotal() { return status == Status.CANCELLED ? 0 : items.stream().mapToDouble(OrderItem::getSubtotal).sum(); }
}
