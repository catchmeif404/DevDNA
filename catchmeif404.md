# catchmeif404.md

Project guidance for any AI coding agent working in this repository.

## Workspace directives

This repository is part of the anonymous `catchmeif404` workspace. These workspace-level rules
apply in addition to the repository guidance below.

### Identity and copy

- Commit with the repository-local `catchmeif404` identity, never the owner's other identity.
- Keep the anonymous-developer framing intact: a case file or investigation on an unidentified
  developer. Projects are evidence or exhibits; updates are field reports; contact is a tip line.
- Use dry, deadpan, mildly noir copy. The recurring line is: "You found the file. You didn't find
  me." Do not reveal a real name, face, employer, or personal social accounts.
- `site/` is the visual and tonal reference: paper records, redacted subjects, reference numbers,
  status stamps, sealed exhibits, and restrained red accents.

### Localization

Every frontend change must keep Korean and English support complete. DevDNA uses `next-intl` with
Korean at clean URLs (`/`, `/dev/[username]`) and English under `/en`. Update both message files
and both locale paths whenever copy or UI behavior changes.

### Releases

- Push ordinary commits to `main`; they do not deploy production.
- Cut production releases with an annotated `vX.Y.Z` tag only after `main` is ready.
- DevDNA's Cloudflare Workers frontend deploy workflow runs from `v*` tags. Do not retag or move an
  existing release tag; create the next semver tag for a subsequent production deploy.

## Project

DevDNA analyzes a GitHub user's public activity (commits, repos, PRs) and generates a "developer type" / stats card, similar in spirit to Spotify Wrapped. It is an open-source portfolio project; the README's "현재 제한사항" section is the accurate current-state summary.

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
4. `AnalysisRunner.runAsync` (`analysis` package, `@Async` — enabled via `@EnableAsync` on `BackendApplication`) runs on Spring's default task executor, off the request thread: `CollectorService` (GitHub API) → `FeatureExtractor` (raw activity → `Features`, commit-type classification inside it may call an LLM — see "Commit-type classification" below) → `DeveloperTypeScorer` (`Features` → `ScoringResult`) → `SummaryGenerator` (rule-based text, not an LLM) → persists `AnalysisResult`, updates `AnalysisJob` status through `COLLECTING` → `ANALYZING` → `COMPLETED`/`FAILED`.
5. `GET /api/users/{username}/result` (`UserController`) also stays public and returns the latest result for any username regardless of who's logged in — analysis results are public GitHub-activity data, only *triggering* a new analysis is gated behind login.

**No queue, no separate worker service.** This used to be a Redis list (`AnalysisQueuePublisher`) consumed by a standalone `worker/` Spring Boot app polling with a blocking `LPOP` loop. It was simplified to an in-process `@Async` call: same async, non-blocking behavior for the caller, one fewer service to run/deploy/pay for, and no real durability was actually lost — the old queue had no visibility timeout either, so a crash mid-job lost the job exactly like this does. If a job dies mid-run now, it just stays stuck at `COLLECTING`/`ANALYZING` with no auto-retry, same as before.

### Developer-type scoring (10 types, not 4)

`DeveloperTypeScorer` scores 10 types into a `Map<String, Integer>` (`ScoringResult.typeScores()`), each keyed to one normalized feature so no type has a structurally easier path to a high score than the others: `NIGHT_OWL`, `BUG_SLAYER`, `BUILDER`, `POLYGLOT`, `WEEKEND_WARRIOR`, `REFACTOR_MASTER`, `DOCUMENTARIAN`, `TESTER`, `EXPLORER` (distinct active repos), `COLLABORATOR` (PR activity). The winning type (highest score, ties keep the first-listed type) becomes `developerType`. When adding an 11th type, update all four places that know the full type list: `DeveloperTypeScorer`, `ShareCardGenerator`, `SummaryGenerator`, and `frontend/src/lib/developerType.ts` — all four now live in `backend/` (see package layout below).

Scores persist as a single JSON `type_scores` column on `analysis_results` (added in `V2__type_scores.sql`, replacing 4 fixed score columns) — adding more types doesn't need another migration.

### Commit-type classification (regex by default, LLM optional)

`FeatureExtractor` no longer classifies each commit inline — it hands every commit message in the
batch to `analyzer.CommitTypeResolver.resolveAll()`, which decides between two classifiers:

- `analyzer.CommitClassifier` — the original Conventional-Commits-prefix + keyword regex rules.
  Still the only classifier used when `ai.commit-classifier.provider` is unset/`regex` (the
  default — no API key required, this is what local dev and any deploy without `ANTHROPIC_API_KEY`
  gets).
- `analyzer.ClaudeAiCommitClassifier` — classifies a chunk of raw commit messages (up to 100 at a
  time, one Anthropic call per chunk via `ai.ClaudeApiClient`) into the same `CommitType` enum.
  Selected by setting `ai.commit-classifier.provider=claude-api` (env `AI_COMMIT_CLASSIFIER_PROVIDER`)
  plus `ANTHROPIC_API_KEY`. This is the one place raw commit text reaches an LLM — see the
  `analyzer`/`ai` package-info docs for why that's still consistent with §14's "AI never computes a
  statistic directly" rule: the model only ever emits one of the fixed `CommitType` labels, and
  every ratio built from those labels afterward is still deterministic.

`CommitTypeResolver` degrades per-chunk, never per-analysis: an AI call that throws, times out, or
returns the wrong number of labels falls back to `CommitClassifier` for just that chunk (same
provider-select-with-fallback shape as aws-cost-calculator's `ai.ExplanationService`) — a caller
never sees an AI failure, only a less accurate classification for the affected commits.
`CommitTypeResolverTest` covers the fallback/chunking behavior with fake `AiCommitClassifier`s, no
Spring context or real API key needed.

### Package layout (Spring Boot, package-by-feature)

Each package has a `package-info.java` with a one-line description — check it before adding to a package.

- `auth` (GitHub OAuth2 login), `github` (API client stub, not yet implemented — unrelated to `collector` below, which is the real GitHub client used by the analysis pipeline), `analysis` (job status/result + `AnalysisRunner`, the `@Async` pipeline entry point), `user`, `share` (share cards / badge endpoints), `common` (security config, cross-cutting)
- `collector` (GitHub API calls — `GithubApiClient`, `CollectorService`), `analyzer` (feature extraction, commit classification — see above), `scoring` (developer-type rules + DNA vector), `ai` (shared AI infra — `ClaudeApiClient` — plus `SummaryGenerator`, which feeds only computed features to text, never raw commit text; the one exception is `analyzer.ClaudeAiCommitClassifier`, described above)

These last four packages moved here from the old `worker/` service — see "Async analysis" above.

### Frontend

App Router locale routes live under `src/app/[locale]/`: the home/register page loads `GET /api/users/me`, and `dev/[username]/page.tsx` polls a `?job=` analysis or loads the latest report. `src/proxy.ts` preserves Korean clean URLs and serves English below `/en`; `src/i18n/` owns the locale routing and navigation helpers. `src/lib/api.ts` holds backend API calls, with session-aware calls using `credentials: "include"`; `src/lib/developerType.ts` owns the localized public type name and description. `frontend/CLAUDE.md` / `frontend/AGENTS.md` are auto-generated by `next dev` — do not hand-edit them.

### Data model

Postgres tables (backend-owned, via Flyway migrations in `backend/src/main/resources/db/migration/`): `users`, `analysis_jobs` (status enum `PENDING`/`COLLECTING`/`ANALYZING`/`COMPLETED`/`FAILED`), `analysis_results` (denormalized final stats: commit/repo/PR counts, peak hour/weekday, language ratios as JSON, `type_scores` JSON, `dna_vector` JSON, `ai_summary` text).

### Share cards, README badge, and social sharing

`backend/.../share/DeveloperTypeMeta` owns the English artifact type names and taglines. `EvidenceSvg` owns bounded, XML-escaped SVG text and the shared illustrative fingerprint. Keep those files, `DeveloperTypeScorer`, and `frontend/src/lib/developerType.ts` aligned when adding a type. `GET /api/share/{username}/card` renders a 600 x 800 case-file SVG (404 without a result); `GET /api/badge/{username}.svg` always returns a paper evidence-label SVG, including an `AWAITING EVIDENCE` state for missing data. Both endpoints are public because they expose only computed public-activity results. Previews use explicitly fictional data in `frontend/public/sample-badge.svg` and `frontend/public/sample-card.svg`.

The result page's X and Threads links are client-side intent URLs. Its README badge markdown and report link use the active locale's public result URL; no backend sharing credentials or API keys are involved.

### Known scope gaps (don't assume these exist)

- No S3/R2 storage — share cards render on-demand as SVG from `analysis_results` on each request
- `SummaryGenerator` is still rule-based template text, not an AI call. Commit-type classification
  *can* use an LLM now (`ai.commit-classifier.provider=claude-api` — see "Commit-type
  classification" above) but defaults to regex, and no deployed environment has
  `ANTHROPIC_API_KEY`/the provider var set yet as of this writing — confirm both are actually
  configured on Railway before assuming live analyses use it.
- Raw collected GitHub data (repos/commits) is not persisted, only the final `analysis_results` row
- Issues aren't collected at all (design doc mentions them); PRs are collected as a count only, not individual PR data (no `merged_at`, etc.)
