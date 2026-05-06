# Development Templates

이 디렉토리는 Websidian을 개발할 때 반복해서 사용하는 작업 문서 템플릿을 모아 둔 곳이다.

이 템플릿들의 목적은 팀 협업 자동화를 위한 형식 문서를 만드는 것이 아니라,  
작업 범위를 분명히 하고, 기록을 남기고, 다음에 다시 이어가기 쉽게 만드는 것이다.

즉, 이 디렉토리는 solo 개발을 위한 실무형 작업 보조 문서 모음이다.

## How to use

기본 사용 흐름은 아래와 같다.

1. 작업을 시작할 때 `issue-template.md`로 범위를 정한다.
2. 구현 중에는 필요하면 `worklog-template.md`로 진행 상황과 결정을 기록한다.
3. 커밋 전에는 `commit-template.md`를 참고해 메시지를 정리한다.
4. 작업이 정리되면 `pr-template.md`로 self-review와 변경 요약을 남긴다.
5. 배포 시점에는 `release-template.md`로 버전과 배포 기록을 남긴다.

모든 템플릿을 매번 다 써야 하는 것은 아니다.
작업 크기와 성격에 따라 필요한 템플릿만 사용하면 된다.

## Templates

### `issue-template.md`

작업을 시작하기 전에 범위를 고정하기 위한 템플릿이다.

주로 아래를 정리한다.

- 왜 이 작업을 하는가
- 이번 작업에서 무엇을 할 것인가
- 이번 작업에서 무엇을 하지 않을 것인가
- 어떤 문서를 먼저 확인해야 하는가
- 완료 기준은 무엇인가

작업이 커지거나 애매할수록 먼저 작성하는 편이 좋다.

### `pr-template.md`

PR 또는 self-review 시점에 사용하는 템플릿이다.

주로 아래를 정리한다.

- 이번 변경의 목적
- 실제 반영된 범위
- 직접 확인한 내용
- 문서 반영 여부
- 후속 작업

### `commit-template.md`

커밋 메시지를 작성할 때 참고하는 짧은 레퍼런스 문서다.

주로 아래를 빠르게 확인한다.

- 기본 형식
- type / scope 사용 기준
- 좋은 예시와 피해야 할 예시
- 커밋 전 체크 항목

길게 쓰기보다 빠르게 보고 바로 적용하는 용도로 사용한다.

### `release-template.md`

배포 시점의 버전 기록과 배포 체크리스트를 남기기 위한 템플릿이다.

주로 아래를 정리한다.

- 배포 버전
- 배포 요약
- 포함된 변경
- 배포 전 확인 항목
- 배포 후 확인 항목
- 롤백 메모

홈랩 환경에서 어떤 상태를 언제 배포했는지 추적하기 위해 사용한다.

### `worklog-template.md`

작업 세션 단위의 메모를 남기기 위한 템플릿이다.

주로 아래를 정리한다.

- 오늘 작업 목표
- 실제로 한 일
- 내린 결정
- 확인한 내용
- 막힌 점
- 다음 작업

작업을 중간에 끊어도 다음에 빠르게 복귀하기 위해 사용한다.

## Recommended usage

작업 크기에 따라 아래 정도로 나누어 쓰면 된다.

### Small task

예:
- 오타 수정
- 작은 버그 수정
- 단순 문서 갱신

권장:
- 필요하면 `worklog-template.md`
- `commit-template.md`

### Normal task

예:
- API 하나 추가
- 화면 기능 하나 추가
- compose 설정 일부 정리

권장:
- `issue-template.md`
- `commit-template.md`
- `pr-template.md`

### Larger task

예:
- 도메인 구조 조정
- 프론트/백엔드 양쪽 변경
- 배포까지 포함된 작업

권장:
- `issue-template.md`
- `worklog-template.md`
- `commit-template.md`
- `pr-template.md`
- `release-template.md`

## Notes

이 템플릿들은 자동 생성 규칙이 아니라 사람이 직접 복사해서 현재 작업 맥락에 맞게 다듬어 쓰는 것을 기본으로 한다.

형식을 지키는 것 자체보다 중요한 것은 아래다.

- 작업 범위를 작게 유지하는 것
- 왜 이렇게 했는지 남기는 것
- 문서와 코드를 같이 움직이게 하는 것
- 다음에 다시 이어가기 쉽게 만드는 것

## Related documents

함께 보면 좋은 문서는 아래와 같다.

- [`../development-guide.md`](../development-guide.md)
- [`../git-workflow-and-ai-usage-rules.md`](../git-workflow-and-ai-usage-rules.md)