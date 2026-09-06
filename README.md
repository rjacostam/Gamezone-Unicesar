# Prueba3 — GameZone Unicesar (test reference)

Private test repo mirroring Taller 2 requirements.

## Team (test)
- Lead Sales: Rafael Junior Acosta Mendoza — `feature/sale-module`
- Dev1 Products: Andres Daniel Cuello Blanco — `feature/product-module`
- Dev2 Persons: Jose David Cervantes Solano — `feature/person-module`

## Run
```powershell
mvn compile
mvn exec:java -Dexec.mainClass="com.gamezone.Main"
```
Data auto-loads from `data/*.csv` and saves after each operation. Sellers seed with 3 records on first run.

## Ten operations
1-3 products, 4-6 persons, 7-10 sales (see `ConsoleMenu`).
