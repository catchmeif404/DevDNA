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

Requires a real GitHub OAuth App (`GITHUB_CLIENT_ID`/`GITHUB_CLIENT_SECRET` in `.env`, callback `http://localhost:8090/api/auth/github/callback`) — the login flow is fully wired end to end, there is no anonymous/test-mode analyze path anymore.

### Running services outside Docker

```bash
# backend (port 8090 via docker-compose env, 8080 by default standalone)
cd backend && ./gradlew bootRun

# worker
cd worker && ./gradlew bootRun

# frontend
cd frontend && npm run dev
```

Backend and worker both need Postgres + Redis reachable via `DB_*`/`REDIS_*` env vars (defaults target `localhost`); `docker compose up -d postgres redis` works, or point them at any local Postgres/Redis (e.g. Homebrew services) as long as the `devwrapped`/`devwrapped` role+database exists.

## Build, lint, test

```bash
# backend
cd backend && ./gradlew build          # compile + test
cd backend && ./gradlew test           # tests only (JUnit 5)
cd backend && ./gradlew test --tests "dev.devwrapped.backend.BackendApplicationTests"

# worker
cd worker && ./gradlew build
cd worker && ./gradlew test
cd worker && ./gradlew test --tests "dev.devwrapped.worker.scoring.DeveloperTypeScorerTest"

# frontend
cd frontend && npm run lint
cd frontend && npm run build
```

Worker tests use plain JUnit 5 + AssertJ for pure-logic classes (`CommitClassifier`, `DeveloperTypeScorer`) — no Spring context needed for those.

## Architecture

### Auth + job pipeline (the core flow)

1. Frontend home page is a single "GitHub로 로그인" link to `GET /api/auth/github` (Spring Security's OAuth2 login, configured in `SecurityConfig` with `authorizationEndpoint` baseUri `/api/auth`).
2. On success, `OAuth2LoginSuccessHandler` upserts the `User` row, **immediately creates and enqueues an `AnalysisJob` for that account** (self-analysis only — there's no way to request analysis of another username), then redirects to `/dev/{login}?job={jobId}`.
3. `POST /api/analyses` (`AnalysisController`) is the same job-creation path but requires an authenticated session; it ignores any client-supplied username and always uses `principal.getAttribute("login")`. `GET /api/analyses/{jobId}` (status) and `GET /api/analyses/{jobId}/result` stay public (`SecurityConfig` permits them) since they only expose data for a job that's already running.
4. Either path pushes the job id onto a Redis list (`AnalysisQueuePublisher`, key `app.analysis-queue-key` = `analysis:queue`). `worker/.../common/AnalysisQueueConsumer` runs a single background thread (`@PostConstruct`-started `ExecutorService`) doing a blocking `LPOP` loop on that same key.
5. Per job it runs: `CollectorService` (GitHub API) → `FeatureExtractor` (raw activity → `Features`) → `DeveloperTypeScorer` (`Features` → `ScoringResult`) → `SummaryGenerator` (rule-based text, not an LLM yet) → persists `AnalysisResult`, updates `AnalysisJob` status through `COLLECTING` → `ANALYZING` → `COMPLETED`/`FAILED`.
6. On failure the job id is pushed to a separate DLQ Redis key (`app.analysis-dlq-key` = `analysis:dlq`); there is currently no automatic DLQ replay.
7. `GET /api/users/{username}/result` (`UserController`) also stays public and returns the latest result for any username regardless of who's logged in — analysis results are public GitHub-activity data, only *triggering* a new analysis is gated behind login.

Redis is standing in for a managed queue (SQS-like) with no built-in visibility timeout or retry — see `docs/설계문서.md` §21 if extending failure handling.

**Backend and worker are two independent Spring Boot apps that share the same Postgres schema** (`AnalysisJob`/`AnalysisResult` entities are duplicated across both, not shared via a common module — check both copies when changing the schema or a field). Only the backend runs Flyway migrations (`backend/src/main/resources/db/migration/`); the worker has `ddl-auto: none` and no Flyway, so it depends on the backend having migrated the schema first.

### Developer-type scoring (10 types, not 4)

`DeveloperTypeScorer` scores 10 types into a `Map<String, Integer>` (`ScoringResult.typeScores()`), each keyed to one normalized feature so no type has a structurally easier path to a high score than the others: `NIGHT_OWL`, `BUG_SLAYER`, `BUILDER`, `POLYGLOT`, `WEEKEND_WARRIOR`, `REFACTOR_MASTER`, `DOCUMENTARIAN`, `TESTER`, `EXPLORER` (distinct active repos), `COLLABORATOR` (PR activity). The winning type (highest score, ties keep the first-listed type) becomes `developerType`. When adding an 11th type, update all four places that know the full type list: `DeveloperTypeScorer`, `ShareCardGenerator` (backend), `SummaryGenerator` (worker), and `frontend/src/lib/developerType.ts`.

Scores persist as a single JSON `type_scores` column on `analysis_results` (added in `V2__type_scores.sql`, replacing 4 fixed score columns) — adding more types doesn't need another migration.

### Package layout (Spring Boot, package-by-feature)

Each package has a `package-info.java` with a one-line description — check it before adding to a package.

- `backend`: `auth` (GitHub OAuth2 login), `github` (API client stub, not yet implemented), `analysis` (job status/result + queue publish), `user`, `share` (share cards / badge endpoints), `common` (security config, cross-cutting)
- `worker`: `collector` (GitHub API calls), `analyzer` (feature extraction, commit classification), `scoring` (developer-type rules + DNA vector), `ai` (feeds only computed features to the summary generator, never raw commit text — see §14/§20 of the design doc for why), `common` (queue consumer, shared entities)

### Frontend

App Router, minimal structure: `src/app/page.tsx` (GitHub login link only — no anonymous analyze form), `src/app/dev/[username]/page.tsx` (result page, polls job status when a `?job=` query param is present, otherwise fetches the latest result directly), `src/lib/api.ts` (backend API calls), `src/lib/developerType.ts` (emoji/label/tagline per type). `frontend/CLAUDE.md` / `frontend/AGENTS.md` are auto-generated by `next dev` (Next.js 16 agent rules block) — do not hand-edit, they get regenerated.

### Data model

Postgres tables (backend-owned, via Flyway migrations in `backend/src/main/resources/db/migration/`): `users`, `analysis_jobs` (status enum `PENDING`/`COLLECTING`/`ANALYZING`/`COMPLETED`/`FAILED`), `analysis_results` (denormalized final stats: commit/repo/PR counts, peak hour/weekday, language ratios as JSON, `type_scores` JSON, `dna_vector` JSON, `ai_summary` text).

### Known scope gaps (don't assume these exist)

- No S3/R2 storage — share cards render on-demand as SVG from `analysis_results` on each request
- No LLM integration — `SummaryGenerator` produces rule-based template text, not an AI call
- Raw collected GitHub data (repos/commits) is not persisted, only the final `analysis_results` row
- Issues aren't collected at all (design doc mentions them); PRs are collected as a count only, not individual PR data (no `merged_at`, etc.)
- No logged-in-state UI: `GET /api/users/me` exists but the frontend never calls it, so there's no header/avatar or logout button after login
- README Badge (`/badge/{username}.svg`) and social-share buttons (design doc Phase 6) aren't built
