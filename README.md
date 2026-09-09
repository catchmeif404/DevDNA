<div align="center">

# DevDNA

**Exhibit B. Your commits left a trail.**

[![DevDNA](https://devdna-production-1d53.up.railway.app/api/badge/catchmeif404.svg)](https://devdna.catchmeif404.com/dev/catchmeif404)

Live demo: **https://devdna.catchmeif404.com**

Latest release: **v0.6.4**

`catchmeif404`

[한국어](README.ko.md) · **English**

</div>

---

GitHub keeps a record of when you commit, what you fix, and where you collaborate. DevDNA turns
that public activity into a developer case file, an evidence badge for your README, and a
shareable investigation report.

DevDNA analyzes only the GitHub account you sign in with. There is no anonymous “analyze anyone”
flow, and developers are not ranked. Scores describe activity patterns; they are not probabilities
or judgments.

## What it does

- Login-based self analysis with GitHub OAuth
- Ten developer profile classifications
- An SVG evidence badge for your README
- A shareable developer report card
- English and Korean screens and sharing text
- Deterministic, rule-based summaries

## Add the badge

Add this Markdown to your GitHub profile README. Replace `catchmeif404` with your GitHub username.

```markdown
[![DevDNA](https://devdna-production-1d53.up.railway.app/api/badge/catchmeif404.svg)](https://devdna.catchmeif404.com/dev/catchmeif404)
```

If an account has no saved analysis yet, the endpoint returns an `AWAITING EVIDENCE` badge instead
of a broken image.

## Stack

| Area | Technology |
|---|---|
| Frontend | Next.js, TypeScript, Tailwind CSS, Cloudflare Workers |
| Backend | Spring Boot, Java 21, Flyway, Railway |
| Data | PostgreSQL |
| Auth | GitHub OAuth |
| Languages | English and Korean |

## Run locally

```bash
cp .env.example .env
docker compose up -d
```

| Service | URL |
|---|---|
| Frontend | http://localhost:3010 |
| Backend API | http://localhost:8090 |

Create a GitHub OAuth App and set `GITHUB_CLIENT_ID` and `GITHUB_CLIENT_SECRET` to use the login
flow. The local callback URL is `http://localhost:8090/api/auth/github/callback`.

## Known limitations

- Summaries are rule-based templates; commit-type classification can optionally use Claude API.
- Raw GitHub repository and commit data is not stored; only final analysis results persist.
- An in-flight analysis is not automatically retried if the backend restarts.
- Result ownership is not fully normalized when a GitHub username changes.

<div align="center">

Built by `catchmeif404` - building things nobody asked for.

</div>
