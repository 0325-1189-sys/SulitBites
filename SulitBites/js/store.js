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

const CLOUD = (typeof CLOUD_URL === "string" ? CLOUD_URL.trim() : "").replace(/\/+$/, "");
const canon = v => JSON.stringify(v, (k, x) => x && typeof x === "object" && !Array.isArray(x) ? Object.fromEntries(Object.entries(x).sort(([a], [b]) => a < b ? -1 : 1)) : x);

/** Data lives on this device (localStorage). When CLOUD_URL is set it is also shared through a Firebase Realtime Database,
    so every device sees the same accounts, products and orders. */
class Store {
  #mem = {}; #snap = {}; #synced = false;
  pending = Promise.resolve(); online = null;
  constructor() {
    this.users = (this.#r("users") || SEED_USERS).map(User.create);
    // keep staff display names in sync with SEED_USERS, even for data saved earlier in this browser
    SEED_USERS.filter(s => s.role !== "Customer").forEach(s => { const u = this.users.find(x => x.username === s.username); if (u) u.name = s.name; });
    this.products = (this.#r("products") || SEED_PRODUCTS).map(FoodProduct.create);
    this.orders = (this.#r("orders") || []).map(d => new Order(d));
    this.cart = this.#r("cart") || [];
    this.#snap = this.#r("snap") || {};
  }
  get cloud() { return !!CLOUD; }
  #r(k) { try { const v = localStorage.getItem("sb_" + k); return v ? JSON.parse(v) : null; } catch { return this.#mem[k] ?? null; } }
  #w(k, v) { try { localStorage.setItem("sb_" + k, JSON.stringify(v)); } catch { this.#mem[k] = v; } }
  save() {
    ["users", "products", "orders", "cart"].forEach(k => this.#w(k, this[k]));
    this.pending = this.pending.then(() => this.#push()).catch(() => {});
  }
  #flat() {   // every record as one flat map: "orders/o1001" -> canonical JSON text
    const o = {}, key = s => s.toLowerCase().replace(/[^a-z0-9_]/g, "_");
    this.users.forEach(u => o["users/" + key(u.username)] = canon(u));
    this.products.forEach(p => o["products/p" + p.id] = canon(p));
    this.orders.forEach(x => o["orders/o" + x.id] = canon(x));
    return o;
  }
  async #push() {   // send only what changed since the last successful upload
    if (!CLOUD || !this.#synced) return;
    const now = this.#flat(), patch = {};
    for (const k in now) if (this.#snap[k] !== now[k]) patch[k] = JSON.parse(now[k]);
    for (const k in this.#snap) if (!(k in now)) patch[k] = null;
    if (!Object.keys(patch).length) return;
    try {
      const r = await fetch(CLOUD + "/.json", { method: "PATCH", body: JSON.stringify(patch) });
      if (!r.ok) throw new Error(r.status);
      this.#snap = now; this.#w("snap", now); this.online = true;
    } catch { this.online = false; }
  }
  async secure() {   // turn any readable password into a hash
    let changed = false;
    for (const u of this.users) if (await u.upgrade()) changed = true;
    if (changed) this.save();
  }
  async pull() {   // download the shared data and merge it with this device. Returns true if anything changed.
    if (!CLOUD) return false;
    await this.pending;
    let d;
    try { const r = await fetch(CLOUD + "/.json", { cache: "no-store" }); if (!r.ok) throw new Error(r.status); d = (await r.json()) || {}; this.online = true; }
    catch { this.online = false; return false; }
    const local = this.#flat(), before = canon(local), cloud = {};
    for (const c of ["users", "products", "orders"]) for (const k in d[c] || {}) cloud[c + "/" + k] = canon(d[c][k]);
    const next = { ...cloud };
    for (const k in local) {
      if (!(k in cloud) && !(k in this.#snap)) next[k] = local[k];                                              // new on this device, not uploaded yet
      else if (k in cloud && local[k] !== this.#snap[k] && cloud[k] === this.#snap[k]) next[k] = local[k];      // edited on this device, not uploaded yet
    }                                                                                                            // everything else: the shared copy wins
    const col = { users: [], products: [], orders: [] };
    for (const k in next) col[k.split("/")[0]].push(JSON.parse(next[k]));
    this.users = col.users.map(User.create);
    this.products = col.products.map(FoodProduct.create).sort((a, b) => a.id - b.id);
    this.orders = col.orders.map(o => new Order(o)).sort((a, b) => b.id - a.id);
    this.#snap = cloud; this.#synced = true;
    ["users", "products", "orders"].forEach(k => this.#w(k, this[k])); this.#w("snap", cloud);
    await this.#push();
    return canon(this.#flat()) !== before;
  }
  findUser(name) { return this.users.find(u => u.username.toLowerCase() === String(name).toLowerCase()); }
  sessionUser() { return this.findUser(this.#r("session") || ""); }
  setSession(name) { this.#w("session", name); }
  product(id) { return this.products.find(p => p.id === Number(id)); }
  nextProductId() { return Math.max(100, ...this.products.map(p => p.id)) + 1; }
  nextOrderId() { return Math.max(1000, ...this.orders.map(o => o.id)) + 1; }
  cartItems() { return this.cart.filter(c => this.product(c.id)).map(c => new OrderItem(this.product(c.id), c.qty)); }
}
