# Environment Variables

## Purpose

이 문서는 Websidian에서 사용하는 환경 변수의 목적, 범위, 관리 원칙을 정의한다.
목적은 설정값과 비밀값을 코드에서 분리하고, Docker Compose와 애플리케이션 실행 시 어떤 값이 어디에서 주입되는지 명확히 하는 것이다.

Websidian은 frontend, backend, PostgreSQL, MinIO를 함께 사용하는 구조이므로, 환경 변수 설계가 곧 운영 안정성과 보안 수준에 직접 영향을 준다.
특히 Compose 기반 프로젝트에서는 `.env` 파일과 서비스별 `environment` 설정의 역할을 구분하는 것이 중요하다.

## Principles

### 1. 비밀값은 코드에 넣지 않는다
비밀번호, 액세스 키, 토큰 같은 값은 저장소에 하드코딩하지 않는다.

### 2. `.env.example`는 문서다
실제 값을 담지 않고, 필요한 키와 예시만 담는다.

### 3. 환경별 값을 분리한다
개발, 테스트, 운영은 서로 다른 값을 사용할 수 있다.

### 4. 설정값과 비밀값을 구분한다
포트, 호스트명, bucket 이름 같은 일반 설정과 패스워드, secret key 같은 민감 정보는 성격이 다르므로 구분해서 관리한다.

### 5. 변수 이름은 명확해야 한다
축약형보다 의미가 드러나는 이름을 사용한다.

## Variable Sources

Websidian에서 환경 변수는 다음 경로로 들어올 수 있다.

- `.env`
- Compose 파일의 `environment`
- Compose 실행 시 `--env-file`
- Spring Boot profile 설정과 OS 환경 변수
- frontend build/runtime 환경 변수

초기 우선순위는 다음처럼 단순하게 가져간다.

1. 실제 개발용 `.env`
2. Compose의 `${VARIABLE}` 치환
3. backend/frontend 앱에서 환경 변수 읽기
4. `.env.example`는 참고용만 사용

## File Policy

권장 파일 정책은 다음과 같다.

- 커밋 가능:
  - `.env.example`
  - `infra/compose/.env.example`
- 커밋 금지:
  - `.env`
  - `.env.local`
  - `.env.dev`
  - `.env.prod`
- `.gitignore`에 포함:
  - `.env`
  - `.env.*` (단, `.env.example` 제외)

## Variable Groups

Websidian의 환경 변수는 크게 다음 그룹으로 나눈다.

### 1. Global App Variables
시스템 전체에서 공통으로 쓰는 값

예:
- `APP_NAME`
- `APP_ENV`
- `APP_BASE_URL`

### 2. Frontend Variables
Vue 애플리케이션이 사용하는 값

예:
- `VITE_API_BASE_URL`
- `VITE_APP_TITLE`

주의:
- 프론트엔드 변수는 브라우저로 노출될 수 있으므로, 비밀값을 두면 안 된다.

### 3. Backend Variables
Spring Boot 애플리케이션이 사용하는 값

예:
- `SPRING_PROFILES_ACTIVE`
- `SERVER_PORT`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `MINIO_ENDPOINT`
- `MINIO_ACCESS_KEY`
- `MINIO_SECRET_KEY`
- `MINIO_BUCKET`

### 4. Database Variables
PostgreSQL 서비스용 값

예:
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `POSTGRES_PORT`

### 5. Object Storage Variables
MinIO 서비스용 값

예:
- `MINIO_ROOT_USER`
- `MINIO_ROOT_PASSWORD`
- `MINIO_API_PORT`
- `MINIO_CONSOLE_PORT`
- `MINIO_BUCKET`

## Suggested Variable Catalog

초기 기준 권장 변수 목록은 다음과 같다.

### Global
- `APP_NAME`
- `APP_ENV`
- `APP_BASE_URL`

### Frontend
- `FRONTEND_PORT`
- `VITE_API_BASE_URL`
- `VITE_APP_TITLE`

### Backend
- `BACKEND_PORT`
- `SPRING_PROFILES_ACTIVE`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `MINIO_ENDPOINT`
- `MINIO_ACCESS_KEY`
- `MINIO_SECRET_KEY`
- `MINIO_BUCKET`

### PostgreSQL
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `POSTGRES_PORT`

### MinIO
- `MINIO_ROOT_USER`
- `MINIO_ROOT_PASSWORD`
- `MINIO_API_PORT`
- `MINIO_CONSOLE_PORT`

## Example `.env.example`

아래는 예시 형식이다.

```env
APP_NAME=websidian
APP_ENV=dev
APP_BASE_URL=http://localhost

FRONTEND_PORT=5173
VITE_API_BASE_URL=http://localhost:8080/api/v1
VITE_APP_TITLE=Websidian

BACKEND_PORT=8080
SPRING_PROFILES_ACTIVE=dev
DB_URL=jdbc:postgresql://postgres:5432/websidian
DB_USERNAME=websidian
DB_PASSWORD=change-me

POSTGRES_DB=websidian
POSTGRES_USER=websidian
POSTGRES_PASSWORD=change-me
POSTGRES_PORT=5432

MINIO_ENDPOINT=http://minio:9000
MINIO_ACCESS_KEY=minioadmin
MINIO_SECRET_KEY=change-me
MINIO_BUCKET=websidian-dev

MINIO_ROOT_USER=minioadmin
MINIO_ROOT_PASSWORD=change-me
MINIO_API_PORT=9000
MINIO_CONSOLE_PORT=9001
```

이 값들은 예시일 뿐 실제 비밀값으로 사용하면 안 된다.

## Naming Policy

환경 변수 이름은 다음 규칙을 따른다.

- 대문자 snake_case 사용
- prefix로 범위를 드러냄
- 의미가 분명한 이름 사용
- 프론트엔드 노출 변수는 `VITE_` prefix 사용

예:
- 좋음: `MINIO_SECRET_KEY`
- 나쁨: `SECRET`
- 좋음: `POSTGRES_PASSWORD`
- 나쁨: `DB_PASS`

## Secret Handling Policy

초기 Websidian은 개인 홈랩 환경을 전제로 하지만, 다음 원칙을 지킨다.

- 실제 비밀값은 `.env`에 둔다
- `.env`는 Git에 커밋하지 않는다
- 운영 비밀값은 개발값과 분리한다
- 예시 비밀번호를 운영에 그대로 쓰지 않는다
- 민감도가 높아지면 Docker secrets 또는 별도 secret manager를 검토한다.

## Compose Integration Policy

Compose 파일에서는 환경 변수를 직접 박아 넣지 않고 참조한다.

예시:
- `${POSTGRES_DB}`
- `${POSTGRES_USER}`
- `${POSTGRES_PASSWORD}`

원칙:
- 공통값은 `.env`에서 가져온다
- 서비스별로 필요한 값만 주입한다
- frontend에는 공개 가능한 값만 넣는다

## Backend Integration Policy

Spring Boot는 profile과 환경 변수 오버라이드를 함께 사용한다.

원칙:
- 기본 설정은 `application.yml`
- 환경별 설정은 `application-dev.yml`, `application-prod.yml`
- 비밀값/가변값은 환경 변수로 주입
- DB/MinIO 연결 정보는 환경 변수 우선

## Validation Policy

환경 변수 누락은 가능한 빨리 실패해야 한다.

원칙:
- 필수 값이 없으면 앱 시작 시 명확한 에러 출력
- 문서에 필수/선택 변수를 구분
- `.env.example`를 항상 최신 상태로 유지

## Open Questions

후속 결정이 필요한 항목:
- 운영 환경에서 Docker secrets를 도입할지
- frontend runtime config를 별도 JSON로 분리할지
- secret rotation 정책을 어디까지 둘지
- CI 환경 변수 주입 방식을 어떻게 설계할지

## Summary

Websidian의 환경 변수는 `.env` 기반 개발 흐름과 `.env.example` 기반 문서화를 중심으로 관리한다.
핵심은 비밀값을 코드와 Compose 파일에서 분리하고, frontend/backend/postgres/minio 각각의 책임에 맞는 변수 집합을 명확히 나누는 것이다.