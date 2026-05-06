# Layered Architecture

## Purpose

이 문서는 Websidian을 구현하기 전에 알아두면 좋은 아키텍처와 레이어드 아키텍처의 기본 개념을 정리한다.

설명 방식은 가능한 한 다음 흐름을 따른다.

- 기존에는 무엇이 문제였는가
- 그 문제를 해결하기 위해 어떤 개념이 등장했는가
- 그 개념은 실제로 무엇을 해결하는가
- Websidian에서는 이 개념이 어디에 연결되는가

이 문서는 학습 문서다. 실제 Websidian의 구조 방향은 `docs/architecture/*`, `docs/backend/module-structure.md`, `docs/operations/development-guide.md` 문서를 따른다. 

## 1. 아키텍처란 무엇인가

### 기존 문제

프로그램이 아주 작을 때는 코드가 한 파일이나 몇 개의 함수로도 돌아간다. 하지만 기능이 늘어나면 화면 처리, 데이터 저장, 외부 시스템 연동, 권한 검사, 파일 처리 같은 서로 다른 책임이 한곳에 섞이기 시작한다.

이 상태가 심해지면 다음 문제가 생긴다.

- 어디를 수정해야 할지 찾기 어렵다
- 작은 변경이 예상보다 큰 부작용을 만든다
- 코드를 읽는 데 시간이 오래 걸린다
- 미래의 내가 다시 돌아왔을 때 문맥을 회복하기 힘들다

### 등장한 개념

이 문제를 해결하기 위해 **소프트웨어 아키텍처**라는 개념이 중요해진다. 아키텍처는 시스템을 어떤 큰 구성요소로 나누고, 그 사이의 관계와 경계를 어떻게 정할지에 대한 구조적 설계다.

건물 비유를 쓰면, 아키텍처는 인테리어가 아니라 뼈대와 동선에 가깝다. 벽을 어디에 세울지, 전기와 배관이 어디를 지나갈지, 방과 복도가 어떻게 연결될지를 먼저 정하는 일이다.

### Websidian에 대입

Websidian의 시스템 수준 아키텍처는 대략 다음 요소로 나뉜다.

- 브라우저에서 동작하는 Frontend Application (Vue)
- Backend API (Spring Boot)
- 메타데이터 저장소인 PostgreSQL
- 파일 원본 저장소인 MinIO
- Docker Compose 기반 실행 환경 

즉, 아키텍처는 단순히 “코드를 예쁘게 나누는 것”이 아니라, 시스템 전체가 어떤 경계와 흐름 위에서 동작할지 정하는 일이다. 

## 2. 왜 구조가 필요한가

### 기존 문제

초보 단계에서는 보통 “일단 돌아가게 만들자”가 가장 자연스럽다. 이것 자체는 나쁜 태도가 아니다. 문제는 기능이 조금만 늘어나도, 처음에 섞어놓은 코드가 빠르게 복잡해진다는 점이다.

예를 들어 문서 조회 하나를 구현한다고 해도 실제로는 이런 책임이 섞일 수 있다.

- HTTP 요청 받기
- 입력값 검증
- DB 조회
- 파일 경로 계산
- 권한 검사
- 링크 정보 조합
- JSON 응답 만들기

이 모든 것이 컨트롤러 한 클래스나 서비스 한 메서드에 몰리면, 당장은 빨라도 점점 유지보수가 어려워진다.

### 등장한 개념

그래서 구조를 잡는 핵심 기준으로 **관심사의 분리(Separation of Concerns)** 가 중요해졌다. 서로 다른 책임은 서로 다른 위치에 두자는 생각이다.

이 생각이 발전하면 다음 질문이 생긴다.

- HTTP는 누가 처리할까
- 비즈니스 규칙은 누가 담당할까
- DB 접근은 어디에 둘까
- 외부 스토리지 연동은 어디에 둘까

이 질문에 대한 대표적인 답 중 하나가 바로 **레이어드 아키텍처**다.

## 3. 레이어드 아키텍처는 왜 등장했는가

### 기존 문제

애플리케이션이 커질수록 “입력 처리”, “업무 규칙”, “저장소 접근”, “외부 시스템 연동” 같은 성격이 다른 코드가 서로 강하게 얽힌다. 그러면 특정 기술을 바꾸거나, 테스트를 하거나, 규칙을 수정할 때 영향을 예측하기 어렵다.

### 등장한 개념

이 문제를 해결하기 위해 시스템을 여러 **층(layer)** 으로 나누는 방식이 널리 쓰이게 되었다. 이것이 레이어드 아키텍처다.

핵심 아이디어는 단순하다.

- 서로 다른 책임을 층으로 분리한다.
- 각 층은 자신에게 가까운 문제만 다룬다.
- 위층은 아래층을 사용할 수 있지만, 아래층이 위층을 알지 않게 한다.

즉, 레이어드 아키텍처는 “모든 코드를 한곳에 섞지 말고, 흐름과 책임에 따라 층을 나누자”는 해결책이다. 

## 4. 레이어란 무엇인가

레이어는 같은 종류의 책임을 가진 코드 묶음이다.

대표적으로 다음과 같이 나눈다.

- **API / Presentation layer**: HTTP 요청과 응답 처리
- **Application layer**: 유스케이스 실행, 작업 흐름 조합
- **Domain layer**: 핵심 비즈니스 규칙과 도메인 모델
- **Infrastructure layer**: DB, MinIO, 외부 라이브러리, 프레임워크 연동 구현 

중요한 점은, 이 이름이 절대적인 표준이라기보다 “역할 구분”이 중요하다는 것이다. 이름은 프로젝트마다 조금 다를 수 있지만, 핵심은 책임을 분리하는 데 있다.

## 5. API / Presentation Layer

### 기존 문제

브라우저와 서버는 HTTP로 통신한다. 그런데 HTTP 요청 파싱, 상태 코드 결정, JSON 응답 생성 같은 일은 비즈니스 규칙 그 자체와는 다른 종류의 문제다.

예를 들어 “문서를 찾을 수 없다”는 사실 자체는 도메인 문제일 수 있지만, 그것을 `404 Not Found`로 돌려줄지 결정하는 것은 HTTP 계층의 문제다.

### 등장한 개념

그래서 **API layer** 또는 **Presentation layer** 를 둔다. 이 층은 외부 세계와 직접 맞닿아 있는 입구다.

### 역할

- URL과 HTTP 메서드 매핑
- 요청 파라미터/바디 받기
- DTO로 변환
- application service 호출
- 결과를 JSON 응답으로 변환
- 상태 코드 결정 

### Websidian에 대입

Websidian에서는 `api/` 패키지의 REST controller가 이 역할을 맡는다. `DocumentController`, `VaultController` 같은 클래스가 여기에 해당한다. 

### 중요한 원칙

컨트롤러는 **얇아야 한다**. 비즈니스 로직을 직접 담기 시작하면 다시 모든 책임이 입구에 몰리기 때문이다. 

## 6. Application Layer

### 기존 문제

컨트롤러를 얇게 만들고 나면, 이제 “실제로 무엇을 해야 하는가”를 조합하는 별도 장소가 필요하다. 예를 들어 문서 조회는 단순 DB 조회 하나로 끝나지 않을 수 있다.

- 문서 존재 여부 확인
- 권한 확인
- 버전 정보 조합
- 첨부 요약 결합
- 응답용 구조 만들기

이런 흐름은 단일 도메인 객체 하나의 책임으로 보기 어렵고, 그렇다고 컨트롤러에 두기도 애매하다.

### 등장한 개념

이 문제를 해결하기 위해 **Application layer** 가 등장한다. 이 층은 흔히 use case, application service, orchestration을 담당한다. 

### 역할

- 유스케이스 실행
- 여러 도메인 객체/리포지토리 협력 조합
- 트랜잭션 경계 관리
- 다른 모듈과의 협력 흐름 조정 

### Websidian 예시

문서에 나온 예시 이름은 다음과 같다. 

- `CreateDocumentService`
- `GetDocumentService`
- `UpdateDocumentService`
- `PublishDocumentService`
- `SetVaultEntryDocumentService` 

이름에서 알 수 있듯, application layer는 “기술”보다 “행동 단위”를 중심으로 잡는 편이 이해하기 쉽다.

## 7. Domain Layer

### 기존 문제

애플리케이션이 커질수록 “이 프로젝트만의 핵심 규칙”이 여기저기 흩어진다. 그러면 어떤 규칙이 진짜 중심 규칙인지 보이지 않게 된다.

예를 들어 Websidian에는 이런 규칙이 있다.

- Vault는 대표 문서(entry document)를 가질 수 있다
- Document는 markdown 또는 html 타입을 가진다
- 문서 상태(status)는 draft/published 같은 흐름을 가진다
- 링크는 wikilink, markdownlink, embed, attachment 등 여러 타입을 가진다 

이런 규칙이 컨트롤러, SQL, 프론트엔드, 유틸 함수에 흩어지면 시스템의 핵심 의미가 흐려진다.

### 등장한 개념

그래서 **Domain layer** 가 중요해진다. 도메인 레이어는 “이 시스템이 무엇을 다루는가”와 “그 안의 핵심 규칙이 무엇인가”를 담는 층이다. 

### 포함될 수 있는 것

- Entity
- Value Object
- Domain Service
- Enum
- Domain Rule
- Repository interface 

### Websidian 예시

- `Document`
- `DocumentVersion`
- `DocumentType`
- `DocumentStatus`
- `DocumentRepository` 

### 왜 중요한가

도메인 레이어가 분명할수록, 기술이 바뀌어도 시스템의 본질은 유지된다. 예를 들어 DB를 바꾸거나 API 형식을 바꿔도, “문서와 Vault의 규칙” 자체는 그대로 남는다.

즉, Domain layer는 Websidian의 **의미 중심부**라고 볼 수 있다.

## 8. Infrastructure Layer

### 기존 문제

현실의 프로그램은 순수한 비즈니스 규칙만으로 동작하지 않는다. 결국 DB에 저장해야 하고, MinIO에 파일을 넣어야 하고, JPA를 써야 하고, JSON 직렬화도 해야 한다.

이런 기술적 세부사항을 도메인 안에 직접 넣기 시작하면, 도메인 코드가 특정 기술에 강하게 묶인다.

### 등장한 개념

그래서 **Infrastructure layer** 를 둔다. 이 층은 외부 시스템과 프레임워크에 연결되는 실제 구현을 담당한다. 

### 포함될 수 있는 것

- JPA entity / mapper
- repository implementation
- MinIO adapter
- parser adapter
- global config (security, Jackson, persistence 등) 

### Websidian에 대입

Websidian 문서에서는 모듈 내부 `infrastructure/` 와 전역 `infrastructure/` 를 구분할 수 있다고 설명한다. 특정 도메인에 강하게 묶인 구현은 해당 모듈 안에 두고, 정말 전역적인 설정만 공통 `infrastructure/` 에 둔다. 

이 구분은 “기술 구현은 필요하지만, 의미 중심부를 오염시키지 말자”는 의도다.

## 9. 의존 방향이 왜 중요한가

### 기존 문제

레이어를 나눠놓기만 하고 서로 아무 방향으로나 참조하게 두면, 결국 다시 얽힌 구조가 된다. 예를 들어 domain이 controller를 알고, controller가 JPA 엔티티를 직접 조작하고, 다른 모듈의 내부 persistence를 직접 건드리면 레이어 분리는 이름만 남는다.

### 등장한 개념

그래서 레이어드 아키텍처에서는 **의존 방향**이 중요하다. 일반적으로 위층이 아래층을 사용하고, 아래층은 위층을 모르게 하는 방향을 선호한다.

단순한 흐름으로 보면:

- API → Application → Domain → Infrastructure

혹은, 도메인 인터페이스를 중심으로 보면:

- API가 application service 호출
- application이 domain 규칙 사용
- infrastructure가 domain에서 정의한 repository interface를 구현 

### 왜 좋은가

- 변경 영향 범위를 줄일 수 있다
- 테스트가 쉬워진다
- 코드 읽기가 쉬워진다
- 특정 기술을 갈아끼울 가능성을 남길 수 있다

Websidian의 `module-structure.md`도 application service가 domain repository interface에 의존하고, 실제 구현은 infrastructure에 두는 방향을 권장한다. 

## 10. 왜 DTO와 Domain을 분리하는가

### 기존 문제

API 요청/응답 형식과 도메인 모델을 완전히 같은 것으로 취급하면, 외부 계약과 내부 의미가 강하게 묶인다. 그러면 API 응답 구조를 조금만 바꿔도 내부 모델이 흔들리고, 반대로 내부 모델을 개선하기도 어려워진다.

### 등장한 개념

그래서 **DTO(Data Transfer Object)** 와 Domain model을 분리하는 패턴이 많이 쓰인다. 

### 역할 구분

- DTO: 요청/응답용 데이터 구조
- Domain: 비즈니스 의미와 규칙을 담는 모델

### Websidian에 대입

`dto/` 패키지에 `CreateDocumentRequest`, `DocumentResponse` 같은 클래스를 두고, `domain/` 에는 `Document`, `DocumentStatus`, `DocumentType` 등을 둔다. 

이 분리는 “외부와의 대화 방식”과 “시스템 내부 의미 구조”를 섞지 않게 해준다.

## 11. 기술 계층 패키징과 도메인 패키징

### 기존 문제

Spring Boot 프로젝트를 처음 만들면 흔히 이런 구조를 떠올리기 쉽다.

```text
controller/
service/
repository/
entity/
```

처음에는 단순해 보이지만, 프로젝트가 커질수록 `document`, `vault`, `file`, `link` 관련 코드가 각 폴더에 흩어진다. 그러면 “문서 기능을 보고 싶은데 왜 네 군데를 왔다 갔다 해야 하지?” 같은 문제가 생긴다.

### 등장한 개념

그래서 Websidian은 기술보다 **도메인 우선 패키징**을 더 중요하게 본다. 즉, 먼저 `document`, `vault`, `file` 같은 모듈을 만들고, 그 안에서 `api/application/domain/infrastructure/dto` 로 나눈다. 

### 왜 좋은가

- 관련 코드가 가까이 모인다
- 모듈 경계가 선명해진다
- 나중에 특정 도메인을 분리하기 쉬워진다
- 개인 프로젝트에서도 다시 읽기 쉽다

### Websidian 권장 구조

```text
backend/
  src/main/java/com/websidian/
    common/
    vault/
      api/
      application/
      domain/
      infrastructure/
      dto/
    document/
      api/
      application/
      domain/
      infrastructure/
      dto/
    file/
    link/
    auth/
    infrastructure/
```

이 구조의 핵심은 “controller/service/entity가 루트에 흩어져 있지 않고, document라는 경계 안에서 함께 움직인다”는 점이다. 

## 12. 모듈형 모놀리스는 왜 좋은 선택인가

### 기존 문제

구조를 고민하다 보면 종종 “처음부터 마이크로서비스로 나눠야 하나?” 같은 생각이 들 수 있다. 하지만 작은 개인 프로젝트에서 너무 일찍 시스템을 잘게 쪼개면 운영과 개발 복잡도가 급격히 커진다.

### 등장한 개념

그래서 Websidian은 초기에 **하나의 Spring Boot 애플리케이션**으로 시작하되, 내부 구조는 모듈형으로 설계하는 **모듈형 모놀리스** 방향을 택한다. 

### 왜 좋은가

- 배포와 실행이 단순하다
- 디버깅이 쉽다
- 트랜잭션 관리가 단순하다
- 그래도 코드 구조는 도메인별로 분리할 수 있다
- 나중에 정말 필요할 때만 분리하면 된다

이 선택은 Websidian의 홈랩, Docker Compose, 개인 개발 전제와 잘 맞는다. 

## 13. Websidian에서 레이어드 아키텍처가 실제로 어떻게 보이는가

문서 조회 흐름을 예로 들어 보면 다음과 같다.

1. 브라우저가 문서 페이지에 접근한다. 
2. 프론트엔드가 Backend API에 문서 메타데이터를 요청한다. 
3. `DocumentController` 가 요청을 받는다. 
4. `GetDocumentService` 같은 application service가 실행된다. 
5. 서비스는 `DocumentRepository` 같은 domain port를 통해 문서 데이터를 조회한다. 
6. 실제 DB 접근 구현은 infrastructure(JPA implementation)가 담당한다. 
7. 필요하면 파일 메타데이터와 MinIO 경로 정보도 조합한다. 
8. 서비스가 응답용 데이터를 만들고, 컨트롤러가 JSON으로 반환한다. 

이 흐름을 이해하면 “HTTP 요청이 서버 안에서 어디를 거쳐 처리되는가”가 보이기 시작한다.

## 14. 왜 모든 것을 완벽하게 나누려고 하면 안 되는가

### 기존 문제

구조를 배우기 시작하면 오히려 반대로, 너무 많은 추상화와 너무 이른 분리가 생길 수 있다. 작은 기능 하나에도 인터페이스, 어댑터, 서비스, 팩토리, 전략 패턴을 과하게 넣으면 코드가 더 읽기 어려워질 수 있다.

### 등장한 관점

그래서 development guide는 “지금은 단순하게, 그러나 경계는 분명하게”라는 태도와 잘 맞는다. 큰 기능을 한 번에 만들지 않고, 작은 단위로 구현하고, 코드와 문서를 같이 움직이게 하는 것이 중요하다. 

### Websidian에서의 실천 방식

MVP 기준 첫 컷으로는 모든 모듈을 다 만들기보다 아래 정도만 먼저 두는 것이 권장된다. 

- `vault`
- `document`
- `file`
- `link`
- `common`
- `infrastructure` 

즉, 레이어드 아키텍처는 “복잡하게 만들기 위한 도구”가 아니라, **작게 시작해도 나중에 무너지지 않게 하기 위한 도구**다.

## 15. 지금 단계에서 꼭 잡아야 할 핵심 문장

아래 문장들만 정확히 이해해도 큰 틀은 잡힌다.

- 아키텍처는 시스템을 어떤 구성요소와 경계로 나눌지에 대한 구조 설계다. 
- 레이어드 아키텍처는 서로 다른 책임을 층으로 나누는 방식이다. 
- API layer는 HTTP 입출력을 다루고, application layer는 유스케이스 흐름을 담당한다. 
- domain layer는 시스템의 핵심 규칙과 의미를 담고, infrastructure layer는 DB와 외부 시스템 연동을 구현한다. 
- 레이어를 나누는 것보다 더 중요한 것은 의존 방향과 책임 분리다. 
- Websidian은 기술별 폴더 나열보다 도메인 중심 모듈 구조 안에서 레이어를 나누는 방향을 택한다. 

## 16. 다음 문서로 이어지는 연결

이 문서를 이해한 다음에는 다음 주제가 자연스럽게 이어진다.

- `database-basics.md`: domain과 repository가 결국 다루는 데이터는 어떻게 구조화되는가
- `spring-basics.md`: controller, service, repository, configuration이 Spring Boot에서 어떻게 구현되는가
- `vue-basics.md`: 프론트엔드에서도 비슷하게 페이지, 컴포넌트, 상태, API 경계를 어떻게 나누는가
- `storage-and-minio.md`: infrastructure layer가 외부 파일 저장소를 어떻게 다루는가