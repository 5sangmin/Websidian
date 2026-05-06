# Spring Basics

## Purpose

이 문서는 Websidian을 구현하기 전에 알아두면 좋은 Spring과 Spring Boot의 기본 개념을 정리한다.

설명 방식은 가능한 한 다음 흐름을 따른다.

- 기존에는 무엇이 문제였는가
- 그 문제를 해결하기 위해 어떤 개념이 등장했는가
- 그 개념은 실제로 무엇을 해결하는가
- Websidian에서는 이 개념이 어디에 연결되는가

이 문서는 학습 문서다. 실제 Websidian의 백엔드 구조 방향은 `docs/backend/module-structure.md`, `docs/backend/api-overview.md`, `docs/architecture/domain-model.md`, `docs/operations/development-guide.md` 문서를 기준으로 본다. 

## 1. 왜 Spring을 먼저 이해해야 하는가

웹 애플리케이션을 만든다고 할 때, 단순히 HTTP 요청을 받고 DB를 조회하는 코드만 있으면 충분해 보일 수 있다. 하지만 기능이 늘어나면 곧 요청 처리, 비즈니스 규칙, DB 접근, 설정, 예외 처리, 외부 저장소 연동 같은 서로 다른 책임이 한데 얽히기 시작한다. 

이 문제를 해결하려면 단순히 “Java로 서버를 띄우는 법”보다, **서버 애플리케이션을 어떤 구조로 만들 것인가**를 먼저 이해해야 한다. Spring은 바로 이 지점에서 중요해진다. Spring은 Java 웹 개발에서 반복되던 구조 문제를 줄이기 위한 대표적인 프레임워크이기 때문이다. 

즉, Spring을 이해한다는 것은 특정 어노테이션 몇 개를 외우는 것이 아니라, **백엔드 애플리케이션을 덜 엉키게 만드는 방식**을 배우는 것에 가깝다. Websidian처럼 Vault, Document, File, Link 같은 도메인이 함께 움직이는 시스템에서는 이 관점이 특히 중요하다. 

## 2. Spring 이전에는 무엇이 불편했는가

### 기존 문제

초기 Java 웹 개발은 가능은 했지만, 프로젝트가 커질수록 유지보수가 어려워지는 방향으로 흘러가기 쉬웠다.

예를 들어 다음과 같은 문제가 자주 생겼다.

- 요청 처리 코드와 비즈니스 로직이 한 클래스에 섞인다
- DB 접근 코드가 여러 곳에 흩어진다
- 객체 생성과 연결을 개발자가 직접 관리해야 한다
- 테스트할 때 필요한 객체를 바꿔 끼우기 어렵다
- 설정 파일이 많고 실행 환경 맞추기가 번거롭다

즉, “서버를 만들 수 있다”와 “서버를 오래 유지할 수 있다”는 전혀 다른 문제였다.

### 등장한 개념

이 문제를 해결하기 위해 Java 진영에서는 점점 더 **구조적인 애플리케이션 프레임워크**가 중요해졌고, 그 흐름에서 Spring이 널리 쓰이게 되었다.

Spring의 핵심 목적은 단순하다.

- 객체를 더 잘 분리하게 돕고
- 의존 관계를 느슨하게 만들고
- 웹, DB, 설정, 테스트를 일관되게 다루게 하는 것

### Websidian에 대입

Websidian 백엔드는 단순 CRUD 몇 개로 끝나는 구조가 아니다. Vault, Document, File, Link, Version 같은 개념을 함께 다루고, PostgreSQL과 MinIO도 연결해야 한다. 이런 시스템에서 기술 코드가 뒤섞이면 시간이 갈수록 수정 비용이 커진다. 그래서 Websidian도 백엔드를 도메인 중심 구조와 레이어 분리를 갖춘 Spring Boot 애플리케이션으로 가져가려는 방향을 택하고 있다. 

## 3. Spring은 무엇을 해결하려고 등장했는가

### 기존 문제

애플리케이션이 커질수록 개발자는 본질적인 문제보다 부수적인 문제에 더 많은 시간을 쓰기 쉬웠다.

- 이 객체는 어디서 만들지
- 저 클래스는 어떤 구현체를 써야 하지
- DB 연결 객체는 누가 관리하지
- 테스트할 때는 어떤 대체 객체를 넣지
- 설정값은 코드에 둘지 파일에 둘지

즉, 도메인 규칙을 구현하기보다 “프로그램 조립” 자체가 큰 부담이 되었다.

### 등장한 개념

Spring은 이런 문제를 줄이기 위해 **객체 관리**, **의존성 연결**, **계층 분리**, **설정 외부화** 같은 관점을 체계적으로 제공했다.

쉽게 말하면 Spring은 이렇게 바꿔 준다.

- 예전: 필요한 객체를 내가 직접 만들고 연결한다
- Spring: 필요한 객체를 등록해 두고 프레임워크가 연결해 준다

### 왜 중요한가

이 차이는 단순히 코드가 짧아지는 수준이 아니다.

- 클래스 간 결합이 줄어든다
- 테스트가 쉬워진다
- 구현체를 교체하기 쉬워진다
- 역할별 책임을 분리하기 쉬워진다

즉, Spring은 단순 편의 도구가 아니라 **구조를 강제하지는 않지만, 좋은 구조를 만들기 쉽게 하는 기반**이다.

## 4. DI(Dependency Injection)는 왜 중요해졌는가

### 기존 문제

기존 방식에서는 어떤 클래스가 다른 클래스를 필요로 하면 내부에서 직접 `new`로 객체를 만들기 쉬웠다.

예를 들어:

- controller가 service를 직접 만든다
- service가 repository를 직접 만든다
- repository가 DB 연결 객체를 직접 만든다

이렇게 되면 코드가 빠르게 단단하게 묶인다.

- 구현체를 바꾸기 어렵다
- 테스트용 가짜 객체를 넣기 어렵다
- 생성 위치가 분산되어 추적이 어렵다

### 등장한 개념

이 문제를 해결하기 위해 **의존성 주입(Dependency Injection)** 이 중요해졌다.

핵심은 단순하다.

- 필요한 객체를 직접 만들지 않는다
- 외부에서 주입받는다

즉, “나는 무엇이 필요한지만 말하고, 그것을 어떻게 만들지는 바깥에서 결정한다”는 방식이다.

### Websidian에 대입

Websidian에서 `GetDocumentService` 가 `DocumentRepository` 구현체를 직접 생성하는 대신, repository 인터페이스에 의존하고 구현체는 Spring이 주입해 주는 구조가 더 자연스럽다. 이렇게 해야 나중에 JPA 구현을 바꾸거나 테스트 대체 객체를 넣기도 쉬워진다. 

## 5. IoC Container는 왜 등장했는가

### 기존 문제

DI가 중요하다는 것까지는 이해해도, 곧 다음 문제가 생긴다.

- 객체를 누가 만들 것인가
- 언제 만들 것인가
- 어디에 보관할 것인가
- 어떤 구현체를 연결할 것인가

즉, “직접 만들지 말자” 다음에는 “그럼 누가 관리하지?”라는 질문이 남는다.

### 등장한 개념

이 문제를 해결하기 위해 **IoC Container** 또는 **Spring Container** 라는 개념이 중요해졌다.

IoC는 Inversion of Control, 즉 제어의 역전이라는 뜻이다. 말이 어렵지만 실제 의미는 단순하다.

- 예전에는 개발자가 객체 생성의 주도권을 직접 가졌다
- Spring에서는 그 주도권을 프레임워크 쪽으로 넘긴다

Spring Container는 애플리케이션이 필요한 객체를 만들고, 보관하고, 연결해 주는 역할을 한다.

### 왜 중요한가

이 구조가 있어야 DI가 실제로 동작할 수 있다.

또한 이 구조 덕분에 각 클래스는 다음 질문에 더 집중할 수 있다.

- 나는 무슨 책임을 가지는가
- 나는 어떤 의존성이 필요한가

즉, “누가 나를 만들지”보다 “나는 무엇을 할지”에 집중할 수 있다.

## 6. 왜 Controller가 필요해졌는가

### 기존 문제

브라우저와 서버는 HTTP로 통신한다. 그런데 HTTP 요청 파싱, 상태 코드 결정, JSON 응답 생성 같은 일은 비즈니스 규칙 자체와는 다른 종류의 문제다. 

예를 들어 “문서가 존재하지 않는다”는 사실은 도메인 문제일 수 있지만, 그것을 `404 Not Found` 로 돌려줄지 결정하는 것은 HTTP 계층의 문제다. 

### 등장한 개념

그래서 Spring에서는 **Controller** 가 HTTP 입구 역할을 맡는다.

Controller는 보통 다음을 담당한다.

- URL과 HTTP 메서드 매핑
- 요청 파라미터와 body 받기
- DTO로 변환
- application service 호출
- JSON 응답과 상태 코드 반환

### Websidian에 대입

Websidian에서는 `VaultController`, `DocumentController`, `FileController` 같은 클래스가 이 역할을 맡게 된다. API 문서에서 정의한 `/api/v1/...` 리소스 구조도 바로 이 controller 계층에서 구현된다. 

### 중요한 원칙

컨트롤러는 **얇아야 한다**. 비즈니스 규칙이 컨트롤러에 들어가기 시작하면 다시 입구에 모든 책임이 몰리기 때문이다. 이 원칙은 레이어드 아키텍처 문서와도 같은 방향이다. 

## 7. 왜 Service 계층이 필요해졌는가

### 기존 문제

컨트롤러를 얇게 만들고 나면, 이제 “실제로 무엇을 해야 하는가”를 조합할 별도 장소가 필요해진다.

예를 들어 문서 조회는 단순 DB 조회 하나로 끝나지 않을 수 있다.

- 문서 존재 여부 확인
- 권한 확인
- 버전 정보 결합
- 첨부파일 요약 결합
- 응답용 구조 조립

이런 흐름은 컨트롤러에 두기에도 무겁고, 도메인 객체 하나에 넣기에도 어색하다.

### 등장한 개념

그래서 **Service**, 특히 **Application Service** 라는 개념이 중요해졌다.

이 계층은 보통 다음을 맡는다.

- 유스케이스 실행
- 여러 객체 협력 조합
- 트랜잭션 경계 관리
- 다른 모듈과의 흐름 조정

### Websidian에 대입

Websidian 문서도 `CreateDocumentService`, `GetDocumentService`, `PublishDocumentService`, `SetVaultEntryDocumentService` 같은 이름을 예시로 든다. 이 이름에서 알 수 있듯, service 계층은 “기술”보다 **행동 단위**를 중심으로 잡는 편이 자연스럽다. 

## 8. 왜 Repository가 필요해졌는가

### 기존 문제

백엔드 애플리케이션이 커지면 DB 접근 코드는 빠르게 반복된다.

- 연결 열기
- SQL 작성
- 파라미터 바인딩
- 결과 매핑
- 예외 처리
- 연결 닫기

이 작업을 여기저기서 직접 처리하면, 조회 로직은 물론이고 도메인 코드까지 DB 세부 구현에 묶이기 쉽다.

### 등장한 개념

이 문제를 해결하기 위해 **Repository** 라는 개념이 중요해졌다. Repository는 도메인 입장에서 “데이터를 가져오고 저장하는 창구” 역할을 한다.

즉, 도메인이나 application 입장에서는 다음 정도만 알면 된다.

- 문서를 찾는다
- 저장한다
- 목록을 가져온다

실제 SQL이나 JPA 세부 구현은 그 아래에서 숨기는 것이다.

### Websidian에 대입

Websidian 문서도 `domain` 에 repository interface 를 두고, 실제 구현은 `infrastructure.persistence` 에 두는 방향을 권장한다. application service 는 repository interface 에 의존하고, JPA 구현체는 infrastructure 가 맡는 구조다. 

### 왜 중요한가

이렇게 해야 DB 기술 세부사항이 상위 계층까지 새지 않는다. 또한 테스트 시 대체 구현을 넣거나, 나중에 조회 전략을 바꾸기도 쉬워진다.

## 9. JPA와 Spring Data JPA는 왜 널리 쓰이게 되었는가

### 기존 문제

Repository 개념을 도입해도, 여전히 실제 영속성 구현은 번거롭다. 매번 SQL을 직접 다루는 방식은 세밀하지만 반복이 많고 실수도 자주 생긴다.

### 등장한 개념

이 문제를 줄이기 위해 Java 진영에서는 **JPA(Java Persistence API)** 와 그 위에서 더 편하게 쓸 수 있는 **Spring Data JPA** 가 널리 쓰이게 되었다.

핵심 생각은 다음과 같다.

- 객체와 테이블 사이 매핑을 구조화한다
- 반복적인 CRUD 코드를 줄인다
- repository 패턴을 더 쉽게 구현한다

### Websidian에 대입

Websidian의 현재 방향도 초기 persistence 전략으로 Spring Data JPA를 적절한 선택으로 본다. 다만 중요한 점은 패키지 구조의 중심이 JPA 엔티티가 아니라, 여전히 `vault`, `document`, `file`, `link` 같은 도메인 모듈이어야 한다는 것이다. 

### 주의할 점

JPA는 DB 문제를 모두 해결해 주는 마법 도구가 아니다.

- 좋은 스키마 설계가 먼저 필요하다
- 인덱스와 제약조건은 여전히 중요하다
- 조회 패턴을 생각하지 않으면 성능 문제가 생길 수 있다

이 점은 database-basics 문서와도 바로 이어진다. 

## 10. Spring Boot는 왜 등장했는가

### 기존 문제

Spring 자체가 구조적으로 강력하더라도, 실제로 프로젝트를 시작하는 과정은 여전히 복잡할 수 있었다.

- 설정 파일이 많다
- 서버 설정이 번거롭다
- 라이브러리 조합이 어렵다
- 실행 환경을 맞추는 데 시간이 든다

즉, 구조는 좋아졌지만 “시작과 실행”의 비용이 여전히 컸다.

### 등장한 개념

이 문제를 크게 줄이기 위해 **Spring Boot** 가 중요해졌다.

Spring Boot는 대략 다음 문제를 줄여 준다.

- 복잡한 초기 설정 감소
- 내장 서버 사용 가능
- starter 의존성으로 빠른 시작
- 설정 방식 표준화
- profile 기반 환경 분리

### 왜 중요한가

그래서 지금은 Spring을 쓴다고 하면 실제로는 Spring Boot 기반 개발을 의미하는 경우가 많다.

Spring이 구조의 기반이라면, Spring Boot는 그 구조를 **현실적으로 빠르게 실행 가능한 형태**로 만든다고 볼 수 있다.

### Websidian에 대입

Websidian도 `application.yml`, `application-dev.yml`, `application-prod.yml` 구조와 환경 변수 주입 방식을 전제로 한다. 이는 Spring Boot의 전형적인 운영 방식과 잘 맞는다. 

## 11. 설정(Configuration)과 환경 변수는 왜 중요해졌는가

### 기존 문제

DB 주소, 비밀번호, MinIO endpoint, bucket 이름 같은 값을 코드에 직접 넣으면 환경이 바뀔 때마다 수정이 필요하고, 비밀값이 코드에 남는 문제도 생긴다. 

### 등장한 개념

그래서 설정값과 비밀값을 코드에서 분리하는 관점이 중요해졌다.

Spring Boot에서는 보통 다음 방식이 함께 쓰인다.

- `application.yml`
- profile 별 설정 파일
- 환경 변수 주입
- `@ConfigurationProperties` 같은 설정 객체 바인딩

### Websidian에 대입

Websidian의 환경 변수 문서도 DB, MinIO, profile 관련 값을 환경 변수로 분리하는 방향을 택한다. Spring Boot는 이런 구성을 자연스럽게 받아들이기 좋다. 

예를 들어 다음 값들이 여기에 해당한다.

- `SPRING_PROFILES_ACTIVE`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `MINIO_ENDPOINT`
- `MINIO_ACCESS_KEY`
- `MINIO_SECRET_KEY`
- `MINIO_BUCKET` 

## 12. Spring에서 자주 보는 구성 요소는 무엇인가

Spring을 처음 보면 어노테이션과 이름이 많아 복잡하게 느껴질 수 있다. 하지만 대부분은 역할 분리 관점에서 보면 이해가 된다.

### 1. `@RestController`

HTTP 요청을 받는 입구다. URL 매핑과 응답 반환을 담당한다.

### 2. `@Service`

유스케이스 실행과 작업 흐름 조합을 담당하는 계층에 주로 붙는다.

### 3. `@Repository`

DB 접근 책임을 표현할 때 자주 사용한다.

### 4. `@Configuration`

설정용 객체를 등록할 때 사용한다. MinIO client, security 설정, Jackson 설정 같은 전역 구성이 여기에 해당할 수 있다.

### 5. `@Bean`

Spring이 관리할 객체를 직접 등록할 때 사용한다.

### 6. `@Value`, `@ConfigurationProperties`

설정값이나 환경 변수를 읽어 객체로 묶을 때 사용한다.

### 중요한 관점

이 이름들을 외우는 것보다 먼저 중요한 것은, **왜 책임을 나누는가** 를 이해하는 것이다. 이름표만 붙인다고 구조가 좋아지는 것은 아니기 때문이다.

## 13. Spring을 써도 구조가 자동으로 좋아지지는 않는가

### 기존 문제

Spring Boot를 쓰면 종종 다음 같은 구조가 자연스럽게 생긴다.

- controller
- service
- repository
- entity

처음에는 단순해 보이지만, 프로젝트가 커질수록 관련 코드가 도메인 기준으로 흩어지고, 어떤 기능이 어디에 있는지 찾기 어려워질 수 있다.

### 등장한 관점

그래서 Spring을 쓰더라도 **기술 중심 폴더 나열** 대신, **도메인 중심 구조 안에서 레이어를 나누는 방식**이 더 중요해졌다.

### Websidian에 대입

Websidian의 백엔드 문서는 바로 이 문제를 피하려고 한다. `document`, `vault`, `file`, `link` 같은 모듈을 먼저 두고, 그 안에서 `api`, `application`, `domain`, `infrastructure`, `dto` 를 나누는 방향을 권장한다. 

즉, Websidian에서 Spring은 그냥 서버 프레임워크가 아니라, **도메인 중심 구조를 실제 코드로 옮길 때 쓰는 기반**에 가깝다. 

## 14. Websidian에서 Spring 흐름은 어떻게 보이는가

아래는 Websidian에서 “Vault의 entry document 조회” 같은 기능을 Spring Boot로 구현할 때의 전형적인 흐름이다. 

1. `VaultController` 가 HTTP 요청을 받는다. 
2. `GetVaultEntryDocumentService` 가 유스케이스를 실행한다. 
3. `VaultRepository`, `DocumentRepository` 가 필요한 데이터를 조회한다. 
4. 필요하면 MinIO 접근 정보는 infrastructure 계층이 다룬다. 
5. 응답 DTO로 정리해 JSON으로 반환한다. 

이 흐름을 보면 다음 연결이 자연스럽다.

- HTTP와 REST는 controller 계층과 연결된다. 
- layered architecture 는 service, domain, infrastructure 분리와 연결된다. 
- database basics 는 repository 와 JPA가 다루는 데이터 구조와 연결된다. 
- storage and minio 는 infrastructure 계층의 외부 저장소 연동과 연결된다. 

즉, Spring은 Websidian에서 여러 문서를 실제 실행 가능한 코드 구조로 묶어 주는 접착제 역할을 한다.

## 15. Spring을 처음 배울 때 자주 생기는 오해

### 1. 어노테이션을 외우는 것이 Spring 공부라고 생각하기

중요한 것은 어노테이션 이름보다, 왜 controller / service / repository / configuration 으로 나누는지가 먼저다.

### 2. `@Service` 를 붙였다고 설계가 좋아졌다고 생각하기

이름표만 붙인다고 책임 분리가 되는 것은 아니다. 실제로 코드가 유스케이스 중심으로 나뉘어야 한다.

### 3. JPA를 쓰면 DB 문제가 끝난다고 생각하기

JPA는 반복을 줄여 주는 도구이지, 스키마 설계 자체를 대신해 주지 않는다. 인덱스, 무결성, 조회 전략은 여전히 설계가 필요하다. 

### 4. 처음부터 모든 계층을 완벽하게 만들려 하기

혼자 개발하는 프로젝트에서는 작은 유스케이스 하나를 끝까지 구현하면서 구조를 익히는 편이 더 낫다. development guide도 작업 단위를 한 API 엔드포인트 수준으로 작게 유지하라고 권장한다. 

## 16. 지금 단계에서 꼭 잡아야 할 핵심 문장

아래 문장들만 정확히 이해해도 Spring의 큰 틀은 많이 잡힌다.

- Spring은 Java 웹 개발의 반복적인 구조 문제를 줄이기 위해 널리 쓰이게 된 프레임워크다. 
- DI와 IoC Container는 객체 생성과 연결의 부담을 줄이고 결합도를 낮추기 위한 핵심 개념이다. 
- Controller는 HTTP 입구를 담당하고, Service는 유스케이스 흐름을 담당하며, Repository는 데이터 접근 창구 역할을 한다. 
- Spring Boot는 복잡한 설정과 실행 과정을 단순화해 실제 애플리케이션을 더 빨리 시작하게 해 준다. 
- JPA와 Spring Data JPA는 영속성 코드를 줄여 주지만, 좋은 데이터 모델 설계까지 대신해 주지는 않는다. 
- Websidian은 Spring Boot를 단순 기술 선택이 아니라, 도메인 중심 백엔드 구조를 구현하는 기반으로 사용한다. 

## 17. 다음 문서로 이어지는 연결

이 문서를 이해한 다음에는 다음 주제가 자연스럽게 이어진다.

- `vue-basics.md`: 백엔드가 어떤 구조로 API를 내놓는지 본 뒤, 프론트엔드가 그 API를 어떻게 받아 화면으로 조직하는지 이해하기 좋다. 
- `docker-basics.md`: frontend, backend, PostgreSQL, MinIO를 각각 이해한 뒤, 이들을 개발 환경에서 어떻게 함께 띄우는지 보기 좋다. 
- `database-basics.md`: repository 와 JPA가 실제로 다루는 데이터 구조를 더 깊게 이해할 수 있다. 
- `storage-and-minio.md`: infrastructure 계층이 외부 파일 저장소를 어떻게 다루는지 연결해서 볼 수 있다. 