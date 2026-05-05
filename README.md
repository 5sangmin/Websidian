# Websidian

Websidian은 Obsidian 스타일의 문서 작성 경험을 웹으로 확장하기 위한 개인 문서 플랫폼이다.

이 프로젝트의 목표는 단순한 Markdown 블로그를 만드는 것이 아니다.
Vault 단위의 문서 묶음을 관리하고, 대표 문서(entry document)를 중심으로 문서를 탐색하며, Markdown/HTML 문서를 함께 다루고, 문서 간 링크·임베드·그래프 구조를 웹에서 표현하는 것을 목표로 한다.

이 저장소는 혼자 개발하는 프로젝트를 전제로 하며, 협업용 절차 문서보다 제품 방향, 핵심 설계, 데이터 구조, 실행 환경처럼 미래의 내가 다시 이해해야 할 내용을 중심으로 문서화한다.

## What this project is

Websidian은 아래 성격을 가진 프로젝트다.

- Obsidian Vault와 비슷한 개념을 웹에서 다루는 문서 시스템
- Markdown 중심이지만 HTML 문서도 내부 문서처럼 취급하는 시스템
- 문서, 첨부 파일, 링크, 임베드, 버전, 권한을 장기적으로 관리할 수 있는 구조
- 홈랩 환경에서 직접 운영 가능한 self-hosted 문서 플랫폼

## Core concepts

핵심 개념은 아래와 같다.

- `Vault`
  - 문서 묶음의 최상위 단위
  - GitHub의 repository와 비슷한 역할
- `Entry Document`
  - Vault에 처음 진입할 때 보여줄 대표 문서
  - 예: `main.md`
- `Document`
  - Markdown 또는 HTML 기반의 문서 객체
- `File Asset`
  - 이미지, PDF, 첨부 파일 등의 업로드 대상
- `Link / Embed`
  - 문서 간 연결 관계와 삽입 관계
- `Graph`
  - 문서 간 링크 관계를 시각적으로 탐색하기 위한 구조

## Goals

### 초기 목표

초기 MVP에서는 아래를 우선한다.

- Vault 생성 및 관리
- 대표 문서 지정
- Markdown 문서 저장/조회/렌더링
- HTML 문서를 내부 문서처럼 취급
- 문서 간 링크 및 기본 임베드 처리
- PostgreSQL + MinIO 기반 저장 구조
- Vue + Spring Boot 기반 애플리케이션 뼈대 구성

### 장기 목표

장기적으로는 아래를 확장한다.

- 그래프 뷰
- 백링크
- 태그/카테고리 기반 탐색
- 전문 검색
- 문서 버전 관리
- 사용자/권한 관리
- 외부 시스템 연동 가능성 확장

## Tech stack

현재 기준 기술 방향은 다음과 같다.

- Frontend: Vue.js
- Backend: Spring Boot
- Database: PostgreSQL
- Object Storage: MinIO
- Infra: Docker Compose 기반 로컬/홈랩 실행
- Host environment: Ubuntu Server VM on Proxmox

## Repository structure

현재 저장소 구조는 아래와 같다.

```text
.
├── README.md
└── docs
    ├── README.md
    ├── adr
    ├── architecture
    ├── backend
    ├── database
    ├── development
    ├── frontend
    ├── infra
    ├── operations
    └── vision
```

각 폴더의 역할은 아래와 같다.

- `docs/vision`
  - 무엇을 만들려는지 설명한다.
  - 제품 목적과 MVP 범위를 정의한다.
- `docs/architecture`
  - 시스템 구조와 핵심 개념을 설명한다.
- `docs/backend`
  - API와 백엔드 구조 방향을 설명한다.
- `docs/frontend`
  - 프론트엔드 구조 방향을 설명한다.
- `docs/database`
  - 데이터 모델과 스키마 초안을 설명한다.
- `docs/infra`
  - Compose 서비스 구조와 환경 변수를 설명한다.
- `docs/operations`
  - 개발 환경 실행 방식과 운영 메모를 설명한다.
- `docs/development`
  - 혼자 개발할 때의 작업 순서와 개발 규약을 설명한다.
- `docs/adr`
  - 중요한 설계 결정과 그 이유를 기록한다.

## Recommended reading order

이 저장소를 다시 이해하거나 작업을 시작할 때는 아래 순서를 권장한다.

1. `README.md`
2. `docs/vision/product-vision.md`
3. `docs/vision/scope-mvp.md`
4. `docs/architecture/system-context.md`
5. `docs/architecture/domain-model.md`
6. `docs/database/erd-overview.md`
7. `docs/infra/compose-services.md`
8. `docs/infra/env-vars.md`
9. `docs/operations/dev-environment.md`
10. `docs/development/development-guide.md`

## Documentation policy

이 프로젝트는 문서를 많이 만드는 것보다, 꼭 필요한 문서를 유지하는 것을 우선한다.

문서화 기준은 아래와 같다.

- 코드만 봐서는 알기 어려운 결정은 문서로 남긴다.
- 시간이 지나면 잊기 쉬운 배경과 이유를 문서로 남긴다.
- 실행 절차, 환경 변수, 데이터 구조는 문서로 남긴다.
- 코드에서 바로 드러나는 세부 구현은 문서를 과하게 늘리지 않는다.
- 문서가 실제 코드와 달라지면 문서를 우선 갱신한다.

즉, 이 저장소의 문서는 “남에게 보여주기 위한 형식적인 문서”보다 “미래의 내가 다시 프로젝트를 이해하기 위한 문서”에 가깝다.

## How work is done

이 프로젝트는 혼자 개발하는 것을 전제로 하므로, 무거운 팀 프로세스보다는 다음 흐름을 따른다.

1. 아이디어나 문제를 정리한다.
2. 관련 문서를 읽고 현재 구조를 확인한다.
3. 작업 범위를 작게 정한다.
4. 필요한 경우 문서를 먼저 보강한다.
5. 구현한다.
6. 직접 실행해서 확인한다.
7. 관련 문서를 갱신한다.
8. 다음 작업으로 넘어간다.

자세한 기준은 `docs/development/development-guide.md`에 정리한다.

## Current status

현재 Websidian은 설계와 개발 기준 문서를 정리한 상태이며, 실제 구현을 본격적으로 진행하는 초기 단계다.

즉, 지금은 문서 작성 자체가 목적이 아니라, 문서를 기준으로 아래를 실제로 만들어 가는 단계다.

- 저장소 실제 폴더 구조
- Docker Compose 기반 개발 환경
- Spring Boot 애플리케이션 뼈대
- Vue 애플리케이션 뼈대
- DB 마이그레이션
- 문서/Vault 도메인 구현

## Non-goals for now

현재 당장 하지 않는 것은 아래와 같다.

- 과도한 협업 프로세스 도입
- 복잡한 CI/CD 파이프라인
- 지나치게 무거운 문서 체계
- 너무 이른 권한/멀티유저 설계 완성
- 검색 엔진 별도 도입

## Personal development principle

이 프로젝트에서 가장 중요한 원칙은 아래 한 줄이다.

**작게 설계하고, 작게 구현하고, 바뀐 만큼만 문서를 남긴다.**

## License

TBD