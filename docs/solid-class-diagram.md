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

New: `StoreCatalog`, `MonthlyBalanceService`, `AmountFormat`, `AccessoryFactory`,
`PromotionFactory`, the three catalogue row contracts and the small contracts `ItemFinder`,
`StockUpdater`, `StockRestorer`, `SaleFinder`, `SaleStore`, `ReturnStore`, `PersonFinder`,
`PromotionChooser`, `WarrantyAssigner`, `WarrantyCanceller` and `SellerLister`. Removed:
`InventoryCatalog`, the per-type `register...` methods of the accessory and promotion services,
`SaleService.getCatalog` and `findItem`, and the monthly balance of `ReturnService`.

```mermaid
classDiagram
    direction LR

    class Product {
        <<abstract>>
        +getCategory() String
        +isConsole() boolean
    }
    class Console {
        +CATEGORY$ String
    }
    class ProductCatalogRow {
        <<interface>>
        +getExtraFields() List~String~
    }
    class AccessoryCatalogRow {
        <<interface>>
        +getExtraFields() List~String~
    }
    class PromotionCatalogRow {
        <<interface>>
        +getExtraFields() List~String~
    }
    class Accessory {
        <<abstract>>
    }
    class Promotion {
        <<abstract>>
    }
    class AccessoryFactory {
        +create(...)$ Accessory
    }
    class PromotionFactory {
        +create(...)$ Promotion
    }
    class AmountFormat {
        +money(double)$ String
        +percent(double)$ String
        +moneyInComputerLocale(double)$ String
    }
    class ItemFinder {
        <<interface>>
        +findById(String id) Product
    }
    class StockUpdater {
        <<interface>>
        +updateStock(String id, int quantity) void
    }
    class StockRestorer {
        <<interface>>
        +restoreStock(String id, int quantity) void
    }
    class ProductService
    class AccessoryService {
        -ItemFinder productService
        +register(Accessory a) Accessory
    }
    class PromotionService {
        +register(Promotion p) Promotion
    }
    class StoreCatalog {
        +listAll() List~Product~
        +findById(String id) Product
    }
    class SaleFinder {
        <<interface>>
        +findById(String id) Sale
        +listAllSales() List~Sale~
    }
    class SaleStore {
        <<interface>>
    }
    class ReturnStore {
        <<interface>>
    }
    class PersonFinder {
        <<interface>>
    }
    class PromotionChooser {
        <<interface>>
    }
    class WarrantyAssigner {
        <<interface>>
    }
    class WarrantyCanceller {
        <<interface>>
    }
    class SaleService {
        -SaleStore repository
        -StoreCatalog catalog
        -List~StockUpdater~ stockUpdaters
        -PersonFinder people
        -PromotionChooser promotions
        -WarrantyAssigner warranties
    }
    class ReturnService {
        -ReturnStore repository
        -SaleFinder saleService
        -List~StockRestorer~ inventoryCatalogs
        -WarrantyCanceller warrantyService
    }
    class MonthlyBalanceService {
        +generateMonthlyBalance(int m, int y) double
    }
    class SaleRepository
    class ReturnRepository
    class Submenu {
        <<abstract>>
        #runSafely(Runnable action) void
    }
    class SaleMenu {
        -ItemFinder productFinder
        -SellerLister sellerLister
    }
    class SellerLister {
        <<interface>>
        +listSellers() void
    }
    class PersonMenu
    class ConsoleMenu {
        +ConsoleMenu(ConsoleInput, 7 Submenu)
    }
    class ReceiptGenerator

    Product <|-- Console
    Product <|-- Accessory
    ProductCatalogRow <|.. Console
    AccessoryCatalogRow <|.. Accessory
    PromotionCatalogRow <|.. Promotion
    ItemFinder <|-- StockUpdater
    ItemFinder <|-- StockRestorer
    StockUpdater <|.. ProductService
    StockRestorer <|.. ProductService
    StockUpdater <|.. AccessoryService
    StockRestorer <|.. AccessoryService
    ItemFinder <|.. StoreCatalog
    SaleFinder <|.. SaleService
    SaleStore <|.. SaleRepository
    ReturnStore <|.. ReturnRepository
    PromotionChooser <|.. PromotionService
    SellerLister <|.. PersonMenu
    Submenu <|-- SaleMenu
    Submenu <|-- PersonMenu
    SaleService --> StoreCatalog
    SaleService --> StockUpdater
    SaleService --> SaleStore
    SaleService --> PersonFinder
    SaleService --> PromotionChooser
    SaleService --> WarrantyAssigner
    ReturnService --> ReturnStore
    ReturnService --> SaleFinder
    ReturnService --> StockRestorer
    ReturnService --> WarrantyCanceller
    MonthlyBalanceService ..> SaleFinder
    AccessoryService --> ItemFinder
    SaleMenu --> ItemFinder
    SaleMenu --> SellerLister
    ConsoleMenu --> Submenu
    ReceiptGenerator ..> AmountFormat
    Accessory ..> AmountFormat
    Promotion ..> AmountFormat

    note for StoreCatalog "V-01 catalogue out of SaleService"
    note for AmountFormat "V-02 one owner of the money and percentage formats"
    note for MonthlyBalanceService "V-03 report out of ReturnService"
    note for Submenu "V-04 final catch of GameZoneException"
    note for Console "V-05 one constant answers both questions"
    note for AccessoryFactory "V-06 one factory, single register"
    note for PromotionFactory "V-06 one factory, single register"
    note for ItemFinder "V-07 one findById contract; V-09 split"
    note for ProductCatalogRow "V-08 one row contract per file"
    note for StockUpdater "V-09 sale side"
    note for StockRestorer "V-09 return side"
    note for SaleMenu "V-10 small contracts"
    note for SaleService "V-11 only small contracts"
    note for ReturnService "V-11 only small contracts"
    note for AccessoryService "V-12 ItemFinder instead of ProductService"
    note for ConsoleMenu "V-13 receives the submenus from Main"
```
