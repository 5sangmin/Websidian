# Repository Structure

## Purpose

이 문서는 Websidian의 monorepo 구조를 정의한다.
목표는 프론트엔드, 백엔드, 인프라, 문서, DB 관련 자산을 한 저장소 안에서 일관되게 관리하면서도, 각 계층의 책임을 명확히 나누는 것이다.

Websidian은 개인 프로젝트이지만 다음 요소를 함께 다뤄야 한다.

- Vue 기반 프론트엔드
- Spring Boot 기반 백엔드
- PostgreSQL / MinIO 연동
- Docker Compose 기반 개발 환경
- Docs-as-Code 문서
- ADR 및 아키텍처 문서

따라서 저장소 구조는 “모든 것을 한 곳에 둔다”와 “책임은 분리한다”를 동시에 만족해야 한다.

## Principles

저장소 구조는 다음 원칙을 따른다.

### 1. 문서와 코드를 함께 관리한다
문서, ADR, 코드, 인프라 설정을 같은 Git 히스토리 안에서 관리한다.

### 2. 런타임 경계를 구조에 반영한다
프론트엔드, 백엔드, 인프라, 데이터 계층은 폴더 수준에서도 구분한다.

### 3. 공통 자산은 명시적으로 분리한다
공통 타입, API 스키마, 예제 데이터, 재사용 가능한 설정은 별도 공유 영역으로 둘 수 있게 한다.

### 4. 지금은 단순하게, 나중에는 확장 가능하게
초기 구조는 단순해야 하지만, 나중에 테스트, 패키지 분리, CI/CD 확장에 대응할 수 있어야 한다.

## Top-Level Structure

초기 Websidian 저장소의 권장 구조는 다음과 같다.

```text
websidian/
├── README.md
├── .gitignore
├── .editorconfig
├── .env.example
├── docs/
├── frontend/
├── backend/
├── infra/
├── db/
├── scripts/
└── .github/
```

## Directory Responsibilities

### `docs/`
Docs-as-Code 문서를 저장한다.

포함 대상:
- 제품 비전
- MVP 범위
- 아키텍처 문서
- ADR
- 운영 가이드
- API 개요 문서
- DB 설계 문서

예시 구조:

```text
docs/
├── README.md
├── vision/
├── architecture/
├── adr/
├── backend/
├── frontend/
├── database/
└── operations/
```

### `frontend/`
Vue 애플리케이션을 둔다.

역할:
- 문서 보기 UI
- Vault / 문서 라우팅
- HTML 임베드 컨테이너
- 관리 화면
- 그래프 뷰
- 공통 컴포넌트와 상태 관리

권장 하위 구조:

```text
frontend/
├── package.json
├── vite.config.ts
├── src/
│   ├── app/
│   ├── pages/
│   ├── features/
│   ├── components/
│   ├── router/
│   ├── stores/
│   ├── api/
│   ├── types/
│   └── utils/
└── public/
```

설명:
- `app/`: 전역 설정, 부트스트랩
- `pages/`: 라우트 단위 화면
- `features/`: Vault, document, graph 같은 기능 단위 코드
- `components/`: 공통 UI 컴포넌트
- `api/`: 백엔드 API 클라이언트
- `types/`: 프론트엔드 타입 정의
- `utils/`: 범용 유틸리티

### `backend/`
Spring Boot 애플리케이션을 둔다.

역할:
- 문서/파일/권한 API 제공
- Markdown 처리
- HTML 문서 메타데이터 관리
- PostgreSQL/MinIO 연동
- 검색 기초 처리
- 인증 및 권한

권장 하위 구조:

```text
backend/
├── build.gradle
├── settings.gradle
└── src/
    ├── main/
    │   ├── java/
    │   │   └── com/websidian/
    │   │       ├── common/
    │   │       ├── vault/
    │   │       ├── document/
    │   │       ├── file/
    │   │       ├── link/
    │   │       ├── auth/
    │   │       └── infrastructure/
    │   └── resources/
    │       ├── application.yml
    │       └── db/
    └── test/
```

설명:
- `common/`: 공통 예외, 응답 포맷, 유틸
- `vault/`: Vault 도메인
- `document/`: 문서 및 버전 도메인
- `file/`: 파일 메타데이터와 객체 저장소 연동
- `link/`: 문서 링크/임베드 관계
- `auth/`: 사용자와 권한
- `infrastructure/`: 외부 시스템 연동 구현체

### `infra/`
개발 및 운영 환경 구성을 둔다.

포함 대상:
- Docker Compose
- reverse proxy 설정
- local/dev/prod 환경 샘플
- 모니터링/로깅 설정 초안

예시 구조:

```text
infra/
├── compose/
│   ├── compose.dev.yml
│   ├── compose.local.yml
│   └── .env.example
├── proxy/
│   └── Caddyfile
└── monitoring/
```

### `db/`
데이터베이스 관련 자산을 둔다.

포함 대상:
- ERD 초안
- SQL 스케치
- 마이그레이션 전략 문서
- 샘플 데이터
- 인덱스/검색 설계 메모

예시 구조:

```text
db/
├── README.md
├── erd/
├── migrations/
└── seeds/
```

### `scripts/`
개발 보조 스크립트를 둔다.

예시:
- 로컬 초기화 스크립트
- 테스트 데이터 적재
- lint / format helper
- 개발환경 bootstrap

### `.github/`
GitHub 운영 자산을 둔다.

포함 대상:
- Issue template
- PR template
- Actions workflow
- CODEOWNERS (필요 시)

## Suggested Growth Path

초기에는 단순 구조로 시작하고, 필요할 때만 세분화한다.

### Phase 1
- `frontend/`
- `backend/`
- `infra/`
- `docs/`

### Phase 2
- `db/`
- `scripts/`
- `.github/`

### Phase 3
공유 패키지가 필요해지면 아래 구조를 추가 검토한다.

```text
packages/
├── shared-types/
├── api-contract/
└── ui-kit/
```

이 단계는 프론트/백엔드 간 타입 공유, API 계약 자동화, 재사용 가능한 UI 컴포넌트가 필요할 때 도입한다.

## Naming Rules

저장소 내부 명명 규칙은 다음을 따른다.

- 폴더명은 소문자-kebab-case 또는 도메인명 사용
- 문서 파일은 의미가 드러나는 이름 사용
- ADR은 `NNNN-short-title.md`
- 환경 파일 샘플은 `.env.example`
- Compose 파일은 용도 기반 이름 사용 (`compose.dev.yml`, `compose.prod.yml`)

## What Not To Do

초기 단계에서 피할 구조는 다음과 같다.

- 프론트/백/문서를 루트에 무질서하게 섞어 두기
- `utils`, `misc`, `temp` 같은 불명확한 폴더 남발
- 실제로 필요하지도 않은 `packages/`, `libs/`, `modules/`를 미리 과도하게 만드는 것
- 구현 경계와 무관한 기술명 중심 폴더 나열

## Summary

Websidian의 저장소 구조는 monorepo를 유지하되, 프론트엔드, 백엔드, 인프라, DB, 문서를 책임 기준으로 분리하는 방향을 따른다.
핵심은 “하나의 저장소”보다 “명확한 경계”이며, 지금은 단순하게 시작하되 나중에 shared package와 CI/CD 구조로 확장할 수 있게 설계하는 것이다.