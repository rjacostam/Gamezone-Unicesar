# SOLID Class Diagram — Before and After

Classes involved in the thirteen violations of `docs/solid-audit/consolidated-audit.md`. Only
the members related to each violation are shown. Notes give the violation identifiers of each
class.

## Before (start of Phase 2, `develop` at `94cfe02`)

```mermaid
classDiagram
    direction LR

    class Product {
        <<abstract>>
        +getCategory() String
        +isConsole() boolean
        +getExtraFields() List~String~
        +getDescription() String
    }
    class Console {
        +getCategory() String
        +isConsole() boolean
    }
    class Accessory {
        <<abstract>>
        +getDescription() String
    }
    class Promotion {
        <<abstract>>
        +getExtraFields() List~String~
        +getDescription() String
    }
    class InventoryCatalog {
        <<interface>>
        +findById(String id) Product
        +updateStock(String id, int quantity) void
        +restoreStock(String id, int quantity) void
    }
    class ProductService {
        +findById(String id) Product
    }
    class AccessoryService {
        -ProductService productService
        +findById(String id) Accessory
        +registerController(...) Controller
        +registerCable(...) Cable
        +registerMemory(...) Memory
    }
    class PromotionService {
        +registerPercentageDiscount(...) PercentageDiscount
        +registerCategoryDiscount(...) CategoryDiscount
        +registerBulkPurchaseDiscount(...) BulkPurchaseDiscount
    }
    class SaleService {
        -SaleRepository repository
        -ProductService productService
        -PersonService personService
        -AccessoryService accessoryService
        -PromotionService promotionService
        -WarrantyService warrantyService
        +getCatalog() List~Product~
        -findItem(String id) Product
    }
    class ReturnService {
        -ReturnRepository repository
        -SaleService saleService
        -WarrantyService warrantyService
        +calculateMonthlySales(int m, int y) double
        +calculateMonthlyReturns(int m, int y) double
        +generateMonthlyBalance(int m, int y) double
    }
    class ProductRepository
    class AccessoryRepository {
        -deserialize(String line) Accessory
    }
    class PromotionRepository {
        -deserialize(String line) Promotion
    }
    class Submenu {
        <<abstract>>
        #runSafely(Runnable action) void
    }
    class SaleMenu {
        -ProductService productService
        -PersonMenu personMenu
    }
    class ConsoleMenu {
        +ConsoleMenu(7 services)
    }
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
    AccessoryService --> ProductService
    ProductRepository ..> Product : getExtraFields
    AccessoryRepository ..> Accessory : getExtraFields
    PromotionRepository ..> Promotion : getExtraFields
    Submenu <|-- SaleMenu
    ConsoleMenu ..> Submenu : creates with new

    note for SaleService "V-01 catalogue; V-09 uses 2 of 3 methods; V-11 concrete fields"
    note for ReceiptGenerator "V-02 money format without locale"
    note for ReturnReceiptFormatter "V-02 money format"
    note for WarrantyCertificateFormatter "V-02 money format"
    note for Accessory "V-02 money format; V-08 forced to answer getExtraFields"
    note for Promotion "V-02 percentage format; V-08 file columns in the model"
    note for ReturnService "V-03 monthly report; V-09 uses 2 of 3 methods; V-11 concrete fields"
    note for Submenu "V-04 catches the four exceptions one by one"
    note for Console "V-05 two literal answers for console"
    note for Product "V-05 isConsole vs getCategory; V-08 getExtraFields"
    note for AccessoryService "V-06 one register per type; V-12 concrete ProductService"
    note for PromotionService "V-06 one register per type"
    note for AccessoryRepository "V-06 branch per type; V-08"
    note for PromotionRepository "V-06 branch per type; V-08"
    note for ProductService "V-07 findById does not trim"
    note for InventoryCatalog "V-09 too wide for its clients"
    note for SaleMenu "V-10 whole ProductService and PersonMenu"
    note for ConsoleMenu "V-13 creates the submenus"
```

## After (end of Phase 2)

To be drawn when every correction is merged.
