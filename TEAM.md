# Team

## Members

| Full name | Student code | Role | Module | Feature branch |
|---|---|---|---|---|
| Rafael Junior Acosta Mendoza | 1005061643 | Technical Leader | Sales and integration | `feature/sale-module` |
| Andrés Daniel Cuello Blanco | 1120243014 | Developer 1 | Products | `feature/product-module` |
| José David Cervantes Solano | 1066872626 | Developer 2 | People | `feature/person-module` |

## Class distribution

The work is split by module (vertical slice). Each member implements their module across the layers, from the domain classes to the service.

| Member | model | persistence | service | ui / root |
|---|---|---|---|---|
| Technical Leader | `Sale` | `SaleRepository` | `SaleService` | `ConsoleMenu`, `Main` |
| Developer 1 | `Product` (abstract), `VideoGame`, `Console` | `ProductRepository` | `ProductService` | — |
| Developer 2 | `Person` (abstract), `Customer`, `Seller` | `PersonRepository` | `PersonService` | — |

## Committed activities

### Technical Leader

1. Create the GitHub repository with the initial setup (README, .gitignore, license).
2. Configure the `main` and `develop` branches and enable branch protection.
3. Configure the Maven project with the initial `pom.xml` and the four-layer package structure.
4. Write this `TEAM.md` file.
5. Implement the `Sale` domain class with its attributes, constructor and basic methods.
6. Implement the sale total calculation.
7. Implement `SaleRepository`.
8. Implement `SaleService` with the validation rules (at least one product, stock check, inventory update).
9. Implement the basic structure of `ConsoleMenu` (main menu).
10. Implement the submenus for the three modules.
11. Implement `Main` with the initial data loading and dependency injection.
12. Review and merge the developers' Pull Requests into `develop`.
13. Write the final `README.md` with build and run instructions.

### Developer 1

1. Create the `feature/product-module` branch.
2. Implement the abstract `Product` class with its common attributes, constructor and methods.
3. Declare the abstract description method.
4. Implement `VideoGame` with its own attributes and description.
5. Implement `Console` with its own attributes and description.
6. Implement `ProductRepository` to save and load products from files.
7. Implement `ProductService` with register, list and stock update methods.
8. Document every class of the module with JavaDoc in English.
9. Open Pull Requests for the Technical Leader to review.

### Developer 2

1. Create the `feature/person-module` branch.
2. Implement the abstract `Person` class with its common attributes, constructor and methods.
3. Declare the abstract or business method the subclasses must implement, according to the analysis.
4. Implement `Customer` with its own attributes.
5. Implement `Seller` with its own attributes.
6. Implement `PersonRepository` to save and load people from files.
7. Implement `PersonService` with register and list methods.
8. Document every class of the module with JavaDoc in English.
9. Open Pull Requests for the Technical Leader to review.

## Working agreements

- Every commit follows Conventional Commits, in English, and is pushed right after it is created.
- No direct commits to `main` or `develop`, and no `git push --force`.
- Every Pull Request is approved by a member other than its author.
- Feature branches are deleted from the remote after they are merged.
- Minimums: 12 atomic commits per member and 6 merged Pull Requests for the team.
