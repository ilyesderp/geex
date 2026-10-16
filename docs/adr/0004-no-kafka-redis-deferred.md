# ADR 0004 — No Kafka; Redis deferred until there are multiple instances

- **Status:** accepted
- **Date:** 2026-10-16

## Context

Should GEEX use Kafka for events and/or Redis for caching and queues? We checked what well-known open-source
products of the same kind do. In October 2026 we scanned their `docker-compose` files and dependency manifests:

| Product | ★ | Kind | Kafka | Background work / cache |
|---|---|---|---|---|
| Plane | 61k | Project management | ❌ | Celery + RabbitMQ, Redis/Valkey |
| Twenty | 58k | CRM | ❌ | Redis + BullMQ |
| Odoo | 55k | ERP | ❌ | PostgreSQL only |
| cal.diy | 49k | Booking | ❌ | Redis |
| Paperless-ngx | 47k | Documents | ❌ | Celery + Redis |
| ERPNext / Frappe | 40k / 11k | ERP | ❌ | Redis queue |
| Novu | 40k | Notifications | ❌ | Redis + BullMQ |
| Chatwoot | 38k | Support / chat | ❌ | Redis + Sidekiq |
| listmonk | 24k | Mass mailing | ❌ | PostgreSQL only |
| Krayin | 24k | CRM | ❌ | Laravel queue (database) |
| Documenso | 15k | E-signature | ❌ | Pluggable jobs (BullMQ / Inngest) |
| Invoice Ninja | 10k | Invoicing + portal | ❌ | Laravel queue (database) |
| **PostHog** | 40k | Product analytics | ✅ | Kafka + ClickHouse |
| **Sentry** | 9.6k | Error tracking | ✅ | Kafka + ClickHouse |
| **Lago** | 11k | Usage billing | ✅ | Kafka/Redpanda + ClickHouse |

Only event-ingestion products (millions of events) use Kafka. No business application does.

## Decision

- **No Kafka.** Domain events go through Spring Modulith's **event publication registry**: an outbox
  table in PostgreSQL, written in the same transaction as the change and retried until processed
  (the same approach as Odoo and listmonk relying on the database). A Kafka adapter could be added
  later behind the same events if GEEX ever needs to stream to external systems.
- **No Redis yet.** With one instance, an in-process cache (Caffeine) and in-memory rate limiting
  (Bucket4j) are enough.
- **Redis will be introduced when GEEX runs as several instances** (AWS phase), for:
  - shared rate-limit buckets (Bucket4j has a Redis backend)
  - permission-cache invalidation
  - real-time chat fan-out

  Each of these already sits behind an adapter.

## Consequences

- ✅ Local development needs only PostgreSQL (plus Mailpit); fewer moving parts to operate and pay for.
- ➖ Rate limits are per instance until Redis is added.
