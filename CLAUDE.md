# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

DevWrapped analyzes a GitHub user's public activity (commits, repos, PRs) and generates a "developer type" / stats card, similar in spirit to Spotify Wrapped. It is an open-source portfolio project (see `docs/설계문서.md` for the full design doc — written ahead of implementation, so treat it as intent/roadmap, not a description of current code; the README's "알려진 제약" section is the accurate current-state summary).

Full-stack, three deployable services in one repo:

- `frontend/` — Next.js (App Router) + TypeScript + Tailwind
- `backend/` — Spring Boot (Java 21) API server
- `worker/` — Spring Boot (Java 21) async analysis worker

## Local development

```bash
cp .env.example .env
docker compose up -d
```

| Service     | URL                     |
|-------------|-------------------------|
| Frontend    | http://localhost:3010   |
| Backend API | http://localhost:8090   |
| Worker      | http://localhost:8081   |

Health checks: `curl http://localhost:8090/actuator/health`, `curl http://localhost:8081/actuator/health`.

Currently there is no verified GitHub OAuth App, so login (`/api/auth/github`) is unexercised. Home page lets you enter a public GitHub username directly (e.g. `octocat`) to drive the full collect → analyze → score pipeline without login.

### Running services outside Docker

```bash
# backend (port 8090 via docker-compose env, 8080 by default standalone)
cd backend && ./gradlew bootRun

# worker
cd worker && ./gradlew bootRun

# frontend
cd frontend && npm run dev
```

Backend and worker both need Postgres + Redis reachable via `DB_*`/`REDIS_*` env vars (defaults target `localhost`); easiest to run `docker compose up -d postgres redis` and point the Gradle apps at those.

## Build, lint, test

```bash
# backend
cd backend && ./gradlew build          # compile + test
cd backend && ./gradlew test           # tests only (JUnit 5)
cd backend && ./gradlew test --tests "dev.devwrapped.backend.BackendApplicationTests"

# worker
cd worker && ./gradlew build
cd worker && ./gradlew test
cd worker && ./gradlew test --tests "dev.devwrapped.worker.analyzer.CommitClassifierTest"

# frontend
cd frontend && npm run lint
cd frontend && npm run build
```

Worker tests use plain JUnit 5 + AssertJ for pure-logic classes (`CommitClassifier`, `DeveloperTypeScorer`) — no Spring context needed for those.

## Architecture

### Job pipeline (the core flow)

1. `POST /api/analyses` (`backend/.../analysis/AnalysisController`) creates an `AnalysisJob` row (status `PENDING`) and pushes the job id onto a Redis list (`AnalysisQueuePublisher`, key `app.analysis-queue-key` = `analysis:queue`).
2. `worker/.../common/AnalysisQueueConsumer` runs a single background thread (`@PostConstruct`-started `ExecutorService`) doing a blocking `LPOP` loop on that same Redis key.
3. Per job it runs: `CollectorService` (GitHub API) → `FeatureExtractor` (raw activity → `Features`) → `DeveloperTypeScorer` (`Features` → `ScoringResult`) → `SummaryGenerator` (rule-based text, not an LLM yet) → persists `AnalysisResult`, updates `AnalysisJob` status through `COLLECTING` → `ANALYZING` → `COMPLETED`/`FAILED`.
4. On failure the job id is pushed to a separate DLQ Redis key (`app.analysis-dlq-key` = `analysis:dlq`); there is currently no automatic DLQ replay.
5. Frontend polls `GET /api/analyses/{jobId}` for status/progress, then `GET /api/analyses/{jobId}/result` once `COMPLETED`.

Redis is standing in for a managed queue (SQS-like) with no built-in visibility timeout or retry — see `docs/설계문서.md` §21 if extending failure handling.

**Backend and worker are two independent Spring Boot apps that share the same Postgres schema** (`AnalysisJob`/`AnalysisResult` entities are duplicated across both, not shared via a common module — check both copies when changing the schema or a field). Only the backend runs Flyway migrations (`backend/src/main/resources/db/migration/`); the worker has `ddl-auto: none` and no Flyway, so it depends on the backend having migrated the schema first.

### Package layout (Spring Boot, package-by-feature)

Each package has a `package-info.java` with a one-line description — check it before adding to a package.

- `backend`: `auth` (GitHub OAuth2 login), `github` (API client stub, not yet implemented), `analysis` (job CRUD + queue publish), `user`, `share` (share cards / badge endpoints), `common` (security config, cross-cutting)
- `worker`: `collector` (GitHub API calls), `analyzer` (feature extraction, commit classification), `scoring` (developer-type rules + DNA vector), `ai` (feeds only computed features to the summary generator, never raw commit text — see §14/§20 of the design doc for why), `common` (queue consumer, shared entities)

### Frontend

App Router, minimal structure: `src/app/page.tsx` (username input), `src/app/dev/[username]/page.tsx` (result page), `src/lib/api.ts` (backend API calls), `src/lib/developerType.ts`. `frontend/CLAUDE.md` / `frontend/AGENTS.md` are auto-generated by `next dev` (Next.js 16 agent rules block) — do not hand-edit, they get regenerated.

### Data model

Postgres tables (backend-owned, via Flyway `V1__init.sql`): `users`, `analysis_jobs` (status enum `PENDING`/`COLLECTING`/`ANALYZING`/`COMPLETED`/`FAILED`), `analysis_results` (denormalized final stats: commit/repo/PR counts, peak hour/weekday, language ratios as JSON, four developer-type scores, `dna_vector` JSON, `ai_summary` text).

### Known scope gaps (don't assume these exist)

- GitHub OAuth is implemented but unverified (no real OAuth App registered yet)
- No S3/R2 storage — share cards render on-demand as SVG from `analysis_results` on each request
- No LLM integration — `SummaryGenerator` produces rule-based template text, not an AI call
- Raw collected GitHub data (repos/commits) is not persisted, only the final `analysis_results` row
