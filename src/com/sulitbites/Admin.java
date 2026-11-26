package com.sulitbites;
public final class Admin extends User {
    public Admin(String username, String password, String name) { super(username, password, name); }
    @Override public String getRole() { return "Admin"; }
    @Override public boolean isStaff() { return true; }
    @Override public boolean canManageProducts() { return true; }
    @Override public boolean canApproveAccounts() { return true; }
}
