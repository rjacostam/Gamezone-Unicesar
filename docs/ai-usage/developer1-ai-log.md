# Developer1 AI Log — Andres Daniel Cuello Blanco (1120243014)

Tool: ChatGPT. Used for concepts, compiler errors and name suggestions.

## Aug 21 — When to use abstract
I confused abstract class vs interface. I asked: "explícame cuándo una clase debe ser abstracta con un ejemplo de tienda, sin darme el código completo". It gave the rule: general category with no real instances. Decision: I declared `Product` abstract with `getFullDescription()` abstract, and wrote `VideoGame` and `Console` myself with `@Override`.

## Aug 26 — Maven exec error
I got `Failed to execute goal exec-maven-plugin` and got lost. I pasted only the error and asked what it usually means. It suggested the missing plugin block in `pom.xml`. Decision: I opened the pom and added the block myself, then `mvn compile` passed.

## Sep 02 — English names
I did not want to mess up camelCase. I asked: "cómo se dice plataforma, género y clasificación en inglés para atributos". It suggested `platform, genre, ageRating`. Decision: I used those exact names in `VideoGame` and kept the rest in English.

