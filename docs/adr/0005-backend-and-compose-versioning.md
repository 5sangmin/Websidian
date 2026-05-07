# 0005 – Backend & Compose 버전/이미지 고정 전략

## Status

Accepted

## Context

Websidian은 홈랩 환경(Proxmox + Ubuntu + Docker/Compose)에서 장기간 혼자 개발하고 운영하는 프로젝트다.  
개발/운영 환경에서 재현성이 중요하며, Docker Compose와 Spring Boot 기반 백엔드가 핵심 인프라 구성 요소다.

특히 다음과 같은 요구가 있다.

- 동일한 설정으로 다시 올렸을 때, 가능한 한 같은 동작을 보장하고 싶다.
- “latest” 태그처럼 시간이 흐르면서 의미가 변하는 참조는 피하고 싶다.
- Spring Boot 백엔드의 기본 스택(Java 버전, Gradle, Boot 버전 등)은 쉽게 바꾸지 않고, 몇 년 단위로 안정적인 조합을 유지하고 싶다.

## Decision

### 1. Docker Compose 이미지 버전 고정

- Docker Compose에서 사용하는 서비스(PostgreSQL, MinIO 등)는 **항상 명시적인 버전 태그를 사용한다.**
- `latest` 태그는 사용하지 않는다.
- PostgreSQL:
  - 메인 개발/운영 DB는 **PostgreSQL 16 계열**을 기본으로 한다.
  - Compose 예시에서는 `postgres:16.13`처럼 **메이저/마이너/패치까지 고정된 태그**를 사용한다.
- MinIO:
  - MinIO는 공식 **릴리즈 태그(예: `RELEASE.2025-09-07T16-13-09Z`)**를 사용한다.
  - 보안 패치가 포함된 안정 릴리즈를 우선적으로 선택하며, 명시적인 release tag를 `.env.example`에 기록한다.
- Compose 파일은 이미지 태그를 직접 하드코딩하지 않고, `infra/compose/.env`의 변수를 참조한다.

예시 (`infra/compose/.env.example`):

```env
POSTGRES_IMAGE=postgres:16.13
MINIO_IMAGE=minio/minio:RELEASE.2025-10-15T17-29-55Z

POSTGRES_DB=websidian-pgsql
POSTGRES_USER=websidian_pgsql
POSTGRES_PASSWORD=change_me
POSTGRES_PORT=5432

MINIO_ROOT_USER=websidian_minio
MINIO_ROOT_PASSWORD=change_me
MINIO_API_PORT=9000
MINIO_CONSOLE_PORT=9001
MINIO_BUCKET=websidian_minio_dev
```

예시 (`infra/compose/compose.dev.yml` 일부):

```yaml
services:
  postgres:
    image: ${POSTGRES_IMAGE}
    ...

  minio:
    image: ${MINIO_IMAGE}
    ...
```

### 2. Backend Spring Boot 프로젝트 기본 설정 (Initializr)

Spring Boot 백엔드 프로젝트는 `start.spring.io`를 사용해 생성하며, 다음 설정을 기본값으로 고정한다.

- Project: **Gradle – Groovy**
- Language: **Java**
- Spring Boot: **3.5.14** (안정 릴리즈 라인)
- Group: `com.websidian`
- Artifact: `backend`
- Name: `backend`
- Packaging: `Jar`
- Configuration: **Properties**
- Java: **17**

초기 의존성 구성:

- Spring Web
- Spring Data JPA
- PostgreSQL Driver
- Spring Boot Actuator

Lombok은 기본에서 제외하고, 필요시 이후 이슈에서 별도 도입 여부를 논의한다.

### 3. 환경 변수 설계와의 연계

- Docker Compose에서 사용하는 이미지 태그 및 주요 설정은 `infra/compose/.env.example`에 명시적으로 기록한다.[file:14]
- 애플리케이션에서 사용하는 DB URL, 계정, 패스워드는 루트 `.env.example`에 기록하고, Spring Boot `application.properties`에서는 `${ENV:default}` 패턴으로 참조한다.
- Naming policy는 다음을 따른다.[file:14]
  - Key: 대문자 snake_case, prefix로 범위 표시 (`POSTGRES_*`, `MINIO_*`, `APP_*`, `VITE_*` 등)
  - Value: 기본은 언더바(`_`), **URL/엔드포인트 리소스에 한해 하이픈(`-`) 허용**

## Consequences

### Positive

- Docker 이미지 버전과 Spring Boot/Java 조합이 문서화되어, **몇 년 뒤에도 동일 환경을 비교적 쉽게 재현**할 수 있다.
- `latest` 사용으로 인한 예측 불가능한 변경을 피할 수 있고, 릴리즈 노트/보안 공지를 기준으로 의도적으로 업그레이드할 수 있다.
- Initializr 설정이 고정되어, 새로운 backend 모듈이나 재생성 시에도 스택 일관성을 유지할 수 있다.
- `infra/compose/.env.example` 업데이트만으로 이미지 버전 업그레이드를 관리할 수 있다.

### Negative / Trade-offs

- 보안 패치나 버그 수정이 포함된 새 버전이 나와도, **수동으로 버전 태그를 올려 주지 않으면 자동으로 반영되지 않는다.**
- 모든 업그레이드는 “버전 변경 → Compose 재기동 → 기본 동작 확인”이라는 수동 절차를 요구한다.
- Spring Boot / Java / Gradle 조합이 고정되어 있기 때문에, 새로운 기능이나 개선을 쓰려면 별도의 ADR 또는 이슈로 “기본 스택 업그레이드”를 다뤄야 한다.

## Future Work

- PostgreSQL/MinIO 버전 업그레이드 정책 정의
  - 예: “연 1회, 보안 패치 또는 LTS 변경이 있는 경우에만 업그레이드”
- Spring Boot/Java 버전 업그레이드 기준 정의
  - 예: “Java LTS + Spring Boot 마이크로 버전만 주기적으로 업데이트”
- 이미지 태그를 digest까지 고정하는 여부 검토 (운영/배포 환경 안정화 시점에 재논의).