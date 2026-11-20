"use strict";
const $ = s => document.querySelector(s);
const peso = n => "₱" + Number(n).toLocaleString("en-PH", { minimumFractionDigits: 2, maximumFractionDigits: 2 });
const esc = s => String(s).replace(/[&<>"']/g, c => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c]));
const when = d => new Date(d).toLocaleString("en-PH", { dateStyle: "medium", timeStyle: "short" });
const badge = s => `<span class="bd ${s}">${s}</span>`;
const db = new Store();
let user = db.sessionUser(), authTab = "login";
const mf = { cat: "All", q: "" };
const STORE = { name: "Sulit Bites", branch: "Main Branch", address: "Laguna State Polytechnic University, Laguna" };   // edit for the receipt header

function toast(m, err) { const t = $("#toast"); t.textContent = m; t.className = "show" + (err ? " err" : ""); clearTimeout(toast.t); toast.t = setTimeout(() => t.className = "", 2800); }
function modal(h) { $("#modal").innerHTML = `<div class="box">${h}</div>`; $("#modal").classList.remove("hidden"); }
function closeModal() { $("#modal").classList.add("hidden"); }
$("#modal").onclick = e => { if (e.target.id === "modal") closeModal(); };

function renderNav() {
  const n = db.cart.reduce((s, i) => s + i.qty, 0);
  $("#nav").innerHTML = `<div class="nav"><a class="logo" href="#/"><b>🍔</b>Sulit Bites</a>
  <nav><a href="#/">Home</a><a href="#/menu">Menu</a>${user && !user.isStaff ? '<a href="#/orders">My Orders</a>' : ""}${user && user.isStaff ? '<a href="#/admin/dashboard">Dashboard</a>' : ""}</nav>
  <div class="nr"><a class="btn alt sm" href="#/cart">Cart<span class="cnt">${n}</span></a>
  ${user ? `<div class="who">${esc(user.name)}<small>${user.role}</small></div><button class="btn sm" onclick="logout()">Logout</button>` : '<a class="btn sm" href="#/login">Login</a>'}</div></div>`;
}

const card = p => `<article class="card pc ${p.category}"><div class="thumb">${p.emoji}</div><div class="cb"><span class="tagp">${p.category}</span><h3>${esc(p.name)}</h3><p>${esc(p.desc)}</p>${p.promo ? `<small class="promo">${p.promo}</small>` : ""}<div class="row"><b class="price">${peso(p.price)}</b>${p.stock > 0 ? `<button class="btn sm" onclick="addCart(${p.id})">Add to cart</button>` : '<span class="mut">Sold out</span>'}</div></div></article>`;

/* ---------- customer views ---------- */
function home() {
  const fav = db.products.filter(p => p.stock > 0).slice(0, 4), t = fav.slice(0, 3);
  const sum = t.reduce((s, p) => s + p.price, 0);
  return `<section class="hero"><div class="wrap hero-in"><div><h1>Big flavor,<br>small price.</h1>
  <p>Burgers, fried chicken, drinks and desserts. Pick what you crave, pay your way, and track your order from the kitchen to your table.</p>
  <div class="cta"><a class="btn lg" href="#/menu">Order now</a><a class="btn alt lg" href="#/login">Sign in</a></div></div>
  <div class="ticket"><b>Your order</b><div class="tdash"></div>${t.map(p => `<div class="tr"><span>${p.emoji} ${esc(p.name)}</span><span>${peso(p.price)}</span></div>`).join("")}<div class="tdash"></div>
  <div class="tr tot"><span>Total</span><span>${peso(sum)}</span></div><a class="btn y full" href="#/menu">Start your order</a></div></div></section>
  <section class="wrap sec"><div class="sh"><div><h2>Our favorites</h2><p class="mut">The picks our regulars keep coming back for.</p></div><a class="btn alt sm" href="#/menu">See full menu</a></div><div class="grid">${fav.map(card).join("")}</div></section>
  <section class="wrap sec"><h2>Browse by category</h2><div class="g3">${[["Meal", "🍛", "Hearty meals"], ["Drink", "🥤", "Cool drinks"], ["Dessert", "🍧", "Sweet treats"]].map(c => `<a class="card cat-t ${c[0]}" href="#/menu/${c[0]}"><i>${c[1]}</i><h3>${c[2]}</h3></a>`).join("")}</div></section>
  <section class="wrap sec"><h2>How ordering works</h2><div class="g3">${[["Pick your food", "Browse the menu and add items to your cart."], ["Pay your way", "Cash, GCash or card. Change is computed for you."], ["Track your order", "Watch it move from Pending to Completed."]].map((s, i) => `<div class="card stp"><span class="n">${i + 1}</span><h3>${s[0]}</h3><p class="mut">${s[1]}</p></div>`).join("")}</div></section>
  <section class="wrap sec"><div class="banner"><div><h2>Hungry already?</h2><p>Create a free account and place your first order in minutes.</p></div><a class="btn y lg" href="#/menu">Browse the menu</a></div></section>`;
}
function menu(c) {
  mf.cat = ["Meal", "Drink", "Dessert"].includes(c) ? c : "All"; mf.q = "";
  return `<section class="wrap sec"><div class="sh"><div><h2>Our menu</h2><p class="mut">Add your favorites to the cart.</p></div><input class="in sm" aria-label="Search food" placeholder="Search food" oninput="mf.q=this.value;grid()"></div><div class="pills" id="pills"></div><div class="grid" id="grid"></div></section>`;
}
function grid() {
  $("#pills").innerHTML = ["All", "Meal", "Drink", "Dessert"].map(c => `<button class="pill ${mf.cat === c ? "on" : ""}" onclick="mf.cat='${c}';grid()">${c}</button>`).join("");
  const q = mf.q.toLowerCase(), l = db.products.filter(p => (mf.cat === "All" || p.category === mf.cat) && p.name.toLowerCase().includes(q));
  $("#grid").innerHTML = l.length ? l.map(card).join("") : '<p class="mut">No food matches your search. Try another name.</p>';
}
function addCart(id) {
  const p = db.product(id), line = db.cart.find(i => i.id === id);
  if ((line ? line.qty : 0) + 1 > p.stock) return toast("Only " + p.stock + " left of " + p.name, 1);
  line ? line.qty++ : db.cart.push({ id, qty: 1 }); db.save(); renderNav(); toast(p.name + " added to cart");
}
function chg(id, d) {
  const l = db.cart.find(i => i.id === id), p = db.product(id); l.qty += d;
  if (l.qty > p.stock) { l.qty = p.stock; toast("Only " + p.stock + " left", 1); }
  if (l.qty <= 0) db.cart = db.cart.filter(i => i.id !== id);
  db.save(); route();
}
function rm(id) { db.cart = db.cart.filter(i => i.id !== id); db.save(); route(); }
function cart() {
  const items = db.cartItems();
  if (!items.length) return `<section class="wrap sec empty"><div class="big">🛒</div><h2>Your cart is empty</h2><p class="mut">Add something delicious from the menu.</p><br><a class="btn lg" href="#/menu">Browse the menu</a></section>`;
  const raw = items.reduce((s, i) => s + i.product.price * i.qty, 0), total = items.reduce((s, i) => s + i.subtotal, 0);
  return `<section class="wrap sec"><h2>Your cart</h2><div class="split"><div class="card">${items.map(i => `<div class="line"><span class="em">${i.product.emoji}</span><div class="li"><b>${esc(i.product.name)}</b><small>${peso(i.product.price)} each</small></div><div class="qty"><button aria-label="Less" onclick="chg(${i.product.id},-1)">−</button><span>${i.qty}</span><button aria-label="More" onclick="chg(${i.product.id},1)">+</button></div><b class="lt">${peso(i.subtotal)}</b><button class="x" aria-label="Remove" onclick="rm(${i.product.id})">✕</button></div>`).join("")}</div>
  <aside class="card sum"><h3>Order summary</h3><div class="r"><span>Subtotal</span><span>${peso(raw)}</span></div><div class="r g"><span>Bulk discount</span><span>−${peso(raw - total)}</span></div><div class="r t"><span>Total</span><span>${peso(total)}</span></div>
  ${user && user.isStaff ? '<label for="cn">Customer name</label><input id="cn" class="in" placeholder="Walk-in customer">' : ""}<label for="pm">Payment method</label><select id="pm" class="in" onchange="pdHint()"><option>Cash</option><option>GCash</option><option>Card</option></select><input id="pd" class="in" placeholder="Cash amount, e.g. 500"><button class="btn lg full" onclick="checkout()">${user ? "Place order" : "Login to checkout"}</button></aside></div></section>`;
}
function pdHint() { $("#pd").placeholder = { Cash: "Cash amount, e.g. 500", GCash: "GCash number, 09XXXXXXXXX", Card: "16-digit card number" }[$("#pm").value]; }
function checkout() {
  if (!user) { location.hash = "#/login"; return; }
  const items = db.cartItems(), bad = items.find(i => i.qty > i.product.stock);
  if (bad) return toast("Not enough stock for " + bad.product.name, 1);
  const total = items.reduce((s, i) => s + i.subtotal, 0);
  const Pay = { Cash: CashPayment, GCash: GCashPayment, Card: CardPayment }[$("#pm").value];   // polymorphism
  const pay = new Pay(total, $("#pd").value);
  try { pay.process(); } catch (e) { return toast(e.message, 1); }
  items.forEach(i => i.product.stock -= i.qty);
  const walk = user.isStaff, cn = walk ? ($("#cn").value.trim() || "Walk-in customer") : user.name;
  const o = new Order({ id: db.nextOrderId(), username: user.username, customer: cn, method: pay.method, change: pay.change,
    type: walk ? "Walk-in" : "Online order", cashier: walk ? user.name : "",
    lines: items.map(i => ({ pid: i.product.id, name: i.product.name, emoji: i.product.emoji, qty: i.qty, price: i.product.price, subtotal: i.subtotal })) });
  db.orders.unshift(o); db.cart = []; db.save(); location.hash = walk ? "#/admin/orders" : "#/orders"; route(); receipt(o);
}
function receipt(o) {
  if (user && user.isStaff && !o.cashier) { o.cashier = user.name; db.save(); }   // the staff who issues the receipt is the cashier
  const x = o.status === "Cancelled", staff = user && user.isStaff, site = location.host || STORE.name + " website";
  const row = (l, v, c = "") => `<div class="r ${c}"><span>${l}</span><span>${v}</span></div>`;
  modal(`<div class="rc"><div class="rh"><div class="rlogo">🍔</div><h2>${STORE.name}</h2><p>${STORE.branch}<br>${STORE.address}<br>${site}</p></div><div class="tdash"></div>
  ${x ? '<div class="stamp">CANCELLED</div>' : ""}${row("Receipt no.", "#" + o.id)}${row("Date and time", when(o.date))}${row("Cashier", esc(o.cashier || "Not yet assigned"))}${row("Customer", esc(o.customer))}${row("Order type", o.type)}<div class="tdash"></div>
  ${o.lines.map(l => row(l.qty + " x " + esc(l.name), peso(l.subtotal))).join("")}<div class="tdash"></div>
  ${row("Subtotal", peso(o.gross))}${o.discount > 0.005 ? row("Bulk discount", "-" + peso(o.discount)) : ""}${x ? row("Order cancelled", "-" + peso(o.billed)) : ""}${row("Total", peso(o.total), "t")}
  ${x ? row("Payment", "None, no charge") : row("Paid with", o.method) + (o.method === "Cash" ? row("Cash received", peso(o.billed + o.change)) + row("Change", peso(o.change)) : "")}${row("Status", badge(o.status))}
  <div class="tdash"></div><p class="thx">Thank you for ordering!<br>${STORE.name}<br>${site}</p></div>
  <div class="${staff ? "two2" : ""} noprint"><button class="btn alt full" onclick="closeModal()">Close</button>${staff ? '<button class="btn full" onclick="printReceipt()">Print receipt</button>' : ""}</div>${staff ? "" : '<p class="hint noprint">Printed copies are handed out by the cashier.</p>'}`);
  $("#modal").classList.toggle("nop", !staff);   // customers cannot print, even with Ctrl+P
}
function printReceipt() {
  if (!user || !user.isStaff) return toast("Only the cashier or admin can print receipts.", 1);
  window.print();
}
function orders() {
  const l = db.orders.filter(o => o.username === user.username);
  return `<section class="wrap sec"><h2>My orders</h2>${l.length ? l.map(o => `<div class="card oc ${o.status === "Cancelled" ? "cx" : ""}"><div><b>Order #${o.id}</b><small>${when(o.date)}</small></div><div class="oi">${o.lines.map(x => `${x.emoji} ${esc(x.name)} x${x.qty}`).join(", ")}</div><div class="or"><b>${peso(o.total)}</b>${badge(o.status)}${o.status === "Cancelled" ? "<small>No charge</small>" : ""}<button class="btn alt sm" onclick="receipt(db.orders.find(x=>x.id===${o.id}))">Receipt</button></div></div>`).join("") : '<p class="mut">You have no orders yet. <a href="#/menu"><u>Order something tasty</u></a>.</p>'}</section>`;
}
function login() {
  const L = authTab === "login", f = (id, ph, t = "text") => `<input id="${id}" type="${t}" class="in" placeholder="${ph}" aria-label="${ph}">`;
  return `<section class="auth"><div class="card ac"><div class="tabs"><button class="pill ${L ? "on" : ""}" onclick="authTab='login';route()">Login</button><button class="pill ${!L ? "on" : ""}" onclick="authTab='reg';route()">Register</button></div>
  ${L ? `<h2>Welcome back</h2><br>${f("u", "Username")}${f("p", "Password", "password")}<br><button class="btn lg full" onclick="doLogin()">Login</button><p class="hint">Demo accounts: admin / admin123, cashier / cashier123, juan / juan123</p>` : `<h2>Create your account</h2><br>${f("n", "Full name")}${f("u", "Username")}${f("p", "Password (4+ characters)", "password")}<br><button class="btn lg full" onclick="doReg()">Create account</button>`}</div></section>`;
}
function doLogin() {
  const u = db.findUser($("#u").value.trim());
  if (!u || !u.checkPassword($("#p").value)) return toast("Wrong username or password. Check both and try again.", 1);
  user = u; db.setSession(u.username); toast("Welcome, " + u.name); location.hash = u.isStaff ? "#/admin/dashboard" : "#/menu";
}
function doReg() {
  const n = $("#n").value.trim(), u = $("#u").value.trim(), p = $("#p").value;
  if (!n || !u || p.length < 4) return toast("Fill in every field. Password needs 4 or more characters.", 1);
  if (db.findUser(u)) return toast("That username is taken. Pick another one.", 1);
  db.users.push(new Customer(u, p, n)); db.save(); $("#u").value = u; $("#p").value = p; authTab = "login"; user = db.findUser(u); db.setSession(u);
  toast("Account created"); location.hash = "#/menu";
}
function logout() { user = null; db.setSession(null); location.hash = "#/"; route(); }

/* ---------- staff views ---------- */
function ordersTable(l, ed) {
  return `<div class="card tw"><table class="tbl"><tr><th>#</th><th>Customer</th><th>Items</th><th>Total</th><th>Paid with</th><th>Date</th><th>Status</th>${ed ? "<th></th>" : ""}</tr>${l.length ? l.map(o => `<tr><td>${o.id}</td><td>${esc(o.customer)}</td><td>${o.lines.map(x => esc(x.name) + " x" + x.qty).join(", ")}</td><td>${peso(o.total)}</td><td>${o.method}</td><td>${when(o.date)}</td><td>${ed ? `<select class="in sm" aria-label="Status" onchange="setStatus(${o.id},this.value)">${Order.STATUSES.map(s => `<option ${s === o.status ? "selected" : ""}>${s}</option>`).join("")}</select>` : badge(o.status)}</td>${ed ? `<td><button class="btn alt sm" onclick="receipt(db.orders.find(x=>x.id===${o.id}))">Receipt</button></td>` : ""}</tr>`).join("") : '<tr><td colspan="8" class="mut">No orders yet. They will appear here once customers check out.</td></tr>'}</table></div>`;
}
const adminViews = {
  dashboard() {
    const ok = db.orders.filter(o => o.status !== "Cancelled"), by = {}, best = {};
    ok.forEach(o => { by[o.method] = (by[o.method] || 0) + o.total; o.lines.forEach(l => best[l.name] = (best[l.name] || 0) + l.qty); });
    const bars = (obj, fmt) => { const e = Object.entries(obj).sort((a, b) => b[1] - a[1]).slice(0, 5), m = Math.max(...e.map(x => x[1]), 1); return e.length ? e.map(([k, v]) => `<div class="bar"><span>${esc(k)}</span><div><i style="width:${v / m * 100}%"></i></div><b>${fmt(v)}</b></div>`).join("") : '<p class="mut">No sales yet.</p>'; };
    return `<h2>Dashboard</h2><div class="stats4">${[["Total sales", peso(ok.reduce((s, o) => s + o.total, 0))], ["Orders", db.orders.length], ["Pending", db.orders.filter(o => o.status === "Pending").length], ["Products", db.products.length]].map(s => `<div class="card st"><span>${s[0]}</span><b>${s[1]}</b></div>`).join("")}</div>
    <div class="two"><div class="card pad"><h3>Best sellers</h3>${bars(best, v => v + " sold")}</div><div class="card pad"><h3>Sales by payment</h3>${bars(by, peso)}</div></div><h3 class="mt">Recent orders</h3><br>${ordersTable(db.orders.slice(0, 5), false)}`;
  },
  products() {
    return `<div class="sh"><h2>Products</h2><button class="btn sm" onclick="prodForm()">Add product</button></div><div class="card tw"><table class="tbl"><tr><th></th><th>Name</th><th>Category</th><th>Price</th><th>Stock</th><th></th></tr>${db.products.map(p => `<tr><td class="em">${p.emoji}</td><td>${esc(p.name)}</td><td>${p.category}</td><td>${peso(p.price)}</td><td>${p.stock}</td><td class="act"><button class="btn alt sm" onclick="prodForm(${p.id})">Edit</button> <button class="btn alt sm" onclick="delProd(${p.id})">Delete</button></td></tr>`).join("")}</table></div>`;
  },
  orders() { return "<h2>Orders</h2><br>" + ordersTable(db.orders, true); },
  customers() {
    const l = db.users.filter(u => !u.isStaff);
    return `<h2>Customers</h2><p class="mut">${l.length} registered ${l.length === 1 ? "account" : "accounts"} saved in this browser.</p><br><div class="card tw"><table class="tbl"><tr><th>Name</th><th>Username</th><th>Orders</th><th>Total spent</th></tr>${l.map(u => { const o = db.orders.filter(x => x.username === u.username && x.status !== "Cancelled"); return `<tr><td>${esc(u.name)}</td><td>${esc(u.username)}</td><td>${o.length}</td><td>${peso(o.reduce((s, x) => s + x.total, 0))}</td></tr>`; }).join("")}</table></div>`;
  },
  payments() {
    return `<h2>Payment records</h2><div class="sh mt"><input class="in sm" id="ps" aria-label="Search" placeholder="Search order or customer" oninput="drawPay()"><select class="in sm" id="pf" aria-label="Method" onchange="drawPay()"><option>All</option><option>Cash</option><option>GCash</option><option>Card</option></select></div><div class="card tw" id="pt"></div>`;
  }
};
function admin(tab) {
  const tabs = user.adminTabs; if (!tabs.includes(tab)) tab = tabs[0];
  return `<section class="adm sec"><aside class="side card">${tabs.map(t => `<a class="${t === tab ? "on" : ""}" href="#/admin/${t}">${t[0].toUpperCase() + t.slice(1)}</a>`).join("")}</aside><div>${adminViews[tab]()}</div></section>`;
}
function drawPay() {
  const q = $("#ps").value.toLowerCase(), m = $("#pf").value;
  const l = db.orders.filter(o => (m === "All" || o.method === m) && (String(o.id) + o.customer).toLowerCase().includes(q));
  $("#pt").innerHTML = `<table class="tbl"><tr><th>Order #</th><th>Customer</th><th>Method</th><th>Amount</th><th>Date</th><th>Status</th></tr>${l.length ? l.map(o => `<tr><td>${o.id}</td><td>${esc(o.customer)}</td><td>${o.method}</td><td>${peso(o.total)}</td><td>${when(o.date)}</td><td>${badge(o.status)}</td></tr>`).join("") : '<tr><td colspan="6" class="mut">No payments match.</td></tr>'}</table>`;
}
function setStatus(id, s) {
  const o = db.orders.find(x => x.id === id);
  if (o.status === "Cancelled") { toast("Cancelled orders cannot be changed.", 1); return route(); }
  if (s === "Cancelled") o.lines.forEach(l => { const p = db.product(l.pid); if (p) p.stock += l.qty; });
  if (!o.cashier) o.cashier = user.name;
  o.status = s; db.save(); route(); toast("Order #" + id + " is now " + s);
}
function prodForm(id) {
  const p = id ? db.product(id) : null;
  modal(`<h2>${p ? "Edit" : "Add"} product</h2><input id="fn" class="in" placeholder="Name" aria-label="Name" value="${p ? esc(p.name) : ""}"><select id="fc" class="in" aria-label="Category" ${p ? "disabled" : ""}>${["Meal", "Drink", "Dessert"].map(c => `<option ${p && p.category === c ? "selected" : ""}>${c}</option>`).join("")}</select>
  <div class="two2"><input id="fp" class="in" type="number" step="0.01" placeholder="Price" aria-label="Price" value="${p ? p.price : ""}"><input id="fs" class="in" type="number" placeholder="Stock" aria-label="Stock" value="${p ? p.stock : ""}"></div>
  <input id="fe" class="in" placeholder="Emoji, e.g. 🍔" aria-label="Emoji" value="${p ? p.emoji : "🍽️"}"><input id="fd" class="in" placeholder="Short description" aria-label="Description" value="${p ? esc(p.desc) : ""}"><br><div class="two2"><button class="btn alt" onclick="closeModal()">Cancel</button><button class="btn" onclick="saveProd(${id || 0})">Save product</button></div>`);
}
function saveProd(id) {
  const v = i => $("#" + i).value.trim();
  try {
    if (!v("fn")) throw new Error("Enter a product name.");
    if (id) { const p = db.product(id); p.name = v("fn"); p.price = v("fp"); p.stock = v("fs"); p.emoji = v("fe") || "🍽️"; p.desc = v("fd"); }
    else db.products.push(new ({ Meal, Drink, Dessert }[$("#fc").value])(db.nextProductId(), v("fn"), v("fp"), v("fs"), v("fe") || "🍽️", v("fd")));
    db.save(); closeModal(); route(); toast("Product saved");
  } catch (e) { toast(e.message, 1); }
}
function delProd(id) { if (confirm("Delete this product?")) { db.products = db.products.filter(p => p.id !== id); db.save(); route(); toast("Product deleted"); } }

/* ---------- router ---------- */
function route() {
  const [, page = "home", arg] = location.hash.replace("#", "").split("/");
  let html;
  if (page === "admin") { if (!user || !user.isStaff) { location.hash = "#/login"; return; } html = admin(arg); }
  else if (page === "orders" && !user) { location.hash = "#/login"; return; }
  else html = ({ home, menu, cart, orders, login }[page] || home)(arg);
  $("#app").innerHTML = html; renderNav(); window.scrollTo(0, 0);
  if ($("#grid")) grid(); if ($("#pt")) drawPay();
}
window.addEventListener("hashchange", route); route();
