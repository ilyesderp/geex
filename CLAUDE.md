# CLAUDE.md

Guidance for AI coding assistants working in this repository.

## What this is

GEEX: case-management SaaS for Spanish immigration law firms (*despachos de extranjería*), built in
phases from a 60-page Spanish requirements document. Portfolio project: correctness and polish matter.

## Start here: goal, plan, requirements and architecture

Read these in order before planning or building a phase:

1. **`README.md`**: the goal, highlights, tech stack, architecture overview and the **Roadmap**
   (phases 0–9 with their status). The current phase is the first one not marked ✅.
2. **`docs/REQUISITOS.md`** (Spanish): every requirement of the spec mapped to its phase and status,
   with the spec's page numbers, plus the *interpretation decisions* where the spec is unclear or
   outdated. Those decisions override the spec.
3. **The original spec: `privado/BORRADOR*.pdf`** (60 pages, Spanish, local only). The source of truth for
   what each screen and field must do. Read only the pages of the current phase, using the page numbers
   in `docs/REQUISITOS.md` (the PDF reader takes at most 20 pages per call). It is git-ignored on purpose
   (it contains real people's data): **never commit it, copy it into tracked folders, or quote personal
   data from its screenshots**. If it is missing, ask the user to put it back in `privado/`.
4. **`docs/adr/`**: why the main architecture decisions were taken (one decision per file). Follow them;
   propose a new ADR rather than silently changing one.
5. **The code of an existing module** (e.g. `backend/.../calendario` and
   `frontend/src/app/features/configuracion`) as the pattern for new modules.

Product goal: a polished, working demo that follows the spec closely and Spanish law, usable by any
despacho in Spain (Alicante is only the default location).

## Project conventions

- Everything in the software is in **Spanish**: UI text, messages, emails, data, and domain names in code
  (`Despacho`, `Expediente`…); technical terms stay English.
- Work on `feature/*` from `develop`; `develop` → `main` only through PRs (both branches are protected,
  all CI checks must pass).
- **No secrets anywhere in git**, not even example values. `.env.example` holds names only.
- **UI/UX is a priority:** check every new screen at desktop and phone width (~390 px) before calling it
  done; no horizontal page scroll; clear errors under fields; obvious mobile navigation.
- When a phase is finished, update its status in `README.md` and `docs/REQUISITOS.md`.

## Definition of done (before saying a phase or feature is finished)

1. Backend `./mvnw -B verify`, frontend lint + unit tests + build, `npm run e2e` and `npm run e2e:visual`
   all pass; new
   behaviour has tests (integration tests for rules and security, e2e for key user flows).
2. Every new screen checked at desktop and phone widths **down to 360 px**, both loaded directly and after
   narrowing an open desktop page (no horizontal scroll, nothing cut off at the right, errors visible,
   mobile navigation obvious), **including fractional display scaling (device pixel ratio 1.25 and 1.5, common
   on Windows laptops)** and with zoomed-in screenshots of each component, not only a downscaled full
   page; add it to the mobile check in `frontend/e2e`.
3. A self-review for security (permissions, tenant isolation), concurrency and UX, and fixes applied.
4. Legal and calendar rules checked against the official source (BOE consolidated text, DOGV, the
   council's calendar), citing it in the code or docs; never from memory.
5. `README.md` roadmap and `docs/REQUISITOS.md` statuses updated.

## Architecture rules (enforced by tests)

- Hexagonal modular monolith (`backend/src/main/java/es/geex/<module>/`): `domain` (pure Java) ←
  `application` (use cases + ports) ← `adapter/in|out`. ArchUnit and Spring Modulith tests fail the build
  if a rule is broken; modules talk through their `api` package or domain events.
- Use cases are plain classes wrapped with `CasosDeUso.transaccional` in the module's `*ModuleConfig`.
- Multi-tenant: every business table has `despacho_id` and a Row Level Security policy (copy an existing
  migration). The firm comes from the JWT via `ContextoEjecucion`, never from request data.
  `ContextoEjecucion.comoSistema(...)` bypasses isolation: use only where unavoidable, and keep it small.
- Check permissions in the use case with `contexto.exigir(Permiso.X)`; frontend guards are UX only.
- Schema changes are new Flyway files (`V<n>__descripcion.sql`); never edit an applied migration.
- Errors: throw domain exceptions with a stable `codigo`; they become RFC 9457 responses.

## Commands (Windows; Maven is not installed, use the wrapper)

```bash
docker compose up -d                      # PostgreSQL + Mailpit (http://localhost:8025)
cd backend && ./mvnw spring-boot:run      # API on :8080 (Swagger at /swagger-ui.html)
cd backend && ./mvnw -B verify            # all backend tests (needs Docker for Testcontainers)
cd frontend && npm start                  # http://localhost:4200
cd frontend && npm run lint && npx ng test --watch=false && npx ng build
cd frontend && npm run e2e                # Playwright, against a running app on an EMPTY install
cd frontend && npx ng build && npm run e2e:visual   # screenshots + accessibility, in the Playwright Docker image
```

- **Visual tests** (`frontend/e2e/visual`): every key screen and dialog, desktop 100 % and phone 390 px at
  125 % scaling, compared pixel by pixel with the reference images in `e2e/visual/referencias/`, plus
  axe-core (WCAG 2.1 AA) checks. They run inside the pinned Playwright Linux image so local and CI pixels
  match. After an intended visual change: `npm run e2e:visual -- --update-snapshots`, **look at every
  changed image** (a wrong reference approves the defect), and commit them. New screens and dialogs must
  be added to `pantallas.visual.ts` with demo data in `datos-demo.ts`.

- **API changed?** `ContratoOpenApiTest` fails until the contract is regenerated:
  `./mvnw test -Dtest=ContratoOpenApiTest -Dgeex.contrato.actualizar=true`, then `npm run api` in
  `frontend` (the generated client in `src/app/api/generated` is never committed).
- First-time setup: `scripts/setup.ps1` (or `.sh`) creates `.env` with random secrets.

## Gotchas

- The project path contains spaces: quote paths in shell commands.
- In Angular guards, call every `inject()` before the first `await` (error NG0203 otherwise).
- Angular Material component styles load after global styles: global overrides need higher specificity
  (e.g. `.mat-mdc-checkbox.mi-clase`), not `::ng-deep`.
- Flex/grid rows that hold text (headers, toolbars, cards): give the text item `min-width: 0` and
  ellipsis so it shrinks, and `flex: none` to fixed items (logo, avatar). Otherwise long text pushes
  them off-screen on narrow phones; grid items likewise need `min-width: 0`.
- Search and filter fields: icon prefix **without** `<mat-label>` (use `placeholder` + `aria-label`, class
  `geex-buscador`). An outlined field with both a prefix icon and a floating label draws stray border
  fragments at fractional display scaling (120 %, 125 %, 150 %).
- Personal or private files go in `privado/`; `docs/**/*.pdf` is *not* git-ignored.

## Personal instructions (local only, not in git)

If present, this file holds the owner's personal working agreement and the path to the private plan:

@CLAUDE.local.md
