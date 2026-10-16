# ADR 0001 — Modular monolith with hexagonal modules

- **Status:** accepted
- **Date:** 2026-10-16

## Context

GEEX covers many business areas: contacts, case files (*expedientes*), deadlines, invoicing,
communications, client portal… A small team builds and runs it. The business rules (deadline
calculations above all) must be testable without infrastructure, and must not erode as the codebase grows.

## Decision

- **One deployable Spring Boot application**, split into business modules: one top-level package
  each (`identidad`, `despacho`, `configuracion`, `calendario`, `auditoria`…).
- **Every module is a hexagon:**
  - `domain`: pure Java (aggregates, value objects, rules, domain events)
  - `application`: use cases, with inbound ports (`port/in`) and outbound ports (`port/out`)
  - `adapter`: technology (REST controllers, JPA/JDBC persistence, mail, JWT, event listeners, wiring)
- **Use cases are plain Java classes.** Each module's `adapter/config` creates them and wraps them
  in a transactional proxy (`CasosDeUso.transaccional`), so the application layer needs no Spring annotations.
- **Modules communicate only through events**, or through another module's explicitly published
  interface (Spring Modulith `@NamedInterface`: `api`, `eventos`).
- **The rules are enforced by tests**, so they fail the build if broken:
  - `ArquitecturaHexagonalTest` (ArchUnit): domain and application must not depend on Spring,
    JPA, the web layer or adapters.
  - `ModulosTest` (Spring Modulith): module boundaries respected, no cycles.
    It also generates the module diagrams.

## Consequences

- ✅ Domain rules and use cases are unit-tested in milliseconds with in-memory fakes
  (e.g. `AutenticacionServiceTest`).
- ✅ Technology swaps (e-mail provider, Redis, an external identity provider) mean adding an
  adapter; the core does not change.
- ✅ Simple to run and deploy (one process, one database). If a module ever needs to scale
  separately, its boundaries already exist.
- ➖ More mapping code (JPA entities ↔ domain objects) than a classic layered Spring app.
