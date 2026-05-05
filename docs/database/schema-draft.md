# Schema Draft

## Purpose

이 문서는 Websidian 데이터베이스의 초기 스키마 초안을 정리한다.
목적은 ERD 개요를 실제 테이블 설계 후보로 내리는 것이며, 아직 최종 SQL이나 마이그레이션 파일을 확정하는 단계는 아니다.

초기 단계에서는 완벽한 정규화보다 “핵심 도메인 개념을 안정적으로 담을 수 있는 구조”가 중요하다.
Websidian은 문서 관계와 탐색 경험이 핵심이므로, Vault, Document, Version, Link, File 구조를 먼저 명확히 잡아야 한다.

## Schema Design Principles

### 1. Naming Consistency
테이블명은 복수형 소문자 snake_case를 사용한다.
컬럼명도 snake_case로 통일한다.

예:
- `vaults`
- `documents`
- `document_versions`

### 2. Surrogate Keys
초기에는 모든 핵심 테이블에 단일 기본키 `id`를 둔다.
타입은 UUID 또는 bigint를 선택할 수 있으나, 현재 문서 단계에서는 둘 다 후보로 둔다.

### 3. Auditing Columns
주요 테이블에는 다음 감사 컬럼을 둔다.

- `created_at`
- `updated_at`
- 필요 시 `created_by`
- 필요 시 `deleted_at`

### 4. Soft Delete First
문서와 Vault는 물리 삭제보다 soft delete를 우선 고려한다.

### 5. Separate Metadata from Blobs
실제 파일 바이너리는 MinIO에 두고, DB에는 메타데이터만 둔다.

## Core Tables

### `users`
초기 사용자 테이블이다.

컬럼 초안:
- id
- username
- email
- password_hash
- status
- created_at
- updated_at

비고:
- MVP에서는 최소 수준만 사용 가능
- 외부 로그인 도입 시 변경 가능

### `vaults`
문서 저장소의 최상위 단위다.

컬럼 초안:
- id
- owner_id
- slug
- name
- description
- visibility
- entry_document_id
- created_at
- updated_at
- deleted_at

제약:
- `slug`는 유일해야 한다
- `owner_id`는 `users.id` 참조
- `entry_document_id`는 같은 Vault의 문서를 참조해야 한다

### `documents`
문서의 기본 엔티티다.

컬럼 초안:
- id
- vault_id
- slug
- title
- document_type
- status
- current_version_id
- created_by
- created_at
- updated_at
- deleted_at

설명:
- `document_type`: `markdown`, `html`
- `status`: `draft`, `published`, `archived`
- HTML도 이 테이블에 포함한다.

제약:
- `vault_id`는 필수
- 같은 Vault 안에서 `slug`는 유일해야 한다

### `document_versions`
문서 내용의 버전 스냅샷이다.

컬럼 초안:
- id
- document_id
- version_no
- source_type
- content_snapshot
- checksum
- created_by
- created_at

설명:
- `source_type`은 `markdown`, `html`
- `content_snapshot`은 초기에는 text 또는 long text 계열로 저장 가능
- 장기적으로 원본을 MinIO에 두고 snapshot을 줄이는 전략도 검토 가능

제약:
- `(document_id, version_no)`는 유일해야 한다

### `files`
첨부파일 메타데이터 테이블이다.

컬럼 초안:
- id
- vault_id
- object_key
- original_name
- mime_type
- size_bytes
- checksum
- uploaded_by
- created_at
- deleted_at

설명:
- `object_key`는 MinIO 객체 키
- DB에는 실제 파일이 아닌 참조 정보만 둔다.

제약:
- `object_key`는 유일해야 한다

### `document_file_refs`
문서와 파일의 관계를 나타낸다.

컬럼 초안:
- id
- document_id
- file_id
- ref_type
- created_at

설명:
- `ref_type` 예시:
  - `image`
  - `attachment`
  - `embed_asset`

제약:
- `(document_id, file_id, ref_type)` 중복 정책은 추후 결정
- 초기에는 단순 허용 후 애플리케이션에서 정리 가능

### `document_links`
문서 간 링크/임베드 관계를 저장한다.

컬럼 초안:
- id
- vault_id
- source_document_id
- target_document_id
- relation_type
- raw_target
- created_at

설명:
- 내부 문서 링크는 `target_document_id`를 채운다
- 외부 링크나 아직 해석되지 않은 링크는 `raw_target`에 남길 수 있다
- `relation_type` 예시:
  - `wikilink`
  - `markdown_link`
  - `embed_document`
  - `embed_html`
  - `external`

### `tags`
문서 분류용 태그다.

컬럼 초안:
- id
- vault_id
- name
- normalized_name
- created_at

제약:
- 같은 Vault 안에서 `normalized_name`은 유일해야 한다

### `document_tags`
문서와 태그의 연결 테이블이다.

컬럼 초안:
- document_id
- tag_id
- created_at

제약:
- `(document_id, tag_id)` 유일

### `permissions`
접근 제어 테이블이다.

컬럼 초안:
- id
- subject_type
- subject_id
- resource_type
- resource_id
- action
- effect
- created_at

설명:
- 초기 MVP는 단순화 가능
- 나중에 RBAC 또는 ACL 확장 가능

## Suggested Relationships

- `users 1:N vaults`
- `vaults 1:N documents`
- `documents 1:N document_versions`
- `vaults 1:N files`
- `documents N:M files` via `document_file_refs`
- `documents N:M documents` via `document_links`
- `documents N:M tags` via `document_tags`

## Suggested Indexes

초기 인덱스 후보:

- `vaults(slug)`
- `documents(vault_id, slug)`
- `documents(vault_id, status)`
- `document_versions(document_id, version_no desc)`
- `files(vault_id, object_key)`
- `document_links(source_document_id)`
- `document_links(target_document_id)`
- `tags(vault_id, normalized_name)`

검색 기능이 커지면 본문 검색용 인덱스를 추후 추가한다.

## Data Type Considerations

아직 확정되지 않은 항목:
- 기본키를 UUID로 할지 bigint로 할지
- `content_snapshot`을 DB에 계속 둘지, 일부를 객체 스토리지로 옮길지
- `visibility`, `status`, `document_type`을 enum으로 둘지 string + check constraint로 둘지

초기에는 구현 단순성을 우선하고, 운영 경험이 생긴 뒤 최적화한다.

## First Migration Cut

첫 번째 마이그레이션에는 아래만 포함해도 충분하다.

- users
- vaults
- documents
- document_versions
- files
- document_links

이후 2차 마이그레이션:
- tags
- document_tags
- permissions
- document_file_refs

이렇게 나누면 MVP를 빠르게 올리고, 후속 기능을 점진적으로 붙이기 쉽다.

## Open Questions

후속 결정이 필요한 항목:
- 문서 본문을 DB에 저장할지 MinIO 중심으로 갈지
- HTML 문서와 HTML 자산의 경계를 어떻게 나눌지
- 권한 모델을 언제부터 실제 구현할지
- soft delete와 orphan file cleanup 정책을 어디까지 자동화할지

## Summary

Websidian 스키마 초안은 Vault와 Document를 중심으로 Version, Link, File, Tag, Permission을 연결하는 구조다.
가장 중요한 설계 포인트는 HTML도 문서로 취급한다는 점, 파일과 메타데이터를 분리한다는 점, 그리고 링크와 임베드를 구조화된 관계 데이터로 저장한다는 점이다.