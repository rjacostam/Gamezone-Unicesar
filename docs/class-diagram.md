# Class Diagram — GameZone Unicesar

Classes of the four layers with their attributes, main methods and relationships.

- Visibility: `+` public, `-` private, `#` protected.
- `<<abstract>>` marks abstract classes; `*` after a method marks an abstract method.
- `$` marks a static method.

---

## Model — `com.gamezone.model`

```mermaid
classDiagram
    direction TB

    class Person {
        <<abstract>>
        -String id
        -String name
        -String phone
        #Person(String id, String name, String phone)
        +getRole() String*
        +getSummary() String*
        +getId() String
        +getName() String
        +setName(String name) void
        +getPhone() String
        +setPhone(String phone) void
        +toString() String
    }

    class Customer {
        -String email
        +Customer(String id, String name, String phone, String email)
        +getRole() String
        +getSummary() String
        +getEmail() String
        +setEmail(String email) void
    }

    class Seller {
        -String employeeCode
        -String shift
        +Seller(String id, String name, String phone, String employeeCode, String shift)
        +getRole() String
        +getSummary() String
        +getEmployeeCode() String
        +setEmployeeCode(String employeeCode) void
        +getShift() String
        +setShift(String shift) void
    }

    class Product {
        <<abstract>>
        -String id
        -String title
        -double price
        -int stock
        #Product(String id, String title, double price, int stock)
        +getDescription() String*
        +hasStock(int quantity) boolean
        +decreaseStock(int quantity) void
        +getId() String
        +getTitle() String
        +setTitle(String title) void
        +getPrice() double
        +setPrice(double price) void
        +getStock() int
        +setStock(int stock) void
        +toString() String
    }

    class VideoGame {
        -String platform
        -String genre
        -String ageRating
        +VideoGame(String id, String title, double price, int stock, String platform, String genre, String ageRating)
        +getDescription() String
        +getPlatform() String
        +setPlatform(String platform) void
        +getGenre() String
        +setGenre(String genre) void
        +getAgeRating() String
        +setAgeRating(String ageRating) void
    }

    class Console {
        -String brand
        -String model
        -String generation
        +Console(String id, String title, double price, int stock, String brand, String model, String generation)
        +getDescription() String
        +getBrand() String
        +setBrand(String brand) void
        +getModel() String
        +setModel(String model) void
        +getGeneration() String
        +setGeneration(String generation) void
    }

    class Sale {
        -String id
        -LocalDate date
        -Customer customer
        -Seller seller
        -List~Product~ products
        -double total
        +Sale(String id, LocalDate date, Customer customer, Seller seller, List~Product~ products)
        +calculateTotal() double
        +generateReceipt() String
        +getId() String
        +getDate() LocalDate
        +getCustomer() Customer
        +getSeller() Seller
        +getProducts() List~Product~
        +getTotal() double
    }

    Person <|-- Customer
    Person <|-- Seller
    Product <|-- VideoGame
    Product <|-- Console

    Sale "0..*" --> "1" Customer : customer
    Sale "0..*" --> "1" Seller : seller
    Sale "0..*" o-- "1..*" Product : products
```

- Every attribute of the model is `private`; access goes through getters and setters.
- `Person` and `Product` are abstract, and their constructors are `protected`.
- `getRole()`, `getSummary()` and `getDescription()` are abstract in the base class and
  implemented with `@Override` in each subclass.
- `Sale` keeps one entry per unit sold in `products` and computes its own `total`.

---

## Persistence — `com.gamezone.persistence`

```mermaid
classDiagram
    direction TB

    class PersonRepository {
        -Path customersFile
        -Path sellersFile
        +PersonRepository(String customersFile, String sellersFile)
        +saveCustomers(List~Customer~ customers) void
        +loadCustomers() List~Customer~
        +saveSellers(List~Seller~ sellers) void
        +loadSellers() List~Seller~
    }

    class ProductRepository {
        -Path filePath
        +ProductRepository(String filePath)
        +saveAll(List~Product~ products) void
        +loadAll() List~Product~
    }

    class SaleRepository {
        -Path filePath
        +SaleRepository(String filePath)
        +saveAll(List~Sale~ sales) void
        +loadAll(List~Product~ products, List~Customer~ customers, List~Seller~ sellers) List~Sale~
    }

    PersonRepository ..> Customer : saves and loads
    PersonRepository ..> Seller : saves and loads
    ProductRepository ..> VideoGame : saves and loads
    ProductRepository ..> Console : saves and loads
    SaleRepository ..> Sale : saves and loads
    SaleRepository ..> Customer : links by id
    SaleRepository ..> Seller : links by id
    SaleRepository ..> Product : links by id
```

- Data files: `data/customers.csv`, `data/sellers.csv` (three preloaded sellers),
  `data/products.csv` and `data/sales.csv`.
- Every load method returns an empty list when its file does not exist yet.
- `ProductRepository` writes the type (`VIDEOGAME` or `CONSOLE`) as the first column, so
  it knows which subclass to rebuild.
- A sale is stored with the ids of its customer, seller and products. `loadAll` receives
  the objects already loaded and links each sale to them.

---

## Services — `com.gamezone.service`

```mermaid
classDiagram
    direction TB

    class PersonService {
        -PersonRepository repository
        -List~Customer~ customers
        -List~Seller~ sellers
        +PersonService(PersonRepository repository)
        +registerCustomer(String id, String name, String phone, String email) Customer
        +listCustomers() List~Customer~
        +listSellers() List~Seller~
        +findCustomerById(String id) Customer
        +findSellerById(String id) Seller
    }

    class ProductService {
        -ProductRepository repository
        -List~Product~ products
        +ProductService(ProductRepository repository)
        +registerVideoGame(String id, String title, double price, int stock, String platform, String genre, String ageRating) VideoGame
        +registerConsole(String id, String title, double price, int stock, String brand, String model, String generation) Console
        +listAllProducts() List~Product~
        +findById(String id) Product
        +updateStock(String productId, int quantity) void
    }

    class SaleService {
        -SaleRepository repository
        -ProductService productService
        -PersonService personService
        -List~Sale~ sales
        +SaleService(SaleRepository repository, ProductService productService, PersonService personService)
        +registerSale(String customerId, String sellerId, List~String~ productIds) Sale
        +listAllSales() List~Sale~
        +findSalesByCustomer(String customerId) List~Sale~
        +findSalesBySeller(String sellerId) List~Sale~
    }

    PersonService --> PersonRepository : uses
    ProductService --> ProductRepository : uses
    SaleService --> SaleRepository : uses
    SaleService --> ProductService : updates stock through
    SaleService --> PersonService : finds customer and seller through
    SaleService ..> Sale : creates
```

- Each service loads its data in the constructor and saves after every change.
- Invalid data or a broken rule throws an `IllegalArgumentException` with a message in
  Spanish, which the menu shows to the user.
- `findSellerById` accepts the seller's identification or employee code.

---

## User interface — `com.gamezone.ui`

```mermaid
classDiagram
    direction TB

    class ConsoleMenu {
        -ProductService productService
        -PersonService personService
        -SaleService saleService
        -Scanner scanner
        +ConsoleMenu(ProductService productService, PersonService personService, SaleService saleService)
        +start() void
        -productMenu() void
        -personMenu() void
        -saleMenu() void
        -registerSale() void
        -readText(String prompt) String
        -readInt(String prompt) int
        -readDouble(String prompt) double
    }

    ConsoleMenu --> ProductService : calls
    ConsoleMenu --> PersonService : calls
    ConsoleMenu --> SaleService : calls
```

- The menu only talks to the services; it never uses a repository.
- Messages shown to the user are in Spanish; code identifiers are in English.

---

## Application entry point — `com.gamezone`

```mermaid
classDiagram
    direction TB

    class Main {
        +main(String[] args) void$
    }

    Main ..> ProductRepository : creates
    Main ..> PersonRepository : creates
    Main ..> SaleRepository : creates
    Main ..> ProductService : creates
    Main ..> PersonService : creates
    Main ..> SaleService : creates
    Main ..> ConsoleMenu : creates
```

`Main` creates the repositories, passes them to the services through their constructors,
passes the services to `ConsoleMenu` and starts the menu.
