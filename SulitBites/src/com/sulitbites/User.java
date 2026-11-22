package com.sulitbites;

public abstract class User {
    private final String username;
    private final String password;
    private String name;

    protected User(String username, String password, String name) {
        if (username == null || username.isBlank()) throw new IllegalArgumentException("Username is required.");
        if (password == null || password.isBlank()) throw new IllegalArgumentException("Password is required.");
        this.username = username;
        this.password = password;
        this.name = name;
    }

    public final String getUsername() { return username; }
    public final String getPassword() { return password; }
    public final String getName() { return name; }
    public final void setName(String name) { this.name = name; }
    public abstract String getRole();
    public boolean isStaff() { return false; }
    public boolean canManageProducts() { return false; }
}
