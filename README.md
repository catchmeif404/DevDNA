<div align="center">

# DevDNA

**Exhibit B. Your commits left a trail.**

**증거물 B. 커밋은 흔적을 남깁니다.**

[![DevDNA](https://devdna-production-1d53.up.railway.app/api/badge/catchmeif404.svg)](https://devdna.catchmeif404.com/dev/catchmeif404)

Live demo: **https://devdna.catchmeif404.com**

`catchmeif404`

</div>

---

## The case

GitHub holds the record: when you commit, what you fix, where you collaborate.
DevDNA processes that public activity into a developer case file, an evidence
label for your README, and a shareable investigation report. Motive still unclear.

GitHub에는 기록이 남습니다. 언제 커밋했는지, 무엇을 고쳤는지, 누구와 작업했는지.
DevDNA는 공개 활동을 모아 개발자 조사 기록, README 증거물 라벨, 공유 보고서를
작성합니다. 동기는 아직 불명.

The paper, typewriter text, redaction marks, and red stamps follow the
[catchmeif404 case-file site](https://catchmeif404.com). Scores describe activity
patterns; they are not probabilities or a ranking of developers.

## What it does

- **Login-based self analysis** - sign in with GitHub and DevDNA analyzes the
  account you logged in with. There is no anonymous "analyze anyone" flow.
- **10 developer profiles** - After-hours Operator, Bug Hunter, Serial Builder,
  Multilingual Operator, Weekend Operative, Code Restorer, Archivist,
  Quality Inspector, Repo Explorer, and Collaborator. Stored scoring IDs are unchanged.
- **README evidence label** - `GET /api/badge/{username}.svg` returns a static
  SVG with the subject, classification, recorded counts, and an illustrative fingerprint.
- **Share report** - `GET /api/share/{username}/card` renders a 600 x 800
  SVG dossier from the latest saved result.
- **English + Korean** - localized screens, statuses, and sharing text. Exported
  evidence labels and reports use English archive headings.
- **Rule-based summary** - the `ai_summary` field exists, but the MVP does not
  call an LLM. It stores a deterministic template summary generated from the
  computed features.

## Badge

Once your account has been analyzed, add this to your GitHub profile README
(swap `catchmeif404` for your own GitHub username):

```markdown
[![DevDNA](https://devdna-production-1d53.up.railway.app/api/badge/catchmeif404.svg)](https://devdna.catchmeif404.com/dev/catchmeif404)
```

If the account has no saved analysis yet, the endpoint returns an
`AWAITING EVIDENCE` label instead of a broken image. The first 3 accounts ever
analyzed retain a red `Founding member #N` annotation.

Fictional evidence-label and report samples are checked in at
[`sample-badge.svg`](frontend/public/sample-badge.svg) and
[`sample-card.svg`](frontend/public/sample-card.svg). Fingerprint artwork is
illustrative, not biometric data or a visualization of the scoring vector.

Local preview:

```markdown
![DevDNA](http://localhost:8090/api/badge/catchmeif404.svg)
```

That only works on your machine. GitHub needs a public HTTPS backend URL —
use the deployed one above for a real profile README.

## Stack

| | |
|---|---|
| Frontend | Next.js, TypeScript, Tailwind CSS |
| Backend | Spring Boot, Java 21, Flyway |
| Data | PostgreSQL for users/jobs/results |
| Auth | GitHub OAuth |
| Deployment target | Railway |

## Architecture

```text
GitHub login
    |
    v
Backend -- upsert user + create job --> PostgreSQL
    |
    +-- run analysis async (same process, no queue) --> GitHub API
                                                            |
                                                            v
                                              extract features
                                              score developer type
                                              generate rule-based summary
                                              persist result ----------------> PostgreSQL

README <img> -- GET /api/badge/{username}.svg --> SVG evidence label
```

Analysis runs as a background task inside the backend process (`@Async`), not
a separate worker service behind a queue. At this project's actual traffic —
one self-analysis per login — a dedicated queue + worker (Redis, a second
Spring Boot app) added ops complexity without a real durability or scaling
benefit, so it was simplified away.

## Running it locally

Create environment variables, then start the full stack:

```bash
cp .env.example .env
docker compose up -d
```

| Service | URL |
|---|---|
| Frontend | http://localhost:3010 |
| Backend API | http://localhost:8090 |

Health check:

```bash
curl http://localhost:8090/actuator/health
```

For the login flow, create a real GitHub OAuth App and set:

```text
GITHUB_CLIENT_ID=...
GITHUB_CLIENT_SECRET=...
```

Local callback URL:

```text
http://localhost:8090/api/auth/github/callback
```

## Known issues

- No LLM integration yet. Summaries are rule-based templates.
- Raw GitHub repo/commit data is not stored; only final analysis results are
  persisted.
- If the backend process restarts mid-analysis, that in-flight job is lost
  (stays stuck at COLLECTING/ANALYZING) with no automatic retry — same
  failure mode the old Redis queue had, just without the extra service.
- GitHub username changes are not fully normalized yet; results still keep a
  denormalized `github_username` string for public lookup.

## Roadmap

- Localized text inside exported evidence labels and reports
- Optional LLM-backed explanation after deterministic scoring
- Retry/resume for a job that dies mid-analysis (currently just fails)
- Username-change-safe result ownership model

## Contributing

Design verification (run the frontend on port 3010 first):

```bash
cd frontend
npx playwright install chromium
npm run test:ui
```

This uses fictional API fixtures and writes desktop/mobile screenshots to
`frontend/artifacts/case-file/`. Set `DEVDNA_TEST_URL` to test a local Workers
preview instead. It does not sign in to GitHub or create a real analysis.

SVG checks and sample regeneration (Java 21):

```bash
cd backend
./gradlew test --tests 'dev.devwrapped.backend.share.*'
JAVA_TOOL_OPTIONS="-Ddevdna.previewDir=../frontend/public" ./gradlew test --tests '*SharePreviewTest' --rerun-tasks
```

Issues, ideas, and PRs are welcome. See [`CONTRIBUTING.md`](CONTRIBUTING.md)
before opening a PR.

## License

MIT

---

<div align="center">

Built by `catchmeif404` - building things nobody asked for.

</div>
