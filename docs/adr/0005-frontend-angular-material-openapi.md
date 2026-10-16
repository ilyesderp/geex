# ADR 0005 — Frontend: Angular + Angular Material, client generated from OpenAPI

- **Status:** accepted
- **Date:** 2026-10-16

## Decision

- **Angular 22**: standalone components, signals, strict TypeScript and strict templates,
  Vitest for unit tests.
- **Angular Material 3** (MIT, maintained by the Angular team) as the component library, with
  self-hosted Material Symbols icons (no external CDN).
  - PrimeNG was evaluated first, but since v22 it is distributed under a commercial licence (a free
    "Community" tier with a licence key and yearly renewal).
  - Angular Material avoids that dependency risk for a product meant to be commercialised.
- **API contract first.**
  - The backend publishes its OpenAPI specification. `ContratoOpenApiTest` snapshots it into
    `frontend/openapi/geex-api.json`, and **fails the backend build** if the API changes and the
    snapshot is not regenerated.
  - The frontend generates its typed client from that file (`openapi-generator`, `typescript-angular`)
    before every build.
  - A backend change that breaks the frontend is therefore caught at compile time, not in production.
- **Response models are fully typed.** An OpenAPI customiser marks all response fields as required;
  request fields keep the requiredness of their validation annotations.
- **Session handling** (see ADR 0003):
  - the access token lives in memory and is renewed shortly before it expires
  - an HTTP interceptor retries once after a 401 with a single-flight refresh
  - route guards obtain all dependencies synchronously, before their first `await`
