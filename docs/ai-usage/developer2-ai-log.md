# Developer2 AI Log — Jose David Cervantes Solano

Tools: Gemini for doubts, Copilot only as autocomplete while typing.

## Aug 22 — Why private everywhere (Gemini)
I thought `public` was faster than getters. I asked: "de verdad por qué todo en private, dame un ejemplo de la vida real". It showed what happens if phone validation changes and you have setters in one place. Decision: I put all `Person/Customer/Seller` fields in `private` and wrote getters/setters by hand.

## Aug 28 — Reading CSV without libraries (Gemini)
I needed `;`-separated files. I asked: "idea general para leer CSV con BufferedReader y split, sin código completo". It outlined the loop + skip header. Decision: I implemented `PersonRepository` my own way and tested with a broken line to see it skip.

## Sep 03 — Copilot while writing (Copilot)
I left autocomplete on while writing JavaDoc. Sometimes it suggested a wrong `@return`. Decision: I accepted only getter bodies, fixed two wrong comments myself, and never asked it for a whole class.
