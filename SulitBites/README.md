# Sulit Bites - Food Ordering Management System (Web)
OOP Final Project, Scenario 4. Plain HTML, CSS and JavaScript (ES6 classes). No installation needed.

## Run locally
Double-click `index.html`.

## Demo accounts
| Role | Username | Password |
|---|---|---|
| Admin | admin | admin123 |
| Cashier | cashier | cashier123 |
| Customer | juan | juan123 |

## Four Pillars (js/models.js)
- **Abstraction**: `User`, `FoodProduct`, `Payment` are abstract.
- **Inheritance**: Admin/Cashier/Customer, Meal/Drink/Dessert, CashPayment/GCashPayment/CardPayment.
- **Polymorphism**: `total()`, `promo`, `validate()`, `adminTabs` differ per subclass.
- **Encapsulation**: `#private` fields with validated setters (price, stock, password).

## Shared database (so every device sees the same accounts and orders)
1. Create a free Firebase project and a Realtime Database (Rules: `{"rules":{".read":true,".write":true}}`).
2. Paste the database URL in `js/config.js`, then deploy again.
3. Open the site on the device that already has your data first. It uploads everything; other devices then download it.
Passwords are stored as SHA-256 hashes. The database rules are open for demo use, so do not use real passwords.
