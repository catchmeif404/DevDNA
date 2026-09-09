<div align="center">

# DevDNA

**Exhibit B. Your commits left a trail.**

**증거물 B. 커밋은 흔적을 남깁니다.**

[![DevDNA](https://devdna-production-1d53.up.railway.app/api/badge/catchmeif404.svg)](https://devdna.catchmeif404.com/dev/catchmeif404)

Live demo: **https://devdna.catchmeif404.com**

`catchmeif404`

</div>

---

## 한국어

GitHub에는 언제 커밋했는지, 무엇을 고쳤는지, 어떤 프로젝트에서 활동했는지 기록이 남습니다.
DevDNA는 이 공개 활동을 분석해 개발자 조사 기록, README용 증거물 배지, 공유 가능한 결과
리포트를 만듭니다.

DevDNA는 로그인한 본인의 GitHub 계정만 분석합니다. 다른 사람의 계정을 익명으로 분석하거나
개발자를 순위화하지 않습니다. 점수는 활동 패턴을 설명하기 위한 지표입니다.

### 주요 기능

- GitHub OAuth 로그인 기반 자기 분석
- 10가지 개발자 프로필 분류
- README에 추가할 수 있는 SVG 증거물 배지
- 공유 가능한 개발자 리포트 카드
- 한국어와 영어 화면 및 공유 문구
- 결정론적 규칙 기반 요약 생성

### 배지 사용법

GitHub 프로필 README에 아래 마크다운을 추가하세요. `catchmeif404`를 자신의 GitHub
사용자명으로 바꾸면 됩니다.

```markdown
[![DevDNA](https://devdna-production-1d53.up.railway.app/api/badge/catchmeif404.svg)](https://devdna.catchmeif404.com/dev/catchmeif404)
```

아직 분석 결과가 없으면 깨진 이미지 대신 `AWAITING EVIDENCE` 배지가 표시됩니다.

### 기술 스택

| 영역 | 기술 |
|---|---|
| 프론트엔드 | Next.js, TypeScript, Tailwind CSS |
| 백엔드 | Spring Boot, Java 21, Flyway |
| 데이터 | PostgreSQL |
| 인증 | GitHub OAuth |
| 배포 | Railway |

### 로컬 실행

```bash
cp .env.example .env
docker compose up -d
```

| 서비스 | 주소 |
|---|---|
| 프론트엔드 | http://localhost:3010 |
| 백엔드 API | http://localhost:8090 |

GitHub OAuth 로그인을 사용하려면 GitHub OAuth App을 만들고 다음 환경변수를 설정하세요.

```text
GITHUB_CLIENT_ID=...
GITHUB_CLIENT_SECRET=...
```

로컬 콜백 주소는 `http://localhost:8090/api/auth/github/callback`입니다.

### 현재 제한사항

- 요약은 아직 LLM이 아닌 규칙 기반 템플릿으로 생성됩니다.
- 원본 GitHub 저장소와 커밋 데이터는 저장하지 않고 최종 분석 결과만 저장합니다.
- 분석 중 백엔드가 재시작되면 진행 중인 작업은 자동으로 재시도되지 않습니다.
- GitHub 사용자명 변경에 대한 결과 소유권 정규화가 아직 완전하지 않습니다.

---

## English

GitHub keeps a record of when you commit, what you fix, and where you collaborate.
DevDNA turns that public activity into a developer case file, an evidence badge for
your README, and a shareable investigation report.

DevDNA analyzes only the GitHub account you sign in with. There is no anonymous
"analyze anyone" flow, and developers are not ranked. Scores describe activity
patterns; they are not probabilities or judgments.

### What it does

- Login-based self analysis with GitHub OAuth
- Ten developer profile classifications
- An SVG evidence badge for your README
- A shareable developer report card
- English and Korean screens and sharing text
- Deterministic, rule-based summaries

### Add the badge

Add this Markdown to your GitHub profile README. Replace `catchmeif404` with your
GitHub username.

```markdown
[![DevDNA](https://devdna-production-1d53.up.railway.app/api/badge/catchmeif404.svg)](https://devdna.catchmeif404.com/dev/catchmeif404)
```

If an account has no saved analysis yet, the endpoint returns an `AWAITING EVIDENCE`
badge instead of a broken image.

### Stack

| Area | Technology |
|---|---|
| Frontend | Next.js, TypeScript, Tailwind CSS |
| Backend | Spring Boot, Java 21, Flyway |
| Data | PostgreSQL |
| Auth | GitHub OAuth |
| Deployment | Railway |

### Run locally

```bash
cp .env.example .env
docker compose up -d
```

| Service | URL |
|---|---|
| Frontend | http://localhost:3010 |
| Backend API | http://localhost:8090 |

To use the GitHub login flow, create a GitHub OAuth App and set:

```text
GITHUB_CLIENT_ID=...
GITHUB_CLIENT_SECRET=...
```

The local callback URL is `http://localhost:8090/api/auth/github/callback`.

### Known limitations

- Summaries are rule-based templates; there is no LLM integration yet.
- Raw GitHub repository and commit data is not stored; only final analysis results persist.
- An in-flight analysis is not automatically retried if the backend restarts.
- Result ownership is not fully normalized when a GitHub username changes.

---

<div align="center">

Built by `catchmeif404` - building things nobody asked for.

</div>
