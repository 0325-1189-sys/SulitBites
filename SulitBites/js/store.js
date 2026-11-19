"use strict";
const SEED_USERS = [
  { role: "Admin", username: "admin", password: "admin123", name: "Nicole Admin" },
  { role: "Cashier", username: "cashier", password: "cashier123", name: "Maria Cashier" },
  { role: "Customer", username: "juan", password: "juan123", name: "Juan Dela Cruz" }
];
const SEED_PRODUCTS = [
  { category: "Meal", id: 101, name: "Classic Burger", price: 99, stock: 40, emoji: "🍔", desc: "Juicy beef patty with fresh vegetables." },
  { category: "Meal", id: 102, name: "Fried Chicken", price: 119, stock: 35, emoji: "🍗", desc: "Crunchy outside, juicy inside." },
  { category: "Meal", id: 103, name: "Crispy Fries", price: 59, stock: 60, emoji: "🍟", desc: "Golden fries with a pinch of salt." },
  { category: "Meal", id: 104, name: "Spaghetti", price: 89, stock: 30, emoji: "🍝", desc: "Sweet Filipino-style spaghetti." },
  { category: "Meal", id: 105, name: "Chicken Adobo Rice", price: 109, stock: 25, emoji: "🍛", desc: "Garlicky adobo over steamed rice." },
  { category: "Drink", id: 106, name: "Iced Tea", price: 39, stock: 80, emoji: "🥤", desc: "Chilled and lightly sweet." },
  { category: "Drink", id: 107, name: "Iced Coffee", price: 65, stock: 50, emoji: "🧋", desc: "Bold coffee over ice." },
  { category: "Drink", id: 108, name: "Hot Coffee", price: 55, stock: 50, emoji: "☕", desc: "Freshly brewed, served hot." },
  { category: "Dessert", id: 109, name: "Halo-Halo", price: 79, stock: 30, emoji: "🍧", desc: "Shaved ice with sweet toppings." },
  { category: "Dessert", id: 110, name: "Leche Flan", price: 55, stock: 30, emoji: "🍮", desc: "Silky caramel custard." },
  { category: "Dessert", id: 111, name: "Ice Cream Cup", price: 45, stock: 40, emoji: "🍨", desc: "Creamy vanilla scoop." }
];

/** Saves everything in the browser's localStorage (falls back to memory if blocked). */
class Store {
  #mem = {};
  constructor() {
    this.users = (this.#r("users") || SEED_USERS).map(User.create);
    // keep staff display names in sync with SEED_USERS, even for data saved earlier in this browser
    SEED_USERS.filter(s => s.role !== "Customer").forEach(s => { const u = this.users.find(x => x.username === s.username); if (u) u.name = s.name; });
    this.products = (this.#r("products") || SEED_PRODUCTS).map(FoodProduct.create);
    this.orders = (this.#r("orders") || []).map(d => new Order(d));
    this.cart = this.#r("cart") || [];
  }
  #r(k) { try { const v = localStorage.getItem("sb_" + k); return v ? JSON.parse(v) : null; } catch { return this.#mem[k] ?? null; } }
  #w(k, v) { try { localStorage.setItem("sb_" + k, JSON.stringify(v)); } catch { this.#mem[k] = v; } }
  save() { ["users", "products", "orders", "cart"].forEach(k => this.#w(k, this[k])); }
  findUser(name) { return this.users.find(u => u.username.toLowerCase() === String(name).toLowerCase()); }
  sessionUser() { return this.findUser(this.#r("session") || ""); }
  setSession(name) { this.#w("session", name); }
  product(id) { return this.products.find(p => p.id === Number(id)); }
  nextProductId() { return Math.max(100, ...this.products.map(p => p.id)) + 1; }
  nextOrderId() { return Math.max(1000, ...this.orders.map(o => o.id)) + 1; }
  cartItems() { return this.cart.filter(c => this.product(c.id)).map(c => new OrderItem(this.product(c.id), c.qty)); }
}
