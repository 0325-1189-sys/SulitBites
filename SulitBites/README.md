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
