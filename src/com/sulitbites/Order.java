package com.sulitbites;

import java.time.Instant;
import java.util.List;

/** An order HAS-A list of OrderLine (composition). A cancelled order always counts as 0. */
public final class Order {
    public enum Status {
        PENDING("Pending"), PREPARING("Preparing"), READY("Ready"), COMPLETED("Completed"), CANCELLED("Cancelled");
        private final String label;
        Status(String label) { this.label = label; }
        public String getLabel() { return label; }
        public static Status fromLabel(String s) { for (Status x : values()) if (x.label.equalsIgnoreCase(s) || x.name().equalsIgnoreCase(s)) return x; throw new IllegalArgumentException("Unknown status."); }
    }
    private final int id;
    private final String username, customerName, type, method;
    private final double change;
    private final Instant placedAt;
    private final List<OrderLine> lines;
    private String cashier;
    private Status status;

    public Order(int id, String username, String customerName, String type, String cashier, String method, double change, Instant placedAt, List<OrderLine> lines, Status status) {
        this.id = id; this.username = username; this.customerName = customerName; this.type = type; this.cashier = cashier == null ? "" : cashier;
        this.method = method; this.change = change; this.placedAt = placedAt; this.lines = List.copyOf(lines); this.status = status;
    }
    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getCustomerName() { return customerName; }
    public String getType() { return type; }
    public String getMethod() { return method; }
    public double getChange() { return change; }
    public Instant getPlacedAt() { return placedAt; }
    public List<OrderLine> getLines() { return lines; }
    public String getCashier() { return cashier; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public void assignCashier(String name) { if (cashier.isEmpty()) cashier = name; }        // the first staff member who handles it

    public double getBilled() { return lines.stream().mapToDouble(OrderLine::getSubtotal).sum(); }
    public double getGross() { return lines.stream().mapToDouble(l -> l.getUnitPrice() * l.getQuantity()).sum(); }
    public double getDiscount() { return getGross() - getBilled(); }
    public double getTotal() { return status == Status.CANCELLED ? 0 : getBilled(); }       // cancelled = 0, never counted as sales
}
