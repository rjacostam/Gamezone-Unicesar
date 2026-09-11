# Leader AI Log — Rafael Junior Acosta Mendoza (1005061643)

Tool: Claude. I only used it for doubts and reviews, never to generate full classes.

## Aug 20 — Why model cannot save files
I was stuck because it felt easier to put `Files.writeString` inside `Sale`. I asked Claude: "la verdad no entiendo por qué no puedo guardar desde el modelo, explícame como si fuera primera vez y sin darme código". It explained with a simple example that testing `calculateTotal` would then need real files. That clicked for me. Decision: I left `Sale` clean with fields + `calculateTotal()`, and moved all I/O to `SaleRepository`. I wrote the repository myself.

## Aug 24 — Protecting main and develop
I kept pushing by mistake to develop. I asked: "cómo se bloquea el push directo en GitHub paso a paso". It listed Settings > Branches > Require pull request. Decision: I configured both rules myself and verified the direct push failed. No code involved.

## Sep 01 — Reviewing my stock validation
I pasted my own `registerSale` draft and asked: "revísalo y dime si me falta algún caso borde, no me reescribas todo". It pointed out null/empty list before summing. Decision: I added the guard myself and tested selling with an empty cart to see the error.

