# ADR-0001: Websidian은 Monorepo로 관리한다

## Status
Accepted

## Context
Websidian은 다음 요소를 모두 포함한다.

- Vue 기반 프론트엔드
- Spring Boot 기반 백엔드
- PostgreSQL 스키마와 마이그레이션
- MinIO 연동 설정
- Docs-as-Code 문서와 ADR

이들을 여러 GitHub 리포지토리로 나누면, 한 번의 변경이 여러 저장소를 동시에 건드릴 가능성이 크다.
개인 프로젝트에서는 멀티 레포를 운영하면서 브랜치/릴리스/이슈를 나눠 관리하는 부담이 크다.
반대로 하나의 monorepo로 두면 “코드 + 문서 + ADR”을 한 히스토리에서 볼 수 있고, 작은 규모에서는 오히려 단순하다.

## Decision
초기 Websidian은 **단일 GitHub 저장소(monorepo)** 로 관리한다.

- 하나의 저장소에 `frontend/`, `backend/`, `infra/`, `docs/`를 모두 둔다.
- 문서(기획/ADR/아키텍처)와 코드, Compose 설정을 한곳에서 버전 관리한다.
- 향후 규모가 커져서 분리가 필요할 때, 그때 가서 multi-repo로 나누는 것을 고려한다.

## Consequences
장점:

- 코드 변경과 설계·ADR 변경을 한 PR/커밋 단위로 묶기 쉽다.
- 개인 프로젝트 운영이 단순해진다.
- 초기에는 CI/CD 설정도 하나만 관리하면 된다.

단점:

- 저장소가 커지면 폴더 구조가 복잡해질 수 있다.
- 팀 규모가 커졌을 때는 모듈 단위 빌드/권한 분리가 어렵다고 느낄 수 있다.

Websidian은 개인 프로젝트이므로, 초기에는 monorepo의 단순함을 선택하고 필요할 때만 분리를 검토한다.