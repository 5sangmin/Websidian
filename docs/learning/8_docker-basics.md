# Docker Basics

## Purpose

이 문서는 Websidian을 구현하기 전에 알아두면 좋은 Docker와 Docker Compose의 기본 개념을 정리한다.

설명 방식은 가능한 한 다음 흐름을 따른다.

- 기존에는 무엇이 문제였는가
- 그 문제를 해결하기 위해 어떤 개념이 등장했는가
- 그 개념은 실제로 무엇을 해결하는가
- Websidian에서는 이 개념이 어디에 연결되는가

이 문서는 학습 문서다. 실제 Websidian의 개발/실행 환경 방향은 `docs/operations/dev-environment.md`, `docs/infra/compose-services.md`, `docs/infra/env-vars.md` 문서를 기준으로 본다. 

## 1. 왜 Docker를 먼저 이해해야 하는가

Websidian은 단일 프로그램 하나만 실행하면 끝나는 프로젝트가 아니다. 프론트엔드(Vue), 백엔드(Spring Boot), 데이터베이스(PostgreSQL), 객체 스토리지(MinIO)가 함께 동작해야 하고, 이들이 서로 연결되어야 한다. 

즉, 문제는 단순히 “코드를 작성하는 것”이 아니라, **여러 실행 구성요소를 안정적으로 띄우고 연결하는 것**이다. Docker는 바로 이 문제를 다루기 위해 중요해진 도구다.

혼자 개발하는 프로젝트에서도 이 개념이 중요한 이유는 같다.

- 내 노트북에서는 되는데 다른 환경에서는 안 되는 문제
- DB 버전이 달라서 실행이 안 되는 문제
- MinIO 설정이 달라 파일 업로드가 안 되는 문제
- 프로젝트를 오랜만에 다시 켰을 때 실행 방법을 잊는 문제

즉, Docker를 이해한다는 것은 단순히 컨테이너 명령어를 외우는 것이 아니라, **개발 환경을 다시 시작 가능하게 만드는 방법**을 배우는 것에 가깝다. 

## 2. Docker 이전에는 무엇이 불편했는가

### 기존 문제

Docker가 널리 쓰이기 전에도 프로그램은 당연히 실행할 수 있었다. 하지만 여러 구성요소가 함께 필요한 프로젝트에서는 환경 준비가 쉽게 복잡해졌다.

예를 들어 어떤 프로젝트를 실행하려면 다음이 필요할 수 있다.

- Java 특정 버전 설치
- Node.js 특정 버전 설치
- PostgreSQL 설치 및 사용자 생성
- MinIO 설치 및 bucket 생성
- 환경 변수 설정
- 포트 충돌 해결
- 서비스 실행 순서 맞추기

이 방식은 처음 한 번 세팅할 때도 번거롭지만, 시간이 지나면 더 큰 문제가 생긴다.

- 운영체제가 바뀌면 다시 맞춰야 한다
- 패키지 버전이 달라진다
- 시스템 전역에 설치된 프로그램끼리 충돌한다
- 문서가 없으면 실행 절차를 금방 잊는다

즉, “애플리케이션 코드” 외에 **환경을 재현하는 비용**이 매우 컸다.

### 등장한 개념

이 문제를 줄이기 위해 애플리케이션을 실행 환경과 함께 **패키징된 단위로 다루자**는 생각이 중요해졌고, 그 대표적인 도구가 Docker다.

### Websidian에 대입

Websidian 문서도 바로 이 문제를 다룬다. 개발 환경 문서는 PostgreSQL과 MinIO를 Compose로 관리하고, 상황에 따라 frontend/backend는 로컬에서 직접 실행하거나 Compose로 함께 실행하는 전략을 제시한다. 

즉, Websidian에서 Docker는 “배포용 기술”이기만 한 것이 아니라, **개발 환경을 표준화하는 기술**이기도 하다. 

## 3. 컨테이너(Container)는 왜 등장했는가

### 기존 문제

전통적으로는 프로그램을 실행할 때, 호스트 운영체제 위에 직접 설치하는 방식이 기본이었다. 문제는 여러 프로젝트를 동시에 다루면 각 프로젝트가 요구하는 실행 환경이 서로 다를 수 있다는 점이다.

예를 들어:

- 프로젝트 A는 PostgreSQL 15를 원한다
- 프로젝트 B는 PostgreSQL 16을 원한다
- 어떤 프로젝트는 Java 21을 원한다
- 다른 프로젝트는 Node 버전이 다르다

이런 상태가 되면 시스템 전역에 설치된 도구들이 서로 영향을 주기 쉽다.

### 등장한 개념

이 문제를 해결하기 위해 **컨테이너(Container)** 라는 개념이 중요해졌다.

컨테이너는 쉽게 말하면:

- 애플리케이션을 실행하는
- 비교적 격리된
- 재현 가능한 실행 단위

라고 볼 수 있다.

완전한 가상머신처럼 운영체제 전체를 따로 띄우는 것은 아니지만, 필요한 실행 환경을 묶어 독립적으로 돌릴 수 있게 해 준다.

### 왜 중요한가

컨테이너를 쓰면 다음 장점이 생긴다.

- 같은 이미지를 어디서 실행해도 비슷한 환경을 기대할 수 있다
- 프로젝트별 의존성을 분리하기 쉽다
- 시작/중지/재생성이 빠르다
- “설치된 상태”보다 “정의된 상태”를 신뢰할 수 있다

### Websidian에 대입

Websidian에서는 PostgreSQL 컨테이너, MinIO 컨테이너, backend 컨테이너, frontend 컨테이너 같은 식으로 각 구성요소를 나눠 실행할 수 있다. 

즉, 컨테이너는 Websidian 시스템 구성요소 하나하나를 실행 가능한 단위로 쪼개는 방법이다. 

## 4. 가상머신(VM)과 컨테이너는 무엇이 다른가

### 기존 문제

컨테이너를 처음 접하면 “그럼 VM이랑 뭐가 다른가?”라는 질문이 자연스럽게 생긴다.

### 등장한 개념

가상머신은 보통 운영체제 전체를 통째로 가상화한다. 즉, 각 VM마다 자체 OS를 가진다.

반면 컨테이너는 보통 호스트 커널을 공유하면서, 프로세스와 파일시스템 관점에서 격리된 실행 환경을 제공한다.

아주 단순하게 비교하면:

- **VM**: 컴퓨터 한 대를 통째로 여러 대처럼 나누는 느낌
- **컨테이너**: 같은 컴퓨터 위에서 실행 공간을 분리하는 느낌

### 왜 중요한가

이 차이 때문에 컨테이너는 보통 다음 특징을 가진다.

- 더 가볍다
- 더 빨리 뜬다
- 애플리케이션 단위 배포에 적합하다

반면 VM은 운영체제 수준 격리가 더 강하고, 인프라 단위 분리에는 여전히 유용하다.

### Websidian에 대입

Websidian의 홈랩 전제는 Proxmox 위에 Ubuntu VM이 있고, 그 안에서 Docker/Compose를 돌리는 구조다. 즉, Websidian은 **VM 위에서 컨테이너를 운영하는 구조**를 사용한다. 

이것은 두 기술을 대체 관계가 아니라, 서로 다른 층위에서 함께 쓰는 예시다.

- Proxmox: 물리 서버 위에서 VM 관리
- Ubuntu VM: Websidian 개발/실행 호스트
- Docker: VM 안에서 서비스 단위 격리 실행 

## 5. Image는 왜 필요한가

### 기존 문제

컨테이너를 실행하려면, 무엇을 기준으로 그 컨테이너를 만들지 정의가 필요하다. 단순히 “postgres 컨테이너 실행”이라고 해도, 어떤 버전인지, 어떤 기본 파일 시스템과 설정을 쓸지 알아야 한다.

### 등장한 개념

이 문제를 해결하기 위해 **이미지(Image)** 라는 개념이 중요하다.

이미지는 컨테이너를 만들기 위한 **읽기 전용 실행 템플릿**이라고 보면 된다.

쉽게 말하면:

- Image는 설계도 또는 스냅샷에 가깝고
- Container는 그 이미지로부터 실제 실행 중인 인스턴스다

예:

- `postgres:16`
- `minio/minio`
- `node:20`
- `eclipse-temurin:21`

### 왜 중요한가

이미지가 있으면 다음이 가능해진다.

- 같은 기준으로 컨테이너를 반복 생성할 수 있다
- 배포와 실행의 재현성이 높아진다
- “내 컴퓨터 상태”에 덜 의존하게 된다

### Websidian에 대입

Websidian의 PostgreSQL, MinIO 같은 서비스는 공식 이미지를 사용할 가능성이 높다. backend 와 frontend 는 직접 Dockerfile을 두고 커스텀 이미지를 빌드할 수도 있다. `compose-services.md` 도 backend/frontend는 build 또는 prebuilt image 전략을 고려하는 방향을 설명한다. 

## 6. Dockerfile은 왜 필요한가

### 기존 문제

공식 이미지로 해결되는 서비스도 있지만, 내 애플리케이션 자체는 직접 실행 환경을 정의해야 할 때가 많다.

예를 들어 Spring Boot 백엔드는:

- 어떤 JDK를 쓸지
- 어떤 JAR를 복사할지
- 어떤 포트로 띄울지

정의가 필요하다.

Vue 프론트엔드도:

- Node 기반 개발 서버로 띄울지
- 빌드된 정적 파일을 Nginx로 서빙할지

전략이 필요하다.

### 등장한 개념

이 문제를 해결하기 위해 **Dockerfile** 이 사용된다. Dockerfile은 이미지를 어떻게 만들지 적는 파일이다.

쉽게 말하면 Dockerfile은:

- 어떤 베이스 이미지를 쓸지
- 어떤 파일을 복사할지
- 어떤 명령으로 실행할지

를 선언하는 문서다.

### 왜 중요한가

Dockerfile이 있으면 애플리케이션의 실행 환경을 코드처럼 관리할 수 있다.

즉, 실행 환경도 “수동 설명”이 아니라 “재현 가능한 정의”가 된다.

### Websidian에 대입

Websidian backend 에는 Spring Boot 앱을 위한 Dockerfile, frontend 에는 Vue 앱 또는 정적 빌드 결과를 위한 Dockerfile을 둘 수 있다. 이후 Full Compose 모드에서는 이 Dockerfile들을 통해 전체 앱을 함께 띄우는 방향이 자연스럽다. 

## 7. 왜 Volume이 필요한가

### 기존 문제

컨테이너는 재생성하기 쉽다는 장점이 있지만, 반대로 컨테이너 내부에만 데이터를 두면 컨테이너를 지웠을 때 데이터도 함께 사라질 수 있다.

이 문제는 특히 DB나 파일 저장소에서 치명적이다.

예를 들어:

- PostgreSQL 컨테이너를 다시 만들었더니 데이터가 사라졌다
- MinIO 컨테이너를 지웠더니 업로드 파일도 사라졌다

### 등장한 개념

이 문제를 해결하기 위해 **Volume** 이라는 개념이 중요해졌다.

Volume은 컨테이너의 생명주기와 분리된 **지속 저장 공간**이다.

즉:

- 컨테이너는 지워도
- 볼륨은 남겨 두고
- 새 컨테이너가 그 데이터를 다시 사용할 수 있다

### 왜 중요한가

Volume이 있어야 다음이 가능하다.

- DB 데이터 유지
- 파일 저장소 데이터 유지
- 재시작과 재생성에 강한 환경 구성
- 개발 중 데이터 초기화 정책 분리

### Websidian에 대입

`compose-services.md` 와 `dev-environment.md` 모두 PostgreSQL 데이터와 MinIO 데이터를 named volume 으로 관리하는 방향을 설명한다. 

예를 들면 다음 같은 볼륨이 자연스럽다.

- `postgres-data`
- `minio-data`

즉, Websidian에서 volume은 “컨테이너는 갈아끼워도, 데이터는 유지하기 위한 장치”다. 

## 8. 왜 Network가 필요한가

### 기존 문제

컨테이너를 여러 개 띄우면, 서로 통신하는 방법도 필요해진다.

예를 들어:

- backend 는 postgres 에 접속해야 한다
- backend 는 minio 에 접속해야 한다
- frontend 는 backend API 를 호출해야 한다

그런데 각 컨테이너가 완전히 고립되어 있다면 이런 연결이 불가능하다.

### 등장한 개념

그래서 Docker는 **Network** 개념을 제공한다. 컨테이너들은 같은 네트워크에 있으면 서로 이름으로 통신할 수 있다.

예를 들어 Compose 환경에서는 다음처럼 생각하면 된다.

- backend 컨테이너는 `postgres:5432` 에 접속
- backend 컨테이너는 `minio:9000` 에 접속
- frontend 컨테이너는 `backend:8080` 에 접근

### 왜 중요한가

이 방식 덕분에 로컬 IP를 하드코딩하지 않고도 서비스 간 연결이 가능해진다.

즉, 네트워크는 컨테이너 여러 개를 하나의 애플리케이션 묶음처럼 동작하게 만든다.

### Websidian에 대입

Websidian Compose 환경에서는 frontend, backend, postgres, minio 가 같은 Compose 네트워크 안에서 동작하게 될 가능성이 크다. `compose-services.md` 도 서비스 간 연결과 startup ordering, healthcheck를 함께 고려하는 방향을 설명한다. 

## 9. 왜 환경 변수(Environment Variable)를 분리하는가

### 기존 문제

DB 주소, 비밀번호, MinIO access key, bucket 이름 같은 설정을 코드에 직접 넣으면 환경이 바뀔 때마다 수정이 필요하고, 민감한 정보가 코드 저장소에 남을 위험도 커진다. 

### 등장한 개념

이 문제를 해결하기 위해 설정값을 코드와 분리하는 방식이 중요해졌다. Docker와 Compose에서는 보통 **환경 변수(Environment Variables)** 를 통해 이런 값을 주입한다.

예를 들면:

- `POSTGRES_DB`
- `POSTGRES_USER`
- `POSTGRES_PASSWORD`
- `MINIO_ROOT_USER`
- `MINIO_ROOT_PASSWORD`
- `SPRING_PROFILES_ACTIVE`
- `DB_URL`
- `MINIO_ENDPOINT`

### 왜 중요한가

환경 변수 분리는 다음 장점을 준다.

- 개발/운영 환경 분리
- 비밀값 분리
- 이미지와 설정 분리
- 같은 이미지를 다른 환경에서 재사용 가능

### Websidian에 대입

`dev-environment.md` 와 `compose-services.md` 모두 `.env` 기반 관리와 profile 분리를 강조한다. 즉, Websidian에서는 “실행 이미지를 고정하고, 환경값은 바깥에서 주입하는 방식”이 기본 전제다. 

## 10. Docker Compose는 왜 중요해졌는가

### 기존 문제

컨테이너 하나만 실행할 때는 `docker run` 명령으로도 어느 정도 관리할 수 있다. 하지만 Websidian처럼 여러 서비스가 동시에 필요한 앱에서는 곧 복잡해진다.

예를 들어 매번 다음을 수동으로 해야 한다.

- postgres 실행
- minio 실행
- backend 실행
- frontend 실행
- 포트 매핑
- 볼륨 연결
- 네트워크 연결
- 환경 변수 전달
- 서비스 실행 순서 고려

이걸 명령어로 하나하나 입력하는 것은 금방 번거로워진다.

### 등장한 개념

이 문제를 해결하기 위해 **Docker Compose** 가 중요해졌다. Compose는 여러 컨테이너 서비스를 **하나의 선언 파일로 정의하고 함께 관리**하는 방식이다.

즉, Compose는 “멀티 컨테이너 애플리케이션의 실행 설계도”에 가깝다.

### 왜 중요한가

Compose를 쓰면 다음이 쉬워진다.

- 여러 서비스 동시 실행/중지
- 서비스 간 네트워크 자동 구성
- 볼륨/포트/환경 변수 일관된 관리
- 실행 방법 문서화 단순화
- 팀이나 미래의 내가 같은 방식으로 다시 실행 가능

### Websidian에 대입

Websidian 문서에서 Compose는 핵심 운영 단위다. 특히 초기 서비스 셋은 다음을 중심으로 잡는다. 

- `postgres`
- `minio`
- `backend`
- `frontend`
- 선택적 `proxy`
- 선택적 `minio-init`

즉, Compose는 Websidian의 여러 구성요소를 **한 번에 묶어 올리는 출발점**이다. 

## 11. Websidian은 왜 Hybrid Development를 권장하는가

### 기존 문제

모든 서비스를 항상 컨테이너로만 돌리면 구조는 깔끔하지만, 개발 중에는 불편할 수 있다.

예를 들어:

- frontend 코드를 고칠 때 즉시 HMR을 보고 싶다
- backend 코드를 IDE에서 디버깅하고 싶다
- 로그를 로컬 JVM/Node 환경에서 바로 보고 싶다

이런 요구는 개발 생산성과 직접 연결된다.

### 등장한 개념

그래서 실전 개발에서는 종종 **Hybrid Development** 전략이 쓰인다.

즉:

- 무거운 인프라(PostgreSQL, MinIO)는 Compose로 띄우고
- 자주 수정하는 앱(frontend, backend)은 로컬 개발 서버/IDE에서 실행하는 방식이다

### Websidian에 대입

`dev-environment.md` 는 바로 이 전략을 1순위 개발 방식으로 제안한다. 

예:

- PostgreSQL: Compose
- MinIO: Compose
- Backend: IntelliJ/Gradle 로컬 실행
- Frontend: Vite dev server 로컬 실행

### 왜 좋은가

이 방식은 다음 장점이 있다.

- DB와 스토리지는 재현 가능하게 유지
- 앱 코드는 빠르게 개발/디버깅 가능
- Compose에 모든 것을 억지로 넣지 않아도 됨
- 개인 프로젝트에서 개발 속도와 구조 사이 균형을 잡기 좋음

즉, Websidian에서 Docker는 “항상 모든 것을 컨테이너화하라”가 아니라, **필요한 것을 적절히 컨테이너화하라**는 방향으로 쓰인다. 

## 12. Full Compose Development는 언제 필요한가

### 기존 문제

Hybrid 모드는 개발 생산성이 좋지만, 실제로 서비스들이 함께 붙었을 때의 동작을 검증하려면 전체를 컨테이너로 묶어보는 것이 필요할 때가 있다.

### 등장한 개념

그래서 **Full Compose Development** 라는 방식도 중요해진다. 이는 frontend, backend, postgres, minio 를 모두 Compose로 실행하는 방식이다.

### 왜 중요한가

이 방식은 다음 상황에서 유용하다.

- 배포 전 전체 연결 검증
- 로컬 환경 차이 최소화
- 운영 환경과 더 비슷한 구성 테스트
- 새 머신에서 빠른 부팅

### Websidian에 대입

`dev-environment.md` 는 Hybrid 와 Full Compose 두 모드를 모두 제시한다. 즉, Websidian은 둘 중 하나만 고집하기보다, **개발 단계에서는 Hybrid, 통합 검증이나 배포 전 점검에서는 Full Compose** 같은 현실적인 접근을 택한다. [file:5]

## 13. Healthcheck와 Startup Ordering은 왜 중요한가

### 기존 문제

멀티 서비스 앱에서는 “컨테이너가 실행 중”이라는 사실만으로 충분하지 않을 때가 많다.

예를 들어:

- postgres 컨테이너는 떠 있지만 아직 연결 받을 준비가 안 됐을 수 있다
- minio 컨테이너는 떠 있지만 bucket init 이 안 됐을 수 있다
- backend 가 먼저 떠서 DB 연결에 실패할 수 있다

### 등장한 개념

이 문제를 해결하기 위해 Compose에서는 **healthcheck** 와 **startup ordering** 개념이 중요해졌다.

- healthcheck: 이 서비스가 실제로 준비되었는지 검사
- startup ordering: 어떤 서비스가 먼저 준비되어야 하는지 고려

### 왜 중요한가

이것이 없으면 컨테이너는 떠 있지만 애플리케이션은 실제로 실패하는 상태가 자주 생긴다.

### Websidian에 대입

`compose-services.md` 는 postgres, minio, backend 의 readiness 와 health 를 고려해야 한다고 설명한다. 즉, Websidian Compose는 단순 실행보다 **정상 준비 상태까지 포함해서 정의하는 방향**을 가져야 한다. 

## 14. Reverse Proxy는 왜 나중에 붙는가

### 기존 문제

개발 초기에 모든 것을 한 번에 넣으려 하면 구조는 그럴듯해 보여도 실제 복잡도는 급격히 높아진다. 특히 reverse proxy, HTTPS, 정적 파일 서빙 최적화까지 초기에 모두 붙이면 디버깅 포인트가 늘어난다.

### 등장한 개념

그래서 Compose 문서에서는 `proxy` 를 선택적 서비스로 본다. 즉, reverse proxy는 중요하지만, **초기 MVP 개발에서 항상 필수는 아니다**. 

### Websidian에 대입

초기 개발에서는:

- frontend dev server 직접 접근
- backend API 직접 접근
- postgres/minio compose 관리

정도로 충분할 수 있다.

이후 Full Compose 또는 실제 배포에 가까워질수록 Caddy/Nginx 같은 reverse proxy를 붙이는 방향이 자연스럽다. 

이것은 Websidian의 “처음부터 과한 복잡도를 넣지 않는다”는 개발 스타일과도 맞는다.

## 15. Docker를 처음 배울 때 자주 생기는 오해

### 1. Docker를 쓰면 무조건 배포까지 자동으로 해결된다고 생각하기

Docker는 실행 환경을 표준화하는 데 강력하지만, 배포 전략 전체를 대신해 주는 것은 아니다.

### 2. 모든 것을 무조건 컨테이너로 돌려야 한다고 생각하기

Websidian 문서도 Hybrid Development를 권장한다. 즉, 개발 단계에서는 인프라만 Compose로 관리하고 앱은 로컬 실행하는 것이 더 나을 수 있다. 

### 3. 컨테이너 안에 데이터를 그냥 두어도 된다고 생각하기

DB와 파일 저장소는 volume 없이 쓰면 컨테이너 재생성 시 데이터가 사라질 수 있다. 

### 4. Compose 파일만 있으면 환경 문제가 끝난다고 생각하기

실제로는 `.env`, healthcheck, startup ordering, volume 정책, 초기 데이터 정책까지 함께 봐야 한다. 

### 5. Docker와 VM은 경쟁 관계라고 생각하기

Websidian처럼 Proxmox VM 위에서 Docker를 쓰는 구조도 자연스럽다. 둘은 다른 층위의 기술이다. 

## 16. Websidian에서 Docker 흐름은 어떻게 보이는가

Websidian의 개발 환경 흐름을 간단히 정리하면 다음과 같다. 

1. Ubuntu VM 위에 Docker Engine과 Docker Compose가 준비된다. 
2. `.env` 파일로 DB, MinIO, 앱 포트 등의 값을 준비한다. 
3. Compose로 PostgreSQL과 MinIO를 먼저 띄운다. 
4. 개발 모드에 따라 backend/frontend는 로컬에서 실행하거나 Compose로 함께 띄운다. 
5. backend 는 postgres 와 minio 에 네트워크로 연결된다. 
6. frontend 는 backend API 를 호출한다. 
7. named volume 이 DB와 파일 데이터를 유지한다. 
8. 필요하면 proxy 와 init job 을 추가해 더 배포에 가까운 구성을 만든다. 

즉, Docker는 Websidian에서 “서비스 여러 개를 안정적으로 함께 실행하는 기반” 역할을 맡는다.

## 17. 지금 단계에서 꼭 잡아야 할 핵심 문장

아래 문장들만 정확히 이해해도 Docker의 큰 틀은 많이 잡힌다.

- Docker는 애플리케이션 실행 환경을 재현 가능하게 만들기 위한 도구다.
- Container는 실행 중인 격리된 단위이고, Image는 그 컨테이너를 만들기 위한 템플릿이다.
- Volume은 컨테이너와 분리된 지속 데이터 저장 공간이다.
- Network는 여러 컨테이너가 서로 통신하게 해 주는 장치다.
- Docker Compose는 여러 서비스를 하나의 선언 파일로 함께 관리하는 방식이다.
- Websidian은 PostgreSQL과 MinIO를 Compose로 관리하고, 개발 단계에서는 Hybrid 방식도 적극 활용한다. 
- Docker는 Websidian에서 코드보다 한 단계 바깥의 “실행 환경 구조”를 다루는 핵심 도구다. 

## 18. 다음 문서로 이어지는 연결

이 문서를 이해한 다음에는 다음 주제가 자연스럽게 이어진다.

- `dev-environment.md`: 실제 Websidian 개발 환경을 어떤 모드로 띄울지 더 구체적으로 이해할 수 있다. 
- `compose-services.md`: Compose 안에 어떤 서비스들을 어떻게 정의할지 구체적인 설계로 이어진다. 
- `env-vars.md`: Docker/Compose와 Spring Boot/Vue 설정이 환경 변수로 어떻게 연결되는지 이해할 수 있다.
- `spring-basics.md`: backend 컨테이너 안에서 실제로 무엇이 실행되는지 연결해서 볼 수 있다.
- `storage-and-minio.md`: MinIO 컨테이너가 왜 필요한지, 어떤 역할을 맡는지 더 구체적으로 이어진다.