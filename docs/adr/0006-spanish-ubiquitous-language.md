# ADR 0006 — Spanish ubiquitous language, English technical vocabulary

- **Status:** accepted
- **Date:** 2026-10-16

## Context

GEEX models Spanish immigration law. Its core concepts have no faithful English equivalent:
*expediente*, *requerimiento*, *arraigo*, *empadronamiento*, *suplido*, *provisión de fondos*,
*días inhábiles*. The users, the requirements document and the UI are all Spanish.

## Decision

- **Domain concepts are named in Spanish** (DDD ubiquitous language), exactly as domain experts
  say them: `Despacho`, `Membresia`, `DiaInhabil`, `CalendarioLaboral`, `ValorCatalogo`.
- **Technical and architectural concepts keep their usual English names:** `domain`, `application`,
  `adapter`, `port/in`, `port/out`, `…Repository`, `…UseCase`, `…Controller`, `…Entity`, `…Adapter`.
- **Everything user-facing is in Spanish:** UI, error messages (`ProblemDetail` with a stable
  `codigo`), e-mails, validation messages, the requirements traceability.
- **Engineering documentation** (README, ADRs) is in English, for a wider technical audience.

## Consequences

- ✅ Code and conversations with domain experts use the same words; no translation layer to keep in sync.
- ➖ Mixed-language identifiers (`ExpedienteRepository`). This is a deliberate convention, applied
  consistently.
