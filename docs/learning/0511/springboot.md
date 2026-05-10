# Websidian용 Spring Boot 완전 기초 학습 노트

이 문서는 웹 개발, Spring Boot, 데이터베이스를 거의 처음 접하는 상태를 기준으로, Websidian 백엔드를 이해하기 위해 필요한 가장 기초적인 내용을 차근차근 설명하는 개인 학습용 가이드다.[1][2] Websidian은 Vue 프론트엔드, Spring Boot 백엔드, PostgreSQL, MinIO, Docker Compose를 사용하는 구조를 목표로 하고 있으므로, 이 문서도 그 흐름에 맞춰 설명한다.[1][3][4]

## 1. 먼저 큰 그림부터

웹 서비스는 아주 단순하게 보면 **화면**, **서버**, **저장소** 세 부분으로 나눌 수 있다.[1] Websidian에서는 Vue가 화면을 담당하고, Spring Boot가 서버를 담당하고, PostgreSQL과 MinIO가 데이터를 저장한다.[1][4]

이를 일상적인 비유로 보면 이렇다.[1]

- 프론트엔드: 사용자가 보는 카운터 화면.[1]
- 백엔드: 주문을 받고 처리하는 주방.[1]
- 데이터베이스: 주문 기록을 적어 두는 장부.[1]
- 객체 스토리지(MinIO): 큰 파일을 보관하는 창고.[1]

즉, 사용자가 웹 화면에서 문서를 열어 달라고 요청하면, 프론트엔드가 백엔드에 요청을 보내고, 백엔드는 DB나 MinIO에서 데이터를 읽어 응답한다.[1][4]

## 2. 백엔드는 정확히 무엇인가

백엔드는 사용자의 요청을 받아 처리하는 서버 쪽 프로그램이다.[1] 예를 들어 “이 Vault의 문서 목록 보여줘”, “새 문서 만들어줘”, “문서를 publish 해줘” 같은 요청을 처리하는 역할이 백엔드다.[5][1]

백엔드는 보통 이런 일을 한다.[5][1]

- 요청 받기, 예: HTTP GET, POST.[5]
- 입력 검증하기, 예: slug가 비어 있는지 확인.[6]
- 비즈니스 규칙 적용하기, 예: publish하려면 current version이 있어야 함.[7][6]
- DB 저장/조회하기.[6]
- 결과를 JSON으로 돌려주기.[5]

처음에는 백엔드를 “화면 뒤에서 실제 일을 하는 프로그램”이라고 이해하면 충분하다.[1]

## 3. Spring Boot는 무엇인가

Spring Boot는 Java로 백엔드를 만들 때 자주 필요한 기능을 미리 잘 묶어 둔 프레임워크다.[8] 웹 서버 실행, 설정 관리, DB 연결, API 작성, 테스트 같은 것을 쉽게 시작하게 해 준다.[8][6]

처음 배우는 입장에서는 Spring Boot를 이렇게 기억하면 된다.[8]

- Java로 백엔드를 만들기 쉽게 해 주는 기본 틀.[8]
- 서버 실행, API 작성, DB 연결을 도와주는 도구 모음.[8][5]
- 프로젝트 구조를 정리된 방식으로 유지하게 해 주는 뼈대.[8]

즉, Spring Boot는 “백엔드 애플리케이션 제작 키트”에 가깝다.[8]

## 4. API라는 말부터 이해하기

API는 프론트엔드와 백엔드가 대화하는 약속이라고 생각하면 된다.[5] 예를 들어 프론트엔드가 `/api/v1/vaults/{vaultId}/documents`로 POST 요청을 보내면, 백엔드는 “새 문서를 만들어 달라는 요청이구나”라고 이해한다.[5]

아주 단순한 예시는 이렇다.[5]

- `GET /api/v1/vaults`: Vault 목록 조회.[5]
- `POST /api/v1/vaults`: Vault 생성.[5]
- `POST /api/v1/vaults/{vaultId}/documents`: 특정 Vault에 문서 생성.[5]
- `GET /api/v1/documents/{documentId}`: 문서 상세 조회.[5]

즉, API는 “어떤 주소로, 어떤 방식으로 요청하면, 서버가 무엇을 해 주는가”에 대한 약속이다.[5]

## 5. HTTP 메서드는 어떻게 읽으면 되는가

웹 개발을 시작하면 `GET`, `POST`, `PATCH`, `DELETE` 같은 단어를 계속 보게 된다.[5] 처음에는 아래 네 개만 알아도 충분하다.[5]

- `GET`: 조회.[5]
- `POST`: 새로 만들기.[5]
- `PATCH`: 일부 수정.[5]
- `DELETE`: 삭제.[5]

예를 들어 문서를 읽어 오면 `GET`, 새 문서를 만들면 `POST`를 쓰는 식이다.[5] Websidian에서도 Vault 생성, Document 생성, 조회, 수정 같은 흐름이 이 패턴을 따른다.[5]

## 6. Java 클래스와 Spring Boot의 기본 역할

Spring Boot 프로젝트를 열면 클래스가 여러 개 나와서 처음에는 매우 복잡해 보인다.[8] 하지만 역할별로 나누어 보면 꽤 단순하다.[8]

### 아주 기초적인 역할 구분

- Controller: HTTP 요청을 받는 입구.[8]
- Service 또는 Application: 실제 작업 순서를 조립하는 곳.[8]
- Domain: 핵심 규칙과 상태를 담는 객체들.[8][9]
- Repository: DB 저장/조회 창구.[8][6]
- Infrastructure: JPA, DB, MinIO 같은 기술 구현.[8]

Websidian의 문서 구조도 이 방향을 권장하고 있다.[8] 즉, “요청 받기”, “규칙 처리”, “저장하기”를 한 클래스에 다 몰아넣지 않고 나눠서 생각하는 것이 핵심이다.[8]

## 7. Spring Boot에서 가장 먼저 만나는 코드

보통 시작점은 `@SpringBootApplication`이 붙은 메인 클래스다.[8] 이 클래스는 “애플리케이션을 시작하라”는 진입점 역할을 한다.[8]

아주 단순한 형태는 아래와 비슷하다.

```java
@SpringBootApplication
public class WebsidianApplication {

    public static void main(String[] args) {
        SpringApplication.run(WebsidianApplication.class, args);
    }
}
```

이 코드는 크게 두 가지만 한다.[8]

- Spring Boot 애플리케이션을 시작한다.[8]
- Spring이 필요한 객체들을 준비하고 서버를 띄운다.[8]

처음에는 “이게 프로그램 시작 버튼이다”라고 이해하면 된다.[8]

## 8. Controller는 요청을 받는 입구다

Controller는 프론트엔드나 외부 클라이언트가 보낸 HTTP 요청을 직접 받는 클래스다.[8][5] 여기서는 URL, HTTP 메서드, 요청 본문을 받아서 서비스 계층으로 넘기는 역할을 한다.[8]

학습용 아주 단순 예시는 아래와 같다.

```java
@RestController
@RequestMapping("/api/v1/hello")
public class HelloController {

    @GetMapping
    public String hello() {
        return "hello";
    }
}
```

이 코드는 `/api/v1/hello`로 GET 요청이 오면 `hello`를 반환한다. 처음에는 Controller를 “문 앞에서 손님을 맞이하는 직원”처럼 생각하면 이해하기 쉽다.[8]

## 9. Service는 작업 순서를 조립한다

Service는 요청 하나를 처리하기 위해 여러 단계를 순서대로 실행하는 곳이다.[8] 예를 들어 문서 생성 서비스라면, Vault 조회 → slug 중복 확인 → Document 생성 → 첫 Version 생성 → 저장 같은 순서를 조립할 수 있다.[6]

예시:

```java
@Service
@Transactional
public class CreateDocumentService {

    private final VaultRepository vaultRepository;
    private final DocumentRepository documentRepository;

    public CreateDocumentService(
            VaultRepository vaultRepository,
            DocumentRepository documentRepository
    ) {
        this.vaultRepository = vaultRepository;
        this.documentRepository = documentRepository;
    }

    public UUID create(UUID vaultId, String slug, String title, String content) {
        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new IllegalArgumentException("Vault not found"));

        Document document = new Document(slug, title, DocumentType.MARKDOWN);
        DocumentVersion version = new DocumentVersion(1, DocumentType.MARKDOWN, content, null);

        vault.addDocument(document);
        document.createAndSetCurrentVersion(version);

        return documentRepository.save(document).getId();
    }
}
```

여기서 중요한 것은 Service가 모든 규칙을 직접 가지는 것이 아니라, 엔티티 메서드를 조합해서 use case를 완성한다는 점이다.[8][6] 즉, Service는 진행 순서를 담당하고, Domain은 핵심 규칙을 가진다.[8][6]

## 10. Domain은 핵심 규칙이 사는 곳이다

Websidian에서 `Vault`, `Document`, `DocumentVersion`는 단순 DTO가 아니라 도메인 엔티티다.[9][6] 이 객체들은 상태와 규칙을 함께 가진다.[9][6]

예를 들어 `Document.publish()`는 그냥 상태 값을 바꾸는 것이 아니라, 현재 버전이 있는지 확인한 뒤에만 published로 변경한다.[6][7]

```java
public void publish() {
    if (this.currentVersion == null) {
        throw new IllegalStateException("Document must have current version before publish.");
    }
    this.status = DocumentStatus.PUBLISHED;
}
```

이런 코드는 “도메인 객체가 자기 규칙을 스스로 지킨다”는 좋은 예시다.[6] 처음에는 setter를 마구 만드는 대신, 의미 있는 메서드로 상태를 바꾼다는 점만 기억해도 충분하다.[6]

## 11. 데이터베이스는 왜 필요한가

백엔드는 메모리 안에서만 동작하면 서버를 껐다 켤 때 데이터가 모두 사라진다.[6] 그래서 문서, 버전, Vault 같은 정보를 오래 보관하려면 DB가 필요하다.[10][6]

Websidian에서는 PostgreSQL이 그 역할을 맡는다.[1][4] Vault, Document, DocumentVersion 같은 메타데이터는 PostgreSQL에 저장하고, 큰 파일 본문이나 첨부파일은 MinIO 같은 객체 스토리지에 저장하는 방향이다.[1][10]

## 12. 테이블, 행, 컬럼부터 이해하기

관계형 DB는 엑셀 표와 비슷한 형태로 생각하면 된다.[10] 테이블은 시트, 행(row)은 한 줄 데이터, 컬럼(column)은 각 항목이다.[10][6]

예를 들어 `documents` 테이블은 이런 느낌이다.[10][6]

| 컬럼 | 의미 |
|---|---|
| `id` [6] | 문서 고유 ID.[6] |
| `vault_id` [6] | 어떤 Vault 소속인지 가리키는 값.[6] |
| `slug` [6] | 사람이 읽기 좋은 식별자.[6] |
| `title` [6] | 문서 제목.[6] |
| `document_type` [6] | markdown/html 구분.[6] |
| `status` [6] | draft/published/archived 상태.[6] |

즉, Java 객체 하나가 저장되면 결국 DB 테이블의 한 행으로 들어간다고 보면 된다.[6]

## 13. JPA는 객체와 테이블을 연결해 준다

JPA는 Java 객체를 DB 테이블과 연결해 주는 기술이다.[6] `@Entity`가 붙은 클래스를 JPA가 보고, 어떤 필드를 어떤 컬럼으로 저장할지 이해한다.[6]

예를 들어 아래 코드는 `Document` 클래스가 `documents` 테이블과 연결된다는 뜻이다.[6]

```java
@Entity
@Table(name = "documents")
public class Document {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "slug", nullable = false, length = 255)
    private String slug;
}
```

처음에는 JPA를 “Java 객체 저장 번역기”라고 이해하면 쉽다. 객체를 넣으면 SQL 저장으로 바꿔 주고, DB 데이터를 읽으면 다시 객체로 만들어 준다.[6]

## 14. 어노테이션은 무엇인가

Java에서 `@Entity`, `@Column`, `@RestController` 같은 것은 어노테이션이다.[6][8] 쉽게 말하면 “이 클래스나 필드의 역할을 프레임워크에게 알려 주는 표식”이다.[8][6]

처음에 자주 보는 것만 기억하면 충분하다.[6][8]

- `@Entity`: JPA가 관리하는 DB 엔티티.[6]
- `@Table`: 연결될 테이블 이름.[6]
- `@Id`: 기본 키.[6]
- `@Column`: 컬럼 설정.[6]
- `@RestController`: HTTP 요청을 받는 컨트롤러.[8]
- `@Service`: 서비스 역할 클래스.[8]
- `@Transactional`: 하나의 작업 단위를 트랜잭션으로 묶음.[8]

처음에는 “Spring과 JPA가 이 코드를 어떻게 해석할지 알려 주는 라벨” 정도로 이해하면 된다.[8][6]

## 15. Repository는 DB 창구다

Repository는 DB에 저장하고 조회하는 창구다.[8][6] Websidian 현재 코드에서는 domain에 repository 인터페이스를 두고, infrastructure에서 JPA repository로 연결하는 방식을 쓰고 있다.[8][6]

예시:

```java
public interface DocumentRepository {
    Optional<Document> findById(UUID id);
    Optional<Document> findByVaultIdAndSlug(UUID vaultId, String slug);
    List<Document> findByVaultId(UUID vaultId);
}
```

```java
public interface JpaDocumentRepository
        extends JpaRepository<Document, UUID>, DocumentRepository {
}
```

처음에는 Repository를 “DB에 물어보는 창구”라고 보면 된다.[6] Service는 Repository를 통해 DB에 접근하고, Controller는 보통 Repository를 직접 건드리지 않도록 구조를 나누는 것이 좋다.[8]

## 16. 트랜잭션은 한 작업 단위다

트랜잭션은 여러 DB 작업을 하나로 묶어서, 전부 성공하거나 전부 실패하게 만드는 단위다.[8] 예를 들어 문서를 만들 때 Document는 저장됐는데 Version은 저장 안 되면 곤란하므로, 이런 작업은 한 덩어리로 처리하는 것이 안전하다.[6]

그래서 서비스에 `@Transactional`을 붙이는 경우가 많다.[8] 처음에는 “중간에 실패하면 원래 상태로 되돌리기 위한 안전장치”라고 이해하면 충분하다.[8]

## 17. 왜 DB 제약이 따로 필요한가

코드에서 검증을 해도, DB는 마지막 방어선 역할을 한다.[6] 예를 들어 같은 Vault 안에서 같은 slug를 금지하는 규칙은 코드에서도 확인할 수 있지만, DB의 unique 제약이 최종적으로 막아 주는 편이 더 안전하다.[6]

현재 migration에는 이런 제약이 들어 있다.[6]

```sql
CONSTRAINT documents_vault_slug_unique UNIQUE (vault_id, slug)
```

즉, 실수로 코드가 잘못 동작해도 DB가 “이건 저장할 수 없어”라고 막아 준다.[6] 처음엔 귀찮아 보여도, 실제 서비스에서는 이런 이중 방어가 매우 중요하다.[6]

## 18. Flyway는 DB 변경 이력 관리 도구다

Spring Boot에서 JPA만 쓰면 엔티티를 보고 테이블을 자동 생성하게 할 수도 있다.[6] 하지만 Websidian은 Flyway로 SQL migration을 직접 관리하는 방식을 택하고 있다.[6]

이 말은 “DB 구조 변경을 코드 자동 생성에 맡기지 않고, SQL 파일로 기록한다”는 뜻이다.[6] 예를 들어 `V1_create_core_document_schema.sql`은 vaults, documents, document_versions 테이블을 만드는 이력을 남긴다.[6]

처음에는 Flyway를 “DB 버전 관리 도구”라고 이해하면 된다.[6] 코드 Git commit처럼, DB 구조도 순서대로 변경 이력을 남기는 것이다.[6]

## 19. application.properties는 설정 파일이다

Spring Boot는 `application.properties` 또는 `application.yml`에서 서버와 DB 설정을 읽는다.[6] 예를 들어 포트, DB URL, 사용자명, 비밀번호, JPA 옵션, Flyway 사용 여부 같은 것이 들어간다.[6]

현재 설정에서 중요한 부분은 아래와 같다.[6]

- `server.port`: 서버 포트 설정.[6]
- `spring.datasource.url`: PostgreSQL 연결 주소.[6]
- `spring.datasource.username`, `password`: DB 계정 정보.[6]
- `spring.jpa.hibernate.ddl-auto=validate`: 엔티티와 DB 스키마가 맞는지만 검증.[6]
- `spring.flyway.enabled=true`: Flyway migration 활성화.[6]

즉, application.properties는 “프로그램 실행 환경 설정 모음”이다.[6]

## 20. Docker Compose는 왜 쓰는가

Websidian은 홈랩 환경에서 self-hosted를 목표로 하므로, PostgreSQL, MinIO, backend, frontend를 컨테이너로 띄우는 구조가 잘 맞는다.[1][4] Docker Compose는 이 여러 서비스를 한 번에 올리고 내리기 쉽게 해 준다.[4][3]

처음에는 Docker Compose를 “개발에 필요한 프로그램 여러 개를 한 세트로 실행하는 도구”라고 보면 된다.[3][4] 예를 들어 PostgreSQL을 직접 설치하지 않고 컨테이너로 띄워 테스트 환경을 쉽게 맞출 수 있다.[3]

## 21. Websidian 기준으로 백엔드 개발 순서

Websidian의 개발 가이드는 혼자 개발하는 프로젝트에 맞춰, 크게 생각하고 작게 구현하고 직접 검증하고 기록하는 흐름을 권장한다.[2] 처음 배우는 상태라면 아래 순서가 가장 부담이 적다.[2]

1. 큰 그림 이해: 프론트/백엔드/DB/MinIO 역할 구분.[1]
2. Spring Boot 기본: Controller, Service, Repository, Entity 역할 구분.[8]
3. DB 기본: 테이블, PK/FK, unique, index 이해.[10][6]
4. JPA 기본: `@Entity`, `@Id`, `@ManyToOne`, `@OneToMany` 이해.[6]
5. 현재 Websidian 코드 읽기: `Vault`, `Document`, `DocumentVersion` 흐름 따라가기.[6]
6. 작은 기능 하나 직접 구현: 예를 들어 Document 생성 use case 만들기.[2][5]
7. 테스트로 확인하고, 학습 메모 남기기.[2][6]

이 순서가 중요한 이유는 처음부터 다 알려고 하면 너무 어렵기 때문이다. 지금은 “동작 흐름”을 먼저 익히고, 세부 문법은 그다음에 따라오는 방식이 훨씬 낫다.[2]

## 22. 지금 당장 외우지 않아도 되는 것

처음부터 JPA 내부 동작, 영속성 컨텍스트, 프록시, N+1, flush 타이밍, 커스텀 쿼리 최적화까지 다 이해할 필요는 없다.[6] 지금 단계에서는 아래 정도만 확실히 잡으면 충분하다.[8][6]

- 요청은 Controller로 들어온다.[8]
- 작업 순서는 Service가 조립한다.[8]
- 핵심 규칙은 Domain이 가진다.[8][6]
- 저장과 조회는 Repository가 담당한다.[8][6]
- 실제 데이터는 PostgreSQL에 저장된다.[1][6]
- DB 구조는 Flyway migration으로 관리한다.[6]

이 여섯 가지가 잡히면 이후 학습이 훨씬 쉬워진다.[8][6]

## 23. 지금 바로 해 보면 좋은 가장 작은 실습

처음 학습용으로는 아래 정도가 적당하다.[2][6]

1. `HelloController` 만들어서 브라우저나 Postman에서 응답 확인하기.[8]
2. `Document` 엔티티 코드에서 `slug`, `title`, `status` 필드 읽어 보기.[6]
3. `Document.publish()` 메서드가 왜 setter보다 안전한지 직접 설명해 보기.[6]
4. migration SQL에서 `documents` 테이블 정의를 읽고, 엔티티와 하나씩 대응시켜 보기.[6]
5. `DocumentPersistenceTest`에서 테스트 이름과 실제 검증 내용을 짝지어 읽어 보기.[6]

이 정도만 해도 Spring Boot 프로젝트가 완전히 낯선 상태에서는 큰 진전이다.[2]

## 24. Websidian 문서 구조와 같이 보는 법

기초 학습과 함께 보면 좋은 공식 문서는 아래 순서다.[1]

1. `README.md`: 프로젝트 큰 그림.[1]
2. `docs/development/development-guide.md`: 어떻게 작업할지.[2]
3. `docs/backend/module-structure.md`: 백엔드 패키지 구조.[8]
4. `docs/backend/api-overview.md`: 어떤 API를 만들지.[5]
5. `docs/architecture/domain-model.md`: 도메인 개념.[9]
6. `docs/database/erd-overview.md`: DB 구조 그림.[10]

이 문서들은 처음부터 완벽히 이해할 필요는 없다. 지금은 “이 문서가 어떤 질문에 답해 주는지”만 알아도 충분하다.[1][2]

## 25. 이 문서를 어디에 두면 좋은가

이 문서는 개인 입문 학습용 성격이 강하므로 `docs/learning/`에 두는 것이 가장 자연스럽다.[2] 공식 설계 문서라기보다, Websidian 코드를 이해하기 위한 개인 참고서에 가깝기 때문이다.[2][1]

추천 파일명은 아래와 같다.[2]

- `docs/learning/spring-boot-backend-basics-for-websidian.md`
- `docs/learning/web-backend-from-zero.md`

## 26. 가장 짧은 한 줄 이해

Websidian에서 Spring Boot는 백엔드 서버를 만드는 틀이고, Controller는 요청을 받고, Service는 작업 순서를 조립하고, Domain은 핵심 규칙을 가지며, Repository는 PostgreSQL에 저장하고 조회하는 창구다.[8][6][1]