# Commit Template

이 문서는 Websidian에서 커밋 메시지를 작성할 때 빠르게 참고하는 짧은 레퍼런스다.

## Default format

기본 형식은 아래를 사용한다.

```text
type(scope): summary
```

예:

```text
feat(document): add document detail API
fix(embed): handle missing html target
docs(readme): rewrite project overview
chore(infra): add compose env example
```

## Rules

- 한 커밋에는 한 가지 목적만 담는다
- 커밋 제목만 읽어도 무엇이 바뀌었는지 보여야 한다
- 가능한 한 짧고 명확하게 쓴다
- 구현 과정의 감정보다 변경 결과를 적는다
- 필요하면 본문에 왜 바꿨는지 추가한다

## Type guide

자주 쓰는 type은 아래를 기준으로 한다.

- `feat`
  - 새로운 기능 추가

- `fix`
  - 버그 수정
  - 잘못된 동작 보완

- `docs`
  - 문서 수정
  - README, 설계 문서, 개발 가이드 변경

- `refactor`
  - 동작 변화 없이 구조 개선
  - 책임 분리, 코드 정리, 이름 개선

- `test`
  - 테스트 추가 또는 수정

- `chore`
  - 빌드, 설정, 환경 변수, 의존성, 기타 운영성 작업

## Scope guide

scope는 변경 위치나 책임이 드러나게 적는다.

예:

- `document`
- `vault`
- `embed`
- `graph`
- `backend`
- `frontend`
- `infra`
- `compose`
- `readme`
- `development`

scope는 너무 넓거나 애매하지 않게 적는다.

좋은 예:
- `feat(document): add document detail API`
- `fix(embed): handle missing html target`
- `docs(development): add workflow guide`

조금 애매한 예:
- `feat(core): update stuff`
- `fix(app): fix bug`

## Summary guide

summary는 “무엇을 했는지”가 드러나게 쓴다.

좋은 예:
- `add document detail API`
- `handle missing html target`
- `split version query logic`
- `rename minio environment variables`

피해야 할 예:
- `update code`
- `fix issue`
- `change something`
- `final edit`

## Body guide

필요할 때만 본문을 추가한다.

이럴 때 본문을 쓴다:

- 왜 이렇게 바꿨는지 설명이 필요할 때
- 단순 제목만으로는 의도가 부족할 때
- 문서/스키마/API 계약 같은 결정이 들어갈 때

예:

```text
feat(document): add document detail API

Add a basic document detail endpoint for the current version lookup.
This change only covers single document retrieval.
Version history response is intentionally excluded for a follow-up task.
```

## Quick examples

### Feature

```text
feat(graph): add basic graph node response
```

### Fix

```text
fix(document): handle missing current version
```

### Docs

```text
docs(development): add git workflow guide
```

### Refactor

```text
refactor(embed): split html resolution logic
```

### Chore

```text
chore(compose): rename backend environment variables
```

## Before commit

커밋 전에 아래를 한 번 확인한다.

- 이 커밋이 한 가지 목적만 담고 있는가
- 제목만 읽어도 변경 의도가 보이는가
- 관련 없는 변경이 섞이지 않았는가
- 디버그 코드나 임시 코드가 남아 있지 않은가
- 문서 수정이 필요한데 빠뜨리지 않았는가

## One-line principle

커밋 메시지는 작업 로그가 아니라, 미래의 내가 다시 읽을 수 있는 변경 기록이어야 한다.