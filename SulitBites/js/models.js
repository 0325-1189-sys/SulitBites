"use strict";
/* ===== Four Pillars of OOP =====
   ABSTRACTION   : User, FoodProduct, Payment cannot be instantiated; subclasses fill in the blanks.
   INHERITANCE   : Admin/Cashier/Customer extend User; Meal/Drink/Dessert extend FoodProduct; Cash/GCash/Card extend Payment.
   POLYMORPHISM  : role, adminTabs, total(), promo, validate() are overridden per subclass.
   ENCAPSULATION : #private fields with validated getters/setters (price, stock, password). */
const abstractCheck = (self, cls) => { if (self.constructor === cls) throw new TypeError(cls.name + " is abstract"); };

class User {
  #password;
  constructor(username, password, name) { abstractCheck(this, User); this.username = username; this.name = name; this.#password = password; }
  checkPassword(p) { return this.#password === p; }
  static async hash(p) {   // SHA-256, so the shared database never stores readable passwords
    if (!(globalThis.crypto && crypto.subtle)) return p;
    const b = await crypto.subtle.digest("SHA-256", new TextEncoder().encode("sulitbites:" + p));
    return [...new Uint8Array(b)].map(x => x.toString(16).padStart(2, "0")).join("");
  }
  async verify(p) { return this.#password === await User.hash(p) || this.#password === p; }   // plain match = accounts made before hashing
  get needsHash() { return !/^[0-9a-f]{64}$/.test(this.#password); }
  async upgrade() { const was = this.#password; if (this.needsHash) this.#password = await User.hash(was); return this.#password !== was; }
  get role() { throw new Error("role not implemented"); }
  get isStaff() { return false; }
  get canManageProducts() { return false; }
  get adminTabs() { return []; }
  toJSON() { return { role: this.role, username: this.username, name: this.name, password: this.#password }; }
  static create(d) { return new ({ Admin, Cashier, Customer }[d.role])(d.username, d.password, d.name); }
}
class Admin extends User {
  get role() { return "Admin"; } get isStaff() { return true; } get canManageProducts() { return true; }
  get adminTabs() { return ["dashboard", "products", "orders", "customers", "payments"]; }
}
class Cashier extends User {
  get role() { return "Cashier"; } get isStaff() { return true; }
  get adminTabs() { return ["dashboard", "orders", "payments"]; }
}
class Customer extends User { get role() { return "Customer"; } }

class FoodProduct {
  #price = 0; #stock = 0; #available = true;
  constructor(id, name, price, stock, emoji = "🍽️", desc = "") {
    abstractCheck(this, FoodProduct);
    Object.assign(this, { id, name, emoji, desc }); this.price = price; this.stock = stock;
  }
  get price() { return this.#price; }
  set price(v) { v = Number(v); if (!(v > 0)) throw new Error("Price must be greater than 0."); this.#price = v; }
  get stock() { return this.#stock; }
  set stock(v) { v = Number(v); if (!Number.isInteger(v) || v < 0) throw new Error("Stock must be a whole number, 0 or more."); this.#stock = v; }
  get available() { return this.#available; }                       // admin can switch an item off manually
  set available(v) { this.#available = Boolean(v); }
  get inStock() { return this.#available && this.#stock > 0; }
  get status() { return !this.inStock ? "Out of stock" : this.#stock <= 5 ? "Low stock" : "In stock"; }
  get category() { throw new Error("category not implemented"); }
  get promo() { return ""; }
  total(qty) { throw new Error("total not implemented"); }
  toJSON() { return { category: this.category, id: this.id, name: this.name, price: this.price, stock: this.stock, emoji: this.emoji, desc: this.desc, available: this.available }; }
  static create(d) { const p = new ({ Meal, Drink, Dessert }[d.category])(d.id, d.name, d.price, d.stock, d.emoji, d.desc); p.available = d.available !== false; return p; }
}
class Meal extends FoodProduct {
  get category() { return "Meal"; } get promo() { return "Buy 5 or more, get 10% off"; }
  total(q) { return this.price * q * (q >= 5 ? 0.9 : 1); }
}
class Drink extends FoodProduct {
  get category() { return "Drink"; }
  total(q) { return this.price * q; }
}
class Dessert extends FoodProduct {
  get category() { return "Dessert"; } get promo() { return "Buy 3 or more, get 5% off"; }
  total(q) { return this.price * q * (q >= 3 ? 0.95 : 1); }
}

class Payment {
  change = 0;
  constructor(amount, detail) { abstractCheck(this, Payment); this.amount = amount; this.detail = String(detail || "").trim(); }
  get method() { throw new Error("method not implemented"); }
  validate() { throw new Error("validate not implemented"); }
  process() { this.validate(); return this; }
}
class CashPayment extends Payment {
  get method() { return "Cash"; }
  validate() {
    const t = parseFloat(this.detail);
    if (isNaN(t)) throw new Error("Enter the cash amount you are handing over.");
    if (t < this.amount) throw new Error("Cash is less than the total. Enter at least " + this.amount.toFixed(2) + ".");
    this.change = t - this.amount;
  }
}
class GCashPayment extends Payment {
  get method() { return "GCash"; }
  validate() { if (!/^09\d{9}$/.test(this.detail)) throw new Error("GCash number must be 11 digits and start with 09."); }
}
class CardPayment extends Payment {
  get method() { return "Card"; }
  validate() { if (!/^\d{16}$/.test(this.detail.replace(/\s/g, ""))) throw new Error("Card number must be 16 digits."); }
}

class OrderItem {
  constructor(product, qty) { this.product = product; this.qty = qty; }
  get subtotal() { return this.product.total(this.qty); }   // polymorphic call
}
class Order {
  static STATUSES = ["Pending", "Preparing", "Ready", "Completed", "Cancelled"];
  constructor(d) { Object.assign(this, { status: "Pending", date: new Date().toISOString(), cashier: "", type: "Online order" }, d); }
  get billed() { return this.lines.reduce((s, l) => s + l.subtotal, 0); }                                   // amount before any cancellation
  get gross() { return this.lines.reduce((s, l) => s + (l.price ?? l.subtotal / l.qty) * l.qty, 0); }        // price x qty, no discount
  get discount() { return this.gross - this.billed; }
  get total() { return this.status === "Cancelled" ? 0 : this.billed; }                                      // cancelled orders count as 0
}
