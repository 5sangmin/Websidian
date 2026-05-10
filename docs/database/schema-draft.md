## First Cut: Document / DocumentVersion

`feat(document): basic document and version persistence` 이슈의 첫 구현 범위에서는
`documents`와 `document_versions`를 우선 확정한다.

이 first cut의 목적은 Websidian의 핵심 도메인인 문서와 버전 이력을
실제 마이그레이션과 JPA persistence로 옮길 수 있을 정도로 구체화하는 것이다.

이번 범위의 전제:
- Markdown과 HTML은 모두 `documents` 테이블에서 관리한다.
- 문서 메타데이터와 버전 스냅샷은 분리한다.
- 저장 시마다 새 버전을 생성하고, 현재 버전은 `documents.current_version_id`로 가리킨다.
- 삭제는 우선 soft delete로 처리한다.
- DB 반영은 이 시점부터 Flyway migration 기준으로 관리한다.
- Vault, Link, Tag, Permission, File/MinIO 연동은 이번 first cut에서 제외한다.

### Why this cut

이 first cut은 아래 문서 원칙을 직접 반영한다.

- 문서는 반드시 하나의 Vault에 속한다.
- Markdown과 HTML 모두 같은 Document 모델을 사용한다.
- 문서 상태와 버전 이력은 분리한다.
- 초기 MVP는 Draft / Published / Archived 상태와 soft delete 우선 정책을 따른다.
- 초기 버전 전략은 “저장 시 새 버전 생성, current version 교체”로 단순화한다.

### `documents`

문서의 메타데이터와 현재 조회 기준 버전을 관리하는 기본 엔티티다.

권장 컬럼:
- `id` UUID PK
- `vault_id` UUID NOT NULL
- `slug` VARCHAR(200) NOT NULL
- `title` VARCHAR(255) NOT NULL
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
- 삭제는 별도 `status=deleted` 대신 `deleted_at`으로 표현한다.
- `current_version_id`는 현재 렌더링/조회 기준이 되는 버전을 가리킨다.
- `created_by`는 초기에는 nullable로 두고, 사용자 모델이 정리되면 FK 강화를 검토한다.

제약:
- PK: `documents_pkey (id)`
- FK: `documents_vault_id_fkey (vault_id -> vaults.id)`
- FK: `documents_current_version_id_fkey (current_version_id -> document_versions.id)`
- UNIQUE: `documents_vault_id_slug_key (vault_id, slug)`

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
- `checksum` VARCHAR(128) NULL
- `created_by` UUID NULL
- `created_at` TIMESTAMPTZ NOT NULL

설명:
- `source_type` 후보 값:
  - `markdown`
  - `html`
- `source_type`은 해당 버전 시점의 문서 소스 타입을 기록한다.
- first cut에서는 본문 원문을 `content_snapshot`에 직접 저장한다.
- 장기적으로는 원본을 MinIO에 두고 DB에는 메타데이터/요약만 두는 구조도 검토할 수 있지만,
  초기 구현 복잡도를 낮추기 위해 현재는 DB snapshot 저장을 우선한다.
- `checksum`은 중복 저장 감지나 무결성 확인에 도움이 되지만, 초기에는 nullable로 둔다.
- `created_by` 역시 초기에는 nullable로 두고, 사용자 모델 정리 후 FK 적용을 검토한다.

제약:
- PK: `document_versions_pkey (id)`
- FK: `document_versions_document_id_fkey (document_id -> documents.id)`
- UNIQUE: `document_versions_document_id_version_no_key (document_id, version_no)`

비고:
- 하나의 문서는 여러 버전을 가진다.
- 하나의 문서 안에서 `version_no`는 1부터 증가하는 순번으로 관리한다.
- 버전 삭제는 초기에는 별도 지원하지 않고, 문서 soft delete와 함께 이력을 유지한다.

### Integrity Rules

이번 first cut에서 중요한 무결성 규칙은 아래와 같다.

1. 모든 문서는 반드시 하나의 Vault에 속해야 한다.
   - `documents.vault_id`는 NOT NULL이다.

2. 같은 Vault 안에서 문서 slug는 유일해야 한다.
   - `UNIQUE (vault_id, slug)`를 둔다.

3. 하나의 문서는 여러 버전을 가질 수 있다.
   - `document_versions.document_id`는 `documents.id`를 참조한다.

4. 하나의 문서 안에서 버전 번호는 중복될 수 없다.
   - `UNIQUE (document_id, version_no)`를 둔다.

5. `documents.current_version_id`는 현재 기준 버전을 가리킨다.
   - first cut에서는 FK만 두고,
     “현재 버전은 반드시 같은 document의 version이어야 한다”는 규칙은
     애플리케이션 서비스에서 우선 검증한다.
   - 이후 필요하면 trigger 또는 추가 제약으로 강화할 수 있다.

### Current Version Handling

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

### Soft Delete Policy

초기 구현에서는 삭제 상태를 별도 enum 값으로 두지 않고, soft delete timestamp로 관리한다.

정책:
- 활성 문서: `deleted_at IS NULL`
- 삭제 문서: `deleted_at IS NOT NULL`

이유:
- `status`는 문서 라이프사이클 상태(Draft / Published / Archived)에 집중한다.
- 삭제는 상태 전이보다 운영적 삭제 처리에 가깝다.
- `status=deleted`와 `deleted_at`을 동시에 두면 의미가 중복될 수 있다.

### Suggested Indexes for First Cut

첫 migration 기준 추천 인덱스:
- `documents(vault_id, slug)` unique index
- `documents(vault_id, status)` index
- `document_versions(document_id, version_no desc)` index

추가 고려:
- soft delete 문서가 많아지면 `documents(vault_id, deleted_at)` 또는 partial index를 검토할 수 있다.
- 본문 검색이 필요해지면 `content_snapshot`에 대한 full text search 인덱스를 별도 migration으로 추가한다.

### Data Type Policy for First Cut

이번 first cut에서는 아래처럼 단순하게 시작한다.

- PK 타입: UUID
- 상태/타입 컬럼: PostgreSQL enum 대신 `VARCHAR + application enum` 우선
- 본문 저장: `TEXT`
- 시간 컬럼: `TIMESTAMPTZ`

이유:
- Flyway 초기 migration을 단순하게 유지할 수 있다.
- 애플리케이션 코드와 스키마를 빠르게 맞출 수 있다.
- 나중에 enum type이나 check constraint를 도입하더라도 migration으로 점진 변경 가능하다.

### Flyway Migration Policy

이 시점부터 DB 스키마 변경은 Flyway 기준으로 관리한다.

정책:
- `spring.jpa.hibernate.ddl-auto`에 의존해 스키마를 암묵적으로 만들지 않는다.
- 테이블/컬럼/인덱스/제약은 모두 migration 파일에 명시한다.
- 첫 migration은 Document / DocumentVersion first cut을 재현 가능한 상태로 만드는 데 집중한다.

첫 migration 후보:
- `V1__create_vaults_documents_document_versions.sql`

주의:
- 실제 적용 순서는 `vaults` 의존성을 고려해야 한다.
- `documents.vault_id` FK가 있으므로, vaults 테이블이 먼저 존재해야 한다.
- 만약 현재 구현에서 Vault를 아직 만들지 않는다면,
  첫 migration 범위를 `vaults + documents + document_versions`로 최소 확장하는 편이 자연스럽다.

### Out of Scope for This Cut

이번 first cut에서는 아래 항목을 의도적으로 제외한다.

- `files` 및 MinIO object metadata 연동
- `document_links` 파싱/저장
- `tags`, `document_tags`
- `permissions`
- 공개 버전과 draft 버전을 분리하는 고급 버전 정책
- version diff 저장/계산
- HTML asset bundle 관리 정책
- orphan file cleanup 자동화

### Open Questions

후속 이슈 또는 ADR에서 다룰 항목:
- `documents.current_version_id`와 `document_versions.document_id` 일치 조건을 DB 레벨에서 어디까지 강제할지
- `created_by`를 언제부터 실제 FK로 연결할지
- `document_type`, `status`를 DB enum 또는 check constraint로 강화할지
- 본문 저장을 장기적으로 DB 중심으로 유지할지, MinIO 중심으로 옮길지
- Vault 테이블 first cut을 어디까지 함께 포함할지

### Recommended Implementation Order

1. `vaults`, `documents`, `document_versions` first cut을 문서 기준으로 확정한다.
2. Flyway 첫 migration 파일을 작성한다.
3. JPA entity / repository를 migration 스키마에 맞춰 구현한다.
4. repository 테스트로 version 생성과 current version 갱신을 검증한다.