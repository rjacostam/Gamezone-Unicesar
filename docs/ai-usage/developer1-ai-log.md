# Developer1 AI Log — Andres Daniel Cuello Blanco
> SAMPLE FICTICIO PARA PRUEBA3 — no usar como entrega final. Reemplazar con uso real.

Tool used: ChatGPT

## 2026-08-21 — Abstractas
**Pregunté:** "Estoy confundido con abstract en Java, ¿cuándo una clase debe ser abstracta? Dame un ejemplo con productos, no me des el código completo."
**Respuesta útil:** Me dio la regla: si es categoría general que no tiene sentido instanciar, va abstracta.
**Decisión:** Declaré `Product` abstracta y `getFullDescription()` abstracto, las hijas lo implementan.

## 2026-08-26 — Error Maven
**Pregunté:** "Me sale 'package org.codehaus.mojo does not exist' al correr exec:java, ¿qué hago?"
**Respuesta útil:** Me explicó que faltaba el plugin exec-maven-plugin en el pom.
**Decisión:** Agregué el plugin yo mismo al pom.xml y compiló.

## 2026-09-02 — Nombres en inglés
**Pregunté:** "¿Cómo nombro en inglés plataforma, género y clasificación de edad siguiendo camelCase?"
**Respuesta útil:** Me sugirió `platform, genre, ageRating`.
**Decisión:** Usé esos nombres en `VideoGame`.
