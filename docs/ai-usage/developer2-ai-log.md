# Developer2 AI Log — Jose David Cervantes Solano
> SAMPLE FICTICIO PARA PRUEBA3 — no usar como entrega final. Reemplazar con uso real.

Tools used: Gemini + GitHub Copilot

## 2026-08-22 — Encapsulamiento (Gemini)
**Pregunté:** "¿Por qué todos los atributos en private y no en public? ¿No es más trabajo hacer getters?"
**Respuesta útil:** Me explicó con ejemplo que si cambio validación del teléfono, con setter solo cambio un lugar.
**Decisión:** Puse todo en `private` en `Person, Customer, Seller` con getters/setters.

## 2026-08-28 — CSV persistencia (Gemini)
**Pregunté:** "¿Cómo leo un CSV con ; sin usar librerías externas?"
**Respuesta útil:** Me mostró idea con BufferedReader + split(";").
**Decisión:** Implementé mi `PersonRepository` a mi manera, sin copiar tal cual.

## 2026-09-03 — Autocompletado (Copilot)
**Uso:** Solo autocompletado de getters y JavaDoc en inglés mientras yo escribía.
**Decisión:** Revisé cada sugerencia y corregí dos comentarios que estaban mal. No generé clases completas.
