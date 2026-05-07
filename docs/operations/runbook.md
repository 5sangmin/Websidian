# Runbook

## Purpose

이 문서는 Websidian의 개발 및 홈랩 운영 절차를 표준화하기 위한 runbook이다.
목적은 서비스를 시작하고, 상태를 확인하고, 장애를 진단하고, 필요한 경우 안전하게 중지하거나 복구하는 절차를 한곳에 정리하는 것이다.

Runbook은 단순 참고 문서가 아니라, 반복 가능한 운영 절차를 문서화한 실행 지침이다.
초기 Websidian에서는 자동화보다 먼저 수동 절차를 명확히 정리하는 것이 중요하다.

## Scope

이 runbook은 다음 상황을 다룬다.

- 개발 환경 시작
- 개발 환경 중지
- 상태 점검
- 일반 장애 대응
- 데이터 초기화 주의사항
- 기본 복구 흐름

초기 기준 대상 서비스:
- frontend
- backend
- postgres
- minio

## Preconditions

실행 전 다음을 확인한다.

- Docker Engine이 동작 중인지 확인
- Docker Compose plugin 사용 가능 여부 확인
- `.env` 파일 존재 여부 확인
- 필요한 포트가 이미 다른 프로세스에 점유되지 않았는지 확인
- 저장소 최신 상태인지 확인

이 기본 확인은 환경 문제를 빠르게 줄이는 데 도움이 된다.

## Standard Startup Procedure

### A. Hybrid Development Start

목표:
- PostgreSQL, MinIO는 Compose로 실행
- frontend, backend는 로컬 프로세스로 실행

절차:
1. 프로젝트 루트로 이동
2. `.env` 파일 확인
3. Compose로 `postgres`, `minio` 시작
4. PostgreSQL health 확인
5. MinIO readiness 확인
6. backend 로컬 실행
7. backend health endpoint 확인
8. frontend 로컬 실행
9. 브라우저에서 메인 페이지 접근 확인

검증 항목:
- DB 연결 성공
- MinIO bucket 접근 가능
- API health 응답 정상
- frontend에서 문서 목록 또는 샘플 화면 표시

### B. Full Compose Start

목표:
- 모든 서비스를 Compose로 실행

절차:
1. `.env` 파일 확인
2. 전체 Compose 서비스 시작
3. `postgres` health 확인
4. `minio` health 확인
5. `backend` health 확인
6. `frontend` 접근 확인
7. 샘플 API 호출 또는 샘플 Vault 접근 확인

## Standard Shutdown Procedure

서비스 중지는 다음 원칙으로 수행한다.

- 일반 중지는 graceful shutdown 우선
- 데이터 삭제가 포함된 명령은 별도 절차로 분리
- 개발 중지와 데이터 파괴를 같은 명령으로 묶지 않는다.

절차:
1. frontend 로컬 프로세스 종료
2. backend 로컬 프로세스 종료
3. 필요 시 Compose 서비스 중지
4. 로그 확인 후 종료 완료 확인

주의:
- 일반 `down`은 데이터를 지우지 않아야 한다
- volume 삭제는 별도 명시 작업으로만 수행한다

## Health Check Procedure

문제 발생 전후로 아래 항목을 확인한다.

### 1. Container Status
확인 항목:
- postgres 컨테이너 실행 여부
- minio 컨테이너 실행 여부
- 필요 시 backend/frontend 컨테이너 실행 여부

### 2. Port Availability
확인 항목:
- PostgreSQL 포트
- MinIO API/Console 포트
- Backend 포트
- Frontend 포트

### Backend health endpoint

현재 기준 backend 애플리케이션 헬스 체크 엔드포인트는 Spring Boot Actuator의 기본 경로를 사용한다.

- URL: `GET /actuator/health`
- 정상 응답 예:
  - 상태 코드: `200 OK`
  - Body(예시): `{"status":"UP"}`
- 확인 방법 (로컬 기준):

```bash
curl -s http://localhost:8080/actuator/health
```

- `status`가 `UP`이면 backend 프로세스와 기본 의존성(DB 연결 등)이 정상으로 간주한다.
- `DOWN` 또는 오류 응답인 경우, 먼저 DB/MinIO 연결 설정과 Compose 컨테이너 상태를 확인한다.

### 4. Dependency Connectivity
확인 항목:
- backend -> postgres 연결
- backend -> minio 연결
- frontend -> backend API 연결

## Common Failure Scenarios

### Scenario 1: Backend starts but DB connection fails
증상:
- backend는 뜨지만 DB 연결 오류 발생
- 마이그레이션 실패
- health endpoint 비정상

확인:
- `POSTGRES_*` 변수 값 확인
- `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` 확인
- postgres 컨테이너 상태 확인
- DB 포트 충돌 여부 확인

조치:
- postgres health 확인
- 잘못된 환경 변수 수정
- backend 재시작
- 필요 시 DB 로그 확인

### Scenario 2: MinIO connection fails
증상:
- 파일 업로드 실패
- backend에서 object storage 연결 오류
- bucket not found 오류

확인:
- `MINIO_ENDPOINT`, access key, secret key 확인
- MinIO 서비스 상태 확인
- bucket 존재 여부 확인

조치:
- MinIO credentials 수정
- bucket bootstrap 재실행
- backend 재시작

### Scenario 3: Frontend cannot reach backend
증상:
- 화면은 뜨지만 API 호출 실패
- CORS 또는 네트워크 오류 발생

확인:
- `VITE_API_BASE_URL` 값 확인
- backend 포트 확인
- 브라우저 개발자 도구 네트워크 탭 확인

조치:
- 잘못된 API base URL 수정
- backend health 확인
- CORS 설정 확인

### Scenario 4: Document loads but embedded HTML fails
증상:
- Markdown 문서는 보이지만 HTML 임베드가 비정상
- iframe 렌더링 실패

확인:
- HTML 문서 메타데이터 확인
- 파일 저장 위치 확인
- embed URL 또는 렌더링 정책 확인
- 브라우저 콘솔 오류 확인

조치:
- HTML 문서 URL 확인
- sandbox/CSP 정책 점검
- backend 응답 형식 확인

### Scenario 5: Uploaded file metadata exists but file is missing
증상:
- DB에는 파일 정보가 있으나 실제 다운로드 실패

확인:
- `files.object_key` 확인
- MinIO 내부 객체 존재 여부 확인
- 업로드 중 실패 로그 확인

조치:
- 고아 메타데이터 여부 확인
- 재업로드 또는 정리 작업 수행
- 무결성 점검 스크립트 필요 여부 검토

## Troubleshooting Checklist

장애 시 아래 순서로 확인한다.

1. 최근 변경 사항 확인
2. 환경 변수 변경 여부 확인
3. 컨테이너 상태 확인
4. 서비스 로그 확인
5. health endpoint 확인
6. 네트워크/포트 충돌 확인
7. DB/스토리지 연결 확인
8. 필요한 경우 마지막 변경 롤백 검토

## Logs to Inspect

우선 확인할 로그:

- backend application logs
- postgres container logs
- minio container logs
- frontend dev server logs
- browser console/network logs

원칙:
- 에러 메시지 복사
- 시간대 일치 여부 확인
- 같은 요청에 대해 frontend/backend 양쪽 로그를 함께 본다

## Data Reset Procedure

데이터 초기화는 위험 작업이므로 별도 절차로 관리한다.

원칙:
- 기본 run/start 명령에는 포함하지 않는다
- 실행 전 백업 또는 재현 가능성 확인
- 어떤 데이터가 삭제되는지 명확히 표시

초기화 후보:
- PostgreSQL schema drop/recreate
- seed data 재적재
- MinIO test object clear

## Rollback Guidance

변경 후 문제가 생기면 다음을 우선 검토한다.

- 최근 `.env` 변경 되돌리기
- 최근 Compose 변경 되돌리기
- 최근 backend/frontend 코드 변경 되돌리기
- DB 마이그레이션 영향 확인

원칙:
- 상태 저장 데이터가 있는 변경은 무작정 되돌리지 않는다
- 되돌릴 수 없는 작업은 실행 전 백업 또는 스냅샷 고려

## Runbook Maintenance Policy

runbook은 한 번 쓰고 끝나는 문서가 아니다.

원칙:
- 환경이 바뀌면 즉시 문서 갱신
- 장애를 겪은 뒤에는 runbook 보완
- 새 스크립트가 생기면 절차도 함께 갱신
- 오래된 명령과 경로는 남기지 않는다

## Summary

Websidian runbook은 개발 및 홈랩 운영 절차를 반복 가능하게 만들기 위한 기준 문서다.
핵심은 시작, 중지, 상태 점검, 장애 대응, 롤백, 데이터 초기화 절차를 분리해 문서화하고, 실제 환경 변화에 맞춰 지속적으로 갱신하는 것이다.