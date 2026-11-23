package com.sulitbites;

import java.time.Instant;
import java.util.*;

/** All data and all business rules. The web layer only calls these methods. Not thread-safe on its own: the web app locks it. */
public final class Store {
    private final List<User> users = new ArrayList<>();
    private final List<FoodProduct> products = new ArrayList<>();
    private final List<Order> orders = new ArrayList<>();
    private final Repository repository;

    public Store(Repository repository) {
        this.repository = repository;
        repository.load(this);
        seedIfEmpty();
        persist();
    }
    void replaceAll(List<User> u, List<FoodProduct> p, List<Order> o) { users.clear(); users.addAll(u); products.clear(); products.addAll(p); orders.clear(); orders.addAll(o); }
    private void persist() { repository.save(this); }

    private void seedIfEmpty() {
        if (users.isEmpty()) {
            users.add(new Admin("admin", "admin123", "Nicole Admin"));
            users.add(new Cashier("cashier", "cashier123", "Maria Cashier"));
            users.add(new Customer("juan", "juan123", "Juan Dela Cruz"));
        }
        if (products.isEmpty()) {
            Object[][] s = {
                {"Meal", 101, "Classic Burger", 99, 40, "\uD83C\uDF54", "Juicy beef patty with fresh vegetables."}, {"Meal", 102, "Fried Chicken", 119, 35, "\uD83C\uDF57", "Crunchy outside, juicy inside."},
                {"Meal", 103, "Crispy Fries", 59, 60, "\uD83C\uDF5F", "Golden fries with a pinch of salt."}, {"Meal", 104, "Spaghetti", 89, 30, "\uD83C\uDF5D", "Sweet Filipino-style spaghetti."},
                {"Meal", 105, "Chicken Adobo Rice", 109, 25, "\uD83C\uDF5B", "Garlicky adobo over steamed rice."}, {"Drink", 106, "Iced Tea", 39, 80, "\uD83E\uDD64", "Chilled and lightly sweet."},
                {"Drink", 107, "Iced Coffee", 65, 50, "\uD83E\uDDCB", "Bold coffee over ice."}, {"Drink", 108, "Hot Coffee", 55, 50, "\u2615", "Freshly brewed, served hot."},
                {"Dessert", 109, "Halo-Halo", 79, 30, "\uD83C\uDF67", "Shaved ice with sweet toppings."}, {"Dessert", 110, "Leche Flan", 55, 30, "\uD83C\uDF6E", "Silky caramel custard."},
                {"Dessert", 111, "Ice Cream Cup", 45, 40, "\uD83C\uDF68", "Creamy vanilla scoop."}};
            for (Object[] r : s) products.add(FoodProduct.create((String) r[0], (Integer) r[1], (String) r[2], ((Integer) r[3]).doubleValue(), (Integer) r[4], (String) r[5], (String) r[6]));
        }
    }

    public List<User> getUsers() { return Collections.unmodifiableList(users); }
    public List<FoodProduct> getProducts() { return Collections.unmodifiableList(products); }
    public List<Order> getOrders() { return Collections.unmodifiableList(orders); }
    public User findUser(String username) { for (User u : users) if (u.getUsername().equalsIgnoreCase(username == null ? "" : username)) return u; return null; }
    public FoodProduct findProduct(int id) { for (FoodProduct p : products) if (p.getId() == id) return p; return null; }
    public Order findOrder(int id) { for (Order o : orders) if (o.getId() == id) return o; return null; }
    private int nextProductId() { int m = 100; for (FoodProduct p : products) m = Math.max(m, p.getId()); return m + 1; }
    private int nextOrderId() { int m = 1000; for (Order o : orders) m = Math.max(m, o.getId()); return m + 1; }

    // ---- accounts
    public User authenticate(String username, String password) { User u = findUser(username); return u != null && u.verify(password) ? u : null; }
    public User register(String name, String username, String password) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Enter your full name.");
        if (name.trim().length() > 60) throw new IllegalArgumentException("Name is too long.");
        if (username == null || !username.matches("[A-Za-z0-9_]{3,20}")) throw new IllegalArgumentException("Username must be 3 to 20 letters, numbers or underscores.");
        if (password == null || password.length() < 4) throw new IllegalArgumentException("Password needs 4 or more characters.");
        if (findUser(username) != null) throw new IllegalArgumentException("That username is taken. Pick another one.");
        User u = new Customer(username, password, name); users.add(u); persist(); return u;
    }

    // ---- products and stock (the web layer checks that the user is the admin)
    public FoodProduct addProduct(String category, String name, double price, int stock, String emoji, String description) {
        FoodProduct p = FoodProduct.create(category, nextProductId(), name, price, stock, emoji, description); products.add(p); persist(); return p;
    }
    public void updateProduct(int id, String name, double price, String emoji, String description) {
        FoodProduct p = require(id); p.setName(name); p.setPrice(price); p.setEmoji(emoji); p.setDescription(description); persist();
    }
    /** mode "add" adds to the current stock, mode "set" sets an exact stock. */
    public void changeStock(int id, String mode, int amount, boolean available) {
        FoodProduct p = require(id);
        if (mode.equals("add")) { if (amount < 1) throw new IllegalArgumentException("Enter 1 or more pieces to add."); p.addStock(amount); } else p.setStock(amount);
        p.setAvailable(available); persist();
    }
    public void toggleAvailable(int id) { FoodProduct p = require(id); p.setAvailable(!p.isAvailable()); persist(); }
    public void deleteProduct(int id) { products.remove(require(id)); persist(); }
    private FoodProduct require(int id) { FoodProduct p = findProduct(id); if (p == null) throw new IllegalArgumentException("Product not found."); return p; }

    // ---- orders
    public Order placeOrder(User actor, Map<Integer, Integer> cart, String method, String details, String walkInName) {
        if (cart == null || cart.isEmpty()) throw new IllegalArgumentException("Your cart is empty.");
        List<OrderItem> items = new ArrayList<>();
        for (Map.Entry<Integer, Integer> e : cart.entrySet()) {
            FoodProduct p = findProduct(e.getKey());
            if (p == null || !p.isInStock()) throw new IllegalArgumentException((p == null ? "An item" : p.getName()) + " is out of stock. Remove it from your cart.");
            if (e.getValue() > p.getStock()) throw new IllegalArgumentException("Only " + p.getStock() + " left of " + p.getName() + ".");
            items.add(new OrderItem(p, e.getValue()));
        }
        double total = 0; for (OrderItem i : items) total += i.getSubtotal();
        Payment payment;
        switch (method == null ? "" : method.toLowerCase()) {                                         // POLYMORPHISM: any Payment works below
            case "cash": payment = new CashPayment(total); break;
            case "gcash": payment = new GCashPayment(total); break;
            case "card": payment = new CardPayment(total); break;
            default: throw new IllegalArgumentException("Choose a valid payment method.");
        }
        double change = payment.process(details);
        List<OrderLine> lines = new ArrayList<>();
        for (OrderItem i : items) { lines.add(i.toLine()); i.getProduct().reduceStock(i.getQuantity()); }
        boolean walkIn = actor.isStaff();
        String customer = walkIn ? (walkInName == null || walkInName.isBlank() ? "Walk-in customer" : walkInName.trim()) : actor.getName();
        Order o = new Order(nextOrderId(), actor.getUsername(), customer, walkIn ? "Walk-in" : "Online order", walkIn ? actor.getName() : "", payment.getMethod(), change, Instant.now(), lines, Order.Status.PENDING);
        orders.add(0, o); persist(); return o;
    }
    public void changeStatus(User actor, int orderId, Order.Status status) {
        if (actor == null || !actor.isStaff()) throw new IllegalArgumentException("Staff login required.");
        Order o = findOrder(orderId); if (o == null) throw new IllegalArgumentException("Order not found.");
        if (o.getStatus() == Order.Status.CANCELLED) throw new IllegalArgumentException("Cancelled orders cannot be changed.");
        if (status == Order.Status.CANCELLED) for (OrderLine l : o.getLines()) { FoodProduct p = findProduct(l.getProductId()); if (p != null) p.addStock(l.getQuantity()); }   // return the stock
        o.setStatus(status); o.assignCashier(actor.getName()); persist();
    }
    /** The staff member who prints or hands over the receipt becomes the cashier if nobody is recorded yet. */
    public void assignCashierIfBlank(User actor, Order o) { if (actor != null && actor.isStaff() && o.getCashier().isEmpty()) { o.assignCashier(actor.getName()); persist(); } }
}
