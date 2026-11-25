package com.sulitbites;

import java.util.*;
import static com.sulitbites.Html.*;

/** Writes every page of the website as HTML. */
final class Pages {
    static final String STORE_NAME = "Sulit Bites", BRANCH = "Main Branch", ADDRESS = "Laguna State Polytechnic University, Laguna";
    private static final String[] EMOJIS = {"\uD83C\uDF54", "\uD83C\uDF5F", "\uD83C\uDF57", "\uD83C\uDF55", "\uD83C\uDF2D", "\uD83E\uDD6A", "\uD83C\uDF5D", "\uD83C\uDF5B", "\uD83C\uDF5C", "\uD83E\uDD57", "\uD83E\uDD64", "\uD83E\uDDCB", "\u2615", "\uD83E\uDDC3", "\uD83C\uDF67", "\uD83C\uDF6E", "\uD83C\uDF68", "\uD83C\uDF69", "\uD83C\uDF70", "\uD83C\uDF6A"};
    private final Store store;
    Pages(Store store) { this.store = store; }

    // ---------------------------------------------------------------- layout
    String layout(Ctx c, String title, String body) {
        StringBuilder b = new StringBuilder("<!doctype html><html lang='en'><head><meta charset='UTF-8'><meta name='viewport' content='width=device-width,initial-scale=1'><title>")
            .append(h(title)).append(" | Sulit Bites</title><link rel='icon' href=\"data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 100 100'%3E%3Ctext y='.9em' font-size='90'%3E\uD83C\uDF54%3C/text%3E%3C/svg%3E\">")
            .append("<link rel='preconnect' href='https://fonts.googleapis.com'><link href='https://fonts.googleapis.com/css2?family=Bricolage+Grotesque:opsz,wght@12..96,700;12..96,800&family=DM+Sans:wght@400;500;700&display=swap' rel='stylesheet'>")
            .append("<link rel='stylesheet' href='/style.css?v=1'></head><body class='").append(c.staff() ? "" : "np").append("'><header><div class='nav'><a class='logo' href='/'><b>\uD83C\uDF54</b>Sulit Bites</a><nav><a href='/'>Home</a><a href='/menu'>Menu</a>");
        if (c.staff()) b.append("<a href='/staff'>Dashboard</a>"); else if (c.user != null) b.append("<a href='/orders'>My Orders</a>");
        b.append("</nav><div class='nr'><a class='btn alt sm' href='/cart'>Cart<span class='cnt'>").append(c.cartCount).append("</span></a>");
        if (c.user != null) b.append("<div class='who'>").append(h(c.user.getName())).append("<small>").append(c.user.getRole()).append("</small></div><form method='post' action='/logout' class='inl'><button class='btn sm'>Logout</button></form>");
        else b.append("<a class='btn alt sm' href='/login'>Login</a><a class='btn sm' href='/register'>Register</a>");
        b.append("</div></div></header><main>");
        if (c.msg != null && !c.msg.isBlank()) b.append("<div class='flash").append(c.err ? " err" : "").append("' role='status'>").append(h(c.msg)).append("</div>");
        return b.append(body).append("</main><footer>\u00A9 2026 Sulit Bites. All rights reserved.<br><small>Built with Java (OOP)</small></footer></body></html>").toString();
    }
    String error(Ctx c, String title, String message) {
        return layout(c, title, "<section class='auth'><div class='card ac'><h2>" + h(title) + "</h2><p class='mut'>" + h(message) + "</p><br><a class='btn' href='/'>Go home</a></div></section>");
    }

    // ---------------------------------------------------------------- customer pages
    private String card(FoodProduct p) {
        StringBuilder b = new StringBuilder("<article class='card pc ").append(p.getCategory()).append(p.isInStock() ? "" : " out").append("'><div class='thumb'>").append(h(p.getEmoji()))
            .append("</div><div class='cb'><div class='tags'><span class='tagp'>").append(p.getCategory()).append("</span>").append(availability(p)).append("</div><h3>").append(h(p.getName()))
            .append("</h3><p>").append(h(p.getDescription())).append("</p>");
        if (!p.getPromo().isEmpty()) b.append("<small class='promo'>").append(p.getPromo()).append("</small>");
        b.append("<div class='row'><b class='price'>").append(money(p.getPrice())).append("</b>");
        if (p.isInStock()) b.append("<form method='post' action='/cart/add' class='qf'><input type='hidden' name='id' value='").append(p.getId()).append("'><input type='hidden' name='qty' value='1'><button class='btn sm'>Add to cart</button></form>");
        else b.append("<span class='mut'>Unavailable</span>");
        return b.append("</div></div></article>").toString();
    }
    String home(Ctx c) {
        List<FoodProduct> fav = new ArrayList<>(); for (FoodProduct p : store.getProducts()) if (p.isInStock() && fav.size() < 4) fav.add(p);
        StringBuilder b = new StringBuilder("<section class='hero'><div class='wrap hero-in'><div><h1>Big flavor,<br>small price.</h1><p>Burgers, fried chicken, drinks and desserts. Pick what you crave, pay your way, and track your order from the kitchen to your table.</p>")
            .append("<div class='cta'><a class='btn lg' href='/menu'>Order now</a>").append(c.user == null ? "<a class='btn alt lg' href='/login'>Sign in</a>" : "").append("</div></div><div class='ticket'><b>Your order</b><div class='tdash'></div>");
        double sum = 0;
        for (int i = 0; i < Math.min(3, fav.size()); i++) { FoodProduct p = fav.get(i); sum += p.getPrice(); b.append("<div class='tr'><span>").append(h(p.getEmoji())).append(" ").append(h(p.getName())).append("</span><span>").append(money(p.getPrice())).append("</span></div>"); }
        b.append("<div class='tdash'></div><div class='tr tot'><span>Total</span><span>").append(money(sum)).append("</span></div><a class='btn y full' href='/menu'>Start your order</a></div></div></section>");
        b.append("<section class='wrap sec'><div class='sh'><div><h2>Our favorites</h2><p class='mut'>The picks our regulars keep coming back for.</p></div><a class='btn alt sm' href='/menu'>See full menu</a></div><div class='grid'>");
        for (FoodProduct p : fav) b.append(card(p));
        b.append("</div></section><section class='wrap sec'><h2>Browse by category</h2><div class='g3'>");
        String[][] cats = {{"Meal", "\uD83C\uDF5B", "Hearty meals"}, {"Drink", "\uD83E\uDD64", "Cool drinks"}, {"Dessert", "\uD83C\uDF67", "Sweet treats"}};
        for (String[] k : cats) b.append("<a class='card cat-t ").append(k[0]).append("' href='/menu?cat=").append(k[0]).append("'><i>").append(k[1]).append("</i><h3>").append(k[2]).append("</h3></a>");
        b.append("</div></section><section class='wrap sec'><h2>How ordering works</h2><div class='g3'>");
        String[][] steps = {{"Pick your food", "Browse the menu and add items to your cart."}, {"Pay your way", "Cash, GCash or card. Change is computed for you."}, {"Track your order", "Watch it move from Pending to Completed."}};
        for (int i = 0; i < 3; i++) b.append("<div class='card stp'><span class='n'>").append(i + 1).append("</span><h3>").append(steps[i][0]).append("</h3><p class='mut'>").append(steps[i][1]).append("</p></div>");
        return layout(c, "Home", b.append("</div></section>").toString());
    }
    String menu(Ctx c, String cat, String q) {
        String category = List.of("Meal", "Drink", "Dessert").contains(cat) ? cat : "All"; String query = q == null ? "" : q.trim();
        StringBuilder b = new StringBuilder("<section class='wrap sec'><div class='sh'><div><h2>Our menu</h2><p class='mut'>Green means available. Items that ran out cannot be ordered.</p></div>")
            .append("<form method='get' action='/menu' class='pfx'><input type='hidden' name='cat' value='").append(h(category)).append("'><input class='in sm' name='q' placeholder='Search food' aria-label='Search food' value='").append(h(query)).append("'><button class='btn sm'>Search</button></form></div><div class='pills'>");
        for (String k : new String[]{"All", "Meal", "Drink", "Dessert"}) b.append("<a class='pill").append(k.equals(category) ? " on" : "").append("' href='/menu?cat=").append(k).append(query.isEmpty() ? "" : "&q=" + enc(query)).append("'>").append(k).append("</a>");
        b.append("</div><div class='grid'>"); int n = 0;
        for (FoodProduct p : store.getProducts()) if ((category.equals("All") || p.getCategory().equals(category)) && p.getName().toLowerCase().contains(query.toLowerCase())) { b.append(card(p)); n++; }
        if (n == 0) b.append("<p class='mut'>No food matches your search. Try another name.</p>");
        return layout(c, "Menu", b.append("</div></section>").toString());
    }
    String cart(Ctx c, Map<Integer, Integer> cart) {
        if (cart == null || cart.isEmpty()) return layout(c, "Cart", "<section class='wrap sec empty'><div class='big'>\uD83D\uDED2</div><h2>Your cart is empty</h2><p class='mut'>Add something delicious from the menu.</p><br><a class='btn lg' href='/menu'>Browse the menu</a></section>");
        StringBuilder lines = new StringBuilder(); double raw = 0, total = 0;
        for (Map.Entry<Integer, Integer> e : cart.entrySet()) {
            FoodProduct p = store.findProduct(e.getKey()); if (p == null) continue;
            OrderItem i = new OrderItem(p, e.getValue()); raw += p.getPrice() * i.getQuantity(); total += i.getSubtotal();
            lines.append("<div class='line'><span class='em'>").append(h(p.getEmoji())).append("</span><div class='li'><b>").append(h(p.getName())).append("</b>").append(p.isInStock() ? "" : " <span class='bd Cancelled'>Out of stock</span>")
                .append("<small>").append(money(p.getPrice())).append(" each</small></div><form method='post' action='/cart/update' class='qf'><input type='hidden' name='id' value='").append(p.getId())
                .append("'><input class='qtyin' type='number' name='qty' min='0' max='").append(Math.max(p.getStock(), 0)).append("' value='").append(i.getQuantity()).append("' aria-label='Quantity'><button class='btn alt sm'>Update</button></form><b class='lt'>")
                .append(money(i.getSubtotal())).append("</b><form method='post' action='/cart/remove' class='inl'><input type='hidden' name='id' value='").append(p.getId()).append("'><button class='x' aria-label='Remove'>\u2715</button></form></div>");
        }
        StringBuilder b = new StringBuilder("<section class='wrap sec'><h2>Your cart</h2><div class='split'><div class='card'>").append(lines).append("</div><aside class='card sum'><h3>Order summary</h3>")
            .append("<div class='r'><span>Subtotal</span><span>").append(money(raw)).append("</span></div><div class='r g'><span>Bulk discount</span><span>-").append(money(raw - total)).append("</span></div><div class='r t'><span>Total</span><span>").append(money(total)).append("</span></div>");
        if (c.user == null) return layout(c, "Cart", b.append("<br><a class='btn lg full' href='/login'>Login to checkout</a></aside></div></section>").toString());
        b.append("<form method='post' action='/checkout'>");
        if (c.staff()) b.append("<label for='cn'>Customer name (walk-in)</label><input id='cn' name='customer' class='in' placeholder='Walk-in customer'>");
        b.append("<label for='pm'>Payment method</label><select id='pm' name='method' class='in'><option>Cash</option><option>GCash</option><option>Card</option></select><label for='pd'>Payment details</label>")
            .append("<input id='pd' name='details' class='in' placeholder='Cash amount, GCash number or card number' required><p class='hint'>Cash: the amount you hand over. GCash: 11 digits starting with 09. Card: 16 digits.</p><button class='btn lg full'>Place order</button></form></aside></div></section>");
        return layout(c, "Cart", b.toString());
    }
    private String orderCard(Order o) {
        boolean x = o.getStatus() == Order.Status.CANCELLED; StringBuilder items = new StringBuilder();
        for (OrderLine l : o.getLines()) items.append(items.length() > 0 ? ", " : "").append(h(l.getEmoji())).append(" ").append(h(l.getName())).append(" x").append(l.getQuantity());
        return "<div class='card oc" + (x ? " cx" : "") + "'><div><b>Order #" + o.getId() + "</b><small>" + when(o.getPlacedAt()) + "</small></div><div class='oi'>" + items + "</div><div class='or'><b>" + money(o.getTotal()) + "</b>"
            + badge(o.getStatus().getLabel()) + (x ? "<small>No charge</small>" : "") + "<a class='btn alt sm' href='/receipt?id=" + o.getId() + "'>Receipt</a></div></div>";
    }
    String orders(Ctx c) {
        StringBuilder b = new StringBuilder("<section class='wrap sec'><h2>My orders</h2>"); int n = 0;
        for (Order o : store.getOrders()) if (o.getUsername().equals(c.user.getUsername())) { b.append(orderCard(o)); n++; }
        if (n == 0) b.append("<p class='mut'>You have no orders yet. <a href='/menu'><u>Order something tasty</u></a>.</p>");
        return layout(c, "My orders", b.append("</section>").toString());
    }
    String receipt(Ctx c, Order o) {
        boolean x = o.getStatus() == Order.Status.CANCELLED; StringBuilder b = new StringBuilder("<section class='wrap sec'><div class='card rcp'><div class='rc'>");
        b.append("<div class='rh'><div class='rlogo'>\uD83C\uDF54</div><h2>").append(STORE_NAME).append("</h2><p>").append(BRANCH).append("<br>").append(ADDRESS).append("<br>").append(h(c.site)).append("</p></div><div class='tdash'></div>");
        if (x) b.append("<div class='stamp'>CANCELLED</div>");
        b.append(row("Receipt no.", "#" + o.getId())).append(row("Date and time", when(o.getPlacedAt()))).append(row("Cashier", h(o.getCashier().isEmpty() ? "Not yet assigned" : o.getCashier())))
            .append(row("Customer", h(o.getCustomerName()))).append(row("Order type", h(o.getType()))).append("<div class='tdash'></div>");
        for (OrderLine l : o.getLines()) b.append(row(l.getQuantity() + " x " + h(l.getName()), money(l.getSubtotal())));
        b.append("<div class='tdash'></div>").append(row("Subtotal", money(o.getGross())));
        if (o.getDiscount() > 0.005) b.append(row("Bulk discount", "-" + money(o.getDiscount())));
        if (x) b.append(row("Order cancelled", "-" + money(o.getBilled())));
        b.append("<div class='r t'><span>Total</span><span>").append(money(o.getTotal())).append("</span></div>");
        if (x) b.append(row("Payment", "None, no charge"));
        else { b.append(row("Paid with", h(o.getMethod()))); if (o.getMethod().equals("Cash")) b.append(row("Cash received", money(o.getBilled() + o.getChange()))).append(row("Change", money(o.getChange()))); }
        b.append(row("Status", badge(o.getStatus().getLabel()))).append("<div class='tdash'></div><p class='thx'>Thank you for ordering!<br>").append(STORE_NAME).append("<br>").append(h(c.site)).append("</p></div></div>");
        b.append("<div class='rcact noprint'>");
        b.append(c.staff() ? "<p class='hint'>To print this receipt for the customer, press <b>Ctrl+P</b> (or use your browser's Print option).</p><a class='btn' href='/staff/orders'>Back to orders</a>"
            : "<p class='hint'>Printed copies are handed out by the cashier.</p><a class='btn' href='/orders'>Back to my orders</a>");
        return layout(c, "Receipt #" + o.getId(), b.append("</div></section>").toString());
    }
    private String row(String l, String r) { return "<div class='r'><span>" + l + "</span><span>" + r + "</span></div>"; }

    String login(Ctx c) {
        return layout(c, "Login", "<section class='auth'><div class='card ac'><div class='tabs'><a class='pill on' href='/login'>Login</a><a class='pill' href='/register'>Register</a></div><h2>Welcome back</h2><br>"
            + "<form method='post' action='/login'><input name='username' class='in' placeholder='Username' aria-label='Username' required autocomplete='username'><input name='password' type='password' class='in' placeholder='Password' aria-label='Password' required autocomplete='current-password'><br><button class='btn lg full'>Login</button></form></div></section>");
    }
    String register(Ctx c) {
        return layout(c, "Register", "<section class='auth'><div class='card ac'><div class='tabs'><a class='pill' href='/login'>Login</a><a class='pill on' href='/register'>Register</a></div><h2>Create your account</h2><br>"
            + "<form method='post' action='/register'><input name='name' class='in' placeholder='Full name' aria-label='Full name' required><input name='username' class='in' placeholder='Username' aria-label='Username' required autocomplete='username'><input name='password' type='password' class='in' placeholder='Password (4+ characters)' aria-label='Password' required autocomplete='new-password'><br><button class='btn lg full'>Create account</button></form><p class='hint'>Username: 3 to 20 letters, numbers or underscores.</p></div></section>");
    }

    // ---------------------------------------------------------------- staff pages
    private String shell(Ctx c, String active, String inner) {
        String[][] links = {{"dashboard", "/staff", "Dashboard", "0"}, {"products", "/staff/products", "Products", "1"}, {"orders", "/staff/orders", "Orders", "0"}, {"customers", "/staff/customers", "Customers", "1"}, {"payments", "/staff/payments", "Payments", "0"}};
        StringBuilder b = new StringBuilder("<section class='adm sec'><aside class='side card'>");
        for (String[] l : links) if (l[3].equals("0") || c.user.canManageProducts()) b.append("<a class='").append(l[0].equals(active) ? "on" : "").append("' href='").append(l[1]).append("'>").append(l[2]).append("</a>");
        return b.append("</aside><div>").append(inner).append("</div></section>").toString();
    }
    private String bars(Map<String, Double> data, boolean money) {
        if (data.isEmpty()) return "<p class='mut'>No sales yet.</p>";
        double max = 1; for (double v : data.values()) max = Math.max(max, v); StringBuilder b = new StringBuilder();
        for (Map.Entry<String, Double> e : data.entrySet()) b.append("<div class='bar'><span>").append(h(e.getKey())).append("</span><div><i style='width:").append(String.format(Locale.US, "%.0f", e.getValue() / max * 100)).append("%'></i></div><b>")
            .append(money ? money(e.getValue()) : (int) (double) e.getValue() + " sold").append("</b></div>");
        return b.toString();
    }
    String dashboard(Ctx c) {
        double sales = 0; long pending = 0; Map<String, Double> sold = new HashMap<>(), pay = new LinkedHashMap<>();
        for (Order o : store.getOrders()) {
            sales += o.getTotal(); if (o.getStatus() == Order.Status.PENDING) pending++;
            if (o.getStatus() != Order.Status.CANCELLED) { for (OrderLine l : o.getLines()) sold.merge(l.getName(), (double) l.getQuantity(), Double::sum); pay.merge(o.getMethod(), o.getTotal(), Double::sum); }
        }
        long low = store.getProducts().stream().filter(p -> !p.getStatus().equals("In stock")).count();
        Map<String, Double> top = new LinkedHashMap<>(); sold.entrySet().stream().sorted((x, y) -> Double.compare(y.getValue(), x.getValue())).limit(5).forEach(e -> top.put(e.getKey(), e.getValue()));
        StringBuilder b = new StringBuilder("<h2>Dashboard</h2><div class='stats4'>");
        String[][] st = {{"Total sales", money(sales)}, {"Orders", "" + store.getOrders().size()}, {"Pending", "" + pending}, {"Products", "" + store.getProducts().size()}, {"Low or out of stock", "" + low}};
        for (String[] s : st) b.append("<div class='card st'><span>").append(s[0]).append("</span><b>").append(s[1]).append("</b></div>");
        b.append("</div><div class='two'><div class='card pad'><h3>Best sellers</h3>").append(bars(top, false)).append("</div><div class='card pad'><h3>Sales by payment</h3>").append(bars(pay, true)).append("</div></div><h3 class='mt'>Recent orders</h3><br>");
        List<Order> recent = new ArrayList<>(); for (Order o : store.getOrders()) if (recent.size() < 5) recent.add(o);
        return layout(c, "Dashboard", shell(c, "dashboard", b.append(ordersTable(recent, false)).toString()));
    }
    private String ordersTable(List<Order> list, boolean editable) {
        StringBuilder b = new StringBuilder("<div class='card tw'><table class='tbl'><tr><th>#</th><th>Customer</th><th>Items</th><th>Total</th><th>Paid with</th><th>Cashier</th><th>Date</th><th>Status</th>").append(editable ? "<th></th>" : "").append("</tr>");
        if (list.isEmpty()) b.append("<tr><td colspan='9' class='mut'>No orders yet. They appear here once customers check out.</td></tr>");
        for (Order o : list) {
            StringBuilder items = new StringBuilder(); for (OrderLine l : o.getLines()) items.append(items.length() > 0 ? ", " : "").append(h(l.getName())).append(" x").append(l.getQuantity());
            String status;
            if (editable && o.getStatus() != Order.Status.CANCELLED) {
                StringBuilder s = new StringBuilder("<form method='post' action='/staff/status' class='qf'><input type='hidden' name='id' value='" + o.getId() + "'><select class='in sm' name='status' aria-label='Status'>");
                for (Order.Status x : Order.Status.values()) s.append("<option").append(x == o.getStatus() ? " selected" : "").append(">").append(x.getLabel()).append("</option>");
                status = s.append("</select><button class='btn alt sm'>Update</button></form>").toString();
            } else status = badge(o.getStatus().getLabel());
            b.append("<tr>").append(td("#", "" + o.getId())).append(td("Customer", h(o.getCustomerName()))).append(td("Items", items.toString())).append(td("Total", money(o.getTotal()))).append(td("Paid with", h(o.getMethod())))
                .append(td("Cashier", h(o.getCashier().isEmpty() ? "-" : o.getCashier()))).append(td("Date", when(o.getPlacedAt()))).append(td("Status", status));
            if (editable) b.append(td("", "<a class='btn alt sm' href='/receipt?id=" + o.getId() + "'>Receipt</a>"));
            b.append("</tr>");
        }
        return b.append("</table></div>").toString();
    }
    String staffOrders(Ctx c) { return layout(c, "Orders", shell(c, "orders", "<h2>Orders</h2><p class='mut'>Cancelling an order returns its stock and makes its total 0.</p><br>" + ordersTable(store.getOrders(), true))); }
    String customers(Ctx c) {
        StringBuilder b = new StringBuilder("<h2>Customers</h2><br><div class='card tw'><table class='tbl'><tr><th>Name</th><th>Username</th><th>Orders</th><th>Total spent</th></tr>"); int n = 0;
        for (User u : store.getUsers()) {
            if (u.isStaff()) continue; n++; int cnt = 0; double spent = 0;
            for (Order o : store.getOrders()) if (o.getUsername().equals(u.getUsername()) && o.getStatus() != Order.Status.CANCELLED) { cnt++; spent += o.getTotal(); }
            b.append("<tr>").append(td("Name", h(u.getName()))).append(td("Username", h(u.getUsername()))).append(td("Orders", "" + cnt)).append(td("Total spent", money(spent))).append("</tr>");
        }
        if (n == 0) b.append("<tr><td colspan='4' class='mut'>No customers yet.</td></tr>");
        return layout(c, "Customers", shell(c, "customers", b.append("</table></div>").toString()));
    }
    String payments(Ctx c, String q, String m) {
        String query = q == null ? "" : q.trim().toLowerCase(), method = m == null || m.isBlank() ? "All" : m;
        StringBuilder b = new StringBuilder("<h2>Payment records</h2><form method='get' action='/staff/payments' class='filters'><input class='in sm' name='q' placeholder='Search order or customer' aria-label='Search' value='").append(h(q)).append("'><select class='in sm' name='m' aria-label='Method'>");
        for (String k : new String[]{"All", "Cash", "GCash", "Card"}) b.append("<option").append(k.equals(method) ? " selected" : "").append(">").append(k).append("</option>");
        b.append("</select><button class='btn sm'>Filter</button></form><div class='card tw'><table class='tbl'><tr><th>Order #</th><th>Customer</th><th>Method</th><th>Amount</th><th>Date</th><th>Status</th></tr>"); int n = 0;
        for (Order o : store.getOrders()) {
            if (!method.equals("All") && !o.getMethod().equals(method)) continue; if (!(o.getId() + " " + o.getCustomerName()).toLowerCase().contains(query)) continue; n++;
            b.append("<tr>").append(td("Order #", "" + o.getId())).append(td("Customer", h(o.getCustomerName()))).append(td("Method", h(o.getMethod()))).append(td("Amount", money(o.getTotal()))).append(td("Date", when(o.getPlacedAt()))).append(td("Status", badge(o.getStatus().getLabel()))).append("</tr>");
        }
        if (n == 0) b.append("<tr><td colspan='6' class='mut'>No payments match.</td></tr>");
        return layout(c, "Payments", shell(c, "payments", b.append("</table></div>").toString()));
    }
    String staffProducts(Ctx c) {
        StringBuilder b = new StringBuilder("<div class='sh'><h2>Products</h2><a class='btn sm' href='/staff/product'>Add product</a></div><div class='card tw'><table class='tbl'><tr><th></th><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th>Status</th><th></th></tr>");
        for (FoodProduct p : store.getProducts()) {
            b.append("<tr><td class='em' data-label=''>").append(h(p.getEmoji())).append("</td>").append(td("Name", h(p.getName()))).append(td("Category", p.getCategory())).append(td("Price", money(p.getPrice()))).append(td("Stock", "" + p.getStock())).append(td("Status", stockBadge(p)))
                .append("<td class='act' data-label=''><a class='btn alt sm' href='/staff/stock?id=").append(p.getId()).append("'>Edit stock</a> <a class='btn alt sm' href='/staff/product?id=").append(p.getId()).append("'>Edit details</a> ")
                .append("<form method='post' action='/staff/toggle' class='inl'><input type='hidden' name='id' value='").append(p.getId()).append("'><button class='btn alt sm'>").append(p.isAvailable() ? "Mark out of stock" : "Mark available").append("</button></form> ")
                .append("<a class='btn alt sm' href='/staff/product/delete?id=").append(p.getId()).append("'>Delete</a></td></tr>");
        }
        return layout(c, "Products", shell(c, "products", b.append("</table></div>").toString()));
    }
    String productForm(Ctx c, FoodProduct p) {
        StringBuilder b = new StringBuilder("<div class='card formcard'><h2>").append(p == null ? "Add a new product" : "Edit details").append("</h2><p class='mut'>").append(p == null ? "It shows on the menu right after you save." : "Change the name, price, emoji or description. Use Edit stock for the stock.")
            .append("</p><form method='post' action='/staff/product/save'><input type='hidden' name='id' value='").append(p == null ? 0 : p.getId()).append("'>")
            .append("<label for='n'>Product name</label><input id='n' name='name' class='in' required placeholder='e.g. Cheese Burger' value='").append(p == null ? "" : h(p.getName())).append("'>");
        if (p == null) { b.append("<label for='cat'>Category</label><select id='cat' name='category' class='in'><option>Meal</option><option>Drink</option><option>Dessert</option></select>"); }
        else b.append("<label>Category</label><p class='mut'>").append(p.getCategory()).append(" (cannot be changed)</p>");
        b.append("<div class='two2'><div><label for='pr'>Price</label><input id='pr' name='price' type='number' step='0.01' min='0.01' class='in' required value='").append(p == null ? "" : String.valueOf(p.getPrice())).append("'></div>");
        if (p == null) b.append("<div><label for='st'>Stock</label><input id='st' name='stock' type='number' min='0' class='in' required value='0'></div>");
        b.append("</div><label for='em'>Emoji</label><select id='em' name='emoji' class='in'>");
        String cur = p == null ? EMOJIS[0] : p.getEmoji(); boolean found = false;
        for (String e : EMOJIS) { boolean sel = e.equals(cur); found |= sel; b.append("<option").append(sel ? " selected" : "").append(">").append(e).append("</option>"); }
        if (!found) b.append("<option selected>").append(h(cur)).append("</option>");
        b.append("</select><label for='em2'>Or type your own emoji</label><input id='em2' name='emoji2' class='in' maxlength='4'><label for='ds'>Short description</label><input id='ds' name='desc' class='in' placeholder='Describe it in a few words' value='").append(p == null ? "" : h(p.getDescription())).append("'><br>")
            .append("<div class='two2'><a class='btn alt' href='/staff/products'>Cancel</a><button class='btn'>").append(p == null ? "Add to menu" : "Save changes").append("</button></div></form></div>");
        return layout(c, p == null ? "Add product" : "Edit details", shell(c, "products", b.toString()));
    }
    String stockForm(Ctx c, FoodProduct p) {
        StringBuilder b = new StringBuilder("<div class='card formcard'><h2>").append(h(p.getEmoji())).append(" ").append(h(p.getName())).append("</h2><p class='mut'>").append(p.getCategory()).append("  |  ").append(p.getStatus()).append("</p>")
            .append("<div class='bigno'><span class='mut'>Current stock</span><b>").append(p.getStock()).append("</b></div><form method='post' action='/staff/stock/save'><input type='hidden' name='id' value='").append(p.getId()).append("'>")
            .append("<div class='choice'><label><input type='radio' name='mode' value='add' checked> Add stock</label><label><input type='radio' name='mode' value='set'> Set exact stock</label></div>")
            .append("<label for='am'>Quantity</label><input id='am' name='amount' type='number' min='0' class='in' value='10' required><label>Quick add</label><div class='chips'>");
        for (int q : new int[]{5, 10, 20, 50}) b.append("<button class='chip' name='quick' value='").append(q).append("'>+").append(q).append("</button>");
        b.append("</div><label class='sw'><input type='checkbox' name='available' value='yes'").append(p.isAvailable() ? " checked" : "").append("> Available to customers</label>")
            .append("<p class='hint'>Untick it to mark the item out of stock without losing the stock count.</p><div class='two2'><a class='btn alt' href='/staff/products'>Cancel</a><button class='btn'>Save stock</button></div></form></div>");
        return layout(c, "Edit stock", shell(c, "products", b.toString()));
    }
    String deleteConfirm(Ctx c, FoodProduct p) {
        return layout(c, "Delete product", shell(c, "products", "<div class='card formcard'><h2>Delete " + h(p.getName()) + "?</h2><p class='mut'>This removes it from the menu and cannot be undone.</p><br><form method='post' action='/staff/product/delete'><input type='hidden' name='id' value='"
            + p.getId() + "'><div class='two2'><a class='btn alt' href='/staff/products'>Keep it</a><button class='btn'>Delete</button></div></form></div>"));
    }
}
