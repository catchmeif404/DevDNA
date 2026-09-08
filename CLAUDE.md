# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

DevDNA analyzes a GitHub user's public activity (commits, repos, PRs) and generates a "developer type" / stats card, similar in spirit to Spotify Wrapped. It is an open-source portfolio project (see `docs/설계문서.md` for the full design doc — written ahead of implementation, so treat it as intent/roadmap, not a description of current code; the README's "알려진 제약" section is the accurate current-state summary).

Full-stack, two deployable services in one repo:

- `frontend/` — Next.js (App Router) + TypeScript + Tailwind
- `backend/` — Spring Boot (Java 21) API server, including the GitHub-collection/scoring/summary
  pipeline that used to live in a separate `worker/` service (removed — see "Async analysis" below)

## Local development

```bash
cp .env.example .env
docker compose up -d
```

| Service     | URL                     |
|-------------|-------------------------|
| Frontend    | http://localhost:3010   |
| Backend API | http://localhost:8090   |

Health check: `curl http://localhost:8090/actuator/health`.

Requires a real GitHub OAuth App (`GITHUB_CLIENT_ID`/`GITHUB_CLIENT_SECRET` in `.env`, callback `http://localhost:8090/api/auth/github/callback`) — the login flow is fully wired end to end, there is no anonymous/test-mode analyze path anymore.

### Running services outside Docker

```bash
# backend (port 8090 via docker-compose env, 8080 by default standalone)
cd backend && ./gradlew bootRun

# frontend
cd frontend && npm run dev
```

Backend needs Postgres reachable via `DB_*` env vars (defaults target `localhost`); `docker compose up -d postgres` works, or point it at any local Postgres (e.g. Homebrew services) as long as the `devwrapped`/`devwrapped` role+database exists. No Redis, no second service — see "Async analysis" below.

## Build, lint, test

```bash
# backend
cd backend && ./gradlew build          # compile + test
cd backend && ./gradlew test           # tests only (JUnit 5)
cd backend && ./gradlew test --tests "dev.devwrapped.backend.BackendApplicationTests"
cd backend && ./gradlew test --tests "dev.devwrapped.backend.scoring.DeveloperTypeScorerTest"

# frontend
cd frontend && npm run lint
cd frontend && npm run build
```

`CommitClassifierTest`/`DeveloperTypeScorerTest` exercise plain JUnit 5 + AssertJ pure-logic classes — no Spring context needed for those.

## Architecture

### Auth + async analysis pipeline (the core flow)

1. Frontend home page is a single "GitHub로 로그인" link to `GET /api/auth/github` (Spring Security's OAuth2 login, configured in `SecurityConfig` with `authorizationEndpoint` baseUri `/api/auth`).
2. On success, `OAuth2LoginSuccessHandler` upserts the `User` row, **immediately creates an `AnalysisJob` for that account and hands it to `AnalysisRunner.runAsync`** (self-analysis only — there's no way to request analysis of another username), then redirects to `/dev/{login}?job={jobId}`.
3. `POST /api/analyses` (`AnalysisController`) is the same job-creation path but requires an authenticated session; it ignores any client-supplied username and always uses `principal.getAttribute("login")`. `GET /api/analyses/{jobId}` (status) and `GET /api/analyses/{jobId}/result` stay public (`SecurityConfig` permits them) since they only expose data for a job that's already running.
4. `AnalysisRunner.runAsync` (`analysis` package, `@Async` — enabled via `@EnableAsync` on `BackendApplication`) runs on Spring's default task executor, off the request thread: `CollectorService` (GitHub API) → `FeatureExtractor` (raw activity → `Features`) → `DeveloperTypeScorer` (`Features` → `ScoringResult`) → `SummaryGenerator` (rule-based text, not an LLM yet) → persists `AnalysisResult`, updates `AnalysisJob` status through `COLLECTING` → `ANALYZING` → `COMPLETED`/`FAILED`.
5. `GET /api/users/{username}/result` (`UserController`) also stays public and returns the latest result for any username regardless of who's logged in — analysis results are public GitHub-activity data, only *triggering* a new analysis is gated behind login.

**No queue, no separate worker service.** This used to be a Redis list (`AnalysisQueuePublisher`) consumed by a standalone `worker/` Spring Boot app polling with a blocking `LPOP` loop — modeled after an SQS-backed architecture as a portfolio choice (see `docs/설계문서.md` §21), not because the traffic (one self-analysis per login) needed it. It was simplified to an in-process `@Async` call: same async, non-blocking behavior for the caller, one fewer service to run/deploy/pay for, and no real durability was actually lost — the old queue had no visibility timeout either, so a crash mid-job lost the job exactly like this does. If a job dies mid-run now, it just stays stuck at `COLLECTING`/`ANALYZING` with no auto-retry, same as before.

### Developer-type scoring (10 types, not 4)

`DeveloperTypeScorer` scores 10 types into a `Map<String, Integer>` (`ScoringResult.typeScores()`), each keyed to one normalized feature so no type has a structurally easier path to a high score than the others: `NIGHT_OWL`, `BUG_SLAYER`, `BUILDER`, `POLYGLOT`, `WEEKEND_WARRIOR`, `REFACTOR_MASTER`, `DOCUMENTARIAN`, `TESTER`, `EXPLORER` (distinct active repos), `COLLABORATOR` (PR activity). The winning type (highest score, ties keep the first-listed type) becomes `developerType`. When adding an 11th type, update all four places that know the full type list: `DeveloperTypeScorer`, `ShareCardGenerator`, `SummaryGenerator`, and `frontend/src/lib/developerType.ts` — all four now live in `backend/` (see package layout below).

Scores persist as a single JSON `type_scores` column on `analysis_results` (added in `V2__type_scores.sql`, replacing 4 fixed score columns) — adding more types doesn't need another migration.

### Package layout (Spring Boot, package-by-feature)

Each package has a `package-info.java` with a one-line description — check it before adding to a package.

- `auth` (GitHub OAuth2 login), `github` (API client stub, not yet implemented — unrelated to `collector` below, which is the real GitHub client used by the analysis pipeline), `analysis` (job status/result + `AnalysisRunner`, the `@Async` pipeline entry point), `user`, `share` (share cards / badge endpoints), `common` (security config, cross-cutting)
- `collector` (GitHub API calls — `GithubApiClient`, `CollectorService`), `analyzer` (feature extraction, commit classification), `scoring` (developer-type rules + DNA vector), `ai` (feeds only computed features to the summary generator, never raw commit text — see §14/§20 of the design doc for why)

These last four packages moved here from the old `worker/` service — see "Async analysis" above.

### Frontend

App Router, minimal structure: `src/app/page.tsx` (calls `GET /api/users/me` to show login-vs-logged-in state — login link, or avatar + re-analyze/logout for a known session), `src/app/dev/[username]/page.tsx` (result page, polls job status when a `?job=` query param is present, otherwise fetches the latest result directly), `src/lib/api.ts` (backend API calls — auth-aware calls pass `credentials: "include"` for the session cookie), `src/lib/developerType.ts` (emoji/label/tagline per type). `frontend/CLAUDE.md` / `frontend/AGENTS.md` are auto-generated by `next dev` (Next.js 16 agent rules block) — do not hand-edit, they get regenerated.

### Data model

Postgres tables (backend-owned, via Flyway migrations in `backend/src/main/resources/db/migration/`): `users`, `analysis_jobs` (status enum `PENDING`/`COLLECTING`/`ANALYZING`/`COMPLETED`/`FAILED`), `analysis_results` (denormalized final stats: commit/repo/PR counts, peak hour/weekday, language ratios as JSON, `type_scores` JSON, `dna_vector` JSON, `ai_summary` text).

### Share cards, README badge, and social sharing

`backend/.../share/DeveloperTypeMeta` is the single source of truth for emoji/animal label/tagline/**accent color** per developer type on the backend side (used by `ShareCardGenerator` for emoji cards and by `BadgeGenerator` for animal labels/type color — keep it in sync with `DeveloperTypeScorer`'s type list and `frontend/src/lib/developerType.ts`; adding an 11th type means adding a `COLOR` entry too, or it silently falls back to the generic indigo default). `GET /api/share/{username}/card` renders the big vertical share card (404 if no result yet); `GET /api/badge/{username}.svg` (`ShareController.badge`) renders a rounded animated "achievement chip" — custom vector animal mascot plus a two-line label on a dark→type-color diagonal gradient, deliberately not a flat shields.io metrics rectangle — and always returns 200: a gray "no data yet" chip instead of a 404 broken image when the username hasn't been analyzed. Both endpoints are public (`SecurityConfig` permits `/api/share/**` and `/api/badge/**`) since they only expose already-computed public data.

The result page's "X에 공유하기" / "Threads에 공유하기" buttons are plain client-side intent-URL links (`twitter.com/intent/tweet`, `threads.net/intent/post`) built in `frontend/src/app/dev/[username]/page.tsx` — no backend involvement, no API keys.

### Known scope gaps (don't assume these exist)

- No S3/R2 storage — share cards render on-demand as SVG from `analysis_results` on each request
- No LLM integration — `SummaryGenerator` produces rule-based template text, not an AI call
- Raw collected GitHub data (repos/commits) is not persisted, only the final `analysis_results` row
- Issues aren't collected at all (design doc mentions them); PRs are collected as a count only, not individual PR data (no `merged_at`, etc.)
