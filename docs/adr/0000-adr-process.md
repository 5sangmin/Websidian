# ADR-0000: ADR 운영 규칙을 정의한다

## Status
Accepted

## Context
Websidian은 장기적으로 발전시킬 개인 프로젝트이지만, 구조적인 결정이 많다.
예를 들어 Vault 모델, Markdown/HTML 통합 방식, 스토리지 구조, 저장소 구조 같은 것들은 한 번 정하면 나중에 크게 바꾸기 어렵다.
이런 결정들을 커밋 메시지나 이슈 댓글에만 두면 나중에 “왜 이렇게 했는지”를 찾기 어렵다.
반대로 너무 사소한 것까지 ADR로 남기면 개인 프로젝트 규모에 비해 문서가 과도하게 많아진다.

## Decision
Websidian에서는 중요한 아키텍처 결정을 ADR(Architecture Decision Record)로 기록한다.

운영 규칙은 다음과 같다.

- 위치: `docs/adr/` 디렉터리 아래에 둔다.
- 파일명: `NNNN-short-title.md` 형식을 쓴다. NNNN은 4자리 숫자, 0000부터 시작한다.
- 섹션: 최소한 `Status`, `Context`, `Decision`, `Consequences` 섹션을 포함한다.
- 작성 기준:
  - 되돌리기 어려운 결정
  - 여러 계층(프론트/백/DB/인프라)에 동시에 영향을 주는 결정
  - 향후 다시 논의될 가능성이 높은 결정만 ADR로 남긴다.
- 변경:
  - 기존 결정을 뒤집을 때는 새 ADR을 작성하고, 이전 ADR의 `Status`를 `Superseded`로 바꾼다.
  - 작은 구현 변경, 리팩토링, 버그 수정 등은 ADR로 남기지 않는다.

## Consequences
중요한 결정의 맥락을 잃지 않고 추적할 수 있다.
Docs-as-Code 방식으로 코드와 설계 결정을 함께 버전 관리할 수 있다.
반면 ADR을 남기는 데 약간의 시간 비용이 들며, 기준을 지키지 않으면 문서만 쌓일 수 있다.
그래서 Websidian에서는 “정말 중요한 결정에만 ADR을 작성한다”는 원칙을 유지한다.