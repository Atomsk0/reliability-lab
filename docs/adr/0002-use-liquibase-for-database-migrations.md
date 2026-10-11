# 2. Use Liquibase for database migrations

Date: 2026-10-11
Status: Accepted

## Context

order-service owns a PostgreSQL schema that will evolve with every new
feature. Schema changes must be versioned with the code, applied the same
way in tests, CI and the Kubernetes cluster, and never edited after they
have run. Hibernate's `ddl-auto` schema generation is not acceptable
beyond prototyping: it cannot express data migrations, gives no history,
and behaves unpredictably on existing data.

The two mainstream options for Spring Boot are Flyway and Liquibase.

- **Flyway**: plain numbered SQL files, minimal concepts, the most common
  default in new Spring Boot projects. Undo scripts and dry runs are
  commercial features.
- **Liquibase**: changesets with IDs and authors in SQL, YAML or XML.
  Free rollbacks, contexts, labels and preconditions. More ceremony.

## Decision

Use Liquibase, with changesets written as **formatted SQL**.

- Master changelog: `src/main/resources/db/changelog/db.changelog-master.yaml`,
  which uses `includeAll` on `db/changelog/changes/`.
- One file per change, numbered: `001-create-orders.sql`, `002-...`.
- Each changeset carries a `-- rollback` statement where a rollback is
  meaningful.
- Hibernate runs with `ddl-auto: validate`, so the application fails fast
  if the entities and the schema disagree.
- Changesets are immutable once merged. Mistakes are fixed forward with a
  new changeset.

Reasons for Liquibase over Flyway:

1. It is the tool used in my production work, so lessons here transfer
   directly.
2. Free rollback support is useful for the failure-injection and
   rollback drills planned later in this lab.
3. Formatted SQL keeps the readability of Flyway's plain SQL while still
   giving changeset IDs and rollback metadata.

## Consequences

- Positive: schema history is tracked in `DATABASECHANGELOG`, migrations
  run identically in Testcontainers, CI and the cluster, and drift between
  entities and schema is caught at startup.
- Negative: slightly more ceremony per change (changeset header, rollback
  line) than Flyway.
- Liquibase takes a lock in `DATABASECHANGELOGLOCK` while migrating. With
  several replicas starting at once only one migrates; a crash mid-migration
  can leave a stale lock that must be released manually. To revisit when the
  service runs with multiple replicas (option: run migrations as a separate
  Kubernetes Job or init container).
- **Gotcha found during setup:** with Spring Boot 4, the bare
  `org.liquibase:liquibase-core` dependency no longer triggers
  auto-configuration, because Boot 4 split auto-configuration into separate
  modules. Liquibase silently did not run, and Hibernate validation failed
  with "missing table [orders]". The fix is to depend on
  `spring-boot-starter-liquibase`. The same applies to other integrations:
  always use the `spring-boot-starter-*` artifact.
