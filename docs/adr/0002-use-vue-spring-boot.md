# ADR-0002: Frontend는 Vue.js, Backend는 Spring Boot를 사용한다

## Status
Accepted

## Context
Websidian은 “문서 뷰어 UI”와 “문서/파일/권한/검색을 관리하는 서버”가 모두 중요한 프로젝트다.

- 프론트엔드는 Markdown/HTML 렌더링, 내부 링크, 그래프 뷰, 관리 화면 등의 상호작용이 필요하다.
- 백엔드는 Vault, 문서, 링크, 태그, 버전, 권한, 파일 메타데이터를 관리하고 PostgreSQL/MinIO와 연동한다.

이미 계획된 기술 스택도 Vue.js + Spring Boot 조합이며, 둘 다 생태계가 크고 문서가 풍부하다.
API 경계를 기준으로 UI와 서버를 분리해 두면, 이후 다른 클라이언트(모바일 등)를 붙이는 것도 수월하다.

## Decision
Websidian의 기본 스택을 다음과 같이 고정한다.

- Frontend: Vue.js
- Backend: Spring Boot

역할:

- Vue: 라우팅, 문서 뷰어, 그래프 UI, HTML 임베드, 관리자 화면
- Spring Boot: REST API, 인증/권한, Markdown 처리, DB/MinIO 연동, 검색 기초 로직

## Consequences
장점:

- 프론트/백엔드 책임이 분리되어 아키텍처를 명확히 잡을 수 있다.
- Vue/Spring Boot는 자료와 예제가 많아 학습/문제 해결이 용이하다.
- 나중에 같은 백엔드에 다른 UI를 붙이는 것도 가능하다.

단점:

- 개인 프로젝트 기준으로는 “2개 앱을 관리”해야 하므로 초기 셋업과 문서화 부담이 있다.
- API 계약, 에러 처리, 인증 흐름을 명시적으로 설계해야 한다.

Websidian은 실습/학습 목적도 있으므로, 이 복잡도를 감수하고 Vue + Spring Boot 구조를 사용한다.