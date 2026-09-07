# DevWrapped

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

분석 결과를 자신의 GitHub 프로필/레포 README에 뱃지로 붙일 수 있다:

```markdown
![DevWrapped](http://localhost:8090/api/badge/{username}.svg)
```

아직 분석한 적 없는 username이면 "no data yet" 회색 뱃지가 뜬다 (깨진 이미지 대신).

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
