<div align="center">

# DevDNA

**Turn your public GitHub activity into a tiny developer identity worth showing off.**

Live demo: **not deployed yet**

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

Once the backend is deployed publicly and your account has been analyzed, add
this to your GitHub profile README:

```markdown
![DevDNA](https://<your-backend-domain>/api/badge/catchmeif404.svg)
```

If the account has no saved analysis yet, the endpoint still returns a gray
`no data yet` badge instead of a broken image.

Local preview:

```markdown
![DevDNA](http://localhost:8090/api/badge/catchmeif404.svg)
```

That only works on your machine. GitHub needs a public HTTPS backend URL.

## Stack

| | |
|---|---|
| Frontend | Next.js, TypeScript, Tailwind CSS |
| Backend | Spring Boot, Java 21, Flyway |
| Worker | Spring Boot, Java 21 |
| Data | PostgreSQL for users/jobs/results, Redis for the analysis queue |
| Auth | GitHub OAuth |
| Deployment target | Railway |

## Architecture

```text
GitHub login
    |
    v
Backend -- upsert user + create job --> PostgreSQL
    |
    +-- enqueue job id ----------------> Redis
                                          |
                                          v
Worker -- collect public GitHub activity -> GitHub API
    |
    +-- extract features
    +-- score developer type
    +-- generate rule-based summary
    +-- persist result ----------------> PostgreSQL

README <img> -- GET /api/badge/{username}.svg --> animated SVG badge
```

The backend and worker are separate Spring Boot apps sharing the same database
schema. Only the backend runs Flyway migrations; the worker expects the schema
to already exist.

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
| Worker | http://localhost:8081 |

Health checks:

```bash
curl http://localhost:8090/actuator/health
curl http://localhost:8081/actuator/health
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

- Not deployed yet, so GitHub profile README badges only work as local preview.
- No LLM integration yet. Summaries are rule-based templates.
- Raw GitHub repo/commit data is not stored; only final analysis results are
  persisted.
- Redis is used as a simple queue and has no visibility timeout or automatic
  retry replay.
- GitHub username changes are not fully normalized yet; results still keep a
  denormalized `github_username` string for public lookup.

## Roadmap

- Public deployment and real `catchmeif404` profile badge
- Better mascot art pass for all 10 animals
- Result page animal illustrations, not just README badges
- Optional LLM-backed explanation after deterministic scoring
- More robust queue retry/DLQ replay
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
