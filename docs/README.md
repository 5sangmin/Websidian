# Websidian

Websidian은 Obsidian Vault를 웹에서 공유하고 탐색할 수 있게 만드는 문서 플랫폼이다.

이 프로젝트의 목표는 단순한 Markdown 블로그를 만드는 것이 아니다.
Markdown 문서뿐 아니라 HTML 문서도 내부 문서처럼 링크하고 임베드하며, 그래프와 탐색 흐름 안에 함께 포함되는 시스템을 만드는 것이 핵심이다.

즉, Websidian은 다음 방향을 가진다.

- Obsidian Vault를 GitHub Repository처럼 관리한다.
- 대표 문서(main.md)를 시작점으로 탐색한다.
- Markdown 링크, Wikilink, 임베드, 첨부파일을 웹에서 자연스럽게 표현한다.
- HTML도 단순 첨부파일이 아니라 내부 문서 노드처럼 다룬다.
- 장기적으로는 문서 그래프, 백링크, 버전 관리, 권한 관리, 검색 기능까지 확장한다.

## Why

Obsidian은 개인 지식 관리와 문서 작성에는 매우 강력하지만, 웹 공유와 HTML 통합 측면에서는 아쉬움이 있다.
특히 HTML 문서는 Markdown 노트처럼 링크/임베드/탐색 흐름에 자연스럽게 들어가기 어렵다.

Websidian은 이 간극을 메우기 위해 시작했다.

- Obsidian 스타일의 문서 구조를 유지한다.
- 웹에서 읽기 좋은 방식으로 공유한다.
- Markdown과 HTML을 함께 다루는 문서 시스템을 만든다.
- 홈랩 환경에서 직접 운영 가능한 구조를 지향한다.

## Goals

현재 기준 핵심 목표는 다음과 같다.

- Vault 단위 문서 관리
- 대표 문서 기반 진입 구조
- Markdown 렌더링 및 내부 링크 이동
- HTML 문서의 내부 문서화
- 문서 간 임베드 지원
- 첨부파일 업로드 및 관리
- 장기적으로 그래프 탐색, 검색, 버전 관리, 권한 관리 확장

## Non-Goals

초기 버전에서 바로 하려는 것은 아니다.

- 완전한 Obsidian 편집기 복제
- 실시간 협업 편집
- GitHub 수준의 코드 호스팅 기능
- 복잡한 팀/조직 단위 권한 체계
- 대규모 SaaS 운영을 전제로 한 멀티테넌시

## Tech Direction

현재 기술 방향은 다음과 같다.

- Frontend: Vue.js
- Backend: Spring Boot
- Database: PostgreSQL
- Object Storage: MinIO
- Infra: Proxmox 위 Ubuntu VM + Docker Compose

## Repository Structure

초기에는 monorepo로 관리한다.

예상 구조:

- `frontend/`: Vue 애플리케이션
- `backend/`: Spring Boot 애플리케이션
- `infra/`: Docker Compose, 환경 설정
- `docs/`: Docs-as-Code 문서
- `docs/adr/`: Architecture Decision Records

이 구조는 코드와 문서를 함께 버전 관리하고, ADR과 구현 변경을 같은 히스토리 안에서 추적하기 위해 선택했다.

## Documentation

프로젝트 문서는 `docs/` 아래에서 관리한다.

우선 읽을 문서:

- `docs/vision/product-vision.md`
- `docs/vision/scope-mvp.md`
- `docs/adr/0000-adr-process.md`
- `docs/adr/0001-use-monorepo.md`
- `docs/adr/0002-use-vue-spring-boot.md`
- `docs/adr/0003-use-postgresql-minio.md`
- `docs/adr/0004-html-as-first-class-document.md`

## Current Stage

현재 Websidian은 프로젝트 정의와 아키텍처 방향을 정리하는 초기 단계에 있다.
우선순위는 다음과 같다.

1. 제품 비전과 MVP 범위 확정
2. ADR과 문서 구조 확정
3. 도메인 모델 및 저장소 구조 설계
4. Docker Compose 기반 개발 환경 구축
5. 최소 기능 제품(MVP) 구현

## Principles

Websidian은 다음 원칙을 따른다.

- 문서를 코드처럼 관리한다 (Docs-as-Code).
- 중요한 아키텍처 결정은 ADR로 남긴다.
- 구현 전에 도메인 모델과 문서 구조를 먼저 고정한다.
- MVP는 작게 시작하고, 확장은 단계적으로 한다.

## Status

이 프로젝트는 active planning 단계에 있다.
초기 목표는 “읽기 중심 공개 Vault + Markdown/HTML 통합 문서 경험”을 만드는 것이다.

## License

TBD