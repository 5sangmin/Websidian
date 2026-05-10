# Backend Module Structure

## Purpose

이 문서는 Websidian 백엔드의 모듈 구조와 패키지 원칙을 정의한다.  
목적은 Spring Boot 프로젝트를 단순한 controller/service/repository 폴더 나열로 키우는 대신, 도메인 경계를 기준으로 구조화된 모듈형 모놀리스에 가깝게 설계하는 것이다.

Websidian 백엔드는 문서, 파일, 링크, 버전, 권한, 저장소 연동까지 함께 다룬다.  
이런 시스템은 기술 계층만 기준으로 폴더를 나누면 시간이 갈수록 관련 코드가 흩어지기 쉽다.

## Structure Principles

### 1. 패키지는 기술보다 도메인을 우선한다

`document`, `file`, `link`, `auth` 같은 비즈니스 모듈이 먼저 오고, 그 안에서 `api`, `application`, `domain`, `infrastructure`, `dto`를 나눈다.

### 2. 모듈 경계를 느슨하게 넘나들지 않는다

한 모듈의 내부 구현을 다른 모듈이 직접 참조하지 않도록 주의한다.  
가능하면 공개 서비스 인터페이스나 애플리케이션 서비스만 통해 접근한다.

### 3. 공통 코드는 정말 공통일 때만 분리한다

무분별한 `common` 확장은 오히려 의존성을 흐린다.  
전역적으로 재사용되는 예외, 응답 래퍼, 설정 정도만 분리한다.

### 4. 지금은 모듈형 모놀리스다

초기 Websidian은 마이크로서비스가 아니라 하나의 Spring Boot 애플리케이션으로 시작한다.  
대신 코드 구조는 나중에 분리 가능하도록 모듈 기준으로 설계한다.

### 5. first cut은 구현된 범위를 기준으로 문서화한다

초기 문서에서는 `vault`, `document`, `file`, `link`, `auth`, `tag` 등을 모두 구상할 수 있다.  
하지만 실제 구현 단계에서는 “지금 코드에 반영된 first cut”을 기준으로 문서를 계속 좁혀 적는 편이 혼자 개발하는 프로젝트에서는 더 유용하다.

즉, 현재 시점의 문서 목적은 “장기 이상 구조 소개”보다 “지금 어떤 모듈을 어떤 깊이까지 구현했는지”를 분명하게 남기는 데 더 가깝다.

## Recommended Top-Level Structure

```text
backend/
├── build.gradle
├── settings.gradle
└── src/
    ├── main/
    │   ├── java/com/websidian/
    │   │   ├── BackendApplication.java
    │   │   ├── common/
    │   │   ├── document/
    │   │   ├── file/
    │   │   ├── link/
    │   │   ├── auth/
    │   │   └── infrastructure/
    │   └── resources/
    │       ├── application.properties
    │       └── db/migration/
    └── test/
```

비고:
- 현재 first cut에서는 `document` 모듈이 가장 먼저 구현된다.
- `vault`는 장기적으로 별도 모듈로 분리 가능하지만, 현재는 document persistence first cut 안에서 함께 다루고 있다.
- 즉, 현재 코드 기준으로 `Vault`, `Document`, `DocumentVersion`은 모두 `document` 모듈 안에 있다.

## Directory Responsibilities

### `common/`

전역 공통 코드를 둔다.

포함 대상:
- 공통 예외
- 에러 코드
- API 응답 래퍼
- 공통 유틸리티
- 공통 설정 보조 클래스

주의:
- 도메인 규칙을 `common`으로 빼지 않는다.
- 특정 모듈에 속하는 코드는 원래 모듈에 남긴다.

### `document/`

문서 관리의 핵심 모듈이다.  
현재 first cut에서 가장 먼저 구현하는 모듈이며, 이 단계에서는 Vault까지 함께 포함해 다룬다.

책임:
- 문서 생성/조회/수정
- Markdown / HTML 문서 타입 처리
- 문서 버전 관리
- 문서 상태 관리
- 대표 문서 규칙 검증
- current version 관리
- 문서 persistence 무결성 보장

현재 포함 대상:
- `Document`
- `DocumentVersion`
- `Vault`
- `DocumentStatus`
- `DocumentType`
- `Visibility`
- 각 도메인용 repository port
- JPA persistence adapter

예시 내부 구조:
```text
document/
├── api/
├── application/
├── domain/
│   ├── Document.java
│   ├── DocumentVersion.java
│   ├── Vault.java
│   ├── DocumentRepository.java
│   ├── DocumentVersionRepository.java
│   ├── VaultRepository.java
│   ├── DocumentStatus.java
│   ├── DocumentType.java
│   ├── Visibility.java
│   ├── DocumentStatusConverter.java
│   ├── DocumentTypeConverter.java
│   └── VisibilityConverter.java
├── infrastructure/
│   └── persistence/
│       ├── JpaDocumentRepository.java
│       ├── JpaDocumentVersionRepository.java
│       └── JpaVaultRepository.java
└── dto/
```

세부 설명:
- `api/`: REST controller
- `application/`: use case, application service
- `domain/`: entity, enum, business rule, repository port
- `infrastructure/`: JPA repository adapter, persistence implementation
- `dto/`: request/response DTO

주의:
- 장기적으로 Vault가 커지면 `vault/` 모듈로 분리할 수 있다.
- 하지만 현재 first cut에서는 `documents.vault_id`, `vaults.entry_document_id`, 대표 문서 규칙이 tightly coupled 되어 있으므로 같은 모듈 안에서 관리하는 편이 더 단순하다.

### `file/`

파일 메타데이터와 객체 스토리지 연동을 담당한다.

책임:
- 파일 업로드/조회
- MinIO object key 관리
- 파일 메타데이터 저장
- orphan file 처리 기반 제공

현재 상태:
- 아직 구현 전
- schema / infra 레벨 논의만 있고, application / persistence는 후순위다

### `link/`

문서 링크 및 임베드 관계를 담당한다.

책임:
- 내부 링크 저장
- 백링크 조회
- embed relation 저장
- 링크 파싱 결과 반영

현재 상태:
- 아직 구현 전
- 문서/버전 persistence가 안정화된 뒤 후속으로 진행한다

### `auth/`

인증과 권한 모듈이다.

책임:
- 사용자 인증
- 권한 검증
- 관리자/소유자 접근 판단
- 향후 토큰/세션 정책 수용

현재 상태:
- MVP에서는 얇게 시작하거나 후순위로 둔다
- 현재 first cut 범위에는 포함하지 않는다

### `infrastructure/`

외부 시스템 연동의 공통 인프라를 둔다.

예시:
- MinIO client config
- security config
- Jackson config
- persistence config
- logging config

주의:
- 특정 도메인에 강하게 묶인 구현은 해당 모듈 내부 `infrastructure/`에 남긴다.
- truly global infrastructure만 여기에 둔다.

## Inside a Module

각 모듈 내부는 아래 역할로 나누는 것을 기본으로 한다.

### `api/`

REST controller와 request mapping을 둔다.

원칙:
- controller는 얇게 유지
- 비즈니스 로직을 직접 담지 않는다
- application service 호출만 담당한다

### `application/`

애플리케이션 서비스와 유스케이스를 둔다.

역할:
- 트랜잭션 경계
- 모듈 내부 도메인 호출 조합
- 외부 모듈과의 협력 orchestration
- persistence 조회/저장 흐름 조합
- DB 제약 이전 단계의 비즈니스 규칙 검증

예:
- `CreateDocumentService`
- `GetDocumentService`
- `UpdateDocumentService`
- `PublishDocumentService`
- `SetVaultEntryDocumentService`

### `domain/`

핵심 도메인 모델을 둔다.

포함 대상:
- entity
- value object
- domain rule
- enum
- converter
- repository port

예:
- `Document`
- `DocumentVersion`
- `Vault`
- `DocumentStatus`
- `DocumentType`
- `Visibility`
- `DocumentRepository`
- `DocumentVersionRepository`
- `VaultRepository`

비고:
- 현재 구현에서는 JPA entity와 domain entity를 분리하지 않고 하나의 클래스로 사용한다.
- 혼자 개발하는 초기 MVP에서는 이 방식이 가장 단순하고 유지 비용이 낮다.
- 추후 복잡성이 커지면 domain model과 persistence model 분리를 검토할 수 있다.

### `infrastructure/`

영속성 및 외부 연동 구현을 둔다.

포함 대상:
- Spring Data JPA repository adapter
- custom persistence query
- MinIO adapter
- parser adapter
- 외부 클라이언트 연동 구현

현재 first cut에서는 주로 `infrastructure/persistence/`가 중심이다.

### `dto/`

요청/응답용 DTO를 둔다.  
API 계약을 domain 모델과 분리한다.

현재 상태:
- 아직 first cut persistence 범위에서는 비어 있어도 괜찮다.
- controller/API를 만들기 시작할 때 본격적으로 추가한다.

## Example: Current Document Module

현재 first cut 기준 예시 구조는 아래와 같다.

```text
document/
├── domain/
│   ├── Document.java
│   ├── DocumentVersion.java
│   ├── Vault.java
│   ├── DocumentRepository.java
│   ├── DocumentVersionRepository.java
│   ├── VaultRepository.java
│   ├── DocumentStatus.java
│   ├── DocumentType.java
│   ├── Visibility.java
│   ├── DocumentStatusConverter.java
│   ├── DocumentTypeConverter.java
│   └── VisibilityConverter.java
├── infrastructure/
│   └── persistence/
│       ├── JpaDocumentRepository.java
│       ├── JpaDocumentVersionRepository.java
│       └── JpaVaultRepository.java
└── api/
    └── (추후 추가)
```

이 구조의 핵심은 controller, service, entity가 루트에 흩어져 있지 않고, `document`라는 경계 안에서 함께 움직인다는 점이다.

## Persistence Strategy

초기에는 Spring Data JPA 기반 구현이 적절하다.  
다만 패키지 구조는 JPA 엔티티 중심이 아니라 도메인 모듈 중심으로 유지한다.

권장 방향:
- `domain/`에는 repository port를 둔다
- `infrastructure/persistence/`에는 Spring Data JPA adapter를 둔다
- `application/`은 domain repository port에 의존한다

현재 방식:
- `JpaDocumentRepository extends JpaRepository<Document, UUID>, DocumentRepository`
- `JpaDocumentVersionRepository extends JpaRepository<DocumentVersion, UUID>, DocumentVersionRepository`
- `JpaVaultRepository extends JpaRepository<Vault, UUID>, VaultRepository`

이 방식의 장점:
- 구현량이 적다
- first cut에서 빠르게 persistence를 고정할 수 있다
- Spring Data query method를 그대로 활용하기 쉽다

이 방식의 주의점:
- domain port와 `JpaRepository`가 같은 메서드 이름을 중복 선언하면 모호성이 생길 수 있다
- 실제로 `findById(UUID)`를 domain port에 다시 선언하면 `CrudRepository#findById`와 겹쳐 테스트나 호출 코드에서 혼란이 생길 수 있다

현재 정리 원칙:
- domain port에는 정말 필요한 조회 메서드만 둔다
- Spring Data 기본 제공 메서드와 중복되는 선언은 가능하면 줄인다
- 복잡해지면 adapter 클래스를 별도로 두는 구조로 전환할 수 있다

## Current First Cut Status

현재 구현 기준으로 완료된 범위:
- Flyway 기반 `vaults`, `documents`, `document_versions` migration
- `Document`, `DocumentVersion`, `Vault` JPA 엔티티
- enum / converter (`DocumentStatus`, `DocumentType`, `Visibility`)
- Spring Data JPA persistence adapter 3개
- persistence 테스트
  - 저장 / 조회
  - 최신 버전 조회
  - Published 문서만 entry document 지정 가능
  - duplicate slug 방지
  - duplicate `version_no` 방지

아직 남은 범위:
- application service
- REST controller / DTO
- soft delete 관련 추가 테스트
- file/link/tag/auth 후속 모듈

## Cross-Module Interaction

모듈 간 상호작용은 최소화한다.

예:
- document 모듈이 file 모듈을 통해 첨부파일 메타데이터를 조회
- link 모듈이 document 파싱 결과를 반영
- auth 모듈이 document 접근 권한을 검증

원칙:
- 다른 모듈의 persistence 구현을 직접 참조하지 않는다
- 필요한 경우 application service 또는 domain port를 통해 접근한다

현재 시점에서는 `document` 모듈 단독으로 persistence first cut을 고정하는 것이 우선이다.

## Configuration and Profiles

환경별 설정은 현재 `application.properties`와 Flyway migration 기준으로 관리한다.

예:
- `application.properties`
- `db/migration/V1__...sql`

향후 필요 시:
- `application-dev.yml`
- `application-prod.yml`

현재 핵심 원칙:
- DB 스키마는 Flyway migration으로 관리한다
- JPA는 `ddl-auto=create/update`가 아니라 validate 중심으로 맞춘다

## What Not To Do

피해야 할 구조:

- 루트에 `controller`, `service`, `repository`, `entity`만 나열하는 방식
- 모든 공통 코드를 `common`에 몰아넣는 방식
- 모듈 경계 없이 여기저기서 엔티티를 직접 참조하는 방식
- 작은 프로젝트인데도 과도한 멀티모듈 빌드 구조를 미리 도입하는 방식
- first cut 단계부터 domain model / persistence model / read model을 과하게 분리하는 방식
- 아직 필요하지 않은데 Vault를 억지로 독립 모듈로 분리해 복잡도를 높이는 방식

## Recommended First Cut

MVP 기준의 현재 first cut에서는 다음 순서로 구현하는 것이 적절하다.

1. `document` 모듈 안에서 `Vault`, `Document`, `DocumentVersion` persistence를 먼저 고정한다.
2. repository 테스트로 무결성과 current version 흐름을 검증한다.
3. 그 다음 `application/`에 `CreateDocumentService` 같은 첫 유스케이스를 추가한다.
4. 이후 controller / DTO를 얹어 API로 확장한다.
5. file, link, tag, auth는 후속 모듈로 점진적으로 붙인다.

## Summary

Websidian 백엔드는 Spring Boot 기반의 모듈형 모놀리스를 지향한다.  
핵심은 기술 계층이 아니라 도메인 경계 기준으로 패키징하고, 각 모듈 안에서 `api/application/domain/infrastructure/dto` 역할을 나누며, 현재 구현된 first cut 범위를 문서와 코드에 함께 반영하는 것이다.

현재 시점의 핵심 구현 단위는 `document` 모듈이며, 이 안에서 `Vault`, `Document`, `DocumentVersion`의 persistence를 먼저 안정화한 뒤 application service와 API 계층으로 확장하는 흐름이 Websidian에 가장 잘 맞는다.