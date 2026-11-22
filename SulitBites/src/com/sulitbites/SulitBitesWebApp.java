package com.sulitbites;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class SulitBitesWebApp {
    private final Store store = new Store();
    private final Map<String, List<OrderItem>> carts = new HashMap<>();
    private HttpServer server;

    public void start() throws IOException {
        int port = Integer.parseInt(System.getenv().getOrDefault("PORT", "3000"));
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/", this::handle);
        server.start();
        System.out.println("SulitBites Java Web running on port " + port);
    }

    private void handle(HttpExchange exchange) throws IOException {
        try {
            String path = exchange.getRequestURI().getPath();
            User user = currentUser(exchange);
            if ("GET".equals(exchange.getRequestMethod())) {
                if (path.equals("/manus-routes.json")) json(exchange, "{\"routes\":[{\"path\":\"/\",\"title\":\"Sulit Bites Home\"},{\"path\":\"/login\",\"title\":\"Login\"},{\"path\":\"/menu\",\"title\":\"Menu\"},{\"path\":\"/cart\",\"title\":\"Cart\"},{\"path\":\"/orders\",\"title\":\"Orders\"},{\"path\":\"/admin\",\"title\":\"Staff Console\"}]}" );
                else if (path.equals("/")) page(exchange, home(user));
                else if (path.equals("/login")) page(exchange, loginPage(""));
                else if (path.equals("/logout")) { exchange.getResponseHeaders().add("Set-Cookie", "sb_user=; Path=/; Max-Age=0; HttpOnly"); redirect(exchange, "/"); }
                else if (path.equals("/menu")) page(exchange, menu(user));
                else if (path.equals("/cart")) page(exchange, cart(user, ""));
                else if (path.equals("/orders")) page(exchange, orders(user));
                else if (path.equals("/admin")) { if (user == null) redirect(exchange, "/login"); else page(exchange, admin(user)); }
                else page(exchange, error("Page not found."));
            } else if ("POST".equals(exchange.getRequestMethod())) {
                Map<String, String> form = form(exchange);
                if (path.equals("/login")) login(exchange, form);
                else if (path.equals("/add-cart")) addCart(exchange, user, form);
                else if (path.equals("/checkout")) checkout(exchange, user, form);
                else if (path.equals("/admin/product")) addProduct(exchange, user, form);
                else if (path.equals("/admin/stock")) updateStock(exchange, user, form);
                else if (path.equals("/admin/status")) updateStatus(exchange, user, form);
                else page(exchange, error("Action not found."));
            }
        } catch (Exception e) {
            page(exchange, error(e.getMessage() == null ? "Unexpected server error." : e.getMessage()));
        }
    }

    private String home(User user) {
        return layout("Home", user, "<section class='hero'><div><p class='eyebrow'>CAMPUS FOOD, FAIR PRICES</p><h1>Big flavor.<br><em>Small price.</em></h1><p class='lead'>Order comfort food from Sulit Bites, built for busy students and hungry friends.</p><a class='button' href='/menu'>Browse the menu</a></div><div class='receipt'><b>Today's favorites</b><hr><p>🍔 Classic Burger <span>PHP 99</span></p><p>🍗 Fried Chicken <span>PHP 119</span></p><p>🍧 Halo-Halo <span>PHP 79</span></p><hr><strong>Freshly prepared daily.</strong></div></section><section class='section'><h2>Simple ordering</h2><div class='cards'><article><b>01</b><h3>Choose your food</h3><p>Browse meals, drinks, and desserts.</p></article><article><b>02</b><h3>Pay your way</h3><p>Cash, GCash, or card validation.</p></article><article><b>03</b><h3>Track your order</h3><p>See updates from pending to completed.</p></article></div></section>");
    }

    private String loginPage(String message) {
        return layout("Login", null, "<section class='auth'><p class='eyebrow'>WELCOME BACK</p><h1>Sign in to Sulit Bites</h1>" + alert(message) + "<form method='post' action='/login'><label>Username<input name='username' required></label><label>Password<input name='password' type='password' required></label><button class='button' type='submit'>Login</button></form><p class='hint'>Demo: admin/admin123 · cashier/cashier123 · juan/juan123</p></section>");
    }

    private String menu(User user) {
        StringBuilder b = new StringBuilder("<section class='section'><div class='section-head'><div><p class='eyebrow'>THE MENU</p><h1>Pick your craving</h1></div><a class='button ghost' href='/cart'>View cart</a></div><div class='product-grid'>");
        for (FoodProduct p : store.getProducts()) {
            b.append("<article class='product'><div class='food-icon'>").append(icon(p.getCategory())).append("</div><p class='eyebrow'>").append(p.getCategory()).append("</p><h3>").append(h(p.getName())).append("</h3><p class='muted'>Freshly made and student-friendly.</p><strong>PHP ").append(String.format("%.2f", p.getPrice())).append("</strong><small>").append(p.getStatus()).append(" · ").append(p.getStock()).append(" left</small>");
            if (p.isInStock()) b.append("<form method='post' action='/add-cart' class='inline'><input type='hidden' name='id' value='").append(p.getId()).append("'><input name='quantity' type='number' min='1' max='").append(p.getStock()).append("' value='1'><button class='button small'>Add</button></form>");
            else b.append("<span class='sold'>Unavailable</span>");
            b.append("</article>");
        }
        return b.append("</div></section>").toString();
    }

    private String cart(User user, String message) {
        if (user == null) return layout("Cart", null, "<section class='auth'><h1>Login required</h1><p>Please sign in before checking out.</p><a class='button' href='/login'>Login</a></section>");
        List<OrderItem> items = carts.getOrDefault(user.getUsername(), List.of());
        if (items.isEmpty()) return layout("Cart", user, "<section class='empty'><h1>Your cart is empty.</h1><p class='muted'>Add something delicious from the menu.</p><a class='button' href='/menu'>Browse menu</a></section>");
        double total = items.stream().mapToDouble(OrderItem::getSubtotal).sum();
        StringBuilder b = new StringBuilder("<section class='section'><p class='eyebrow'>YOUR ORDER</p><h1>Cart and checkout</h1>").append(alert(message)).append("<div class='cart-layout'><div class='panel'>");
        for (OrderItem i : items) b.append("<div class='cart-line'><span>").append(icon(i.getProduct().getCategory())).append(" ").append(h(i.getProduct().getName())).append(" × ").append(i.getQuantity()).append("</span><b>PHP ").append(String.format("%.2f", i.getSubtotal())).append("</b></div>");
        b.append("</div><div class='panel'><h2>Total: PHP ").append(String.format("%.2f", total)).append("</h2><form method='post' action='/checkout'><label>Payment method<select name='method'><option>Cash</option><option>GCash</option><option>Card</option></select></label><label>Payment details<input name='details' placeholder='Cash amount, GCash number, or card number' required></label><button class='button' type='submit'>Place order</button></form></div></div></section>");
        return layout("Cart", user, b.toString());
    }

    private String orders(User user) {
        if (user == null) return loginPage("Please log in to see your orders.");
        StringBuilder b = new StringBuilder("<section class='section'><p class='eyebrow'>ORDER HISTORY</p><h1>My orders</h1>");
        List<Order> list = store.getOrders().stream().filter(o -> o.getCustomer() == user).toList();
        if (list.isEmpty()) b.append("<div class='empty'><p>No orders yet.</p><a href='/menu'>Order something tasty</a></div>");
        for (Order o : list) b.append(orderCard(o));
        return layout("Orders", user, b.append("</section>").toString());
    }

    private String admin(User user) {
        if (user == null || !user.isStaff()) return error("Staff login required.");
        StringBuilder b = new StringBuilder("<section class='section'><p class='eyebrow'>STAFF CONSOLE</p><h1>Manage Sulit Bites</h1><div class='admin-grid'><div class='panel'><h2>Add product</h2><form method='post' action='/admin/product'><label>Name<input name='name' required></label><label>Category<select name='category'><option>Meal</option><option>Drink</option><option>Dessert</option></select></label><label>Price<input name='price' type='number' step='0.01' min='1' required></label><label>Stock<input name='stock' type='number' min='0' required></label><button class='button'>Add product</button></form></div><div class='panel'><h2>Inventory</h2>");
        for (FoodProduct p : store.getProducts()) b.append("<form method='post' action='/admin/stock' class='admin-row'><span><b>").append(h(p.getName())).append("</b><small>").append(p.getStatus()).append("</small></span><input type='hidden' name='id' value='").append(p.getId()).append("'><input name='stock' type='number' min='0' value='").append(p.getStock()).append("'><button class='button small'>Save</button></form>");
        b.append("</div></div><div class='panel'><h2>Orders</h2>");
        for (Order o : store.getOrders()) { b.append(orderCard(o)).append("<form method='post' action='/admin/status' class='status-form'><input type='hidden' name='id' value='").append(o.getId()).append("'><select name='status'>"); for (Order.Status s : Order.Status.values()) b.append("<option ").append(s == o.getStatus() ? "selected" : "").append(">").append(s).append("</option>"); b.append("</select><button class='button small'>Update status</button></form>"); }
        return layout("Admin", user, b.append("</div></section>").toString());
    }

    private void login(HttpExchange e, Map<String, String> f) throws IOException { User u = store.authenticate(f.getOrDefault("username", ""), f.getOrDefault("password", "")); if (u == null) page(e, loginPage("Invalid username or password.")); else { e.getResponseHeaders().add("Set-Cookie", "sb_user=" + u.getUsername() + "; Path=/; HttpOnly"); redirect(e, u.isStaff() ? "/admin" : "/menu"); } }
    private void addCart(HttpExchange e, User u, Map<String, String> f) throws IOException { if (u == null) { redirect(e, "/login"); return; } FoodProduct p = store.findProduct(Integer.parseInt(f.get("id"))); int q = Integer.parseInt(f.get("quantity")); if (p == null || q < 1 || q > p.getStock()) { page(e, error("Invalid product quantity.")); return; } carts.computeIfAbsent(u.getUsername(), k -> new ArrayList<>()).add(new OrderItem(p, q)); redirect(e, "/cart"); }
    private void checkout(HttpExchange e, User u, Map<String, String> f) throws IOException { if (u == null) { redirect(e, "/login"); return; } List<OrderItem> items = carts.getOrDefault(u.getUsername(), List.of()); if (items.isEmpty()) { redirect(e, "/menu"); return; } double total = items.stream().mapToDouble(OrderItem::getSubtotal).sum(); Payment p = switch (f.getOrDefault("method", "").toLowerCase()) { case "cash" -> new CashPayment(total); case "gcash" -> new GCashPayment(total); case "card" -> new CardPayment(total); default -> null; }; if (p == null) { page(e, cart(u, "Choose a valid payment method.")); return; } try { double change = p.process(f.getOrDefault("details", "")); for (OrderItem i : items) i.getProduct().setStock(i.getProduct().getStock() - i.getQuantity()); Order o = store.addOrder(u, items, p, change); carts.remove(u.getUsername()); page(e, layout("Order placed", u, "<section class='success'><p class='eyebrow'>ORDER CONFIRMED</p><h1>Thank you, " + h(u.getName()) + "!</h1><p>Your order <b>#" + o.getId() + "</b> is now pending.</p><a class='button' href='/orders'>View my orders</a></section>")); } catch (IllegalArgumentException ex) { page(e, cart(u, ex.getMessage())); } }
    private void addProduct(HttpExchange e, User u, Map<String, String> f) throws IOException { if (u == null || !u.canManageProducts()) { page(e, error("Only the admin can add products.")); return; } store.addProduct(f.get("category"), f.get("name"), Double.parseDouble(f.get("price")), Integer.parseInt(f.get("stock"))); redirect(e, "/admin"); }
    private void updateStock(HttpExchange e, User u, Map<String, String> f) throws IOException { if (u == null || !u.canManageProducts()) { page(e, error("Only the admin can update stock.")); return; } FoodProduct p = store.findProduct(Integer.parseInt(f.get("id"))); p.setStock(Integer.parseInt(f.get("stock"))); p.setAvailable(p.getStock() > 0); redirect(e, "/admin"); }
    private void updateStatus(HttpExchange e, User u, Map<String, String> f) throws IOException { if (u == null || !u.isStaff()) { page(e, error("Staff login required.")); return; } Order o = store.getOrders().stream().filter(x -> x.getId() == Integer.parseInt(f.get("id"))).findFirst().orElseThrow(); o.setStatus(Order.Status.valueOf(f.get("status"))); redirect(e, "/admin"); }

    private User currentUser(HttpExchange e) { String cookie = e.getRequestHeaders().getFirst("Cookie"); if (cookie == null) return null; for (String c : cookie.split(";")) { String[] x = c.trim().split("=", 2); if (x.length == 2 && x[0].equals("sb_user")) return store.authenticate(x[1], store.getUsers().stream().filter(u -> u.getUsername().equals(x[1])).map(User::getPassword).findFirst().orElse("")); } return null; }
    private void page(HttpExchange e, String html) throws IOException { byte[] body = html.getBytes(StandardCharsets.UTF_8); e.getResponseHeaders().set("Content-Type", "text/html; charset=UTF-8"); e.sendResponseHeaders(200, body.length); try (OutputStream out = e.getResponseBody()) { out.write(body); } }
    private void json(HttpExchange e, String value) throws IOException { byte[] body = value.getBytes(StandardCharsets.UTF_8); e.getResponseHeaders().set("Content-Type", "application/json; charset=UTF-8"); e.sendResponseHeaders(200, body.length); try (OutputStream out = e.getResponseBody()) { out.write(body); } }
    private void redirect(HttpExchange e, String path) throws IOException { e.getResponseHeaders().set("Location", path); e.sendResponseHeaders(303, -1); e.close(); }
    private Map<String, String> form(HttpExchange e) throws IOException { String raw = new String(e.getRequestBody().readAllBytes(), StandardCharsets.UTF_8); Map<String, String> r = new HashMap<>(); for (String pair : raw.split("&")) { String[] x = pair.split("=", 2); if (x.length == 2) r.put(URLDecoder.decode(x[0], StandardCharsets.UTF_8), URLDecoder.decode(x[1], StandardCharsets.UTF_8)); } return r; }
    private String layout(String title, User user, String body) { String nav = user == null ? "<a href='/login'>Login</a>" : "<span>Hi, " + h(user.getName()) + " · " + user.getRole() + "</span><a href='/orders'>Orders</a>" + (user.isStaff() ? "<a href='/admin'>Staff</a>" : "") + "<a href='/logout'>Logout</a>"; return "<!doctype html><html><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'><title>" + title + " | Sulit Bites</title><style>" + css() + "</style></head><body><header><a class='brand' href='/'>🍔 SULIT BITES</a><nav><a href='/menu'>Menu</a>" + nav + "</nav></header><main>" + body + "</main><footer>© 2026 Sulit Bites · Java OOP Web Application</footer></body></html>"; }
    private String orderCard(Order o) { StringBuilder x = new StringBuilder("<article class='order'><div><b>Order #").append(o.getId()).append("</b><span class='pill'>").append(o.getStatus()).append("</span></div><p>"); for (OrderItem i : o.getItems()) x.append(h(i.getProduct().getName())).append(" × ").append(i.getQuantity()).append(" · "); return x.append("</p><strong>PHP ").append(String.format("%.2f", o.getTotal())).append("</strong><small>Paid with ").append(o.getPaymentMethod()).append("</small></article>").toString(); }
    private String error(String message) { return layout("Error", null, "<section class='error'><h1>Something went wrong</h1><p>" + h(message) + "</p><a class='button' href='/'>Go home</a></section>"); }
    private String alert(String message) { return message == null || message.isBlank() ? "" : "<p class='alert'>" + h(message) + "</p>"; }
    private String icon(String category) { return category.equals("Drink") ? "🥤" : category.equals("Dessert") ? "🍧" : "🍛"; }
    private String h(String s) { return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); }
    private String css() { return "*{box-sizing:border-box}body{margin:0;background:#f7f7f4;color:#151515;font-family:Arial,sans-serif}header{display:flex;justify-content:space-between;align-items:center;padding:22px 7%;border-bottom:2px solid #251f1b;background:#fff}a{color:#251f1b}nav{display:flex;gap:22px;align-items:center}nav a{text-decoration:none;font-weight:bold}.brand{text-decoration:none;font-weight:900;letter-spacing:1px}.hero,.section{max-width:1120px;margin:auto;padding:72px 7%}.hero{display:grid;grid-template-columns:1.2fr .8fr;gap:60px;align-items:center;min-height:550px;max-width:none;padding-left:12%;padding-right:12%;background:#ffb703}h1{font-size:clamp(42px,7vw,82px);line-height:.95;margin:10px 0 25px}h1 em{color:#ef3d35;font-style:normal}h2{font-size:32px}h3{margin:8px 0}.lead{font-size:20px;line-height:1.6;max-width:520px}.eyebrow{font-size:12px;letter-spacing:2px;font-weight:bold;color:#ef3d35}.button{display:inline-block;background:#ef3d35;color:white;border:2px solid #251f1b;padding:13px 20px;font-weight:bold;text-decoration:none;box-shadow:4px 4px #151515;cursor:pointer}.button.ghost{background:transparent;color:#251f1b}.button.small{padding:8px 12px;font-size:12px}.receipt,.panel,.product,.order,.cards article{background:#fff;border:2px solid #251f1b;box-shadow:6px 6px #251f1b;padding:24px}.receipt{transform:rotate(2deg);font-family:monospace;border:3px solid #151515;box-shadow:8px 8px #151515}.receipt p{display:flex;justify-content:space-between}.section-head{display:flex;justify-content:space-between;align-items:end;border-bottom:3px solid #151515;padding-bottom:18px;margin-bottom:28px}.cards,.product-grid,.admin-grid{display:grid;grid-template-columns:repeat(3,1fr);gap:22px}.product-grid{grid-template-columns:repeat(3,1fr)}.product{display:flex;flex-direction:column;gap:8px;transition:transform .15s ease}.product:hover{transform:translateY(-4px)}.food-icon{font-size:40px}.product small,.product .sold,.order small{display:block;color:#6c6259}.muted,.hint{color:#6c6259}.inline{display:flex;gap:8px;margin-top:auto}.inline input{width:70px}.auth,.empty,.error,.success{max-width:560px;margin:80px auto;padding:24px}.auth form,.panel form{display:grid;gap:14px}.auth label,.panel label{display:grid;gap:6px;font-weight:bold}input,select{padding:12px;border:2px solid #251f1b;background:white;font:inherit}.cart-layout{display:grid;grid-template-columns:1.2fr .8fr;gap:24px}.cart-line,.admin-row{display:flex;justify-content:space-between;gap:12px;padding:15px 0;border-bottom:1px solid #cfc1b0}.order{margin:18px 0;border-left:8px solid #ef3d35}.order .pill{float:right;background:#ffb703;padding:5px 8px;font-size:12px}.status-form{display:flex;gap:8px;margin-top:-12px;margin-bottom:28px}.alert{background:#ffb703;border:2px solid #251f1b;padding:12px}.error{background:#ffe5df;border:2px solid #c94c2d}footer{text-align:center;padding:30px;border-top:2px solid #251f1b}@media(max-width:760px){header,.hero,.section-head{display:block}.hero{padding:48px 7%;min-height:auto}.receipt{margin-top:45px}.cards,.product-grid,.admin-grid,.cart-layout{grid-template-columns:1fr}nav{margin-top:18px;flex-wrap:wrap}.section{padding-top:38px}}"; }
}
