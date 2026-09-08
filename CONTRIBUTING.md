# Contributing to DevDNA

DevDNA는 오픈소스 프로젝트입니다. 이슈 제보, 기능 제안, PR 모두 환영합니다.

## 시작하기

로컬 개발 환경 세팅은 [`README.md`의 "로컬 개발"](README.md#로컬-개발)을 참고하세요. 요약:

```bash
cp .env.example .env   # GitHub OAuth App 정보 채우기
docker compose up -d
```

전체 아키텍처(잡 파이프라인, 패키지 구조, 데이터 모델)는 [`CLAUDE.md`](CLAUDE.md)에 정리되어 있습니다. 원래 설계 의도는 [`docs/설계문서.md`](docs/설계문서.md)를 참고하세요 — 다만 이 문서는 구현보다 먼저 작성된 로드맵이라, 실제 코드와 다른 부분이 있을 수 있습니다. 이 경우 코드와 `CLAUDE.md`가 우선합니다.

## 브랜치 & 커밋

- `main`에서 브랜치를 따서 작업 후 PR을 올려주세요.
- 커밋 메시지는 [Conventional Commits](https://www.conventionalcommits.org/) 스타일을 권장합니다 (`feat:`, `fix:`, `docs:`, `refactor:` 등).
- 하나의 PR/커밋은 하나의 논리적 변경 단위로 유지해주세요.

## 코드 스타일

- **Backend (Spring Boot)**: 패키지는 기능 단위로 구성됩니다 (`auth`, `analysis`, `collector`, `scoring` 등). 새 패키지를 만들 땐 `package-info.java`에 한 줄 설명을 남겨주세요 — 기존 패키지들이 전부 그렇게 되어 있습니다.
- **Frontend (Next.js)**: `src/lib/`에 백엔드 API 호출을 모아두고, 컴포넌트에서 직접 `fetch`를 호출하지 않습니다.
- 과도한 추상화나 지금 당장 필요 없는 설정 옵션은 지양합니다 — 이 프로젝트는 MVP 단계입니다.

## 테스트

PR을 올리기 전에 관련 테스트를 돌려주세요:

```bash
# backend
cd backend && ./gradlew test

# frontend
cd frontend && npm run lint && npx tsc --noEmit
```

아직 CI가 붙어있지 않으니, PR 설명에 로컬에서 어떤 테스트/확인을 했는지 적어주시면 리뷰가 빨라집니다.

## 이슈 리포트

버그를 발견했다면 재현 방법과 함께 이슈를 남겨주세요. 기능 제안은 어떤 문제를 해결하고 싶은지부터 설명해주시면 좋습니다 — 설계문서(`docs/설계문서.md`)에 이미 있는 "MVP 이후" 범위 항목(AI 자연어 분석, GitLab 지원 등)이라면 그 사실도 같이 언급해주세요.

## 보안 이슈

GitHub OAuth 토큰, 자격증명 등 보안과 관련된 문제를 발견했다면 공개 이슈 대신 저장소 관리자에게 직접 연락해주세요.
