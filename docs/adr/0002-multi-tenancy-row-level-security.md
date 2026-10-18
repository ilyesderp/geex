# ADR 0002 — Multi-tenancy: shared schema + PostgreSQL Row Level Security

- **Status:** accepted
- **Date:** 2026-10-16

## Context

GEEX is designed to be offered as SaaS to many *gestorías* (each one is a *despacho*, i.e. a tenant).
The data is highly sensitive: passports, residence permits, criminal records. A single forgotten
`WHERE despacho_id = ?` must never leak one firm's data to another.

## Decision

- **Shared database, shared schema.** Every tenant-owned row has a `despacho_id` column.
- **The application connects as `geex_app`**: not superuser, no `BYPASSRLS`. Flyway migrations
  run as a separate admin role.
- **Every tenant table has `FORCE ROW LEVEL SECURITY`**, with policies based on two session variables:
  - `app.despacho_id`: the tenant of the current request
  - `app.sistema`: system mode, for operations that legitimately cross tenants (login, installation,
    background jobs)
- **`DataSourceMultiDespacho` sets both variables on every connection checkout**, from a Java 25
  `ScopedValue` (`ContextoEjecucion`). They are always set, even to empty, so a pooled connection
  can never carry over another request's tenant.
- **Identity tables are covered too.** Users can belong to several firms, so `usuarios` rows are visible
  only to the firms they are members of; `sesiones_refresco` and `tokens_un_uso` only to their firm (and
  password-reset links, which have no firm, only in system mode). Looking a user up by email (login,
  inviting someone who already works at another firm) runs in system mode in its own transaction.
- **The tenant always comes from the signed JWT** (`did` claim), never from request parameters.
- **System mode is entered only explicitly**, in adapters (`ContextoEjecucion.comoSistema(...)`),
  never from user input.

## Alternatives considered

- **Schema per tenant** (Twenty does this, because its users create custom tables): strong isolation,
  but N migrations per release and connection pool sprawl. GEEX only needs custom *fields*, not
  custom tables.
- **Database per tenant:** strongest isolation, highest operational cost. Not justified at this scale.

## Consequences

- ✅ Defence in depth. `AislamientoDespachosTest` proves that, connected as the application role:
  - with tenant A's context, the database returns **zero** rows of tenant B
  - inserting a row for tenant B is rejected
  - with no context, nothing is visible
- ✅ One schema, one migration path.
- ➖ One extra round-trip per connection checkout (`set_config`); negligible next to the query cost.
