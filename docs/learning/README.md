# Learning Notes

## Purpose

이 폴더는 Websidian을 구현하는 데 필요한 웹 개발 기본 개념을 정리하는 학습 노트 모음이다.

이 문서들은 설계의 공식 기준 문서가 아니라, 구현을 이해하기 위한 보조 문서다.

## Rules

- 개념 중심으로 작성한다.
- 가능하면 Websidian 예시와 함께 설명한다.
- 최종 설계 결정은 `docs/architecture`, `docs/backend`, `docs/database`, `docs/frontend`, `docs/infra` 문서를 따른다.
- 하나의 문서는 하나의 주제를 다룬다.
- 너무 길어지면 분리한다.

## Learning Order

현재 학습 문서 권장 순서는 아래와 같다.

1. web-basics.md
2. http-and-rest.md
3. layered-architecture.md
4. database-basics.md
5. storage-and-minio.md
6. spring-basics.md
7. vue-basics.md
8. docker-basics.md

이 순서는 Websidian의 현재 설계 흐름을 기준으로 잡았다.
즉, 웹 기초 → 요청/응답 → 아키텍처 → 데이터 → 저장소 → 백엔드 구현 구조 → 프론트엔드 구조 → 실행 환경 순서로 이해하는 것을 권장한다.