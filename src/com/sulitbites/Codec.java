package com.sulitbites;

import java.time.Instant;
import java.util.*;

/** Converts users, products and orders to and from plain maps (the JSON stored in the file or in Firebase). */
final class Codec {
    private Codec() { }
    static String userKey(String username) { return username.toLowerCase().replaceAll("[^a-z0-9_]", "_"); }

    static Map<String, Object> toMap(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("role", u.getRole()); m.put("username", u.getUsername()); m.put("password", u.getPasswordHash()); m.put("name", u.getName()); return m;
    }
    static Map<String, Object> toMap(FoodProduct p) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("category", p.getCategory()); m.put("id", p.getId()); m.put("name", p.getName()); m.put("price", p.getPrice()); m.put("stock", p.getStock());
        m.put("emoji", p.getEmoji()); m.put("desc", p.getDescription()); m.put("available", p.isAvailable()); return m;
    }
    static Map<String, Object> toMap(Order o) {
        Map<String, Object> m = new LinkedHashMap<>(); List<Object> lines = new ArrayList<>();
        for (OrderLine l : o.getLines()) {
            Map<String, Object> x = new LinkedHashMap<>();
            x.put("pid", l.getProductId()); x.put("name", l.getName()); x.put("emoji", l.getEmoji()); x.put("qty", l.getQuantity()); x.put("price", l.getUnitPrice()); x.put("subtotal", l.getSubtotal());
            lines.add(x);
        }
        m.put("id", o.getId()); m.put("username", o.getUsername()); m.put("customer", o.getCustomerName()); m.put("cashier", o.getCashier()); m.put("type", o.getType());
        m.put("method", o.getMethod()); m.put("change", o.getChange()); m.put("status", o.getStatus().getLabel()); m.put("date", o.getPlacedAt().toString()); m.put("lines", lines);
        return m;
    }

    private static String str(Object o, String def) { return o == null ? def : o.toString(); }
    private static double num(Object o) { return o instanceof Number ? ((Number) o).doubleValue() : Double.parseDouble(String.valueOf(o)); }
    @SuppressWarnings("unchecked") private static Map<String, Object> map(Object o) { return (Map<String, Object>) o; }

    static User user(Map<String, Object> m) { return User.create(str(m.get("role"), "Customer"), str(m.get("username"), ""), str(m.get("password"), ""), str(m.get("name"), "")); }
    static FoodProduct product(Map<String, Object> m) {
        FoodProduct p = FoodProduct.create(str(m.get("category"), "Meal"), (int) num(m.get("id")), str(m.get("name"), ""), num(m.get("price")), (int) num(m.get("stock")), str(m.get("emoji"), ""), str(m.get("desc"), ""));
        p.setAvailable(!Boolean.FALSE.equals(m.get("available"))); return p;
    }
    static Order order(Map<String, Object> m) {
        List<OrderLine> lines = new ArrayList<>();
        Object raw = m.get("lines");
        Collection<?> src = raw instanceof List ? (List<?>) raw : raw instanceof Map ? ((Map<?, ?>) raw).values() : List.of();
        for (Object o : src) { Map<String, Object> x = map(o); lines.add(new OrderLine((int) num(x.get("pid")), str(x.get("name"), ""), str(x.get("emoji"), ""), (int) num(x.get("qty")), num(x.get("price")), num(x.get("subtotal")))); }
        return new Order((int) num(m.get("id")), str(m.get("username"), ""), str(m.get("customer"), ""), str(m.get("type"), "Online order"), str(m.get("cashier"), ""),
            str(m.get("method"), "Cash"), m.get("change") == null ? 0 : num(m.get("change")), Instant.parse(str(m.get("date"), Instant.now().toString())), lines, Order.Status.fromLabel(str(m.get("status"), "Pending")));
    }

    /** Every record as one flat map ("orders/o1001" -> JSON text). Used to find out what changed. */
    static Map<String, String> flat(Store s) {
        Map<String, String> r = new TreeMap<>();
        for (User u : s.getUsers()) r.put("users/" + userKey(u.getUsername()), Json.write(toMap(u)));
        for (FoodProduct p : s.getProducts()) r.put("products/p" + p.getId(), Json.write(toMap(p)));
        for (Order o : s.getOrders()) r.put("orders/o" + o.getId(), Json.write(toMap(o)));
        return r;
    }
    static Map<String, Object> tree(Store s) {
        Map<String, Object> users = new LinkedHashMap<>(), products = new LinkedHashMap<>(), orders = new LinkedHashMap<>(), root = new LinkedHashMap<>();
        for (User u : s.getUsers()) users.put(userKey(u.getUsername()), toMap(u));
        for (FoodProduct p : s.getProducts()) products.put("p" + p.getId(), toMap(p));
        for (Order o : s.getOrders()) orders.put("o" + o.getId(), toMap(o));
        root.put("users", users); root.put("products", products); root.put("orders", orders); return root;
    }
    /** Reads a whole database (the output of tree()) into the store. Bad records are skipped. */
    static void fill(Store s, Object root) {
        List<User> us = new ArrayList<>(); List<FoodProduct> ps = new ArrayList<>(); List<Order> os = new ArrayList<>();
        if (root instanceof Map) {
            Map<String, Object> r = map(root);
            if (r.get("users") instanceof Map) for (Object o : map(r.get("users")).values()) try { us.add(user(map(o))); } catch (RuntimeException e) { System.err.println("Skipped a bad user: " + e.getMessage()); }
            if (r.get("products") instanceof Map) for (Object o : map(r.get("products")).values()) try { ps.add(product(map(o))); } catch (RuntimeException e) { System.err.println("Skipped a bad product: " + e.getMessage()); }
            if (r.get("orders") instanceof Map) for (Object o : map(r.get("orders")).values()) try { os.add(order(map(o))); } catch (RuntimeException e) { System.err.println("Skipped a bad order: " + e.getMessage()); }
        }
        ps.sort(Comparator.comparingInt(FoodProduct::getId)); os.sort((a, b) -> b.getId() - a.getId());
        s.replaceAll(us, ps, os);
    }
}
