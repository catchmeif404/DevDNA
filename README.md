# DevWrapped

GitHub 공개 활동 데이터를 분석해 개발자의 코딩 패턴, 기술 스택, 활동 시간, 개발 성향을 재미있게 보여주는 오픈소스 프로젝트.

> 자세한 설계는 [`docs/설계문서.md`](docs/설계문서.md) 참고.

## 상태

MVP 파이프라인 동작 확인 완료 (GitHub 로그인 제외 — 아래 "알려진 제약" 참고)

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

브라우저에서 http://localhost:3010 접속 후 GitHub username을 입력하면 실제 GitHub 데이터를 수집·분석해 결과 페이지를 보여준다 (예: `octocat`).

## 알려진 제약 (MVP 현재 범위)

- **GitHub 로그인 미검증**: Spring Security OAuth2 코드는 구현돼 있지만(`/api/auth/github`), 실제 GitHub OAuth App을 아직 만들지 않아 로그인 자체는 테스트되지 않았다. 현재는 홈 화면에서 공개 GitHub username을 직접 입력해 로그인 없이 파이프라인을 검증하는 테스트 모드로 동작한다.
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

## 라이선스

MIT

---

<div align="center">

Built by `catchmeif404` — building things nobody asked for.

</div>
