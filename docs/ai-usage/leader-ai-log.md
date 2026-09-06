# Leader AI Log — Rafael Junior Acosta Mendoza
> SAMPLE FICTICIO PARA PRUEBA3 — no usar como entrega final. Reemplazar con uso real.

Tool used: Claude

## 2026-08-20 — Duda de capas
**Pregunté:** "No entiendo por qué el modelo no puede guardar en archivos, si es más fácil hacerlo todo en la misma clase. ¿Me explicas con un ejemplo simple?"
**Respuesta útil:** Me explicó separación de responsabilidades y que si mezclo I/O con dominio, probar la clase Sale se vuelve difícil.
**Decisión:** Dejé `Sale` solo con atributos y `calculateTotal()`, y pasé el guardado a `SaleRepository`. No copié código, solo la idea.

## 2026-08-24 — Git Flow
**Pregunté:** "¿Cómo protejo main y develop para que nadie haga push directo?"
**Respuesta útil:** Pasos de Settings > Branches > Require pull request.
**Decisión:** Yo mismo configuré las reglas en GitHub y probé que el push directo fallara.

## 2026-09-01 — Revisión de mi SaleService
**Pregunté:** Le pegué mi método de validar stock y le pedí "revísame esto, ¿hay algún caso borde?"
**Respuesta útil:** Me sugirió validar lista nula/vacía antes de calcular total.
**Decisión:** Agregué el `if (products == null || products.isEmpty())` por mi cuenta.
