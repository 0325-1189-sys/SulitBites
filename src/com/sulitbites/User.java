package com.sulitbites;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/** ABSTRACTION + ENCAPSULATION: every user has a private password hash; subclasses decide their role and privileges. */
public abstract class User {
    private final String username;
    private final String passwordHash;
    private String name;
    private boolean approved = true;                     // new customers start as false until the admin approves them

    protected User(String username, String password, String name) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required.");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password is required.");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required.");
        this.username = username;
        this.passwordHash = isHash(password) ? password : hash(password);
        this.name = name.trim();
    }
    public static boolean isHash(String s) { return s != null && s.matches("[0-9a-f]{64}"); }
    public static String hash(String plain) {
        try {
            byte[] b = MessageDigest.getInstance("SHA-256").digest(("sulitbites:" + plain).getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte x : b) sb.append(String.format("%02x", x));
            return sb.toString();
        } catch (Exception e) { throw new IllegalStateException(e); }
    }
    public final boolean verify(String plain) { return plain != null && passwordHash.equals(hash(plain)); }
    public final String getUsername() { return username; }
    public final String getPasswordHash() { return passwordHash; }
    public final String getName() { return name; }
    public final void setName(String name) { if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required."); this.name = name.trim(); }

    public abstract String getRole();                    // POLYMORPHISM: each subclass names its own role
    public boolean isStaff() { return false; }
    public boolean canManageProducts() { return false; }
    public boolean canApproveAccounts() { return false; }                       // only the admin approves new accounts
    public final boolean isApproved() { return approved; }
    public final void setApproved(boolean approved) { this.approved = approved; }

    /** Factory method used when loading users from storage. */
    public static User create(String role, String username, String password, String name) {
        switch (role) {
            case "Admin": return new Admin(username, password, name);
            case "Cashier": return new Cashier(username, password, name);
            default: return new Customer(username, password, name);
        }
    }
}
