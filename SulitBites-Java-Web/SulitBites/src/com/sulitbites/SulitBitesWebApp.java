package com.sulitbites;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;

/** The web server and its routes. It only translates requests into Store calls and Pages output. */
public final class SulitBitesWebApp {
    private static final class Request {
        String method, path, site = "localhost", referer; boolean https; Map<String, String> query = new HashMap<>(), form = new HashMap<>(); User user;
        String q(String k) { return query.getOrDefault(k, ""); }
        String f(String k) { return form.getOrDefault(k, ""); }
    }
    private static final class Response {
        final int status; final String body, type, location; final List<String> cookies = new ArrayList<>(); String cache = "no-store";
        Response(int status, String body, String type, String location) { this.status = status; this.body = body; this.type = type; this.location = location; }
        static Response html(int status, String body) { return new Response(status, body, "text/html; charset=UTF-8", null); }
    }

    private final Store store; private final Pages pages; private final SecureRandom random = new SecureRandom();
    private final Map<String, String> sessions = new ConcurrentHashMap<>();                  // session token -> username
    private final Map<String, LinkedHashMap<Integer, Integer>> carts = new HashMap<>();      // username -> (product id -> quantity)
    private final Map<String, long[]> failures = new HashMap<>();                            // username -> {failed attempts, first attempt time}
    private HttpServer server;

    public SulitBitesWebApp(Store store) { this.store = store; this.pages = new Pages(store); }

    /** Starts the server. Use port 0 to pick a free port. Returns the port in use. */
    public int start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        server.createContext("/", this::dispatch);
        server.setExecutor(Executors.newFixedThreadPool(8));
        server.start();
        return server.getAddress().getPort();
    }
    public void stop() { if (server != null) server.stop(0); }

    // ------------------------------------------------------------ request handling
    private void dispatch(HttpExchange ex) throws IOException {
        Response res;
        try {
            Request r = read(ex);
            synchronized (store) { res = route(r); }
        } catch (Exception e) {
            e.printStackTrace();
            res = Response.html(500, pages.error(new Ctx(null, 0, null, false, "localhost"), "Something went wrong", "Please go back and try again."));
        }
        send(ex, res);
    }
    private Request read(HttpExchange ex) throws IOException {
        Request r = new Request(); r.method = ex.getRequestMethod().toUpperCase(); r.path = ex.getRequestURI().getPath();
        r.query = parse(ex.getRequestURI().getRawQuery());
        String host = ex.getRequestHeaders().getFirst("X-Forwarded-Host");                 // set when a proxy such as Netlify sits in front
        if (host == null || host.isBlank()) host = ex.getRequestHeaders().getFirst("Host");
        if (host != null) r.site = host.split(",")[0].trim();
        r.https = "https".equalsIgnoreCase(ex.getRequestHeaders().getFirst("X-Forwarded-Proto")); r.referer = ex.getRequestHeaders().getFirst("Referer");
        if (r.method.equals("POST")) r.form = parse(new String(ex.getRequestBody().readNBytes(65536), StandardCharsets.UTF_8));
        String cookie = ex.getRequestHeaders().getFirst("Cookie");
        if (cookie != null) for (String c : cookie.split(";")) { String[] x = c.trim().split("=", 2); if (x.length == 2 && x[0].equals("sb_session")) { String name = sessions.get(x[1]); if (name != null) r.user = store.findUser(name); } }
        return r;
    }
    private static Map<String, String> parse(String raw) {
        Map<String, String> m = new HashMap<>(); if (raw == null || raw.isEmpty()) return m;
        for (String pair : raw.split("&")) { String[] x = pair.split("=", 2); try { m.put(URLDecoder.decode(x[0], StandardCharsets.UTF_8), x.length == 2 ? URLDecoder.decode(x[1], StandardCharsets.UTF_8) : ""); } catch (IllegalArgumentException ignored) { } }
        return m;
    }
    private void send(HttpExchange ex, Response res) throws IOException {
        var h = ex.getResponseHeaders();
        h.set("Cache-Control", res.cache); h.set("X-Content-Type-Options", "nosniff"); h.set("X-Frame-Options", "DENY"); h.set("Referrer-Policy", "same-origin");
        for (String c : res.cookies) h.add("Set-Cookie", c);
        if (res.location != null) { h.set("Location", res.location); ex.sendResponseHeaders(res.status, -1); ex.close(); return; }
        byte[] body = res.body == null || ex.getRequestMethod().equalsIgnoreCase("HEAD") ? new byte[0] : res.body.getBytes(StandardCharsets.UTF_8);
        if (res.type != null) h.set("Content-Type", res.type);
        if (body.length == 0) { ex.sendResponseHeaders(res.status, -1); ex.close(); return; }
        ex.sendResponseHeaders(res.status, body.length);
        try (OutputStream out = ex.getResponseBody()) { out.write(body); }
    }

    // ------------------------------------------------------------ helpers
    private Ctx ctx(Request r) { return new Ctx(r.user, cartCount(r.user), r.q("msg"), "1".equals(r.q("e")), r.site); }
    private int cartCount(User u) { if (u == null) return 0; int n = 0; for (int q : carts.getOrDefault(u.getUsername(), new LinkedHashMap<>()).values()) n += q; return n; }
    private LinkedHashMap<Integer, Integer> cartOf(User u) { return carts.computeIfAbsent(u.getUsername(), k -> new LinkedHashMap<>()); }
    private static int num(String s, int def) { try { return Integer.parseInt(s.trim()); } catch (RuntimeException e) { return def; } }
    private static double dbl(String s) { try { return Double.parseDouble(s.trim()); } catch (RuntimeException e) { throw new IllegalArgumentException("Enter a valid number."); } }
    private Response page(Request r, String body) { return Response.html(200, body); }
    private Response notFound(Request r) { return Response.html(404, pages.error(ctx(r), "Page not found", "That page does not exist.")); }
    private Response forbidden(Request r) { return Response.html(403, pages.error(ctx(r), "Not allowed", "Only the admin can do that.")); }
    private Response redirect(String to, String msg, boolean err) {
        String url = to; if (msg != null) url += (to.contains("?") ? "&" : "?") + "msg=" + Html.enc(msg) + (err ? "&e=1" : "");
        return new Response(303, null, null, url);
    }
    /** Goes back to the page the user came from (same site only), otherwise to the fallback. */
    private Response back(Request r, String fallback, String msg, boolean err) {
        String to = fallback;
        if (r.referer != null) try { URI u = URI.create(r.referer); if (u.getHost() != null && r.site.startsWith(u.getHost()) && u.getPath() != null && u.getPath().startsWith("/")) { to = u.getPath() + (u.getRawQuery() == null ? "" : "?" + u.getRawQuery().replaceAll("(&|^)(msg|e)=[^&]*", "")); to = to.replaceAll("\\?&", "?").replaceAll("\\?$", ""); } } catch (RuntimeException ignored) { }
        return redirect(to, msg, err);
    }
    private Response needLogin(Request r) { return r.user == null ? redirect("/login", "Please log in first.", true) : null; }
    private Response needStaff(Request r) { Response x = needLogin(r); return x != null ? x : r.user.isStaff() ? null : forbidden(r); }
    private Response needAdmin(Request r) { Response x = needStaff(r); return x != null ? x : r.user.canManageProducts() ? null : forbidden(r); }
    private Response startSession(Request r, User u, String to) {
        byte[] b = new byte[24]; random.nextBytes(b); StringBuilder t = new StringBuilder(); for (byte x : b) t.append(String.format("%02x", x));
        sessions.put(t.toString(), u.getUsername());
        Response res = redirect(to, null, false); res.cookies.add("sb_session=" + t + "; Path=/; HttpOnly; SameSite=Lax; Max-Age=86400" + (r.https ? "; Secure" : "")); return res;
    }

    // ------------------------------------------------------------ routes
    private Response route(Request r) {
        boolean get = r.method.equals("GET"), post = r.method.equals("POST"); String p = r.path;
        if (!get && !post) return new Response(405, "Method not allowed", "text/plain; charset=UTF-8", null);
        try { return get ? routeGet(r, p) : routePost(r, p); }
        catch (IllegalArgumentException e) { return Response.html(400, pages.error(ctx(r), "Please check", e.getMessage())); }
    }
    private Response routeGet(Request r, String p) {
        Ctx c = ctx(r); Response block;
        switch (p) {
            case "/": return page(r, pages.home(c));
            case "/style.css": { Response css = new Response(200, Css.TEXT, "text/css; charset=UTF-8", null); css.cache = "public, max-age=3600"; return css; }
            case "/favicon.ico": return new Response(204, null, null, null);
            case "/menu": return page(r, pages.menu(c, r.q("cat"), r.q("q")));
            case "/cart": return page(r, pages.cart(c, r.user == null ? null : cartOf(r.user)));
            case "/login": return r.user != null ? redirect("/", null, false) : page(r, pages.login(c));
            case "/register": return r.user != null ? redirect("/", null, false) : page(r, pages.register(c));
            case "/orders": if ((block = needLogin(r)) != null) return block; return r.user.isStaff() ? redirect("/staff/orders", null, false) : page(r, pages.orders(c));
            case "/receipt": {
                if ((block = needLogin(r)) != null) return block;
                Order o = store.findOrder(num(r.q("id"), -1)); if (o == null) return notFound(r);
                if (!r.user.isStaff() && !o.getUsername().equals(r.user.getUsername())) return forbidden(r);
                store.assignCashierIfBlank(r.user, o); return page(r, pages.receipt(ctx(r), o));
            }
            case "/staff": if ((block = needStaff(r)) != null) return block; return page(r, pages.dashboard(c));
            case "/staff/orders": if ((block = needStaff(r)) != null) return block; return page(r, pages.staffOrders(c));
            case "/staff/payments": if ((block = needStaff(r)) != null) return block; return page(r, pages.payments(c, r.q("q"), r.q("m")));
            case "/staff/customers": if ((block = needAdmin(r)) != null) return block; return page(r, pages.customers(c));
            case "/staff/products": if ((block = needAdmin(r)) != null) return block; return page(r, pages.staffProducts(c));
            case "/staff/product": {
                if ((block = needAdmin(r)) != null) return block;
                if (r.q("id").isEmpty()) return page(r, pages.productForm(c, null));
                FoodProduct f = store.findProduct(num(r.q("id"), -1)); return f == null ? notFound(r) : page(r, pages.productForm(c, f));
            }
            case "/staff/stock": { if ((block = needAdmin(r)) != null) return block; FoodProduct f = store.findProduct(num(r.q("id"), -1)); return f == null ? notFound(r) : page(r, pages.stockForm(c, f)); }
            case "/staff/product/delete": { if ((block = needAdmin(r)) != null) return block; FoodProduct f = store.findProduct(num(r.q("id"), -1)); return f == null ? notFound(r) : page(r, pages.deleteConfirm(c, f)); }
            default: return notFound(r);
        }
    }
    private Response routePost(Request r, String p) {
        Response block;
        switch (p) {
            case "/login": return doLogin(r);
            case "/register": return doRegister(r);
            case "/logout": {
                String cookie = ""; for (Map.Entry<String, String> e : new ArrayList<>(sessions.entrySet())) if (r.user != null && e.getValue().equals(r.user.getUsername())) sessions.remove(e.getKey());
                Response res = redirect("/", "You are logged out.", false); res.cookies.add("sb_session=; Path=/; HttpOnly; SameSite=Lax; Max-Age=0"); return res;
            }
            case "/cart/add": case "/cart/update": case "/cart/remove": case "/checkout": if ((block = needLogin(r)) != null) return block.location.startsWith("/login") ? redirect("/login", "Log in to order.", true) : block; break;
            default: break;
        }
        switch (p) {
            case "/cart/add": return cartAdd(r);
            case "/cart/update": return cartUpdate(r);
            case "/cart/remove": cartOf(r.user).remove(num(r.f("id"), -1)); return back(r, "/cart", "Removed from your cart.", false);
            case "/checkout": return checkout(r);
            case "/staff/status": if ((block = needStaff(r)) != null) return block; return changeStatus(r);
            case "/staff/product/save": if ((block = needAdmin(r)) != null) return block; return saveProduct(r);
            case "/staff/stock/save": if ((block = needAdmin(r)) != null) return block; return saveStock(r);
            case "/staff/toggle": {
                if ((block = needAdmin(r)) != null) return block;
                try { store.toggleAvailable(num(r.f("id"), -1)); FoodProduct f = store.findProduct(num(r.f("id"), -1)); return redirect("/staff/products", f.getName() + (f.isAvailable() ? " is available again." : " is marked out of stock."), false); }
                catch (IllegalArgumentException e) { return redirect("/staff/products", e.getMessage(), true); }
            }
            case "/staff/product/delete": {
                if ((block = needAdmin(r)) != null) return block;
                try { store.deleteProduct(num(r.f("id"), -1)); return redirect("/staff/products", "Product deleted.", false); } catch (IllegalArgumentException e) { return redirect("/staff/products", e.getMessage(), true); }
            }
            default: return notFound(r);
        }
    }

    // ------------------------------------------------------------ actions
    private Response doLogin(Request r) {
        String name = r.f("username").trim().toLowerCase(); long now = System.currentTimeMillis(); long[] f = failures.get(name);
        if (f != null && now - f[1] > 300_000) { failures.remove(name); f = null; }
        if (f != null && f[0] >= 5) return redirect("/login", "Too many wrong attempts. Please wait a few minutes and try again.", true);
        User u = store.authenticate(r.f("username").trim(), r.f("password"));
        if (u == null) { long[] x = failures.computeIfAbsent(name, k -> new long[]{0, now}); x[0]++; return redirect("/login", "Wrong username or password. Check both and try again.", true); }
        failures.remove(name);
        return startSession(r, u, u.isStaff() ? "/staff" : "/menu");
    }
    private Response doRegister(Request r) {
        try { User u = store.register(r.f("name"), r.f("username").trim(), r.f("password")); return startSession(r, u, "/menu"); }
        catch (IllegalArgumentException e) { return redirect("/register", e.getMessage(), true); }
    }
    private Response cartAdd(Request r) {
        FoodProduct p = store.findProduct(num(r.f("id"), -1)); int qty = num(r.f("qty"), 1);
        if (p == null || qty < 1) return back(r, "/menu", "That item could not be added.", true);
        if (!p.isInStock()) return back(r, "/menu", p.getName() + " is out of stock right now.", true);
        LinkedHashMap<Integer, Integer> cart = cartOf(r.user); int have = cart.getOrDefault(p.getId(), 0);
        if (have + qty > p.getStock()) return back(r, "/menu", "Only " + p.getStock() + " left of " + p.getName() + (have > 0 ? " (you already have " + have + " in your cart)." : "."), true);
        cart.put(p.getId(), have + qty); return back(r, "/menu", p.getName() + " added to your cart.", false);
    }
    private Response cartUpdate(Request r) {
        FoodProduct p = store.findProduct(num(r.f("id"), -1)); int qty = num(r.f("qty"), -1); LinkedHashMap<Integer, Integer> cart = cartOf(r.user);
        if (p == null || qty < 0) return redirect("/cart", "Enter a quantity of 0 or more.", true);
        if (qty == 0) { cart.remove(p.getId()); return redirect("/cart", "Removed from your cart.", false); }
        if (qty > p.getStock()) return redirect("/cart", "Only " + p.getStock() + " left of " + p.getName() + ".", true);
        cart.put(p.getId(), qty); return redirect("/cart", "Cart updated.", false);
    }
    private Response checkout(Request r) {
        try {
            Order o = store.placeOrder(r.user, cartOf(r.user), r.f("method"), r.f("details"), r.f("customer"));
            carts.remove(r.user.getUsername()); return redirect("/receipt?id=" + o.getId(), "Order placed. Thank you!", false);
        } catch (IllegalArgumentException e) { return redirect("/cart", e.getMessage(), true); }
    }
    private Response changeStatus(Request r) {
        try { store.changeStatus(r.user, num(r.f("id"), -1), Order.Status.fromLabel(r.f("status"))); return redirect("/staff/orders", "Order #" + r.f("id") + " is now " + r.f("status") + ".", false); }
        catch (IllegalArgumentException e) { return redirect("/staff/orders", e.getMessage(), true); }
    }
    private Response saveProduct(Request r) {
        int id = num(r.f("id"), 0); String emoji = r.f("emoji2").isBlank() ? r.f("emoji") : r.f("emoji2");
        try {
            if (id == 0) { store.addProduct(r.f("category"), r.f("name"), dbl(r.f("price")), num(r.f("stock"), 0), emoji, r.f("desc")); return redirect("/staff/products", "Product added to the menu.", false); }
            store.updateProduct(id, r.f("name"), dbl(r.f("price")), emoji, r.f("desc")); return redirect("/staff/products", "Product saved.", false);
        } catch (IllegalArgumentException e) { return redirect(id == 0 ? "/staff/product" : "/staff/product?id=" + id, e.getMessage(), true); }
    }
    private Response saveStock(Request r) {
        int id = num(r.f("id"), -1); boolean quick = !r.f("quick").isEmpty();
        try {
            int amount = quick ? num(r.f("quick"), 0) : num(r.f("amount"), -1);
            if (amount < 0) throw new IllegalArgumentException("Enter a whole number, 0 or more.");
            store.changeStock(id, quick ? "add" : r.f("mode").equals("set") ? "set" : "add", amount, "yes".equals(r.f("available")));
            FoodProduct f = store.findProduct(id); return redirect("/staff/products", f.getName() + ": stock is now " + f.getStock() + (f.isAvailable() ? "." : ", hidden from customers."), false);
        } catch (IllegalArgumentException e) { return redirect("/staff/stock?id=" + id, e.getMessage(), true); }
    }
}
