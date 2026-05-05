# Domain Model

## Purpose

이 문서는 Websidian의 핵심 도메인 개념을 정의한다.
목적은 데이터베이스 테이블을 바로 설계하는 것이 아니라, 먼저 제품이 다루는 개념과 관계, 책임, 규칙을 명확히 하는 것이다.

Websidian은 단순 블로그가 아니라 다음 개념을 함께 다루는 문서 시스템이다.

- Vault 단위 관리
- Markdown / HTML 문서
- 링크와 임베드
- 첨부파일
- 버전
- 태그
- 사용자와 권한

따라서 DB 스키마보다 먼저 도메인 언어를 정리하는 것이 중요하다.

## Core Domain Statement

Websidian은 Vault 안의 문서와 파일을 관리하고, 이들 사이의 링크와 임베드 관계를 탐색 가능한 그래프로 구성하는 문서 플랫폼이다.

핵심은 “파일 저장”이 아니라 “문서 관계와 탐색 경험”이다.

## Ubiquitous Language

도메인에서 자주 쓰는 핵심 용어를 아래처럼 정의한다.

### Vault
문서 묶음의 최상위 관리 단위다.
GitHub의 Repository와 유사한 개념이며, 하나의 주제 또는 프로젝트를 나타낸다.

### Document
사용자가 읽고 탐색하는 기본 단위다.
Markdown 문서와 HTML 문서를 모두 포함한다.

### Entry Document
Vault의 대표 진입 문서다.
기본적으로 `main.md`를 우선 후보로 생각하지만, 시스템상 특정 문서를 대표 문서로 지정할 수 있다.

### Document Source
문서의 실제 원본 파일이다.
예: Markdown 원문, HTML 파일, 업로드된 원본 자산.

### Embed
한 문서 안에 다른 문서 또는 파일을 삽입해 보여주는 관계다.

### Link
문서 간 이동을 위한 참조 관계다.
일반 링크, wikilink, embed는 표현은 다르지만 모두 관계 모델 안에서 관리할 수 있다.

### File Asset
문서에 부속되는 실제 파일이다.
예: 이미지, PDF, 첨부 HTML, 기타 바이너리 파일.

### Tag
문서를 분류하고 탐색하기 위한 라벨이다.

### Version
문서 내용의 시점별 스냅샷이다.

### Principal
권한 부여의 주체다.
사용자 또는 향후 그룹/역할이 될 수 있다.

## Core Aggregates

초기 Websidian에서 중요하게 보는 집합은 다음과 같다.

### 1. Vault
Vault는 문서 집합의 루트 경계다.

책임:
- 문서 집합을 소유한다
- 대표 문서를 가진다
- 공개/비공개 범위를 가진다
- 태그/문서/파일의 최상위 컨텍스트를 제공한다

주요 속성:
- id
- slug
- name
- description
- visibility
- entryDocumentId
- ownerId

핵심 규칙:
- 하나의 Vault는 하나의 대표 문서를 가질 수 있다.
- 문서는 반드시 하나의 Vault에 속한다.

### 2. Document
Document는 사용자가 읽고 탐색하는 핵심 엔티티다.

책임:
- 제목, 식별자, 타입, 상태를 가진다
- Markdown 또는 HTML 문서일 수 있다
- 링크와 임베드의 출발점/도착점이 된다
- 여러 버전을 가진다

주요 속성:
- id
- vaultId
- slug
- title
- documentType (`markdown`, `html`)
- status (`draft`, `published`, `archived`)
- currentVersionId

핵심 규칙:
- Document는 하나의 Vault에만 속한다.
- Document는 하나 이상의 Version을 가질 수 있다.
- HTML 문서도 Markdown과 동일한 “문서”로 취급한다.

### 3. Document Version
문서 내용의 특정 시점을 나타낸다.

책임:
- 본문 스냅샷 저장
- 누가 언제 바꿨는지 기록
- 롤백 또는 변경 이력의 기반 제공

주요 속성:
- id
- documentId
- versionNumber
- sourceType
- contentSnapshot
- createdBy
- createdAt

핵심 규칙:
- 하나의 Document는 여러 Version을 가질 수 있다.
- 현재 표시되는 문서는 currentVersionId를 통해 결정된다.

### 4. File Asset
문서와 연결되는 실제 파일 자산이다.

책임:
- 객체 스토리지의 실제 파일과 연결
- 문서에서 참조 가능
- 메타데이터 제공

주요 속성:
- id
- vaultId
- objectKey
- filename
- mimeType
- sizeBytes
- checksum
- uploadedBy

핵심 규칙:
- 파일 원본은 MinIO에 저장된다.
- DB에는 메타데이터와 참조 정보만 저장한다.

### 5. Link Relation
문서 관계를 표현하는 엔티티다.

책임:
- 문서 간 링크 관계 저장
- 그래프 계산의 기본 데이터 제공
- 일반 링크와 임베드를 구분

주요 속성:
- id
- vaultId
- sourceDocumentId
- targetDocumentId
- relationType

`relationType` 예시:
- `wikilink`
- `markdown_link`
- `embed_document`
- `embed_html`
- `attachment`
- `external`

핵심 규칙:
- 링크는 같은 Vault 내부 문서를 우선 대상으로 한다.
- 외부 링크는 별도 타입으로 구분한다.
- 임베드는 링크의 하위 개념이 아니라 별도 관계 타입으로 관리한다.

## Supporting Concepts

### Tag
문서 분류용 개념이다.

속성:
- id
- vaultId
- name
- normalizedName

관계:
- 하나의 문서는 여러 태그를 가질 수 있다.
- 하나의 태그는 여러 문서에 연결될 수 있다.

### Permission
문서 또는 Vault 접근 제어를 표현한다.

속성 예시:
- id
- subjectType
- subjectId
- resourceType
- resourceId
- action
- effect

초기 MVP에서는 단순 공개/비공개 수준으로 시작하고, 이후 확장한다.

### User
시스템 사용자를 나타낸다.

역할:
- Vault 소유
- 문서 생성/수정
- 파일 업로드
- 향후 권한 주체 역할

## Domain Relationships

핵심 관계는 다음과 같다.

- 하나의 Vault는 여러 Document를 가진다.
- 하나의 Vault는 여러 File Asset을 가진다.
- 하나의 Document는 여러 Document Version을 가진다.
- 하나의 Document는 여러 Tag를 가질 수 있다.
- 하나의 Document는 여러 Link Relation의 source 또는 target이 될 수 있다.
- 하나의 Vault는 하나의 Entry Document를 가질 수 있다.
- 하나의 User는 여러 Vault와 Document 변경 이력에 연결될 수 있다.

## Domain Rules

초기 단계에서 중요한 비즈니스 규칙은 다음과 같다.

### 1. 문서는 반드시 Vault에 속해야 한다
Vault외 문서는 허용하지 않는다.

### 2. 대표 문서는 Vault 내부 문서여야 한다
다른 Vault의 문서를 entry document로 지정할 수 없다.

### 3. HTML도 문서다
HTML은 파일 첨부가 아니라 문서 노드로 취급한다.

### 4. 파일과 문서는 다르다
문서는 탐색의 대상이고, 파일은 참조 자산이다.
단, HTML은 예외적으로 문서가 될 수 있다.

### 5. 링크와 임베드는 별도 관계 타입으로 기록한다
나중에 그래프 뷰와 필터링 정책에 영향을 주기 때문이다.

### 6. 버전은 문서 단위로 관리한다
문서 내용 변경 이력은 버전 엔티티를 통해 관리한다.

## Domain Boundaries

초기 Websidian은 하나의 애플리케이션으로 시작하지만, 개념적으로는 다음 경계를 생각할 수 있다.

- Vault Management
- Document Management
- File Storage
- Link Graph
- Access Control
- Search

초기 구현은 모듈형 모놀리식 구조에 가깝게 가져가되, 문서와 코드에서 이 경계를 유지하는 것이 좋다.

## What This Model Is Not

이 문서는 아직 ERD가 아니다.
즉, 컬럼 타입, 인덱스, 외래키 제약, 조인 전략을 확정하는 문서가 아니다.
이 문서는 제품의 핵심 개념, 관계, 규칙을 먼저 정의해 이후 ERD와 API 설계의 기준점으로 쓰기 위한 것이다.

## Next Step

이 도메인 모델 다음에 이어서 작성할 문서는 다음과 같다.

- `docs/database/erd-overview.md`
- `docs/backend/api-overview.md`
- `docs/architecture/system-context.md`
- `docs/architecture/document-lifecycle.md`

## Summary

Websidian의 핵심 도메인은 Vault, Document, Document Version, File Asset, Link Relation이다.
이 모델의 핵심 포인트는 Markdown과 HTML을 함께 “문서”로 다루고, 링크와 임베드를 그래프 관계로 해석하며, 파일 원본과 메타데이터를 분리하는 것이다.