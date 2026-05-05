# Backend Module Structure

## Purpose

이 문서는 Websidian 백엔드의 모듈 구조와 패키지 원칙을 정의한다.
목적은 Spring Boot 프로젝트를 단순한 controller/service/repository 폴더 나열로 키우는 대신, 도메인 경계를 기준으로 구조화된 모듈형 모놀리스에 가깝게 설계하는 것이다.

Websidian 백엔드는 문서, 파일, 링크, 버전, 권한, 저장소 연동까지 함께 다룬다.
이런 시스템은 기술 계층만 기준으로 폴더를 나누면 시간이 갈수록 관련 코드가 흩어지기 쉽다.

## Structure Principles

### 1. 패키지는 기술보다 도메인을 우선한다
`document`, `vault`, `file`, `auth` 같은 비즈니스 모듈이 먼저 오고, 그 안에서 controller, service, repository를 둔다.

### 2. 모듈 경계를 느슨하게 넘나들지 않는다
한 모듈의 내부 구현을 다른 모듈이 직접 참조하지 않도록 주의한다.
가능하면 공개 서비스 인터페이스나 애플리케이션 서비스만 통해 접근한다.

### 3. 공통 코드는 정말 공통일 때만 분리한다
무분별한 `common` 확장은 오히려 의존성을 흐린다.
전역적으로 재사용되는 예외, 응답 래퍼, 설정 정도만 분리한다.

### 4. 지금은 모듈형 모놀리스
초기 Websidian은 마이크로서비스가 아니라 하나의 Spring Boot 애플리케이션으로 시작한다.
대신 코드 구조는 나중에 분리 가능하도록 모듈 기준으로 설계한다.

## Recommended Top-Level Structure

```text
backend/
├── build.gradle
├── settings.gradle
└── src/
    ├── main/
    │   ├── java/com/websidian/
    │   │   ├── WebsidianApplication.java
    │   │   ├── common/
    │   │   ├── vault/
    │   │   ├── document/
    │   │   ├── file/
    │   │   ├── link/
    │   │   ├── tag/
    │   │   ├── auth/
    │   │   └── infrastructure/
    │   └── resources/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       ├── application-prod.yml
    │       └── db/migration/
    └── test/
```

## Directory Responsibilities

### `common/`
전역 공통 코드를 둔다.

포함 대상:
- 공통 예외
- 에러 코드
- API 응답 래퍼
- 공통 유틸리티
- base entity
- 공통 설정 보조 클래스

주의:
- 도메인 규칙을 `common`으로 빼지 않는다.
- 특정 모듈에 속하는 코드는 원래 모듈에 남긴다.

### `vault/`
Vault 관리 모듈이다.

책임:
- Vault 생성/조회/수정
- 대표 문서 설정
- Vault visibility 정책
- Vault 소유자 관리

예시 내부 구조:
```text
vault/
├── api/
├── application/
├── domain/
├── infrastructure/
└── dto/
```

### `document/`
문서 관리의 핵심 모듈이다.

책임:
- 문서 생성/조회/수정
- Markdown / HTML 문서 타입 처리
- 문서 버전 관리
- 문서 상태 관리
- 대표 문서/게시 문서 규칙 검증

예시 내부 구조:
```text
document/
├── api/
├── application/
├── domain/
├── infrastructure/
└── dto/
```

세부 설명:
- `api/`: REST controller
- `application/`: use case, application service
- `domain/`: entity, domain service, business rule
- `infrastructure/`: JPA repository adapter, persistence implementation
- `dto/`: request/response DTO

### `file/`
파일 메타데이터와 객체 스토리지 연동을 담당한다.

책임:
- 파일 업로드/조회
- MinIO object key 관리
- 파일 메타데이터 저장
- orphan file 처리 기반 제공

### `link/`
문서 링크 및 임베드 관계를 담당한다.

책임:
- 내부 링크 저장
- 백링크 조회
- embed relation 저장
- 링크 파싱 결과 반영

이 모듈은 document와 밀접하지만, 그래프 탐색과 관계 조회가 커질 수 있으므로 분리한다.

### `tag/`
태그 관리 모듈이다.

책임:
- 태그 생성/정규화
- 문서-태그 연결
- 태그 기반 탐색

### `auth/`
인증과 권한 모듈이다.

책임:
- 사용자 인증
- 권한 검증
- 관리자/소유자 접근 판단
- 향후 토큰/세션 정책 수용

MVP에서는 얇게 시작할 수 있다.

### `infrastructure/`
외부 시스템 연동의 공통 인프라를 둔다.

예시:
- MinIO client config
- security config
- Jackson config
- persistence config
- logging config

주의:
- 특정 도메인에 강하게 묶인 구현은 해당 모듈 내부 `infrastructure/`에 남길 수 있다.
- truly global infrastructure만 여기에 둔다.

## Inside a Module

각 모듈 내부는 아래 역할로 나누는 것을 기본으로 한다.

### `api/`
REST controller와 request mapping을 둔다.
원칙:
- controller는 얇게 유지
- 비즈니스 로직을 직접 담지 않음

### `application/`
애플리케이션 서비스와 유스케이스를 둔다.

역할:
- 트랜잭션 경계
- 모듈 내부 도메인 호출 조합
- 외부 모듈과의 협력 orchestration

예:
- `CreateDocumentService`
- `PublishDocumentService`
- `SetVaultEntryDocumentService`

### `domain/`
핵심 도메인 모델을 둔다.

포함 대상:
- entity
- value object
- domain service
- domain rule
- enum

예:
- `Document`
- `DocumentVersion`
- `DocumentStatus`
- `DocumentType`

### `infrastructure/`
영속성 및 외부 연동 구현을 둔다.

포함 대상:
- JPA entity / mapper
- repository implementation
- MinIO adapter
- parser adapter

### `dto/`
요청/응답용 DTO를 둔다.
API 계약을 domain 모델과 분리한다.

## Example: Document Module

예시 구조:

```text
document/
├── api/
│   └── DocumentController.java
├── application/
│   ├── CreateDocumentService.java
│   ├── GetDocumentService.java
│   ├── UpdateDocumentService.java
│   └── PublishDocumentService.java
├── domain/
│   ├── Document.java
│   ├── DocumentVersion.java
│   ├── DocumentType.java
│   ├── DocumentStatus.java
│   └── DocumentRepository.java
├── infrastructure/
│   ├── persistence/
│   ├── parser/
│   └── storage/
└── dto/
    ├── CreateDocumentRequest.java
    └── DocumentResponse.java
```

이 구조의 핵심은 controller, service, entity가 루트에 흩어져 있지 않고, document라는 경계 안에서 함께 움직인다는 점이다.

## Cross-Module Interaction

모듈 간 상호작용은 최소화한다.

예:
- document 모듈이 file 모듈을 통해 첨부파일 메타데이터를 조회
- vault 모듈이 document 모듈을 통해 대표 문서 검증
- link 모듈이 document 파싱 결과를 반영

원칙:
- 다른 모듈의 persistence 구현을 직접 참조하지 않는다.
- 필요한 경우 application service 또는 domain port를 통해 접근한다.

## Persistence Strategy

초기에는 Spring Data JPA 기반 구현이 적절하다.
다만 패키지 구조는 JPA 엔티티 중심이 아니라 도메인 모듈 중심으로 유지한다.

권장 방향:
- domain에는 repository interface
- infrastructure.persistence에는 JPA implementation
- application service는 domain repository interface에 의존

## Configuration and Profiles

환경별 설정은 `application.yml`과 profile 파일로 분리한다.

예:
- `application.yml`
- `application-dev.yml`
- `application-prod.yml`

DB 마이그레이션은 `resources/db/migration/` 아래에서 관리한다.

## What Not To Do

피해야 할 구조:

- 루트에 `controller`, `service`, `repository`, `entity`만 나열하는 방식
- 모든 공통 코드를 `common`에 몰아넣는 방식
- 모듈 경계 없이 여기저기서 엔티티를 직접 참조하는 방식
- 작은 프로젝트인데도 과도한 멀티모듈 빌드 구조를 미리 도입하는 방식

## Recommended First Cut

MVP 기준으로는 다음 모듈만 우선 만들면 충분하다.

- `vault`
- `document`
- `file`
- `link`
- `common`
- `infrastructure`

`auth`, `tag`, `permission`은 얇게 시작하거나 후순위로 둘 수 있다.

## Summary

Websidian 백엔드는 Spring Boot 기반의 모듈형 모놀리스 구조를 따른다.
핵심은 기술 계층이 아니라 도메인 경계 기준으로 패키징하고, 각 모듈 안에서 api/application/domain/infrastructure 역할을 나누며, 모듈 간 결합을 느슨하게 유지하는 것이다.