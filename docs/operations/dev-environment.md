# Development Environment

## Purpose

이 문서는 Websidian의 로컬 및 홈랩 개발 환경 구성을 정의한다.
목적은 개발자가 같은 방식으로 프로젝트를 실행하고, 데이터베이스와 객체 스토리지를 포함한 기본 인프라를 일관되게 재현할 수 있도록 하는 것이다.

Websidian은 단순 프론트엔드 프로젝트가 아니라 Vue 프론트엔드, Spring Boot 백엔드, PostgreSQL, MinIO가 함께 동작해야 하는 구조다.
따라서 개발 환경 문서는 설치 순서, 실행 방식, 환경 변수, 데이터 지속성, 서비스 의존 관계를 명확히 설명해야 한다.

## Environment Goals

개발 환경은 다음 목표를 만족해야 한다.

- 새 환경에서도 재현 가능해야 한다.
- Docker Compose 한 번으로 핵심 서비스가 올라와야 한다.
- 데이터는 컨테이너 재시작 후에도 유지되어야 한다.
- 프론트/백엔드의 로컬 개발 경험이 불필요하게 느려지지 않아야 한다.
- 홈랩 운영 환경과 너무 다르지 않아야 한다.

## Target Environment

현재 기준 권장 개발 환경은 다음과 같다.

- Host: Ubuntu Server VM on Proxmox
- Container runtime: Docker Engine
- Orchestration: Docker Compose
- Frontend: Vue.js development server
- Backend: Spring Boot application
- Database: PostgreSQL
- Object Storage: MinIO

이 구성을 기준으로 문서와 스크립트를 맞춘다.

## Environment Strategy

초기 Websidian 개발 환경은 다음 전략을 따른다.

### 1. 애플리케이션과 인프라를 적절히 분리한다
PostgreSQL, MinIO 같은 상태 저장 서비스는 Compose로 띄우는 것이 적절하다.
프론트엔드와 백엔드는 상황에 따라 로컬 프로세스로 실행하거나 Compose로 실행할 수 있다.

### 2. 개발 모드와 운영 모드를 구분한다
개발 환경은 빠른 반복과 디버깅에 맞추고, 운영 환경은 안정성과 고정된 이미지 중심으로 맞춘다.

### 3. 환경 변수는 코드에서 분리한다
민감 정보와 실행 설정은 `.env` 또는 환경별 설정 파일로 분리한다.

### 4. persistent data는 named volume으로 유지한다
DB와 오브젝트 스토리지 데이터는 named volume으로 분리한다.

## Development Modes

Websidian은 초기 단계에서 두 가지 개발 모드를 지원하는 것이 좋다.

### Mode A: Hybrid Development
추천 기본 모드다.

- PostgreSQL, MinIO만 Docker Compose로 실행
- Frontend는 로컬 dev server로 실행
- Backend는 로컬 JVM으로 실행

장점:
- 핫 리로드와 디버깅이 빠르다
- 프론트/백엔드 개발 속도가 좋다
- IDE 연동이 쉽다

단점:
- Docker 기반 완전 재현성은 다소 낮다

### Mode B: Full Compose Development
모든 서비스를 Compose로 실행한다.

- frontend
- backend
- postgres
- minio

장점:
- 환경 재현성이 높다
- 온보딩이 쉽다
- 홈랩 운영과 유사한 흐름을 유지할 수 있다

단점:
- 초기 개발 반복 속도가 느릴 수 있다
- 로그/디버깅 경험이 로컬 프로세스보다 불편할 수 있다

초기에는 Hybrid를 기본으로 두고, Full Compose를 보조 경로로 제공하는 것이 현실적이다.

## Required Tools

개발자는 다음 도구를 준비해야 한다.

- Git
- Docker Engine
- Docker Compose plugin
- Node.js 및 npm 또는 pnpm
- Java 21 이상
- Gradle wrapper
- curl 또는 httpie
- psql 또는 DB 클라이언트 (선택)

이 목록은 README와 함께 유지한다.

## Environment Variables

환경 변수는 다음 원칙으로 관리한다.

- 실제 비밀값은 저장소에 커밋하지 않는다.
- `.env.example`에는 필요한 키 이름과 예시값만 둔다.
- 개발 환경에서는 `.env` 또는 `infra/compose/.env`를 사용할 수 있다.
- Spring Boot는 profile 기반 설정과 환경 변수 오버라이드를 함께 사용한다.

예시 항목:
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `MINIO_ROOT_USER`
- `MINIO_ROOT_PASSWORD`
- `MINIO_BUCKET`
- `APP_BASE_URL`
- `FRONTEND_PORT`
- `BACKEND_PORT`

## Data Persistence

상태 저장 데이터는 반드시 볼륨으로 분리한다.

대상:
- PostgreSQL data directory
- MinIO data directory

원칙:
- 컨테이너 재생성 시에도 데이터 유지
- 테스트용 데이터 초기화는 명시적 스크립트로만 수행
- 개발 중 실수로 영속 데이터가 날아가지 않도록 기본값은 보존으로 둔다

## Configuration Layout

권장 파일 배치는 다음과 같다.

```text
infra/
├── compose/
│   ├── compose.dev.yml
│   ├── compose.local.yml
│   └── .env.example
└── scripts/
    ├── up-dev.sh
    ├── down-dev.sh
    └── reset-dev-data.sh
```

설명:
- `compose.dev.yml`: 일반 개발 환경
- `compose.local.yml`: 로컬 오버라이드 또는 실험용
- `up-dev.sh`: 반복 실행을 단순화
- `reset-dev-data.sh`: 명시적 초기화 전용

## Startup Flow

권장 시작 흐름은 다음과 같다.

### Hybrid Development
1. `.env` 파일 준비
2. PostgreSQL, MinIO를 Compose로 기동
3. Backend를 로컬에서 실행
4. Frontend를 로컬에서 실행
5. 브라우저에서 앱 접속

### Full Compose Development
1. `.env` 파일 준비
2. Compose로 전체 서비스 기동
3. health check 확인
4. 브라우저에서 앱 접속

이 흐름은 문서와 스크립트에서 동일하게 표현해야 한다.

## Health and Verification

개발 환경이 제대로 올라왔는지 확인할 최소 체크는 다음과 같다.

- PostgreSQL 포트 연결 가능
- MinIO 콘솔 또는 API 접근 가능
- Backend health endpoint 응답 확인
- Frontend에서 API 호출 가능
- 테스트 Vault/문서 조회 성공

Compose 기반 환경이라면 healthcheck를 적극적으로 정의하는 것이 좋다.

## Logging

개발 환경 로그는 다음 수준에서 확인 가능해야 한다.

- frontend dev server log
- backend application log
- postgres container log
- minio container log

원칙:
- 로그는 우선 표준 출력으로 수집
- 디버깅을 위해 너무 과도한 파일 기반 로그 구조는 초기에는 피함
- 문제 발생 시 서비스별 로그를 쉽게 분리해 볼 수 있어야 함

## Reset Policy

개발 환경 초기화는 명시적으로만 수행한다.

초기화 대상 예시:
- DB schema reset
- test data reseed
- MinIO test objects clear

원칙:
- 일반 `up`/`down` 명령은 데이터를 지우지 않는다
- 초기화는 별도 스크립트나 별도 문서 절차를 사용한다
- 영속 데이터 삭제는 위험 작업으로 간주한다

## Recommended First Setup Policy

초기 기준 권장 정책:

- 개발 기본 모드: Hybrid
- 상태 저장 서비스: Compose
- 애플리케이션 서비스: 로컬 실행 우선
- 운영과 최대한 비슷한 네이밍과 환경 변수 유지
- 모든 핵심 설정은 README와 이 문서에 중복 없이 연결

## Open Questions

후속 결정이 필요한 항목:
- frontend/backend도 항상 Compose로 실행할지
- reverse proxy를 dev에서도 포함할지
- 개발용 seed data를 어디까지 자동화할지
- MinIO bucket bootstrap을 앱이 할지, init 스크립트가 할지

## Summary

Websidian 개발 환경은 Docker Compose 기반 인프라와 재현 가능한 설정을 중심으로 설계한다.
초기에는 PostgreSQL과 MinIO를 Compose로 운영하고, 프론트/백엔드는 로컬 실행을 우선하는 Hybrid 방식이 가장 현실적이며, 장기적으로 Full Compose 경로를 함께 제공하는 것이 좋다.