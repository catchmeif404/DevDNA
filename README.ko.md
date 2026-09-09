<div align="center">

# DevDNA

**증거물 B. 커밋은 흔적을 남깁니다.**

[![DevDNA](https://devdna-production-1d53.up.railway.app/api/badge/catchmeif404.svg)](https://devdna.catchmeif404.com/dev/catchmeif404)

실행 중인 서비스: **https://devdna.catchmeif404.com**

최신 릴리스: **v0.6.4**

`catchmeif404`

**한국어** · [English](README.md)

</div>

---

GitHub에는 언제 커밋했는지, 무엇을 고쳤는지, 어떤 프로젝트에서 활동했는지 기록이 남습니다.
DevDNA는 이 공개 활동을 개발자 조사 기록, README용 증거물 배지, 공유 가능한 결과 리포트로
바꿉니다.

DevDNA는 로그인한 본인의 GitHub 계정만 분석합니다. 다른 사람의 계정을 익명으로 분석하거나
개발자를 순위화하지 않습니다. 점수는 활동 패턴을 설명하기 위한 지표입니다.

## 주요 기능

- GitHub OAuth 로그인 기반 자기 분석
- 10가지 개발자 프로필 분류
- README에 추가할 수 있는 SVG 증거물 배지
- 공유 가능한 개발자 리포트 카드
- 한국어와 영어 화면 및 공유 문구
- 결정론적 규칙 기반 요약 생성

## 배지 사용법

GitHub 프로필 README에 아래 마크다운을 추가하세요. `catchmeif404`를 자신의 GitHub 사용자명으로
바꾸면 됩니다.

```markdown
[![DevDNA](https://devdna-production-1d53.up.railway.app/api/badge/catchmeif404.svg)](https://devdna.catchmeif404.com/dev/catchmeif404)
```

아직 분석 결과가 없으면 깨진 이미지 대신 `AWAITING EVIDENCE` 배지가 표시됩니다.

## 기술 스택

| 영역 | 기술 |
|---|---|
| 프론트엔드 | Next.js, TypeScript, Tailwind CSS, Cloudflare Workers |
| 백엔드 | Spring Boot, Java 21, Flyway, Railway |
| 데이터 | PostgreSQL |
| 인증 | GitHub OAuth |
| 언어 | 한국어 및 영어 |

## 로컬 실행

```bash
cp .env.example .env
docker compose up -d
```

| 서비스 | 주소 |
|---|---|
| 프론트엔드 | http://localhost:3010 |
| 백엔드 API | http://localhost:8090 |

GitHub 로그인을 사용하려면 GitHub OAuth App을 만들고 `GITHUB_CLIENT_ID`와
`GITHUB_CLIENT_SECRET`을 설정하세요. 로컬 콜백 주소는
`http://localhost:8090/api/auth/github/callback`입니다.

## 현재 제한사항

- 요약은 규칙 기반 템플릿으로 생성되며, 커밋 유형 분류만 선택적으로 Claude API를 사용할 수 있습니다.
- 원본 GitHub 저장소와 커밋 데이터는 저장하지 않고 최종 분석 결과만 저장합니다.
- 분석 중 백엔드가 재시작되면 진행 중인 작업은 자동으로 재시도되지 않습니다.
- GitHub 사용자명 변경에 대한 결과 소유권 정규화가 아직 완전하지 않습니다.

<div align="center">

Built by `catchmeif404` - building things nobody asked for.

</div>
