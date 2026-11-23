package com.sulitbites;

/** Everything a page needs to know about the current request. */
final class Ctx {
    final User user; final int cartCount; final String msg; final boolean err; final String site;
    Ctx(User user, int cartCount, String msg, boolean err, String site) { this.user = user; this.cartCount = cartCount; this.msg = msg; this.err = err; this.site = site; }
    boolean staff() { return user != null && user.isStaff(); }
}
