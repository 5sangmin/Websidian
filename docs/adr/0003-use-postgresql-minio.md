# ADR-0003: 메타데이터는 PostgreSQL, 파일 원본은 MinIO에 저장한다

## Status
Accepted

## Context
Websidian은 다음 종류의 데이터를 저장해야 한다.

- Vault, 문서, 태그, 링크, 권한, 버전 같은 **구조적 데이터**
- Markdown/HTML 원본 파일, PNG, PDF 같은 **바이너리 파일**

모든 것을 RDB 한 곳에 넣을 수도 있지만, 바이너리 파일이 늘어날수록 백업/복구/마이그레이션이 무거워진다.
실제 문서/파일 서비스에서는 메타데이터는 RDB에, 파일 원본은 객체 스토리지(S3, MinIO 등)에 두는 패턴을 많이 사용한다.

현재 Websidian 계획에서도 PostgreSQL + MinIO 조합이 1순위로 올라와 있다.

## Decision
저장소 책임을 다음처럼 나눈다.

- PostgreSQL:
  - vaults, documents, document_versions
  - links, tags, permissions
  - files 테이블(파일 메타데이터: object_key, filename, mime_type, size 등)
- MinIO:
  - 실제 Markdown/HTML 파일
  - 이미지, PDF, 기타 첨부파일 바이너리

애플리케이션은 PostgreSQL을 통해 메타데이터를 조회하고, 그 안의 object_key로 MinIO에 접근한다.

## Consequences
장점:

- 구조적 데이터와 파일 원본을 분리해 운영과 확장성이 좋아진다.
- 대용량 파일도 상대적으로 부담 적게 다룰 수 있다.
- 스토리지와 DB를 독립적으로 백업/복구/교체하기 쉬워진다.

단점:

- PostgreSQL과 MinIO 두 시스템을 함께 운영해야 한다.
- 메타데이터와 실제 파일 간 무결성(고아 파일, 누락 파일)을 관리하는 로직이 필요하다.

Websidian은 문서·파일 서비스 성격이 강하므로, 이 복잡도를 감수하고 PostgreSQL + MinIO 분리 구조를 채택한다.