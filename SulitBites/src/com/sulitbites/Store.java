package com.sulitbites;

import java.util.ArrayList;
import java.util.List;

public final class Store {
    private final List<User> users = new ArrayList<>();
    private final List<FoodProduct> products = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private int nextOrderId = 1001;
    private int nextProductId = 112;

    public Store() {
        users.add(new Admin("admin", "admin123", "Nicole Admin"));
        users.add(new Cashier("cashier", "cashier123", "Maria Cashier"));
        users.add(new Customer("juan", "juan123", "Juan Dela Cruz"));
        products.add(new Meal(101, "Classic Burger", 99, 40));
        products.add(new Meal(102, "Fried Chicken", 119, 35));
        products.add(new Meal(103, "Crispy Fries", 59, 60));
        products.add(new Meal(104, "Spaghetti", 89, 30));
        products.add(new Meal(105, "Chicken Adobo Rice", 109, 25));
        products.add(new Drink(106, "Iced Tea", 39, 80));
        products.add(new Drink(107, "Iced Coffee", 65, 50));
        products.add(new Drink(108, "Hot Coffee", 55, 50));
        products.add(new Dessert(109, "Halo-Halo", 79, 30));
        products.add(new Dessert(110, "Leche Flan", 55, 30));
        products.add(new Dessert(111, "Ice Cream Cup", 45, 40));
    }
    public List<User> getUsers() { return users; }
    public List<FoodProduct> getProducts() { return products; }
    public List<Order> getOrders() { return orders; }
    public User authenticate(String username, String password) { return users.stream().filter(u -> u.getUsername().equalsIgnoreCase(username) && u.getPassword().equals(password)).findFirst().orElse(null); }
    public FoodProduct findProduct(int id) { return products.stream().filter(p -> p.getId() == id).findFirst().orElse(null); }
    public Order addOrder(User customer, List<OrderItem> items, Payment payment, double change) { Order o = new Order(nextOrderId++, customer, items, payment.getMethod(), change); orders.add(0, o); return o; }
    public FoodProduct addProduct(String category, String name, double price, int stock) {
        FoodProduct p = switch (category.toLowerCase()) { case "meal" -> new Meal(nextProductId++, name, price, stock); case "drink" -> new Drink(nextProductId++, name, price, stock); case "dessert" -> new Dessert(nextProductId++, name, price, stock); default -> throw new IllegalArgumentException("Unknown category."); };
        products.add(p); return p;
    }
}
