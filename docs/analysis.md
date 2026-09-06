# Analysis (EN) — TODO: answer the 11 guiding questions in English

> Per policy, AI must NOT write this file for you. Use it as outline only — team must write own answers.
> Required path: docs/analysis.md on `develop` before Part 5.

Outline:
1. Common vs specific Person attributes; hierarchy mapping.
2. Should generic Person exist? abstract vs instantiable.
3. Common vs specific Product attributes.
4. Abstract getDescription + polymorphism mechanism.
5. Sale vs Customer/Seller/Product: association/composition, not inheritance.
6. Sale calculates its own total (information expert).
7. Guarantee >=1 product: validate in Sale constructor + SaleService.
8. Auto stock discount: SaleService orchestrates ProductService + persistence.
9. 4 layers content + placement criterion.
10. Why no file I/O in domain: SRP, coupling, testability.
11. Allowed deps: ui->service->persistence->model; model depends on none.
