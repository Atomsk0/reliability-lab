# 1. Record architecture decisions

Date: 2026-10-11
Status: Accepted

## Context

This lab will accumulate many design decisions: service boundaries,
database migration tooling, observability stack, resilience patterns.
Without a record, the reasoning behind them is lost, and it becomes
hard to tell a deliberate trade-off from an accident. The repository
is also a portfolio, so reviewers should be able to see *why* the
system looks the way it does, not just *what* it is.

## Decision

We record every significant decision as an Architecture Decision
Record (ADR) in `docs/adr/`, using Michael Nygard's format:
Title, Status, Context, Decision, Consequences.

- Files are numbered sequentially: `NNNN-short-title.md`.
- A decision is significant if it is hard to reverse, affects more
  than one service, or was a real choice between alternatives.
- ADRs are immutable once accepted. A changed decision gets a new
  ADR that marks the old one as "Superseded by NNNN".

## Consequences

- Writing an ADR takes 10 to 20 minutes per decision.
- Decisions can be reviewed and challenged later with their
  original context intact.
- The ADR log doubles as interview material: each one is a
  ready-made trade-off story.