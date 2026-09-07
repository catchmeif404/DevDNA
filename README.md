# DevDNA

GitHub 공개 활동 데이터를 분석해 개발자의 코딩 패턴, 기술 스택, 활동 시간, 개발 성향을 재미있게 보여주는 오픈소스 프로젝트.

> 자세한 설계는 [`docs/설계문서.md`](docs/설계문서.md) 참고.

## 상태

MVP 파이프라인 동작 확인 완료 — GitHub 로그인 포함 (아래 "알려진 제약" 참고)

## 기술 스택

- **Frontend**: Next.js, TypeScript, Tailwind CSS
- **Backend**: Spring Boot (Java 21)
- **Worker**: Spring Boot (Java 21)
- **DB**: PostgreSQL, Redis
- **배포**: Railway

## 로컬 개발

```bash
cp .env.example .env
docker compose up -d
```

| 서비스 | URL |
|---|---|
| Frontend | http://localhost:3010 |
| Backend API | http://localhost:8090 |
| Worker | http://localhost:8081 |

각 서비스 헬스체크:

```bash
curl http://localhost:8090/actuator/health
curl http://localhost:8081/actuator/health
```

브라우저에서 http://localhost:3010 접속 후 "GitHub로 로그인"을 누르면 로그인한 계정 자신의 공개 활동을 자동으로 수집·분석해 결과 페이지를 보여준다. (분석 결과 자체는 인증 없이도 `GET /api/users/{username}/result`로 조회 가능 — 애초에 공개 GitHub 활동이라 접근 자체를 막지는 않는다.)

## README 배지

분석 결과를 자신의 GitHub 프로필(`{username}/{username}` 레포)이나 다른 레포의 README에 뱃지로 박아넣을 수 있다. DevDNA가 자동으로 뭔가를 해주는 게 아니라, **본인이 직접 아래 마크다운 한 줄을 자기 README.md에 추가하고 커밋·푸시**하면 된다 — GitHub이 README를 보여줄 때마다 이 URL로 이미지를 다시 요청해서 항상 최신 상태로 그려진다.

```markdown
![DevDNA](https://<배포한-도메인>/api/badge/{username}.svg)
```

- `{username}` 자리에 본인 GitHub username을 넣는다.
- **먼저 그 계정으로 한 번 분석을 돌려놔야 한다** (사이트에서 로그인 → 자동 분석). 분석 이력이 없으면 회색 "no data yet" 뱃지가 뜬다 (깨진 이미지 대신).
- 개발자 유형마다 뱃지 색이 다르다 (예: NIGHT OWL은 보라, BUG SLAYER는 빨강, DOCUMENTARIAN은 주황) — 유형이 바뀌면 뱃지 색도 자동으로 바뀐다.
- 로컬 개발 중에는 `https://<배포한-도메인>` 대신 `http://localhost:8090`을 써서 로컬에서만 미리 볼 수 있다 (`http://localhost:8090/api/badge/{username}.svg`). GitHub 자체에 실제로 보이게 하려면 backend를 공개 도메인에 배포해야 한다 — 아직 배포 전이라 지금은 로컬 미리보기만 가능하다.

## 알려진 제약 (MVP 현재 범위)

- **AI 자연어 분석 제외**: 설계문서 3.2절에 따라 LLM 기반 해석은 MVP 이후 범위. 지금은 규칙 기반 템플릿 문장으로 대체.
- **공유 카드는 온디맨드 SVG**: S3/R2에 미리 저장하지 않고 `GET /api/share/{username}/card` 요청 시마다 DB 데이터로 즉시 렌더링한다.
- **원본 데이터 미보관**: GitHub에서 수집한 repo/commit 원본은 저장하지 않고 분석 중에만 메모리에 두며, 최종 계산 결과(`analysis_results`)만 DB에 남긴다 (설계문서 20절 최소 수집 원칙).

## 프로젝트 구조

```text
devwrapped/
├── frontend/    # Next.js
├── backend/     # Spring Boot API 서버
├── worker/      # Spring Boot 분석 워커
├── docs/        # 설계 문서
└── docker-compose.yml
```

## 기여하기

이슈 제보, 기능 제안, PR 모두 환영합니다. 시작하기 전에 [`CONTRIBUTING.md`](CONTRIBUTING.md)를 한 번 봐주세요 (브랜치/커밋 컨벤션, 테스트 실행 방법 등).

## 라이선스

MIT

---

<div align="center">

Built by `catchmeif404` — building things nobody asked for.

</div>
