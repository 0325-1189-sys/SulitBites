package com.sulitbites;
public final class Cashier extends User {
    public Cashier(String username, String password, String name) { super(username, password, name); }
    @Override public String getRole() { return "Cashier"; }
    @Override public boolean isStaff() { return true; }
}
