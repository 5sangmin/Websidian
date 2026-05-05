# ERD Overview

## Purpose

이 문서는 Websidian의 데이터 모델을 ERD 관점에서 요약한다.
목적은 실제 마이그레이션 SQL을 작성하기 전에 핵심 엔티티, 속성, 관계, 무결성 규칙을 명확히 하는 것이다.

ERD는 구현 전 데이터 구조를 시각적으로 이해하고, 빠진 관계나 중복 개념을 조기에 발견하는 데 도움이 된다.
Websidian처럼 문서, 링크, 파일, 버전, 권한이 얽힌 시스템에서는 특히 중요하다.

## Modeling Principles

Websidian의 ERD는 다음 원칙을 따른다.

### 1. 문서와 파일을 구분한다
문서는 탐색과 링크의 대상이고, 파일은 저장과 참조의 대상이다.
다만 HTML은 예외적으로 문서가 될 수 있다.

### 2. 관계를 명시적으로 기록한다
링크, 임베드, 첨부 관계는 문자열 파싱 결과가 아니라 구조적 데이터로 저장한다.

### 3. 버전은 별도 엔티티로 관리한다
문서 변경 이력은 문서 본체와 분리해 관리한다.

### 4. Vault를 최상위 경계로 둔다
모든 문서는 반드시 하나의 Vault에 속한다.

## Core Entities

### 1. vaults
문서 저장소의 최상위 단위다.

주요 컬럼 예시:
- id
- owner_id
- slug
- name
- description
- visibility
- entry_document_id
- created_at
- updated_at

설명:
- 하나의 Vault는 여러 문서를 가진다.
- 하나의 Vault는 하나의 대표 문서를 가질 수 있다.

### 2. documents
사용자가 읽고 탐색하는 기본 문서 엔티티다.

주요 컬럼 예시:
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

설명:
- `document_type`은 `markdown`, `html` 등을 가질 수 있다.
- HTML도 Markdown과 동일한 documents 엔티티를 사용한다.

### 3. document_versions
문서 내용의 시점별 스냅샷이다.

주요 컬럼 예시:
- id
- document_id
- version_no
- source_type
- content_snapshot
- checksum
- created_by
- created_at

설명:
- 하나의 Document는 여러 Version을 가진다.
- 현재 사용 버전은 `documents.current_version_id`로 가리킨다.

### 4. files
첨부파일 메타데이터를 저장한다.

주요 컬럼 예시:
- id
- vault_id
- object_key
- original_name
- mime_type
- size_bytes
- checksum
- uploaded_by
- created_at

설명:
- 실제 파일 원본은 MinIO에 있고, DB에는 메타데이터만 둔다.

### 5. document_file_refs
문서와 파일 간 참조 관계를 나타낸다.

주요 컬럼 예시:
- id
- document_id
- file_id
- ref_type
- created_at

설명:
- 예: 본문 이미지, PDF 첨부, 기타 자산 참조
- `ref_type`은 `image`, `attachment`, `embed_asset` 등으로 나눌 수 있다.

### 6. document_links
문서 간 연결 관계를 저장한다.

주요 컬럼 예시:
- id
- vault_id
- source_document_id
- target_document_id
- relation_type
- raw_target
- created_at

설명:
- 문서 링크, wikilink, 임베드 등을 구조화해 저장한다.
- `relation_type` 예시:
  - `wikilink`
  - `markdown_link`
  - `embed_document`
  - `embed_html`
  - `external`

### 7. tags
문서 분류용 태그다.

주요 컬럼 예시:
- id
- vault_id
- name
- normalized_name
- created_at

### 8. document_tags
문서와 태그의 다대다 관계를 푼 연결 테이블이다.

주요 컬럼 예시:
- document_id
- tag_id

### 9. users
시스템 사용자다.

주요 컬럼 예시:
- id
- username
- email
- status
- created_at

설명:
- 초기 MVP에서는 최소 수준으로 시작할 수 있다.
- 향후 권한 모델 확장 시 중심 엔티티가 된다.

### 10. permissions
사용자 또는 주체와 리소스 간 권한 관계를 저장한다.

주요 컬럼 예시:
- id
- subject_type
- subject_id
- resource_type
- resource_id
- action
- effect
- created_at

설명:
- 초기에는 단순 공개/비공개 수준으로 시작하고, 장기적으로 확장한다.

## Core Relationships

핵심 관계는 다음과 같다.

- `vaults 1:N documents`
- `vaults 1:N files`
- `documents 1:N document_versions`
- `documents N:M tags` via `document_tags`
- `documents N:M documents` via `document_links`
- `documents N:M files` via `document_file_refs`
- `users 1:N vaults`
- `users 1:N document_versions`
- `users/subjects N:M resources` via `permissions`

## Integrity Rules

초기 단계에서 중요한 무결성 규칙은 다음과 같다.

### 1. 문서는 반드시 Vault에 속해야 한다
`documents.vault_id`는 not null이어야 한다.

### 2. 대표 문서는 같은 Vault 안에 있어야 한다
`vaults.entry_document_id`는 동일 Vault의 문서를 가리켜야 한다.

### 3. 현재 버전은 해당 문서의 버전이어야 한다
`documents.current_version_id`는 반드시 같은 `document_id`를 가진 version이어야 한다.

### 4. 링크는 우선 내부 문서 기준으로 관리한다
내부 문서 링크는 `target_document_id`를 채우고, 외부 링크는 `raw_target`만 유지할 수 있다.

### 5. 파일 메타데이터와 실제 객체 키는 1:1 대응해야 한다
`files.object_key`는 유일해야 하며 실제 MinIO 객체와 대응해야 한다.

## Recommended First ERD Cut

초기 구현에서는 모든 엔티티를 한 번에 만들지 않아도 된다.
첫 번째 ERD 컷은 아래 정도면 충분하다.

필수:
- vaults
- documents
- document_versions
- files
- document_links

그 다음:
- tags
- document_tags
- users
- permissions
- document_file_refs

## Open Questions

아직 확정되지 않은 질문은 다음과 같다.

- Markdown 원본 파일 자체를 MinIO에 둘지, DB version snapshot 중심으로 갈지
- HTML 문서를 항상 독립 document로 둘지, 일부는 file asset으로만 둘지
- 사용자/권한 모델을 MVP에 포함할지
- 버전 diff를 DB에서 직접 계산할지 별도 계층에서 처리할지

이 질문들은 후속 ADR 또는 DB 상세 설계 문서에서 다룬다.

## Summary

Websidian의 ERD는 Vault를 최상위 경계로 두고, Document를 중심으로 Version, Link, File, Tag, Permission이 연결되는 구조를 가진다.
가장 중요한 특징은 HTML도 Document로 취급한다는 점, 링크와 임베드를 구조적 관계로 저장한다는 점, 파일 원본과 메타데이터를 분리한다는 점이다.