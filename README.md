# Prueba3 — GameZone Unicesar

Private repository for Taller 2 - GameZone Unicesar (Programación III, UPC).

## Structure
```
Prueba3/
├── README.md
├── TEAM.md
├── pom.xml
├── .gitignore
├── src/main/java/com/gamezone/
│   ├── model/
│   ├── persistence/
│   ├── service/
│   ├── ui/
│   └── Main.java
├── data/
└── docs/
    ├── analysis.md
    ├── hierarchy-diagram.md
    ├── class-diagram.md
    ├── layers-diagram.md
    └── ai-usage/
```

## Branches (Git Flow)
- `main`: stable version (protected, no direct commits)
- `develop`: integration branch (protected)
- `feature/product-module`, `feature/person-module`, `feature/sale-module`: work branches from `develop` via PR

## Build
```powershell
mvn compile
mvn exec:java -Dexec.mainClass="com.gamezone.Main"
```

> This repo is **private**. Only collaborators added in GitHub Settings > Collaborators can see it.
