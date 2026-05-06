# PR Template

## Title

이번 PR의 결과를 한 줄로 적는다.

예:
- add document detail API
- fix html rendering fallback
- reorganize compose environment variables

## Summary

이 PR이 무엇을 바꾸는지 짧게 적는다.

- 무엇을 추가했는가
- 무엇을 수정했는가
- 왜 이 변경이 필요한가

## Related issue

연결되는 이슈를 적는다.

예:
- relates to: #12
- closes: #15

이슈가 없다면 작업 목적을 한 줄로 대신 적어도 된다.

## Scope

이번 PR에서 실제로 포함한 변경 범위를 적는다.

- 변경 사항 1
- 변경 사항 2
- 변경 사항 3

가능하면 기능, 구조 정리, 문서 수정이 뒤섞이지 않게 적는다.

## Out of scope

이번 PR에서 의도적으로 제외한 항목을 적는다.

- 제외한 항목 1
- 제외한 항목 2

이 항목은 PR 범위가 불필요하게 커지는 것을 막기 위해 사용한다.

## Main changes

구현 관점에서 주요 변경 내용을 적는다.

### Backend
- 예: DocumentController에 단건 조회 API 추가
- 예: 서비스 계층에서 entry document 조회 로직 분리

### Frontend
- 예: 문서 상세 화면에서 HTML 타입 분기 처리 추가

### Database
- 예: 없음
- 예: version 테이블 인덱스 추가

### Infra
- 예: 없음
- 예: compose 환경 변수 이름 정리

### Docs
- 예: `docs/backend/api-overview.md` 반영
- 예: `docs/development/development-guide.md` 링크 추가

해당 없는 항목은 `없음`으로 적어도 된다.

## Verification

직접 확인한 항목을 체크한다.

### Common
- [ ] 변경 목적이 실제로 반영되었다
- [ ] 관련 없는 변경이 섞이지 않았다
- [ ] 디버그 코드가 남아 있지 않다
- [ ] 필요한 문서를 반영했다

### Backend
- [ ] 앱이 정상 기동한다
- [ ] 관련 API가 의도대로 응답한다
- [ ] DB 연결에 문제가 없다
- [ ] MinIO 연동에 문제가 없다

### Frontend
- [ ] 관련 화면이 정상 동작한다
- [ ] 브라우저 콘솔 오류가 없다
- [ ] API 연동이 정상 동작한다
- [ ] 기본 라우팅이 깨지지 않았다

### Infra
- [ ] Compose 기동이 가능하다
- [ ] 환경 변수 누락이 없다
- [ ] 포트/볼륨 충돌이 없다

필요 없는 영역은 체크하지 않거나 `해당 없음`으로 남긴다.

## Document sync

이번 변경과 함께 확인하거나 수정한 문서를 적는다.

- [ ] README
- [ ] `docs/vision/*`
- [ ] `docs/architecture/*`
- [ ] `docs/backend/*`
- [ ] `docs/frontend/*`
- [ ] `docs/database/*`
- [ ] `docs/infra/*`
- [ ] `docs/operations/*`
- [ ] `docs/development/*`
- [ ] 문서 수정 없음

수정한 문서가 있다면 파일 경로를 아래에 추가로 적는다.

- `docs/...`
- `docs/...`

## AI usage note

이번 PR에서 AI를 어떻게 사용했는지 간단히 적는다.

예:
- 설계 검토에 사용
- PR 본문 초안 작성에 사용
- 예외 처리 누락 점검에 사용
- 코드 리뷰 보조에 사용
- 실제 구현과 최종 수정은 직접 수행

## Review notes

self-review 또는 AI 리뷰에서 확인한 포인트를 적는다.

- 특히 다시 본 부분
- 고민했던 설계 포인트
- 아직 조금 애매한 부분
- 후속 PR에서 다룰 예정인 내용

## Next step

이 PR 이후 바로 이어질 수 있는 다음 작업을 적는다.

- 다음 작업 후보 1
- 다음 작업 후보 2