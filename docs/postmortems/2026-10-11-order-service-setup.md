# Postmortem: order-service build failed four times before the first green run

**Date:** 2026-10-11
**Status:** Draft
**Format:** Blameless
**Duration of troubleshooting:** about 20 minutes (14:17 to 14:35 Kyiv time)

## Summary

Setting up the repository and the first service (Spring Boot 4.1, JDK 25,
Liquibase, Testcontainers) hit four consecutive failures before `./mvnw verify`
passed: a compile error, a missing Docker engine, a misleading Testcontainers
warning, and Liquibase silently not running. Each failure was hidden behind the
previous one, so they surfaced one at a time. Two of the four came from the setup
guide being written against older library behavior (JUnit 5, Spring Boot 3), not
from the code itself.

## Impact

- About 20 minutes of debugging; the first green build was delayed until 14:35.
- No data or production impact. Local development only.

## Timeline (Kyiv time)

| Time | Event |
| --- | --- |
| 12:02 | Repo review before first commit: `.idea/` not ignored at the root, empty `docs/` and `deploy/` folders that Git would not track. Fixed with root `.gitignore` entries and `.gitkeep` files |
| 13:49 | Decision to use Liquibase instead of Flyway. The guide was updated; the already-generated project was migrated by hand, replacing the Flyway dependency with `liquibase-core` |
| 14:17 | **Failure 1, compile:** `reference to MediaType is ambiguous` between `org.springframework.http.MediaType` and `org.junit.jupiter.api.MediaType`. Cause: wildcard imports of both packages; newer JUnit added its own `MediaType`. Fixed with explicit JUnit imports |
| 14:21 | **Failure 2, tests:** `postgresContainer` bean failed: "Previous attempts to find a Docker environment failed" |
| 14:26 | The log's last line, "docker-machine executable was not found on PATH", was investigated. A red herring: docker-machine is Testcontainers' last-resort strategy, reported only after the real ones fail |
| 14:29 | `docker info` showed the client (29.8.2) but no server: `/var/run/docker.sock` did not exist. **Root cause of failure 2:** only the Docker CLI was installed, with no engine |
| 14:31 | OrbStack installed and started; Testcontainers connected. A Ryuk warning ("Connection refused") appeared, another red herring |
| 14:34 | **Failure 3, startup:** `SchemaManagementException: Schema validation: missing table [orders]` |
| 14:34 | Root cause found in `pom.xml`: with Spring Boot 4, bare `liquibase-core` no longer triggers auto-configuration, so Liquibase never ran and Hibernate `validate` found no table. Replaced with `spring-boot-starter-liquibase` |
| 14:35 | `./mvnw verify` passed: three tests green against PostgreSQL in Testcontainers |

## Root causes

1. **Spring Boot 4 modular auto-configuration.** Boot 4 moved each integration's
   auto-configuration into its own module, pulled in by `spring-boot-starter-*`.
   Adding a library such as `liquibase-core` directly no longer activates it, and
   nothing warns about that. The only symptom was a downstream Hibernate error.
2. **Docker CLI without an engine.** `brew install docker` installs only the
   client. The setup guide said "a Docker runtime" but did not include a check
   that the engine was actually running.
3. **Wildcard imports across libraries.** `import org.junit.jupiter.api.*` and
   `import org.springframework.http.*` both export a `MediaType` in current
   versions. The guide's test code used wildcards.

## Contributing factors

- **Failures were serialized.** Each error hid the next, so a 4-step problem
  looked like an endless one.
- **Misleading last log lines.** Testcontainers prints its least relevant
  strategy failure (docker-machine) last, and Ryuk logs a WARN even when the run
  can still succeed. The real errors were higher up or further down.
- **Guide drift.** The guide assumed Spring Boot 3 and JUnit 5 era behavior;
  Initializr generated Boot 4.1 with newer JUnit.
- **Manual dependency migration.** Switching Flyway to Liquibase by editing
  `pom.xml` by hand skipped what Initializr would have generated (the starter).

## What went well

- `ddl-auto: validate` caught the missing migration immediately at startup,
  instead of letting Hibernate create the table and hide the problem.
- `docker info` separated "client works" from "engine works" in one command.
- Each failure was fixed at its cause rather than worked around: no disabling of
  Ryuk, no switching to `ddl-auto: update`.

## Where we got lucky

- The Ryuk warning did not turn into a real failure. Its cause (port readiness,
  VPN or proxy interference) was never confirmed.

## Action items

| # | Action | Type | Status |
| --- | --- | --- | --- |
| 1 | Use explicit imports in tests; set IntelliJ's "class count to use import with *" high enough to avoid wildcards | Prevent | Done for `OrderApiTest` |
| 2 | Rule for this repo: in Spring Boot 4 always depend on `spring-boot-starter-*`, never the bare library | Prevent | Recorded in ADR 0002 |
| 3 | Add a prerequisites check to the README: `java -version` shows 25, `docker info` shows a Server section | Prevent | Open |
| 4 | Assert in a test that Liquibase ran (e.g. `DATABASECHANGELOG` has the expected changesets), so a silent skip fails loudly | Detect | Open |
| 5 | When a Testcontainers error appears, read the first strategy failure, not the last line | Learn | Noted |
| 6 | Confirm whether the Ryuk warning recurs, and why | Verify | Open |

## Lessons

- **When a framework changes major version, check how integrations are wired,
  not just the API.** Boot 4's change produced no compile error, only a
  missing side effect.
- **Read logs from the first error, not the last line.** Libraries that try
  several strategies often report the least relevant one last.
- **A validating startup is a cheap detector.** `ddl-auto: validate` turned a
  silent misconfiguration into an immediate, explicit failure.
