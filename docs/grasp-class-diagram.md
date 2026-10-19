# GRASP Class Diagram — Before and After

Classes involved in the nine violations of `docs/grasp-audit/consolidated-audit.md`. Only the
members related to each violation are shown. Notes give the violation identifiers of each
class.

## Before (start of Phase 2, `develop` at `ae12127`)

```mermaid
classDiagram
    direction LR

    class Product {
        <<abstract>>
        -int stock
        +decreaseStock(int quantity) void
        +setStock(int stock) void
        +getCategory() String
        +getDescription() String
    }
    class Accessory {
        <<abstract>>
        +getDescription() String
    }
    class Sale {
        -List~Product~ products
        +getProducts() List~Product~
        +getSubtotal() double
        +generateReceipt() String
    }
    class Return {
        +generateReturnReceipt() String
    }
    class Warranty {
        <<abstract>>
        +generateWarrantyCertificate() String
        -coverageDescription() String
    }
    class PercentageDiscount {
        +calculateDiscount(Sale sale) double
    }
    class BulkPurchaseDiscount {
        +calculateDiscount(Sale sale) double
    }
    class ProductService {
        +updateStock(String id, int quantity) void
        +restoreStock(String id, int quantity) void
    }
    class AccessoryService {
        +updateStock(String id, int quantity) void
        +restoreStock(String id, int quantity) void
    }
    class SaleService {
        -assignWarranties(Sale sale, List~String~ ids) void
        -updateInventory(Map units) void
    }
    class ReturnService {
        -ProductService productService
        -AccessoryService accessoryService
        -countUnits(Sale sale, String id) int
        -findProduct(Sale sale, String id) Product
        -cancelWarrantiesOfConsoles(...) void
        -restoreStock(...) void
    }
    class ReturnRepository {
        -findProduct(Sale sale, String id) Product
    }
    class ConsoleMenu {
        -productMenu() void
        -personMenu() void
        -saleMenu() void
        -accessoryMenu() void
        -promotionMenu() void
        -warrantyMenu() void
        -returnMenu() void
        -registerSale() void
        -readText(String prompt) String
        -readInt(String prompt) int
    }
    class SaleValidator {
        +validateExtendedWarrantyRequest(...)$ void
    }
    class AccessoryValidator {
        +validateCompatibleConsole(Product p, String id)$ void
    }
    class ProductRepository {
        +saveAll(List~Product~ products) void
    }
    class AccessoryRepository {
        -serialize(Accessory a) String
    }
    class PromotionRepository {
        -serialize(Promotion p) String
        -getTypeOf(Promotion p) String
    }
    class WarrantyRepository {
        -getTypeOf(Warranty w) String
    }

    Product <|-- Accessory
    SaleService --> ProductService
    SaleService --> AccessoryService
    ReturnService --> ProductService
    ReturnService --> AccessoryService
    ReturnService ..> Sale : walks getProducts()
    ReturnRepository ..> Sale : walks getProducts()
    ConsoleMenu --> SaleService

    note for ProductService "V-01: restoreStock uses setStock(getStock() + quantity)"
    note for PercentageDiscount "V-02: rebuilds the subtotal"
    note for BulkPurchaseDiscount "V-02: rebuilds the subtotal"
    note for ReturnService "V-03 countUnits, findProduct; V-04 instanceof Console; V-07 instanceof Accessory"
    note for ReturnRepository "V-03: findProduct"
    note for ConsoleMenu "V-04 instanceof Console; V-05 prints the model texts; V-06 seven submenus"
    note for SaleService "V-04 instanceof Console; V-07 instanceof Accessory"
    note for SaleValidator "V-04: compares category CONSOLE"
    note for AccessoryValidator "V-04: compares category CONSOLE"
    note for Sale "V-05: generateReceipt"
    note for Return "V-05: generateReturnReceipt"
    note for Warranty "V-05 certificate; V-09 coverage compares the type name"
    note for Product "V-01 no increaseStock; V-04 no console answer; V-05 getDescription; V-08 no type marker"
    note for Accessory "V-05 getDescription; V-08 no type marker"
    note for ProductRepository "V-08: instanceof VideoGame / Console"
    note for AccessoryRepository "V-08: instanceof Controller / Cable / Memory"
    note for PromotionRepository "V-08: instanceof chain"
    note for WarrantyRepository "V-08: instanceof BasicWarranty"
```

## After (end of Phase 2, `develop` at `63604f3`)

New classes: `InventoryCatalog`, `ReceiptGenerator`, `ReturnReceiptFormatter`,
`WarrantyCertificateFormatter`, `ConsoleInput`, `Submenu` and the seven submenus. Removed
members: `Sale.generateReceipt`, `Return.generateReturnReceipt`,
`Warranty.generateWarrantyCertificate`, the `instanceof` chains of the repositories and of the
services, and the private helpers that walked the products of a sale from outside.

```mermaid
classDiagram
    direction LR

    class Product {
        <<abstract>>
        -int stock
        +decreaseStock(int quantity) void
        +increaseStock(int quantity) void
        +isConsole() boolean
        +getCategory() String
        +getExtraFields() List~String~
        +getDescription() String
    }
    class Console {
        +isConsole() boolean
    }
    class Accessory {
        <<abstract>>
        +getAccessoryType() String
    }
    class Sale {
        -List~Product~ products
        +getSubtotal() double
        +countUnitsOf(String productId) int
        +findProductById(String productId) Product
    }
    class Promotion {
        <<abstract>>
        +calculateDiscount(Sale sale) double
        +getTypeCode() String
        +getExtraFields() List~String~
    }
    class Warranty {
        <<abstract>>
        +getTypeCode() String
        +coverageDescription() String
    }
    class InventoryCatalog {
        <<interface>>
        +findById(String id) Product
        +updateStock(String id, int quantity) void
        +restoreStock(String id, int quantity) void
    }
    class ProductService
    class AccessoryService
    class SaleService {
        -List~InventoryCatalog~ inventoryCatalogs
        -catalogOf(Product item) InventoryCatalog
    }
    class ReturnService {
        -List~InventoryCatalog~ inventoryCatalogs
        -catalogOf(String productId) InventoryCatalog
    }
    class ProductRepository
    class AccessoryRepository
    class PromotionRepository
    class WarrantyRepository
    class ConsoleMenu {
        +start() void
    }
    class ConsoleInput {
        +readText(String prompt) String
        +readInt(String prompt) int
        +readDouble(String prompt) double
        +readDate(String prompt) LocalDate
        +readYesNo(String prompt) boolean
    }
    class Submenu {
        <<abstract>>
        #ConsoleInput input
        +show() void
        #runSafely(Runnable action) void
    }
    class ProductMenu
    class PersonMenu
    class SaleMenu
    class AccessoryMenu
    class PromotionMenu
    class WarrantyMenu
    class ReturnMenu
    class ReceiptGenerator {
        +generate(Sale sale)$ String
    }
    class ReturnReceiptFormatter {
        +format(Return r)$ String
    }
    class WarrantyCertificateFormatter {
        +format(Warranty w)$ String
    }

    Product <|-- Console
    Product <|-- Accessory
    InventoryCatalog <|.. ProductService
    InventoryCatalog <|.. AccessoryService
    SaleService --> InventoryCatalog
    ReturnService --> InventoryCatalog
    ReturnService ..> Sale : countUnitsOf, findProductById
    ProductRepository ..> Product : getCategory, getExtraFields
    AccessoryRepository ..> Accessory : getAccessoryType, getExtraFields
    PromotionRepository ..> Promotion : getTypeCode, getExtraFields
    WarrantyRepository ..> Warranty : getTypeCode
    Submenu <|-- ProductMenu
    Submenu <|-- PersonMenu
    Submenu <|-- SaleMenu
    Submenu <|-- AccessoryMenu
    Submenu <|-- PromotionMenu
    Submenu <|-- WarrantyMenu
    Submenu <|-- ReturnMenu
    Submenu --> ConsoleInput
    ConsoleMenu --> Submenu : opens one per module
    SaleMenu ..> ReceiptGenerator
    ReturnMenu ..> ReturnReceiptFormatter
    WarrantyMenu ..> WarrantyCertificateFormatter

    note for Product "V-01 increaseStock; V-04 isConsole; V-08 getExtraFields"
    note for Sale "V-02 getSubtotal is the discount base; V-03 countUnitsOf, findProductById; V-05 no receipt"
    note for Promotion "V-02 subclasses read getSubtotal; V-08 getTypeCode, getExtraFields"
    note for Warranty "V-05 no certificate; V-08 getTypeCode; V-09 abstract coverageDescription"
    note for InventoryCatalog "V-07: new interface"
    note for SaleService "V-04 isConsole; V-07 catalogOf"
    note for ReturnService "V-03 asks Sale; V-04 isConsole; V-07 catalogOf"
    note for ProductRepository "V-08: no instanceof"
    note for AccessoryRepository "V-08: no instanceof"
    note for PromotionRepository "V-08: no instanceof"
    note for WarrantyRepository "V-08: no instanceof"
    note for ReceiptGenerator "V-05: new"
    note for ReturnReceiptFormatter "V-05: new"
    note for WarrantyCertificateFormatter "V-05: new"
    note for ConsoleMenu "V-06: main menu only, 86 lines"
    note for Submenu "V-06: new base of the seven submenus"
    note for ConsoleInput "V-06: new"
```

`Product.getDescription` and `Accessory.getDescription` stay in the model on purpose: moving
them would force an `instanceof` per subclass in the interface, which is a new violation of
Polymorphism. The decision is justified in `docs/grasp-refactor-log.md`.
