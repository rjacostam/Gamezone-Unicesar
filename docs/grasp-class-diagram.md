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

## After (end of Phase 2)

To be drawn when every correction is merged.
