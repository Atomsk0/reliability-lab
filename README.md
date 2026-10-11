# Reliability Lab

A small order-processing platform built to be **operated, broken and fixed**.
The business domain is deliberately boring; the point is running it in
production-like conditions: observability, SLOs, load tests, chaos
experiments, runbooks and postmortems.

## Architecture

_Diagram TBD._

Current state: one Spring Boot service on a self-hosted kubeadm cluster
(3 VMs on Proxmox) backed by PostgreSQL.

## Services

| Service | Stack | Status |
|---|---|---|
| order-service | Java 25, Spring Boot, PostgreSQL, Liquibase | In progress |
| payment-service | Simulated provider with injectable latency and errors | Planned |
| notification-service | Async event consumer | Planned |

## Running locally

Requirements: JDK 25 and a Docker runtime (OrbStack, Docker Desktop or Colima).

    cd services/order-service
    ./mvnw verify                 # integration tests against a real PostgreSQL container
    ./mvnw spring-boot:test-run   # app on :8080 with a throwaway database

    curl -i -X POST localhost:8080/orders \
      -H 'Content-Type: application/json' \
      -d '{"customerId":"c-1","totalAmount":19.99}'

## Lab environment

| Component | Details |
|---|---|
| Hypervisor | Proxmox VE on an Intel i5 PC, 64 GB RAM |
| Kubernetes | kubeadm, 1 control plane + 2 workers, Cilium CNI |
| Storage | local-path-provisioner |
| Registry | GitHub Container Registry |

## Repository layout

    docs/adr/           Architecture Decision Records
    docs/runbooks/      How to respond to specific alerts
    docs/postmortems/   Blameless incident write-ups
    docs/cheatsheets/   Troubleshooting references
    services/           One folder per service
    deploy/             Kubernetes manifests and Helm values

## Documentation

- [ADR 0001: Record architecture decisions](docs/adr/0001-record-architecture-decisions.md)
- [ADR 0002: Use Liquibase for database migrations](docs/adr/0002-use-liquibase-for-database-migrations.md)
- [Postmortem: lab cluster setup](docs/postmortems/2026-10-10-lab-setup.md)
- [Postmortem: order-service setup](docs/postmortems/2026-10-11-order-service-setup.md)

## Roadmap

- [x] Lab cluster: Proxmox + kubeadm + Cilium
- [ ] order-service with integration tests and CI
- [ ] JVM troubleshooting exercises
- [ ] PostgreSQL with replica and failover tests
- [ ] Full observability: OpenTelemetry, Prometheus, Grafana, Loki, Tempo
- [ ] SLOs and burn-rate alerts
- [ ] Load tests (k6)
- [ ] Resilience patterns and chaos experiments
- [ ] reliability-guard Kubernetes operator (Go)
- [ ] AI incident assistant

## License

MIT
