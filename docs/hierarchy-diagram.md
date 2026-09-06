# Hierarchy diagram (Mermaid) — TODO

```mermaid
classDiagram
    class Person {
        <<abstract>>
    }
    Person <|-- Customer
    Person <|-- Seller
    class Product {
        <<abstract>>
    }
    Product <|-- VideoGame
    Product <|-- Console
```
