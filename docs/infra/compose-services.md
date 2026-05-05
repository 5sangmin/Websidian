# Compose Services

## Purpose

이 문서는 Websidian의 Docker Compose 서비스 구성을 정의한다.
목적은 어떤 서비스를 Compose에 포함할지, 각 서비스의 책임은 무엇인지, 어떤 네트워크와 볼륨과 환경 변수가 필요한지를 명확히 하는 것이다.

Compose 파일은 단순 실행 스크립트가 아니라, 개발 및 홈랩 운영의 기본 토폴로지를 표현하는 문서이기도 하다.
따라서 서비스 수를 최소화하되, 역할은 분명히 나누는 것이 중요하다.

## Service Design Principles

### 1. 서비스는 목적 중심으로 나눈다
하나의 서비스는 하나의 책임을 가진다.
예를 들어 DB, 객체 스토리지, 프론트엔드, 백엔드는 별도 서비스로 둔다.

### 2. 상태 저장 서비스는 명확히 분리한다
PostgreSQL과 MinIO는 반드시 별도 볼륨과 별도 설정을 가진다.

### 3. dev와 prod 구성을 완전히 같게 만들 필요는 없지만, 이름과 역할은 최대한 일치시킨다.

### 4. 민감 정보는 Compose 파일에 직접 박아 넣지 않는다
환경 변수 파일이나 별도 관리 방식을 사용한다.

## Initial Service Set

초기 Websidian Compose 서비스는 다음 구성을 권장한다.

- `postgres`
- `minio`
- `backend`
- `frontend`
- 선택: `proxy`
- 선택: `minio-init`

초기 Hybrid 모드에서는 `postgres`와 `minio`만 Compose에서 항상 사용하고, Full Compose 모드에서는 나머지도 포함할 수 있다.

## Core Services

### `postgres`
역할:
- 애플리케이션 메타데이터 저장

책임:
- vaults, documents, document_versions, links, files metadata, tags, permissions 저장

권장 설정:
- 공식 PostgreSQL 이미지 사용
- named volume 사용
- healthcheck 정의
- 포트 노출은 개발 환경에서만 허용

필요 환경 변수 예시:
- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`

볼륨 예시:
- `postgres_data:/var/lib/postgresql/data`

### `minio`
역할:
- 실제 파일 원본 저장

책임:
- Markdown 원본
- HTML 원본
- 이미지
- PDF
- 기타 업로드 파일

권장 설정:
- named volume 사용
- API 포트와 콘솔 포트 구분
- 루트 계정 정보는 환경 변수로 주입
- bucket 정책은 앱 또는 init 작업에서 설정 가능

필요 환경 변수 예시:
- `MINIO_ROOT_USER`
- `MINIO_ROOT_PASSWORD`

볼륨 예시:
- `minio_data:/data`

### `backend`
역할:
- Spring Boot API 실행

책임:
- 문서/파일/권한 API
- DB 연동
- MinIO 연동
- 링크 분석 결과 저장
- 상태/버전 관리

권장 설정:
- `build` 또는 prebuilt image 사용 가능
- DB와 MinIO에 의존
- health endpoint 제공
- 환경별 profile 지원

필요 환경 변수 예시:
- `SPRING_PROFILES_ACTIVE`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `MINIO_ENDPOINT`
- `MINIO_ACCESS_KEY`
- `MINIO_SECRET_KEY`

### `frontend`
역할:
- Vue 애플리케이션 제공

책임:
- 문서 화면 렌더링
- 라우팅
- HTML 임베드 표시
- API 호출

권장 설정:
- 개발에서는 dev server
- 운영성 테스트 시 정적 빌드 또는 containerized frontend
- API base URL은 환경 변수로 주입

필요 환경 변수 예시:
- `VITE_API_BASE_URL`

### `proxy` (optional)
역할:
- 단일 진입점 제공
- 라우팅 및 reverse proxy 처리

사용 시점:
- Full Compose 개발
- 운영과 유사한 테스트
- HTTPS 또는 단일 도메인 라우팅이 필요한 경우

후보:
- Caddy
- Nginx

초기 Hybrid에서는 필수는 아니다.

### `minio-init` (optional)
역할:
- bucket 생성
- 초기 정책 설정
- 개발용 초기 객체 준비

특징:
- 일회성 init job 형태가 적절
- 매번 실행되는 장기 서비스로 둘 필요는 없다

## Suggested Service Profiles

### Profile A: Minimal Infra
- postgres
- minio

용도:
- 로컬 frontend/backend 개발
- 가장 빠른 개발 반복

### Profile B: Full App
- postgres
- minio
- backend
- frontend

용도:
- 재현 가능한 전체 개발 환경
- 홈랩 테스트

### Profile C: Full App with Proxy
- postgres
- minio
- backend
- frontend
- proxy

용도:
- 운영과 유사한 라우팅 테스트
- 단일 진입점 검증

이런 식의 분리는 Compose 파일 분리 또는 profile 사용으로 표현할 수 있다.

## Networking

Compose 네트워크는 기본적으로 내부 통신을 우선한다.

원칙:
- 서비스 간 통신은 서비스명으로 수행
- DB와 MinIO는 외부에 불필요하게 노출하지 않는다
- 개발 시 필요한 포트만 호스트에 노출한다
- frontend -> backend -> postgres/minio 흐름을 기본으로 둔다

예시 서비스명:
- `postgres`
- `minio`
- `backend`
- `frontend`

## Volumes

초기 named volume 권장 목록:

- `postgres_data`
- `minio_data`
- 선택: `frontend_node_modules`
- 선택: `backend_gradle_cache`

원칙:
- 데이터 보존이 필요한 것은 named volume
- 소스 코드는 bind mount 가능
- 캐시성 데이터는 성능 개선용으로 선택적 사용

## Environment Variable Policy

Compose 환경 변수 정책은 다음과 같다.

- `.env.example`는 저장소에 포함
- 실제 `.env`는 커밋 금지
- 운영 비밀값은 별도 관리
- 값의 우선순위와 주입 위치를 문서화
- build-time 값과 run-time 값을 구분한다.

## Healthchecks and Startup Ordering

상태 저장 서비스는 healthcheck를 가지는 것이 좋다.

예:
- postgres: `pg_isready`
- backend: `/actuator/health`
- minio: 간단한 HTTP readiness 확인 가능

주의:
- `depends_on`만으로 완전한 readiness가 보장되지는 않으므로, health 기반 대기 전략을 고려한다.

## Suggested First Compose Policy

초기 Compose 설계 기준은 다음과 같다.

- 기본 compose: `postgres`, `minio`
- 확장 compose: `backend`, `frontend`
- 선택 compose 또는 profile: `proxy`, `minio-init`
- named volume 필수
- 환경 변수 분리 필수
- healthcheck 정의 권장

## What Not To Do

피해야 할 구성:

- 모든 서비스를 하나의 커스텀 컨테이너에 몰아넣기
- DB 데이터를 bind mount 경로 난잡하게 관리하기
- Compose 파일에 비밀번호 하드코딩하기
- dev/prod 용도를 명확히 나누지 않은 채 설정을 과도하게 뒤섞기
- readiness 고려 없이 앱이 DB보다 먼저 뜨게 두기

## Open Questions

후속 결정이 필요한 항목:
- proxy를 초기부터 포함할지
- backend/frontend 이미지를 dev에서 매번 빌드할지
- MinIO init bucket 생성을 앱에서 담당할지 별도 잡으로 둘지
- Compose profiles를 쓸지, 파일 분리를 택할지

## Summary

Websidian의 Compose 서비스는 PostgreSQL, MinIO, Backend, Frontend를 중심으로 구성하고, 필요 시 Proxy와 Init Job을 추가하는 방향이 적절하다.
핵심은 서비스별 책임을 분리하고, 상태 저장 데이터는 볼륨으로 보존하며, 환경 변수와 healthcheck를 명확히 관리하는 것이다.