# Accessory Module Class Diagram — GameZone Unicesar

New classes of Requirement 1 and how they connect with the existing system. Classes that
already existed are shown only with the members relevant to this module.

- Visibility: `+` public, `-` private, `#` protected.
- `<<abstract>>` marks abstract classes; `*` after a method marks an abstract method.

---

## Model — `com.gamezone.model`

```mermaid
classDiagram
    direction TB

    class Product {
        <<abstract>>
        -String id
        -String title
        -double price
        -int stock
        +getDescription() String*
        +hasStock(int quantity) boolean
        +decreaseStock(int quantity) void
    }

    class Console {
        -String brand
        -String model
        -String generation
        +getDescription() String
    }

    class Accessory {
        <<abstract>>
        -List~String~ compatibleConsoleIds
        #Accessory(String id, String title, double price, int stock, List~String~ compatibleConsoleIds)
        +getAccessoryType() String*
        +getDescription() String
        +isCompatibleWith(String consoleId) boolean
        +addCompatibleConsole(String consoleId) void
        +removeCompatibleConsole(String consoleId) void
        +getCompatibleConsoleIds() List~String~
    }

    class Controller {
        -String connectionType
        +Controller(String id, String title, double price, int stock, List~String~ compatibleConsoleIds, String connectionType)
        +getAccessoryType() String
        +getDescription() String
        +getConnectionType() String
        +setConnectionType(String connectionType) void
    }

    class Cable {
        -double lengthInMeters
        -String connectorType
        +Cable(String id, String title, double price, int stock, List~String~ compatibleConsoleIds, double lengthInMeters, String connectorType)
        +getAccessoryType() String
        +getDescription() String
        +getLengthInMeters() double
        +setLengthInMeters(double lengthInMeters) void
        +getConnectorType() String
        +setConnectorType(String connectorType) void
    }

    class Memory {
        -int capacityInGb
        -String memoryType
        +Memory(String id, String title, double price, int stock, List~String~ compatibleConsoleIds, int capacityInGb, String memoryType)
        +getAccessoryType() String
        +getDescription() String
        +getCapacityInGb() int
        +setCapacityInGb(int capacityInGb) void
        +getMemoryType() String
        +setMemoryType(String memoryType) void
    }

    class Sale {
        -List~Product~ products
        +calculateTotal() double
    }

    Product <|-- Console
    Product <|-- Accessory
    Accessory <|-- Controller
    Accessory <|-- Cable
    Accessory <|-- Memory

    Accessory "0..*" ..> "0..*" Console : compatible with (by id)
    Sale "0..*" o-- "1..*" Product : products
```

- `Accessory` extends `Product`, so it inherits `id`, `title`, `price`, `stock`, `hasStock`
  and `decreaseStock`.
- `getAccessoryType()` is abstract in `Accessory`. Each subclass returns `"CONTROLLER"`,
  `"CABLE"` or `"MEMORY"`.
- `Accessory.getDescription()` describes the common data and the compatible consoles. Each
  subclass overrides it, calls `super.getDescription()` and adds its own attributes.
- Compatibility is stored as console ids in `compatibleConsoleIds`.
- `Sale` does not change. Its `products` list can contain accessories because they are
  products.

---

## Persistence — `com.gamezone.persistence`

```mermaid
classDiagram
    direction TB

    class AccessoryRepository {
        -Path filePath
        +AccessoryRepository(String filePath)
        +saveAll(List~Accessory~ accessories) void
        +loadAll() List~Accessory~
    }

    AccessoryRepository ..> Controller : saves and loads
    AccessoryRepository ..> Cable : saves and loads
    AccessoryRepository ..> Memory : saves and loads
```

- File: `data/accessories.csv`, preloaded with one controller, one cable and one memory.
- Format: `type;id;title;price;stock;compatibleConsoles;extra1;extra2`, with a header line.
  The type (`CONTROLLER`, `CABLE`, `MEMORY`) tells which subclass to rebuild. The console
  ids go separated by commas.
- `loadAll` returns an empty list when the file does not exist. Like the other
  repositories, both methods declare `throws IOException`.

---

## Services — `com.gamezone.service`

```mermaid
classDiagram
    direction TB

    class AccessoryService {
        -AccessoryRepository repository
        -ProductService productService
        -List~Accessory~ accessories
        +AccessoryService(AccessoryRepository repository, ProductService productService)
        +registerController(String id, String title, double price, int stock, List~String~ compatibleConsoleIds, String connectionType) Controller
        +registerCable(String id, String title, double price, int stock, List~String~ compatibleConsoleIds, double lengthInMeters, String connectorType) Cable
        +registerMemory(String id, String title, double price, int stock, List~String~ compatibleConsoleIds, int capacityInGb, String memoryType) Memory
        +listAllAccessories() List~Accessory~
        +listAccessoriesByType(String type) List~Accessory~
        +findAccessoriesCompatibleWith(String consoleId) List~Accessory~
        +findById(String id) Accessory
        +updateStock(String accessoryId, int quantity) void
    }

    class ProductService {
        +listAllProducts() List~Product~
        +findById(String id) Product
        +updateStock(String productId, int quantity) void
    }

    class SaleService {
        -SaleRepository repository
        -ProductService productService
        -PersonService personService
        -AccessoryService accessoryService
        -List~Sale~ sales
        +SaleService(SaleRepository repository, ProductService productService, PersonService personService, AccessoryService accessoryService)
        +registerSale(String customerId, String sellerId, List~String~ productIds) Sale
        +getCatalog() List~Product~
        +listAllSales() List~Sale~
        +findSalesByCustomer(String customerId) List~Sale~
        +findSalesBySeller(String sellerId) List~Sale~
    }

    AccessoryService --> AccessoryRepository : uses
    AccessoryService --> ProductService : checks consoles and ids
    SaleService --> AccessoryService : finds accessories and updates their stock
    SaleService --> ProductService : finds products and updates their stock
```

**`AccessoryService`**
- Loads the accessories in its constructor and saves after every change. File errors
  become `IllegalStateException`, like in `ProductService`.
- It rejects invalid data with an `IllegalArgumentException` and a message in Spanish:
  - empty id or title, a price that is not greater than zero, or a negative stock;
  - an id already used by an accessory or a product;
  - a compatible id that is not a console of the inventory;
  - a type other than `CONTROLLER`, `CABLE` or `MEMORY`;
  - not enough stock.
- `findById` ignores letter case, like `ProductService`.

**`SaleService`**
- New dependency: `AccessoryService`.
- `getCatalog()` returns products and accessories together.
- `registerSale` resolves each id as a product or an accessory and updates the stock
  through the service that owns the item. Its signature does not change.

---

## User interface and entry point

```mermaid
classDiagram
    direction TB

    class ConsoleMenu {
        -AccessoryService accessoryService
        +ConsoleMenu(ProductService productService, PersonService personService, SaleService saleService, AccessoryService accessoryService)
        +start() void
        -accessoryMenu() void
        -registerController() void
        -registerCable() void
        -registerMemory() void
        -listAccessories(List~Accessory~ accessories) void
    }

    class Main {
        +main(String[] args) void$
    }

    ConsoleMenu --> AccessoryService : calls
    Main ..> AccessoryRepository : creates
    Main ..> AccessoryService : creates
```

- New main menu option **4. Gestión de accesorios**, with:
  1. Registrar control
  2. Registrar cable
  3. Registrar memoria
  4. Listar todos los accesorios
  5. Listar accesorios por tipo
  6. Consultar accesorios compatibles con una consola
- When registering a sale, the menu shows products and accessories (`getCatalog()`), so
  both can be added to the same sale.
- `Main` creates `AccessoryRepository` with `data/accessories.csv` and `AccessoryService`
  after `ProductService`, and passes it to `SaleService` and `ConsoleMenu`.

## Who implements what

| Member | Classes |
|---|---|
| Developer 1 (Andrés) | `Accessory`, `Controller`, `Cable`, `Memory` |
| Developer 2 (José) | `AccessoryRepository`, `AccessoryService`, `data/accessories.csv` |
| Technical Leader (Rafael) | Changes in `SaleService`, `ConsoleMenu`, `Main` and `README.md` |
