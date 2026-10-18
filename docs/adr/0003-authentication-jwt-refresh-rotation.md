# ADR 0003 — Authentication: stateless JWT + rotating refresh tokens

- **Status:** accepted
- **Date:** 2026-10-16

## Decision

| | Access token | Refresh token |
|---|---|---|
| Format | JWT signed with HS256 (Spring Security OAuth2 Resource Server) | 256-bit random string; **only its SHA-256 hash is stored** |
| Lifetime | 15 minutes | 8 hours (staff working day) |
| Browser storage | Memory only (never `localStorage`: protects against XSS theft) | `HttpOnly` + `Secure` + `SameSite=Strict` cookie, path `/api/auth` |
| Claims | `sub`, `did` (tenant), `rol`, `sa`, `ver`, `aud=geex-personal`, `iss`, `jti` | — |

- **Rotation with reuse detection.** Every refresh invalidates the token and issues a new one in the
  same *family*. If an already-used token is presented again (a sign of theft), the whole family is
  revoked. The row is locked (`SELECT … FOR UPDATE`), so of N simultaneous refreshes with the same
  token exactly one succeeds. `ConcurrenciaTest` checks this.
- **Revocation.** Logging out revokes the family. Changing or resetting the password increments
  the user's `versionToken`, so no existing session can be renewed.
- **Instant revocation of access tokens (added 2026-10).** A stateless JWT normally stays valid until it
  expires, so deactivating someone or changing their role would only take effect within 15 minutes.
  Every request therefore also checks that the user is still active, still a member of that firm with
  the same role, and on the same `versionToken`; otherwise it gets 401 `SESION_REVOCADA`. The result is
  cached for 30 seconds and the cache is cleared after every change, so the effect is immediate on one
  server. Trade-off: one cached lookup per request; with several servers the cache would need to be
  shared (Redis) or the delay is up to 30 seconds.
- **Passwords:**
  - Argon2id hashing
  - minimum 12 characters (NIST SP 800-63B: length over composition rules)
  - temporary lockout after 5 failed attempts
  - per-IP rate limiting (Bucket4j)
  - identical response for unknown emails, with a dummy hash check to equalise timing
- **One-time links** (invitation 48 h, reset 1 h) are random, stored hashed, single-use, and travel
  in the request body (POST), never in URLs that end up in logs.
- **Rejections are results, not exceptions.** Use cases return `ResultadoSesion.Rechazada`, so state
  changes that must persist even when access is denied (failed-attempt counter, revocation of a
  stolen family) are committed.
- **HS256 is appropriate** because the same service issues and validates tokens. Token issuance sits
  behind the `EmisorTokensAcceso` port: moving to RS256/JWKS or an external IdP (Keycloak, Cognito)
  is an adapter change.
- **CSRF.** The API is stateless (bearer header), and the refresh cookie is `SameSite=Strict` and
  additionally requires a custom header (`X-GEEX-Refresco`) that cross-site forms cannot send.
