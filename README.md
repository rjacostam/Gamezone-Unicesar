# Prueba3 — GameZone Unicesar (test reference)

Private test repo mirroring Taller 2 - GameZone Unicesar, Programacion III.

## Team (test, codes pending)
- Lead Sales + Integration: Rafael Junior Acosta Mendoza — `feature/sale-module`
- Dev1 Products: Andres Daniel Cuello Blanco — `feature/product-module`
- Dev2 Persons: Jose David Cervantes Solano — `feature/person-module`

## Structure
```
Prueba3/
├── README.md / TEAM.md / LICENSE / pom.xml / .gitignore
├── src/main/java/com/gamezone/model/ (Person,Customer,Seller,Product,VideoGame,Console,Sale)
├── src/main/java/com/gamezone/persistence/ (3 repositories CSV)
├── src/main/java/com/gamezone/service/ (3 services)
├── src/main/java/com/gamezone/ui/ConsoleMenu.java + Main.java
├── data/persons.csv (3 sellers seed), products.csv, sales.csv
└── docs/analysis.md, hierarchy-diagram.md, class-diagram.md, layers-diagram.md, ai-usage/
```

## Build and run
NetBeans bundles Maven, or terminal:
```powershell
mvn compile
mvn exec:java -Dexec.mainClass="com.gamezone.Main"
# sin maven (solo javac disponible aquí):
javac -d target/classes (Get-ChildItem -Recurse -Filter "*.java" src/main/java | % FullName)
"0" | java -cp target/classes com.gamezone.Main
```

## Branches
`main` estable, `develop` integración, 3 `feature/*` conservadas para prueba (en entrega real se borran tras merge).
