# API Overview

## Purpose

이 문서는 Websidian 백엔드 API의 전체 방향을 정의한다.
목적은 모든 엔드포인트를 상세 명세하는 것이 아니라, API 스타일, 리소스 경계, 인증 방식, 응답 규칙, 페이지네이션, 버전 전략 같은 공통 원칙을 먼저 고정하는 것이다.

Websidian은 문서 플랫폼이므로, API의 중심은 “문서와 파일을 어떻게 주고받을 것인가”에 있다.
특히 Vault, Document, File, Link, Tag, Permission 같은 핵심 리소스를 일관된 방식으로 노출해야 한다.

## API Style

Websidian API는 초기 단계에서 REST 스타일을 사용한다.

원칙:
- 리소스 중심 URL을 사용한다.
- 엔드포인트 이름에 동사를 남발하지 않는다.
- HTTP 메서드의 의미를 그대로 사용한다.
- JSON을 기본 요청/응답 포맷으로 사용한다.
- 서버는 stateless하게 동작한다.

예시:
- `GET /api/v1/vaults`
- `POST /api/v1/vaults`
- `GET /api/v1/vaults/{vaultId}`
- `GET /api/v1/documents/{documentId}`

## API Versioning

초기 API는 URL prefix 기반 버저닝을 사용한다.

형식:
- `/api/v1/...`

이 방식을 선택하는 이유:
- 프론트엔드와 백엔드 계약을 명확히 할 수 있다.
- 향후 breaking change가 생겨도 새 버전을 병행할 수 있다.
- 내부 프로젝트에서도 버전 관리 기준을 단순하게 유지할 수 있다.

## Core Resources

초기 API에서 다루는 핵심 리소스는 다음과 같다.

- Vault
- Document
- Document Version
- File
- Link
- Tag
- User
- Permission

이 중 MVP에서는 Vault, Document, File이 가장 중요하고, Link는 문서 파싱 결과를 반영하는 보조 리소스로 취급한다.

## Authentication and Authorization

초기 MVP는 복잡한 권한 모델보다 단순한 접근 제어로 시작한다.

초기 방향:
- 공개 Vault는 인증 없이 조회 가능
- 관리 기능(생성, 수정, 삭제)은 인증 필요
- 인증 방식은 추후 세부 결정이 필요하지만, 초기 문서 수준에서는 세션 기반 또는 토큰 기반 중 하나를 선택할 수 있다
- 권한은 우선 `owner` 중심으로 단순화하고, 나중에 사용자/역할 기반으로 확장한다.

후속 문서에서 정해야 할 것:
- 인증 방식(JWT / session / OAuth)
- 관리자 접근 정책
- API 토큰 필요 여부

## Resource Boundaries

### Vault API
역할:
- Vault 생성
- Vault 목록 조회
- Vault 상세 조회
- Vault 수정
- 대표 문서 지정

예시:
- `GET /api/v1/vaults`
- `POST /api/v1/vaults`
- `GET /api/v1/vaults/{vaultId}`
- `PATCH /api/v1/vaults/{vaultId}`

### Document API
역할:
- 문서 생성
- 문서 조회
- 문서 수정
- 문서 게시/보관
- 문서 버전 조회
- Markdown/HTML 타입 구분 처리

예시:
- `GET /api/v1/documents/{documentId}`
- `POST /api/v1/vaults/{vaultId}/documents`
- `PATCH /api/v1/documents/{documentId}`
- `GET /api/v1/documents/{documentId}/versions`

### File API
역할:
- 파일 업로드
- 파일 메타데이터 조회
- 파일 다운로드 URL 발급
- 문서와 파일 연결 관리

예시:
- `POST /api/v1/files`
- `GET /api/v1/files/{fileId}`
- `GET /api/v1/files/{fileId}/download`

### Link API
역할:
- 문서 링크 관계 조회
- 백링크 조회
- 임베드 관계 조회

예시:
- `GET /api/v1/documents/{documentId}/links`
- `GET /api/v1/documents/{documentId}/backlinks`

### Tag API
역할:
- 태그 목록 조회
- 문서-태그 연결 조회

예시:
- `GET /api/v1/tags`
- `GET /api/v1/tags/{tagId}/documents`

## Document Retrieval Model

문서 조회는 Websidian의 핵심이다.
문서 API는 단순 본문만 주는 것이 아니라, 문서 탐색에 필요한 메타데이터를 함께 줄 수 있어야 한다.

문서 조회 응답에 포함되기 좋은 정보:
- 문서 기본 정보(id, title, slug, type, status)
- 현재 버전 정보
- 렌더링용 원문 또는 렌더링 결과
- 링크 요약
- 첨부파일 요약
- 태그 목록
- 대표 문서 여부
- HTML 임베드 가능 여부

초기에는 응답이 다소 커져도 괜찮지만, 장기적으로는 summary/detail 응답을 나눌 수 있다.

## Rendering Strategy

Websidian은 Markdown과 HTML을 모두 다루므로 API도 두 타입을 고려해야 한다.

초기 방향:
- Markdown 문서: 원문과 메타데이터를 내려주고, 렌더링은 서버 또는 클라이언트 중 한쪽에서 담당
- HTML 문서: 메타데이터와 렌더링 가능한 접근 경로를 내려줌
- 임베드 시에는 별도 embed-friendly 응답을 둘 수도 있음

후속 결정이 필요한 항목:
- Markdown 렌더링을 서버에서 할지, 프론트에서 할지
- HTML 문서 응답을 raw/metadata/embed-view로 나눌지
- presigned URL을 직접 쓸지, 백엔드 프록시를 쓸지

## Request and Response Principles

Websidian API는 다음 응답 규칙을 따른다.

### Success Responses
- `200 OK`: 일반 조회/수정 성공
- `201 Created`: 생성 성공
- `204 No Content`: 삭제 또는 본문 없는 성공 응답

### Error Responses
- `400 Bad Request`: 잘못된 입력
- `401 Unauthorized`: 인증 필요
- `403 Forbidden`: 권한 없음
- `404 Not Found`: 리소스 없음
- `409 Conflict`: 상태 충돌
- `422 Unprocessable Entity`: 의미적으로 잘못된 입력
- `500 Internal Server Error`: 서버 오류

에러 응답은 stack trace를 노출하지 않고, 클라이언트가 처리 가능한 구조적 형태를 유지한다.

예시 형식:
```json
{
  "error": {
    "code": "DOCUMENT_NOT_FOUND",
    "message": "The requested document does not exist.",
    "details": null
  }
}
```

## Pagination, Filtering, Sorting

목록성 API는 페이지네이션을 지원해야 한다.

초기 규칙:
- 표준 파라미터 사용: `page`, `size`, `sort`
- 필터 파라미터는 리소스별로 명확히 정의
- 응답에는 최소한 페이지 메타데이터 포함

예시:
- `GET /api/v1/vaults?page=1&size=20`
- `GET /api/v1/documents?status=published&sort=updatedAt,desc`

응답 예시:
```json
{
  "items": [],
  "page": 1,
  "size": 20,
  "totalElements": 135,
  "totalPages": 7
}
```

문서 수가 매우 많아지면 cursor 기반 페이지네이션을 검토할 수 있다.

## API Documentation Policy

API 문서는 코드와 가까운 곳에서 유지한다.
현재 단계에서는 Markdown 기반 개요 문서를 우선 작성하고, 구현 단계에서 OpenAPI 문서로 확장하는 것을 권장한다.

권장 방향:
- 지금: `docs/backend/api-overview.md`
- 다음: 리소스별 API 초안 문서
- 구현 이후: OpenAPI spec 생성 및 동기화

## Open Questions

아직 결정하지 않은 사항:
- 인증 방식
- Markdown 렌더링 위치
- HTML embed 응답 형식
- 파일 다운로드를 presigned URL로 할지 API 프록시로 할지
- 버전 diff API 제공 여부

이 항목들은 후속 ADR 또는 상세 API 문서에서 정한다.

## Summary

Websidian API는 REST 스타일, `/api/v1` 버전 prefix, JSON 기반 응답을 사용한다.
핵심 리소스는 Vault, Document, File이며, 문서형 서비스 특성상 링크, 버전, 임베드, 메타데이터를 함께 고려하는 설계가 필요하다.