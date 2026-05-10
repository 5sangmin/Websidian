# Schema Draft

## Purpose

이 문서는 Websidian 데이터베이스의 현재 스키마 초안과 first cut 범위를 정리한다.  
목적은 ERD 개요를 실제 테이블 설계와 migration 기준으로 내리는 것이며, 현재는 특히 `vaults`, `documents`, `document_versions`를 중심으로 정리한다.

초기 단계에서는 완벽한 일반화보다 “핵심 도메인 개념을 안정적으로 담을 수 있는 구조”가 중요하다.  
Websidian은 문서, 버전, 링크, 첨부, 탐색 경험이 핵심이지만, 현재 구현 단계에서는 문서와 버전 이력을 먼저 안정적으로 고정하는 것을 우선한다.

## Schema Design Principles

### 1. Naming Consistency

테이블명은 복수형 소문자 snake_case를 사용한다.  
컬럼명도 snake_case로 통일한다.

예:
- `vaults`
- `documents`
- `document_versions`

### 2. Surrogate Keys

현재 first cut에서는 핵심 테이블의 기본키를 **UUID**로 사용한다.

적용 대상:
- `vaults.id`
- `documents.id`
- `document_versions.id`

이유:
- 애플리케이션 계층과 DB 계층에서 식별자 정책을 일관되게 가져갈 수 있다.
- 이후 MinIO object metadata, 외부 API, 분산 환경을 고려해도 확장성이 좋다.
- first cut에서 bigint와 UUID를 함께 열어두는 것보다 구현 기준을 빠르게 고정하는 편이 낫다.

### 3. Auditing Columns

주요 테이블에는 다음 감사 컬럼을 둔다.

- `created_at`
- `updated_at` (버전 테이블 제외)
- `created_by` (초기에는 nullable)
- `deleted_at` (문서, Vault)

### 4. Soft Delete First

문서와 Vault는 물리 삭제보다 soft delete를 우선 고려한다.

정책:
- 활성 데이터: `deleted_at IS NULL`
- 삭제 데이터: `deleted_at IS NOT NULL`

초기 MVP에서는 삭제 상태를 별도 enum으로 두지 않고, soft delete timestamp로 표현한다.

### 5. Separate Metadata from Blobs

장기적으로는 파일 바이너리와 대용량 자산은 MinIO에 두고, DB에는 메타데이터만 둔다.  
다만 first cut에서는 문서 버전 본문을 `document_versions.content_snapshot`의 `TEXT` 컬럼에 직접 저장한다.

이유:
- 초기 구현 복잡도를 낮출 수 있다.
- 저장 시 새 버전 생성 정책을 단순하게 구현할 수 있다.
- 이후 필요하면 MinIO 중심 구조로 migration할 수 있다.

### 6. Flyway First

이 시점부터 DB 스키마 변경은 Flyway migration 기준으로 관리한다.

정책:
- `spring.jpa.hibernate.ddl-auto`에 의존해 스키마를 암묵적으로 만들지 않는다.
- 테이블, 컬럼, 인덱스, 제약은 모두 migration 파일에 명시한다.
- JPA는 스키마 생성이 아니라 스키마와 엔티티의 정합성 검증에 집중한다.

## First Cut Scope

현재 `feat(document): basic document and version persistence` 이슈의 first cut 범위는 아래와 같다.

포함:
- `vaults`
- `documents`
- `document_versions`

제외:
- `users`
- `files`
- `document_file_refs`
- `document_links`
- `tags`
- `document_tags`
- `permissions`

이 first cut의 목적은 Websidian의 핵심 도메인인 문서와 버전 이력을 실제 migration과 JPA persistence로 옮길 수 있을 정도로 구체화하는 것이다.

전제:
- Markdown과 HTML은 모두 `documents`에서 관리한다.
- 문서 메타데이터와 버전 스냅샷은 분리한다.
- 저장 시마다 새 버전을 생성하고, 현재 버전은 `documents.current_version_id`로 가리킨다.
- 삭제는 우선 soft delete로 처리한다.
- DB 반영은 Flyway migration 기준으로 관리한다.

## Core Tables

### `vaults`

문서 저장소의 최상위 단위다.

권장 컬럼:
- `id` UUID PK
- `owner_id` UUID NULL
- `slug` VARCHAR(255) NOT NULL
- `name` VARCHAR(500) NOT NULL
- `description` TEXT NULL
- `visibility` VARCHAR(50) NOT NULL
- `entry_document_id` UUID NULL
- `created_at` TIMESTAMPTZ NOT NULL
- `updated_at` TIMESTAMPTZ NOT NULL
- `deleted_at` TIMESTAMPTZ NULL

설명:
- `owner_id`는 사용자 모델이 아직 없으므로 초기에는 nullable로 둔다.
- `visibility` 후보 값:
  - `public`
  - `private`
- `entry_document_id`는 Vault의 대표 진입 문서를 가리킨다.
- 대표 문서는 애플리케이션 규칙상 Published 문서만 허용한다.

제약:
- PK: `vaults_pkey (id)`
- UNIQUE: `vaults_slug_unique (slug)`
- FK: `vaults_entry_document_fk (entry_document_id -> documents.id)`

비고:
- “entry document는 반드시 같은 Vault 소속 문서여야 한다”는 규칙은 현재 애플리케이션 레벨에서 우선 검증한다.
- soft deleted Vault는 기본 조회에서 제외한다.

### `documents`

문서 메타데이터와 현재 조회 기준 버전을 관리하는 기본 엔티티다.

권장 컬럼:
- `id` UUID PK
- `vault_id` UUID NOT NULL
- `slug` VARCHAR(255) NOT NULL
- `title` VARCHAR(1000) NOT NULL
- `document_type` VARCHAR(50) NOT NULL
- `status` VARCHAR(50) NOT NULL
- `current_version_id` UUID NULL
- `created_by` UUID NULL
- `created_at` TIMESTAMPTZ NOT NULL
- `updated_at` TIMESTAMPTZ NOT NULL
- `deleted_at` TIMESTAMPTZ NULL

설명:
- `document_type` 후보 값:
  - `markdown`
  - `html`
- `status` 후보 값:
  - `draft`
  - `published`
  - `archived`
- HTML도 이 테이블에서 Markdown과 같은 1급 문서로 관리한다.
- 삭제는 별도 `status=deleted` 대신 `deleted_at`으로 표현한다.
- `current_version_id`는 현재 렌더링/조회 기준이 되는 버전을 가리킨다.
- `created_by`는 초기에는 nullable로 두고, 사용자 모델이 정리되면 FK 강화를 검토한다.

제약:
- PK: `documents_pkey (id)`
- FK: `documents_vault_fk (vault_id -> vaults.id)`
- FK: `documents_current_version_fk (current_version_id -> document_versions.id)`
- UNIQUE: `documents_vault_slug_unique (vault_id, slug)`

비고:
- `slug`는 같은 Vault 안에서만 유일하면 충분하다.
- 문서 제목(`title`)은 변경 가능성이 있으므로 unique 대상이 아니다.
- `deleted_at`이 null이 아니면 soft deleted 문서로 간주한다.

### `document_versions`

문서 본문의 시점별 스냅샷을 저장하는 버전 엔티티다.

권장 컬럼:
- `id` UUID PK
- `document_id` UUID NOT NULL
- `version_no` INTEGER NOT NULL
- `source_type` VARCHAR(50) NOT NULL
- `content_snapshot` TEXT NOT NULL
- `checksum` VARCHAR(64) NULL
- `created_by` UUID NULL
- `created_at` TIMESTAMPTZ NOT NULL

설명:
- `source_type` 후보 값:
  - `markdown`
  - `html`
- `source_type`은 해당 버전 시점의 문서 소스 타입을 기록한다.
- first cut에서는 본문 원문을 `content_snapshot`에 직접 저장한다.
- 장기적으로는 원본을 MinIO에 두고 DB에는 메타데이터/요약만 두는 구조도 검토할 수 있지만, 초기 구현 복잡도를 낮추기 위해 현재는 DB snapshot 저장을 우선한다.
- `checksum`은 중복 저장 감지나 무결성 확인에 도움이 되지만, 초기에는 nullable로 둔다.
- `created_by` 역시 초기에는 nullable로 두고, 사용자 모델 정리 후 FK 적용을 검토한다.

제약:
- PK: `document_versions_pkey (id)`
- FK: `document_versions_document_fk (document_id -> documents.id)`
- UNIQUE: `document_versions_document_version_unique (document_id, version_no)`

비고:
- 하나의 문서는 여러 버전을 가진다.
- 하나의 문서 안에서 `version_no`는 1부터 증가하는 순번으로 관리한다.
- 버전 삭제는 초기에는 별도 지원하지 않고, 문서 soft delete와 함께 이력을 유지한다.

## Current Version Handling

초기 구현에서는 아래 흐름을 표준으로 삼는다.

문서 생성:
1. `documents` row 생성 (`current_version_id`는 우선 null)
2. `document_versions` row 생성 (`version_no = 1`)
3. 생성된 version id로 `documents.current_version_id` 갱신

문서 수정:
1. 기존 문서를 조회
2. 같은 `document_id`로 새 `document_versions` row 생성
3. `version_no`는 기존 최대값 + 1
4. `documents.current_version_id`를 새 버전 id로 교체
5. `documents.updated_at` 갱신

이 정책은 Document Lifecycle 문서의 “저장 시 새 버전 생성, current version 교체” 원칙과 맞춘다.

## Integrity Rules

현재 first cut에서 중요한 무결성 규칙은 아래와 같다.

1. 모든 문서는 반드시 하나의 Vault에 속해야 한다.
   - `documents.vault_id`는 NOT NULL이다.

2. 같은 Vault 안에서 문서 slug는 유일해야 한다.
   - `UNIQUE (vault_id, slug)`를 둔다.

3. 하나의 문서는 여러 버전을 가질 수 있다.
   - `document_versions.document_id`는 `documents.id`를 참조한다.

4. 하나의 문서 안에서 버전 번호는 중복될 수 없다.
   - `UNIQUE (document_id, version_no)`를 둔다.

5. `documents.current_version_id`는 현재 기준 버전을 가리킨다.
   - first cut에서는 FK만 두고, “현재 버전은 반드시 같은 document의 version이어야 한다”는 규칙은 애플리케이션 서비스에서 우선 검증한다.
   - 이후 필요하면 trigger 또는 추가 제약으로 강화할 수 있다.

6. Vault entry document는 Published 문서만 지정할 수 있다.
   - 이 규칙은 현재 애플리케이션 계층과 도메인 메서드에서 우선 검증한다.

## Constraint Policy

현재 first cut에서는 PostgreSQL enum type 대신 **`VARCHAR + application enum + DB check constraint`** 방향을 사용한다.

적용 이유:
- Flyway 초기 migration을 단순하게 유지할 수 있다.
- 애플리케이션 코드와 스키마를 빠르게 맞출 수 있다.
- enum type 변경 비용을 초기에 낮출 수 있다.

강화 대상:
- `vaults.visibility`
- `documents.document_type`
- `documents.status`
- `document_versions.source_type`
- `document_versions.version_no > 0`

## Suggested Indexes

현재 first cut 기준 추천 인덱스:

- `vaults(slug)` unique index
- `documents(vault_id, slug)` unique index
- `documents(vault_id, status)` index
- `document_versions(document_id, version_no desc)` index

추가 고려:
- soft delete 데이터가 많아지면 `deleted_at IS NULL` 기준 partial index를 적극 활용한다.
- 본문 검색이 필요해지면 `content_snapshot`에 대한 full text search 인덱스를 별도 migration으로 추가한다.

## Verification Notes

현재 persistence 테스트에서 아래 제약이 실제로 검증되었다.

- `documents_vault_slug_unique`
  - 같은 Vault 안에서 동일한 `slug`의 문서는 저장할 수 없다.
- `document_versions_document_version_unique`
  - 같은 Document 안에서 동일한 `version_no`는 저장할 수 없다.

이 검증은 `DocumentPersistenceTest`에서 DB 레벨 unique constraint 위반으로 확인했다.

## First Migration Cut

현재 first migration은 아래 범위를 기준으로 가져간다.

- `vaults`
- `documents`
- `document_versions`

이유:
- `documents.vault_id` FK가 있으므로 `vaults`는 함께 포함하는 것이 자연스럽다.
- 이번 이슈의 목표는 document/version persistence first cut을 재현 가능한 상태로 만드는 것이다.
- `users`, `files`, `document_links`, `tags`, `permissions`까지 한 번에 넣으면 범위가 너무 커진다.

migration 예시:
- `V1__create_core_document_schema.sql`

## Out of Scope for This Cut

이번 first cut에서는 아래 항목을 의도적으로 제외한다.

- `users` 실제 모델링 및 FK 연결
- `files` 및 MinIO object metadata 연동
- `document_file_refs`
- `document_links` 파싱/저장
- `tags`, `document_tags`
- `permissions`
- 공개 버전과 draft 버전을 분리하는 고급 버전 정책
- version diff 저장/계산
- HTML asset bundle 관리 정책
- orphan file cleanup 자동화

## Open Questions

후속 이슈 또는 ADR에서 다룰 항목:

- `documents.current_version_id`와 `document_versions.document_id` 일치 조건을 DB 레벨에서 어디까지 강제할지
- `vaults.entry_document_id`가 같은 Vault 소속 문서인지 DB 레벨에서 어디까지 강제할지
- `created_by`를 언제부터 실제 FK로 연결할지
- 문서 본문 저장을 장기적으로 DB 중심으로 유지할지, MinIO 중심으로 옮길지
- soft delete와 orphan file cleanup 정책을 어디까지 자동화할지
- repository port와 Spring Data interface를 어떤 방식으로 분리할지

## Recommended Implementation Order

1. `vaults`, `documents`, `document_versions` first cut을 문서 기준으로 확정한다.
2. Flyway 첫 migration 파일을 작성한다.
3. JPA entity / repository를 migration 스키마에 맞춰 구현한다.
4. repository 테스트로 저장/조회, 최신 버전 조회, unique constraint, current version 갱신 규칙을 검증한다.
5. 그 다음 application service 계층으로 넘어간다.

## Summary

Websidian의 현재 schema draft는 Vault와 Document를 중심으로, first cut에서는 `vaults`, `documents`, `document_versions`를 우선 확정하는 방향이다.  
가장 중요한 설계 포인트는 Markdown과 HTML을 같은 문서 모델로 다룬다는 점, 문서 메타데이터와 버전 스냅샷을 분리한다는 점, soft delete와 current version 정책을 명확히 한다는 점, 그리고 DB 제약과 persistence 테스트를 함께 사용해 무결성을 고정한다는 점이다.