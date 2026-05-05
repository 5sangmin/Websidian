# Document Lifecycle

## Purpose

이 문서는 Websidian에서 문서가 어떤 단계를 거쳐 생성, 수정, 게시, 보관되는지 정의한다.
문서 라이프사이클을 명확히 하면 상태 전이, 버전 관리, 권한 처리, 보관 정책을 일관되게 설계할 수 있다.

Websidian은 Markdown과 HTML을 모두 문서로 다루기 때문에, 단순 블로그보다 문서 상태 관리가 더 중요하다.

## Lifecycle Scope

이 문서에서 말하는 “문서”는 다음을 포함한다.

- Markdown 문서
- HTML 문서
- 장기적으로는 특정 구조화 문서 타입

파일 첨부 자체는 별도 자산 수명주기를 가지지만, 문서 라이프사이클과 밀접하게 연결된다.

## Lifecycle Stages

초기 Websidian의 문서 상태는 다음 4단계로 정의한다.

### 1. Draft
작성 중인 상태다.

특징:
- 아직 공개되지 않음
- 링크/임베드 파싱은 가능
- 작성자만 수정 가능
- 방문자에게는 기본적으로 노출되지 않음

### 2. Published
외부 사용자에게 공개 가능한 상태다.

특징:
- 대표 문서나 일반 문서 페이지에서 접근 가능
- 내부 링크의 탐색 대상이 될 수 있음
- HTML 임베드도 활성화 가능
- 그래프/백링크 계산 대상이 됨

### 3. Archived
보관 상태다.

특징:
- 더 이상 주요 탐색 대상은 아님
- 직접 링크로만 접근시키거나, 기본 목록에서 숨길 수 있음
- 기록 보존 용도
- 복구 가능

### 4. Deleted
논리적으로 삭제된 상태다.

특징:
- 사용자 기본 UI에서는 보이지 않음
- 복구 정책이 있다면 soft delete로 관리 가능
- 실제 파일 삭제는 지연 처리할 수 있음

## Lifecycle Events

문서 상태를 바꾸는 주요 이벤트는 다음과 같다.

### Create
새 문서를 만든다.
초기 상태는 기본적으로 `Draft`다.

### Update
문서를 수정한다.
수정 시 새 `Document Version`이 생성된다.

### Publish
문서를 공개 상태로 전환한다.
대표 문서나 일반 목록에 노출될 수 있다.

### Archive
문서를 보관 상태로 전환한다.
기존 링크는 유지할 수 있으나, 메인 탐색 흐름에서는 후순위로 둔다.

### Restore
Archived 또는 Deleted 상태의 문서를 다시 복구한다.

### Delete
문서를 삭제한다.
초기에는 soft delete를 우선 고려한다.

## Versioning Rules

Websidian은 문서 상태와 별도로 버전 이력을 관리한다.

원칙:
- 문서 수정은 기존 버전을 덮어쓰지 않고 새 버전을 생성한다.
- Published 문서도 수정 시 새 Draft 버전을 만들지, 곧바로 교체할지 정책이 필요하다.
- 초기에는 단순화를 위해 “저장 시 새 버전 생성, current version 교체” 전략을 사용할 수 있다.
- 장기적으로는 초안 버전과 공개 버전을 분리하는 모델도 검토할 수 있다.

## Attachment Lifecycle

첨부파일은 문서와 함께 다루되, 완전히 같은 라이프사이클을 따르지는 않는다.

단계 예시:
- Uploaded
- Referenced
- Orphaned
- Deleted

설명:
- 파일이 업로드되었지만 어떤 문서에도 연결되지 않으면 orphan 후보가 된다.
- 문서 삭제 후에도 다른 문서가 참조 중이면 파일은 유지해야 한다.
- orphan file 정리 정책은 별도 운영 문서로 관리한다.

## State Transition Rules

초기 상태 전이 규칙은 다음처럼 둔다.

- Draft -> Published: 가능
- Draft -> Deleted: 가능
- Published -> Archived: 가능
- Archived -> Published: 가능
- Archived -> Deleted: 가능
- Deleted -> Draft 또는 Archived: 복구 정책이 있을 경우 가능

직접 전이를 제한할지 여부는 운영 정책에 따라 달라질 수 있지만, MVP에서는 너무 복잡하게 만들지 않는다.

## Access Considerations

문서 상태는 접근 제어와도 연결된다.

- Draft: 작성자/관리자만 접근
- Published: 공개 Vault라면 방문자 접근 가능
- Archived: 기본 탐색에서는 숨기되 직접 접근은 허용 가능
- Deleted: 기본적으로 접근 불가

초기 MVP는 복잡한 역할 기반 권한보다 상태 중심 접근 제어로 시작하는 것이 현실적이다.

## HTML-Specific Considerations

HTML 문서는 일반 문서 상태 외에 렌더링 보안 고려가 추가된다.

- Published 상태의 HTML은 iframe sandbox 정책 아래에서 노출할 수 있다.
- Deleted 상태가 되어도 연결된 파일 자산 정리는 지연될 수 있다.
- HTML 문서의 외부 스크립트 허용 여부는 별도 정책으로 둔다.

## Operational Considerations

운영 측면에서 중요한 포인트는 다음과 같다.

- 상태 전이 기준을 명확히 해야 한다.
- 버전 히스토리를 남겨야 한다.
- 삭제는 즉시 물리 삭제보다 soft delete가 안전하다.
- orphan file 정리 루틴이 필요하다.
- 상태 변경 로그를 남길 수 있으면 좋다.

## Recommended MVP Policy

초기 MVP에서는 다음 정책을 권장한다.

- 문서 상태: Draft / Published / Archived
- 삭제는 우선 soft delete
- 저장 시마다 새 버전 생성
- 대표 문서는 Published 상태 문서만 지정 가능
- HTML 문서도 같은 상태 모델을 사용

이 정도면 구현 난도를 크게 높이지 않으면서도 향후 확장 가능한 기반을 마련할 수 있다.

## Summary

Websidian의 문서 라이프사이클은 Draft, Published, Archived, Deleted 상태를 중심으로 설계한다.
핵심은 문서 상태와 버전 이력을 분리하고, Markdown과 HTML 모두 같은 라이프사이클 틀 안에서 관리하되, HTML은 보안 렌더링 정책을 추가로 고려하는 것이다.