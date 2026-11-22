package com.sulitbites;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public final class SulitBitesApp {
    private final Store store = new Store();
    private final Scanner scanner = new Scanner(System.in);
    private User currentUser;

    public void run() {
        System.out.println("=== SULIT BITES FOOD ORDERING SYSTEM ===");
        while (true) {
            if (currentUser == null) { if (!login()) break; }
            else if (currentUser instanceof Admin) adminMenu();
            else if (currentUser instanceof Cashier) cashierMenu();
            else customerMenu();
        }
        System.out.println("Thank you for using Sulit Bites!");
    }

    private boolean login() {
        System.out.println("\n1. Login  0. Exit");
        String choice = ask("Choose: ");
        if (choice.equals("0")) return false;
        if (!choice.equals("1")) return true;
        String username = ask("Username: "), password = ask("Password: ");
        currentUser = store.authenticate(username, password);
        if (currentUser == null) System.out.println("Invalid username or password.");
        else System.out.println("Welcome, " + currentUser.getName() + " (" + currentUser.getRole() + ")");
        return true;
    }

    private void customerMenu() {
        System.out.println("\n--- CUSTOMER MENU ---\n1. View menu\n2. Add to cart and checkout\n3. My orders\n0. Logout");
        switch (ask("Choose: ")) { case "1" -> showProducts(); case "2" -> checkout(); case "3" -> showMyOrders(); case "0" -> currentUser = null; default -> System.out.println("Invalid choice."); }
    }
    private void cashierMenu() {
        System.out.println("\n--- CASHIER MENU ---\n1. View menu\n2. Create walk-in order\n3. View/update orders\n0. Logout");
        switch (ask("Choose: ")) { case "1" -> showProducts(); case "2" -> checkout(); case "3" -> manageOrders(false); case "0" -> currentUser = null; default -> System.out.println("Invalid choice."); }
    }
    private void adminMenu() {
        System.out.println("\n--- ADMIN MENU ---\n1. View menu\n2. Create order\n3. Add product\n4. Restock product\n5. View/update orders\n6. Customer summary\n0. Logout");
        switch (ask("Choose: ")) { case "1" -> showProducts(); case "2" -> checkout(); case "3" -> addProduct(); case "4" -> restock(); case "5" -> manageOrders(true); case "6" -> customerSummary(); case "0" -> currentUser = null; default -> System.out.println("Invalid choice."); }
    }

    private void showProducts() {
        System.out.println("\nID  PRODUCT                 CATEGORY   PRICE       STOCK STATUS");
        store.getProducts().forEach(System.out::println);
    }
    private void checkout() {
        List<OrderItem> cart = new ArrayList<>();
        while (true) {
            showProducts(); String value = ask("Product ID (0 to finish): ");
            int id = parseInt(value, -1); if (id == 0) break;
            FoodProduct product = store.findProduct(id);
            if (product == null || !product.isInStock()) { System.out.println("Product unavailable."); continue; }
            int quantity = parseInt(ask("Quantity: "), 0);
            if (quantity <= 0 || quantity > product.getStock()) { System.out.println("Invalid quantity or insufficient stock."); continue; }
            cart.add(new OrderItem(product, quantity)); System.out.println("Added to cart.");
        }
        if (cart.isEmpty()) { System.out.println("Cart is empty."); return; }
        double total = cart.stream().mapToDouble(OrderItem::getSubtotal).sum();
        System.out.printf("Total: PHP %.2f%n", total);
        String method = ask("Payment (Cash/GCash/Card): ");
        Payment payment = switch (method.toLowerCase()) { case "cash" -> new CashPayment(total); case "gcash" -> new GCashPayment(total); case "card" -> new CardPayment(total); default -> null; };
        if (payment == null) { System.out.println("Invalid payment method."); return; }
        try {
            double change = payment.process(ask("Payment details: "));
            cart.forEach(i -> i.getProduct().setStock(i.getProduct().getStock() - i.getQuantity()));
            Order order = store.addOrder(currentUser, cart, payment, change);
            System.out.printf("Order #%d placed. Change: PHP %.2f%n", order.getId(), change);
        } catch (IllegalArgumentException e) { System.out.println("Payment failed: " + e.getMessage()); }
    }
    private void showMyOrders() { store.getOrders().stream().filter(o -> o.getCustomer() == currentUser).forEach(this::printOrder); }
    private void manageOrders(boolean canCancel) {
        if (store.getOrders().isEmpty()) { System.out.println("No orders yet."); return; }
        store.getOrders().forEach(this::printOrder);
        int id = parseInt(ask("Order ID to update (0 to cancel): "), 0); if (id == 0) return;
        Order order = store.getOrders().stream().filter(o -> o.getId() == id).findFirst().orElse(null);
        if (order == null) { System.out.println("Order not found."); return; }
        System.out.println("1 Pending  2 Preparing  3 Ready  4 Completed" + (canCancel ? " 5 Cancelled" : ""));
        int status = parseInt(ask("New status: "), 0);
        if (status < 1 || status > (canCancel ? 5 : 4)) { System.out.println("Invalid status."); return; }
        order.setStatus(Order.Status.values()[status - 1]); System.out.println("Order updated.");
    }
    private void printOrder(Order o) { System.out.printf("Order #%d | %s | PHP %.2f | %s | %s%n", o.getId(), o.getCustomer().getName(), o.getTotal(), o.getPaymentMethod(), o.getStatus()); }
    private void addProduct() { String category = ask("Category (Meal/Drink/Dessert): "), name = ask("Name: "); double price = parseDouble(ask("Price: "), 0); int stock = parseInt(ask("Stock: "), -1); try { System.out.println("Added: " + store.addProduct(category, name, price, stock)); } catch (IllegalArgumentException e) { System.out.println(e.getMessage()); } }
    private void restock() { showProducts(); FoodProduct p = store.findProduct(parseInt(ask("Product ID: "), -1)); if (p == null) { System.out.println("Product not found."); return; } p.setStock(parseInt(ask("New stock: "), p.getStock())); p.setAvailable(true); System.out.println("Stock updated."); }
    private void customerSummary() { store.getUsers().stream().filter(u -> u instanceof Customer).forEach(u -> System.out.println(u.getName() + " (" + u.getUsername() + ")")); }
    private String ask(String prompt) { System.out.print(prompt); return scanner.nextLine().trim(); }
    private int parseInt(String value, int fallback) { try { return Integer.parseInt(value); } catch (NumberFormatException e) { return fallback; } }
    private double parseDouble(String value, double fallback) { try { return Double.parseDouble(value); } catch (NumberFormatException e) { return fallback; } }
}
