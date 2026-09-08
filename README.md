<div align="center">

# DevDNA

**Turn your public GitHub activity into a tiny developer identity worth showing off.**

[![DevDNA](https://devdna-production-1d53.up.railway.app/api/badge/catchmeif404.svg)](https://devdna-frontend.99kimst.workers.dev/dev/catchmeif404)

Live demo: **https://devdna-frontend.99kimst.workers.dev**

`catchmeif404`

</div>

---

## Why I built this

GitHub already knows how you build: when you commit, what you fix, which
languages you keep coming back to, and whether you live in PRs, tests, docs, or
late-night streaks.

But most profile tools flatten that into raw numbers. DevDNA turns the same
public activity into a developer type, a short summary, and an animated animal
badge you can put in your GitHub profile README.

The point is not to rank developers. The point is to make your coding pattern
feel like a small collectible.

## What it does

- **Login-based self analysis** - sign in with GitHub and DevDNA analyzes the
  account you logged in with. There is no anonymous "analyze anyone" flow.
- **10 developer animals** - Night Owl, Debug Cat, Builder Beaver, Polyglot
  Parrot, Weekend Otter, Tidy Fox, Archivist Elephant, Lab Mouse, Explorer
  Turtle, and Team Penguin.
- **Animated README badge** - `GET /api/badge/{username}.svg` returns a moving
  SVG mascot badge designed for GitHub README `<img>` embedding.
- **Share card** - `GET /api/share/{username}/card` renders a larger SVG card
  from the latest saved result.
- **Rule-based summary** - the `ai_summary` field exists, but the MVP does not
  call an LLM. It stores a deterministic template summary generated from the
  computed features.

## Badge

Once your account has been analyzed, add this to your GitHub profile README
(swap `catchmeif404` for your own GitHub username):

```markdown
[![DevDNA](https://devdna-production-1d53.up.railway.app/api/badge/catchmeif404.svg)](https://devdna-frontend.99kimst.workers.dev/dev/catchmeif404)
```

If the account has no saved analysis yet, the endpoint still returns a gray
`no data yet` badge instead of a broken image. The first 3 accounts ever
analyzed also get a small gold crown on their badge.

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

README <img> -- GET /api/badge/{username}.svg --> animated SVG badge
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

- Better mascot art pass for all 10 animals
- Result page animal illustrations, not just README badges
- Optional LLM-backed explanation after deterministic scoring
- Retry/resume for a job that dies mid-analysis (currently just fails)
- Username-change-safe result ownership model

## Contributing

Issues, ideas, and PRs are welcome. See [`CONTRIBUTING.md`](CONTRIBUTING.md)
before opening a PR.

## License

MIT

---

<div align="center">

Built by `catchmeif404` - building things nobody asked for.

</div>
