# Document Lifecycle

## Purpose

이 문서는 Websidian에서 문서가 어떤 단계를 거쳐 생성, 수정, 게시, 보관되는지 정의한다.
문서 라이프사이클을 명확히 하면 상태 전이, 버전 관리, 권한 처리, 보관 정책을 일관되게 설계할 수 있다.

Websidian은 Markdown과 HTML을 모두 문서로 다루기 때문에, 단순 블로그보다 문서 상태 관리가 더 중요하다.

## Lifecycle Scope

이 문서에서 말하는 "문서"는 다음을 포함한다.

- Markdown 문서
- HTML 문서
- 장기적으로는 특정 구조화 문서 타입

파일 첨부 자체는 별도 자산 수명주기를 가지지만, 문서 라이프사이클과 밀접하게 연결된다.

## Lifecycle Stages

Websidian의 문서 라이프사이클은 **3가지 상태(status)와 삭제 플래그(deleted_at)**로 구성된다.

### 상태(status): Draft, Published, Archived

문서의 공개 수준과 탐색 가능성을 나타내는 라이프사이클 상태다.

### 삭제(deleted_at): 논리적 삭제 처리

상태와 독립적으로 관리되는 soft delete 플래그다.

---

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

### 4. Deleted (논리적 삭제)
논리적으로 삭제된 상태다.

특징:
- 사용자 기본 UI에서는 보이지 않음
- DB에서는 `deleted_at` timestamp가 NULL이 아닌 상태로 표현됨
- `status` 컬럼은 Draft/Published/Archived 중 하나를 유지하며, 삭제는 별도 처리됨
- 복구 정책이 있다면 `deleted_at`을 NULL로 되돌려 soft delete 복구 가능
- 실제 파일 삭제는 지연 처리할 수 있음

구현 방식:
- `documents.deleted_at IS NOT NULL`이면 삭제된 문서로 간주
- 삭제는 `status` 값 변경이 아니라 timestamp 기록으로 처리
- 이 방식은 라이프사이클 상태(Draft/Published/Archived)와 운영적 삭제 처리를 독립적으로 관리할 수 있게 함

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

구현 방식:
- `documents.deleted_at` 컬럼에 현재 timestamp를 기록
- `status` 값은 변경하지 않음 (Draft/Published/Archived 유지)
- 조회 시 `deleted_at IS NULL` 조건으로 활성 문서만 필터링
- 복구 시에는 `deleted_at`을 NULL로 되돌림

## Versioning Rules

Websidian은 문서 상태와 별도로 버전 이력을 관리한다.

원칙:
- 문서 수정은 기존 버전을 덮어쓰지 않고 새 버전을 생성한다.
- 하나의 문서는 여러 `document_versions`를 가지며, `documents.current_version_id`가 현재 기준 버전을 가리킨다.
- 버전은 이력 보존이 목적이므로 soft delete를 적용하지 않는다.

### First Cut 정책: 저장 시 새 버전 생성, current version 즉시 교체

초기 MVP에서는 단순화를 위해 다음 전략을 사용한다.

문서 생성:
1. `documents` 레코드 생성 (`current_version_id`는 우선 NULL)
2. `document_versions` 레코드 생성 (`version_no = 1`)
3. 생성된 version의 id로 `documents.current_version_id` 갱신

문서 수정:
1. 기존 문서 조회
2. 같은 `document_id`로 새 `document_versions` 레코드 생성
3. `version_no`는 기존 최대값 + 1
4. `documents.current_version_id`를 새 버전 id로 즉시 교체
5. `documents.updated_at` 갱신

이 방식의 특징:
- Published 문서도 수정 시 새 버전을 만들고 곧바로 교체함
- 별도 초안 버전과 공개 버전을 분리하지 않음
- 구현이 단순하고, 버전 이력은 그대로 누적됨

### 장기 검토 사항

Published 문서의 경우, 수정 시 새 Draft 버전을 만들어 검토 후 승인하는 워크플로를 도입할 수도 있다.
이는 초기 MVP 이후, 실제 사용 패턴이 생긴 뒤 검토한다.

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

### 라이프사이클 상태 전이 (`status` 컬럼)

- Draft → Published: 가능
- Draft → Archived: 가능
- Published → Draft: 가능 (재편집 또는 공개 철회 시)
- Published → Archived: 가능
- Archived → Published: 가능
- Archived → Draft: 가능 (재편집 시)

### 삭제 처리 (`deleted_at` 컬럼)

- 어떤 `status`에서든 삭제 가능 (`deleted_at`에 timestamp 기록)
- 삭제된 문서는 `status`와 무관하게 기본 UI에서 숨김 처리
- 복구 시 `deleted_at`을 NULL로 되돌리면, 이전 `status` 그대로 복원됨

주의:
- 삭제는 `status` 전이가 아니라 별도 timestamp 기록으로 처리함
- Draft → Deleted, Published → Deleted 같은 표현은 편의상 사용하지만,
  실제로는 `status`는 변하지 않고 `deleted_at`만 채워짐

직접 전이를 제한할지 여부는 운영 정책에 따라 달라질 수 있지만, MVP에서는 너무 복잡하게 만들지 않는다.

## Access Considerations

문서 상태는 접근 제어와도 연결된다.

- Draft: 작성자/관리자만 접근
- Published: 공개 Vault라면 방문자 접근 가능
- Archived: 기본 탐색에서는 숨기되 직접 접근은 허용 가능
- Deleted: 기본적으로 접근 불가
  - `deleted_at IS NOT NULL`인 문서는 기본 조회 쿼리에서 제외
  - 복구 UI나 관리자 전용 뷰에서만 조회 가능

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

### 문서 상태 관리
- 라이프사이클 상태: Draft / Published / Archived (`status` 컬럼)
- 삭제 처리: soft delete (`deleted_at` timestamp)
- `status`와 `deleted_at`은 독립적으로 관리
- 활성 문서: `deleted_at IS NULL`
- 삭제 문서: `deleted_at IS NOT NULL`

### 버전 관리
- 저장 시마다 새 버전 생성
- `documents.current_version_id`를 새 버전으로 즉시 교체
- 초안/공개 버전 분리는 MVP 이후 검토

### 기타 정책
- 대표 문서는 Published 상태 문서만 지정 가능
- HTML 문서도 같은 상태 모델을 사용
- Vault도 동일한 soft delete 정책 적용

이 정도면 구현 난도를 크게 높이지 않으면서도 향후 확장 가능한 기반을 마련할 수 있다.

## Summary

Websidian의 문서 라이프사이클은 Draft, Published, Archived 3가지 상태와 Deleted 플래그를 중심으로 설계한다.
핵심은 문서 상태와 버전 이력을 분리하고, Markdown과 HTML 모두 같은 라이프사이클 틀 안에서 관리하되, HTML은 보안 렌더링 정책을 추가로 고려하는 것이다.