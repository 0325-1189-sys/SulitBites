package com.sulitbites;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/** Small helpers for writing HTML safely. */
final class Html {
    private Html() { }
    private static final DateTimeFormatter WHEN = DateTimeFormatter.ofPattern("MMM d, yyyy h:mm a", Locale.US).withZone(ZoneId.of("Asia/Manila"));
    static String h(String s) { return s == null ? "" : s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&#39;"); }
    static String money(double d) { return String.format(Locale.US, "\u20B1%,.2f", d); }
    static String enc(String s) { return URLEncoder.encode(s == null ? "" : s, StandardCharsets.UTF_8); }
    static String when(Instant i) { return WHEN.format(i); }
    static String badge(String status) { return "<span class='bd " + h(status) + "'>" + h(status) + "</span>"; }
    static String stockBadge(FoodProduct p) {
        String cls = p.getStatus().equals("In stock") ? "Ready" : p.getStatus().equals("Low stock") ? "Pending" : "Cancelled";
        return "<span class='bd " + cls + "'>" + h(p.getStatus()) + "</span>";
    }
    /** What a customer sees on the menu. */
    static String availability(FoodProduct p) {
        if (!p.isInStock()) return "<span class='av av-no'>Out of stock</span>";
        if (p.getStock() <= 5) return "<span class='av av-low'>Only " + p.getStock() + " left</span>";
        return "<span class='av av-ok'>Available</span>";
    }
    static String td(String label, String content) { return "<td data-label='" + h(label) + "'>" + content + "</td>"; }
}
