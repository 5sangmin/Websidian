# Release Template

## Version

배포 버전을 적는다.

예:
- `v0.1.0`
- `v0.2.0`
- `v0.2.1`

## Release date

배포 날짜를 적는다.

예:
- `2026-05-07`

## Release type

이번 배포의 성격을 표시한다.

- [ ] Major
- [ ] Minor
- [ ] Patch

간단 기준:

- Major: 구조적 변경, 호환성 영향, 큰 방향 전환
- Minor: 새로운 기능 추가
- Patch: 버그 수정, 안정화, 소규모 개선

## Summary

이번 배포를 한두 문장으로 요약한다.

예:
- 기본 문서 조회 흐름과 HTML 문서 응답 처리를 포함한 첫 기능 배포
- 그래프 뷰 초기 버전 추가 및 렌더링 관련 버그 수정 포함

## Included changes

이번 배포에 포함된 주요 변경을 적는다.

- 변경 사항 1
- 변경 사항 2
- 변경 사항 3

가능하면 사용자/개발자 입장에서 보이는 변화 중심으로 적는다.

## Related PRs

이번 배포에 포함된 PR 또는 작업 단위를 적는다.

- `feat(document): add document detail API`
- `fix(embed): handle html rendering fallback`
- `docs(development): add workflow guide`

PR 번호가 있다면 함께 적는다.

- `#12 feat(document): add document detail API`

## Scope by area

영역별로 어떤 변화가 들어갔는지 적는다.

### Backend
- 예: Document 단건 조회 API 추가
- 예: HTML 타입 문서 응답 처리 보완

### Frontend
- 예: 문서 상세 화면의 타입 분기 처리 추가
- 예: 없음

### Database
- 예: 없음
- 예: document_version 인덱스 추가

### Infra
- 예: compose 환경 변수 이름 정리
- 예: 없음

### Docs
- 예: `docs/backend/api-overview.md` 갱신
- 예: `docs/development/development-guide.md` 링크 추가

## Out of release scope

이번 배포에 포함하지 않은 항목을 적는다.

- 제외한 항목 1
- 제외한 항목 2

이 항목은 “이번에 무엇을 일부러 안 올렸는지”를 분명히 하기 위해 적는다.

## Verification before release

배포 전에 확인한 항목을 체크한다.

### Common
- [ ] 포함할 변경 범위가 명확하다
- [ ] 관련 없는 변경이 섞이지 않았다
- [ ] 필요한 문서가 반영되었다
- [ ] 배포 버전이 적절하다

### Backend
- [ ] 애플리케이션이 정상 기동한다
- [ ] 핵심 API가 정상 응답한다
- [ ] DB 연결에 문제가 없다
- [ ] MinIO 연결에 문제가 없다

### Frontend
- [ ] 주요 화면 접근이 가능하다
- [ ] 콘솔 오류가 없다
- [ ] 핵심 사용자 흐름이 동작한다

### Infra
- [ ] Docker Compose 기동이 가능하다
- [ ] 환경 변수 누락이 없다
- [ ] 포트/볼륨 충돌이 없다

## Deployment steps

이번 배포에서 실제로 수행할 단계를 적는다.

1. `develop` 최신 상태 확인
2. 배포 범위 최종 점검
3. 문서/버전 정보 갱신
4. `develop`을 `main`으로 머지
5. 홈랩 환경에 배포
6. 정상 동작 확인
7. Git tag 생성
8. 릴리즈 기록 남기기

## Deployment notes

배포 시 주의할 점이나 수동 작업이 있으면 적는다.

- 환경 변수 변경 필요 여부
- DB migration 수행 여부
- MinIO bucket / object 경로 확인 필요 여부
- 프론트엔드 빌드/캐시 반영 여부
- 리버스 프록시 재시작 필요 여부

## Rollback notes

문제가 생겼을 때 되돌리기 위한 단서를 적는다.

- 이전 안정 버전
- 되돌려야 할 브랜치/태그
- 주의할 DB 변경 사항
- 수동 복구가 필요한 항목

예:
- previous stable: `v0.2.0`
- DB 스키마 변경이 있어 단순 코드 롤백만으로는 복구되지 않을 수 있음

## Post-release check

배포 후 확인할 항목을 적는다.

- [ ] 서비스가 정상 기동한다
- [ ] 핵심 화면 진입이 가능하다
- [ ] 핵심 API 호출이 정상이다
- [ ] 문서 조회/렌더링이 정상이다
- [ ] 로그에 치명적 오류가 없다

## Next release candidates

다음 배포 후보나 남은 작업을 적는다.

- 다음 배포 후보 1
- 다음 배포 후보 2
- 이번에 제외한 후속 작업