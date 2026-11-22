package com.sulitbites;

public final class Customer extends User {
    public Customer(String username, String password, String name) { super(username, password, name); }
    @Override public String getRole() { return "Customer"; }
}
