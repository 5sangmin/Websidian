# System Context

## Purpose

이 문서는 Websidian이 외부 세계와 어떻게 상호작용하는지 정의한다.
목적은 시스템 내부 구현을 설명하는 것이 아니라, Websidian의 경계와 주요 사용자, 외부 시스템, 데이터 흐름을 높은 수준에서 정리하는 것이다.

Websidian은 단순한 정적 사이트가 아니라, 문서 뷰어, 문서 관리, 파일 저장, 링크/임베드 처리, 권한 제어를 포함하는 문서 플랫폼이다.
따라서 먼저 “이 시스템이 무엇과 연결되는지”를 분명히 해야 한다.

## System Overview

Websidian은 Obsidian 스타일의 Vault를 웹에서 공유하고 탐색할 수 있게 만드는 시스템이다.
사용자는 Vault를 만들고 문서와 파일을 등록하며, 방문자는 대표 문서부터 시작해 Markdown과 HTML 문서를 탐색한다.

Websidian은 특히 다음 특성을 가진다.

- Markdown 문서를 읽기 좋게 렌더링한다.
- HTML 문서도 내부 문서처럼 취급해 링크와 임베드에 포함한다.
- 파일 메타데이터와 실제 파일 저장소를 분리한다.
- 홈랩 환경에서 Docker Compose 기반으로 운영 가능하도록 설계한다.

## Primary Actors

### 1. Owner
Websidian을 설치하고 운영하는 사용자다.
초기 단계에서는 사실상 시스템 관리자이자 콘텐츠 작성자다.

주요 행위:
- Vault 생성
- 문서 업로드
- 대표 문서 지정
- 공개/비공개 설정
- 시스템 운영 및 배포

### 2. Author
문서를 작성하고 수정하는 사용자다.
초기 MVP에서는 Owner와 동일 인물일 수 있다.

주요 행위:
- Markdown 문서 등록
- HTML 문서 등록
- 파일 업로드
- 문서 링크 및 임베드 구성

### 3. Reader
공개된 Vault를 방문해 문서를 읽고 탐색하는 사용자다.

주요 행위:
- 대표 문서 읽기
- 내부 링크 따라 이동
- HTML 문서 보기
- 임베드된 문서 또는 데모 상호작용

## External Systems

### 1. PostgreSQL
Websidian의 구조적 메타데이터 저장소다.

저장 대상:
- Vault
- Document
- Document Version
- Link
- Tag
- Permission
- File metadata

### 2. MinIO
실제 파일 원본을 저장하는 객체 스토리지다.

저장 대상:
- Markdown 원본 파일
- HTML 원본 파일
- 이미지
- PDF
- 기타 첨부파일

### 3. GitHub
소스 코드와 Docs-as-Code 문서를 관리하는 저장소 플랫폼이다.

역할:
- monorepo 호스팅
- ADR 및 문서 버전 관리
- 이슈/PR/워크플로우 관리

### 4. Browser
사용자가 Websidian에 접근하는 기본 클라이언트다.

역할:
- 문서 렌더링 UI 표시
- HTML 임베드 표시
- 내부 링크/라우팅 수행
- 사용자 상호작용 처리

### 5. Docker Compose Environment
개발 및 홈랩 운영 환경이다.

역할:
- 프론트엔드/백엔드/DB/스토리지 실행
- 로컬 개발 환경 구성
- 운영 환경의 최소 배포 단위 제공

## System Boundary

Websidian 시스템 내부에는 다음 논리 요소가 포함된다.

- Frontend Application (Vue)
- Backend API (Spring Boot)
- Document Rendering Logic
- Link/Embed Resolution Logic
- Access Control Logic
- File Metadata Management

이들 바깥에는 다음이 있다.

- PostgreSQL
- MinIO
- GitHub
- 사용자 브라우저
- 호스트 인프라(Proxmox, Ubuntu, Docker)

## High-Level Interactions

### 문서 조회 흐름
1. Reader가 브라우저에서 Websidian에 접속한다.
2. Frontend가 Backend API에 문서 메타데이터를 요청한다.
3. Backend는 PostgreSQL에서 문서/링크/파일 메타데이터를 조회한다.
4. 필요 시 MinIO에서 실제 파일 또는 렌더링 대상 파일 정보를 읽는다.
5. Frontend는 Markdown 또는 HTML 문서를 적절한 렌더러로 표시한다.

### 문서 등록 흐름
1. Author가 문서 또는 파일을 업로드한다.
2. Backend가 메타데이터를 PostgreSQL에 저장한다.
3. 실제 파일 원본은 MinIO에 저장한다.
4. 문서 링크와 임베드 관계를 분석해 저장한다.

### 문서 탐색 흐름
1. Reader가 대표 문서에서 시작한다.
2. 내부 링크나 임베드를 따라 다른 문서로 이동한다.
3. HTML 문서도 같은 문서 그래프 안에서 탐색된다.

## Context Diagram Narrative

시스템 컨텍스트 관점에서 Websidian은 다음처럼 이해할 수 있다.

- 사람:
  - Owner / Author / Reader
- 시스템:
  - Websidian
- 외부 의존성:
  - PostgreSQL
  - MinIO
  - GitHub
  - Docker/호스트 인프라

Owner와 Author는 Websidian에 콘텐츠와 운영 변경을 가하고, Reader는 콘텐츠를 소비한다.
Websidian은 메타데이터를 PostgreSQL에, 파일 원본을 MinIO에 저장한다.
개발과 문서 변경은 GitHub를 통해 추적되며, 실제 실행은 Docker Compose 기반 환경에서 이뤄진다.

## Key Architectural Constraints

초기 단계에서 중요한 제약은 다음과 같다.

### 1. 홈랩 환경 우선
Websidian은 클라우드 SaaS보다 먼저 홈랩/개인 운영 환경에서 안정적으로 동작해야 한다.

### 2. 파일과 메타데이터 분리
문서 메타데이터와 실제 파일 원본은 분리한다.

### 3. HTML의 내부 문서화
HTML은 외부 웹페이지가 아니라 내부 문서 노드로도 다뤄야 한다.

### 4. Docs-as-Code
문서, ADR, 설계 산출물은 GitHub 저장소 안에서 버전 관리한다.

## Out of Scope for This Document

이 문서는 아래 내용을 상세히 다루지 않는다.

- ERD 상세 컬럼 정의
- API 엔드포인트 명세
- UI 레이아웃 설계
- 배포 자동화 상세 단계
- 클래스/컴포넌트 수준 설계

이 내용은 각각 별도 문서에서 다룬다.

## Summary

Websidian은 Owner/Author/Reader가 사용하는 문서 플랫폼이며, GitHub, PostgreSQL, MinIO, Docker Compose 환경과 상호작용한다.
핵심은 문서 탐색 경험을 제공하는 애플리케이션으로서 Markdown과 HTML을 함께 다루고, 메타데이터와 파일 저장을 분리해 운영하는 것이다.