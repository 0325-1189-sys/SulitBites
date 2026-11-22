# SulitBites Java Web App

SulitBites is a Java-only server-rendered web application. The backend, OOP domain model, routing, forms, and HTML rendering are written in Java. It uses HTML forms and CSS for the browser interface and does not use JavaScript.

## Run locally

Requirements: JDK 17 or newer.

```bash
rm -rf out
javac -d out $(find src -name "*.java")
PORT=3000 java -cp out com.sulitbites.Main
```

Then open `http://localhost:3000`.

## Demo accounts

- Admin: `admin` / `admin123`
- Cashier: `cashier` / `cashier123`
- Customer: `juan` / `juan123`

## OOP structure

`User`, `FoodProduct`, and `Payment` are abstract classes. Admin, Cashier, Customer, Meal, Drink, Dessert, and payment classes inherit from them. Product totals and payment validation use polymorphism, while private fields and validated methods provide encapsulation.
