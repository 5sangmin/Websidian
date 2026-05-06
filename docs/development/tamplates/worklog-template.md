# Worklog Template

## Date

작업 날짜를 적는다.

예:
- `2026-05-07`

## Session title

이번 작업 세션을 한 줄로 적는다.

예:
- document detail API 초안 구현
- graph view 응답 구조 정리
- compose 환경 변수 구조 점검

## Goal

이번 세션에서 무엇을 끝내려는지 적는다.

- 오늘 해결하려는 문제
- 오늘 확인하려는 가설
- 오늘 마무리하려는 작은 범위

가능하면 한두 문장으로 짧게 적는다.

## Related issue / branch

연결된 이슈나 브랜치를 적는다.

- issue: `#12`
- branch: `feat/document-detail-api`

없으면 생략해도 된다.

## Related docs

이번 작업 전에 읽었거나, 작업 중 참고한 문서를 적는다.

- `docs/...`
- `docs/...`

예:
- `docs/backend/api-overview.md`
- `docs/architecture/domain-model.md`

## Starting context

작업 시작 시점의 상태를 적는다.

- 현재 어떤 문제가 있었는가
- 어디까지 구현되어 있었는가
- 무엇이 애매했는가

이 항목은 나중에 다시 봤을 때 “왜 이 작업을 시작했는지” 기억하기 위한 용도다.

## What I did

이번 세션에서 실제로 한 일을 적는다.

- 작업 내용 1
- 작업 내용 2
- 작업 내용 3

가능하면 결과가 드러나게 적는다.

예:
- DocumentController에 단건 조회 엔드포인트 추가
- HTML 문서 응답 분기 처리 로직 정리
- compose 환경 변수 이름을 `MINIO_*` 기준으로 통일

## Decisions

이번 세션에서 내린 결정이 있다면 적는다.

- 왜 이 구조를 선택했는가
- 어떤 대안을 보류했는가
- 나중에 다시 봐야 할 판단은 무엇인가

예:
- 이번 단계에서는 Markdown/HTML 공통 조회 흐름만 만들고, embed 해석은 후속 작업으로 분리
- version API는 지금 추가하지 않고 document detail 응답 안정화 후 진행

## What changed

변경된 파일, 계층, 문서를 적는다.

### Code
- `backend/...`
- `frontend/...`

### Docs
- `docs/...`

### Infra
- `compose/...`
- `.env.example`

없으면 `없음`으로 적어도 된다.

## Verification

직접 확인한 내용을 적는다.

- [ ] 앱이 정상 기동한다
- [ ] 관련 기능이 의도대로 동작한다
- [ ] 관련 없는 영역에 문제가 없다
- [ ] 문서를 함께 갱신했다

필요하면 아래를 추가한다.

- [ ] Backend API 응답 확인
- [ ] Frontend 화면 확인
- [ ] DB 연결 확인
- [ ] MinIO 연결 확인
- [ ] Compose 실행 확인

## Problems / blockers

막힌 점이나 남은 문제를 적는다.

- 현재 해결되지 않은 문제
- 추가 확인이 필요한 부분
- 지금은 미뤄 둔 이슈

이 항목은 다음 세션 진입 비용을 줄이는 데 중요하다.

## Out of scope discovered

작업하다가 새로 보였지만 이번 세션에서 일부러 하지 않은 것을 적는다.

- 나중에 처리할 문제 1
- 나중에 처리할 문제 2

이 항목은 한 세션에서 문제가 커지는 것을 막기 위해 사용한다.

## Next step

다음 세션에서 바로 이어서 할 수 있는 작업을 적는다.

- 다음 작업 1
- 다음 작업 2
- 먼저 읽어야 할 문서
- 먼저 확인해야 할 코드 위치

가능하면 “다음에 바로 손댈 수 있는 수준”으로 적는다.

## Notes

그 외 남길 메모를 적는다.

- 커밋 메시지 후보
- PR 제목 후보
- 릴리즈에 포함할지 여부
- 나중에 문서화할 결정 사항