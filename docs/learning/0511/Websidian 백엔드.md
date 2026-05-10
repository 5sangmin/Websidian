# Websidian 백엔드 개인 학습 노트

이 문서는 Websidian의 현재 `Vault`, `Document`, `DocumentVersion` 구현을 기준으로, Spring Boot, JPA, 데이터베이스의 핵심 개념을 처음 배우는 사람 입장에서 정리한 개인 학습용 메모다.[1][2] Websidian의 MVP 문서 모델은 Vault 안에 Document가 있고, Document는 여러 Version을 가지며, Markdown와 HTML을 모두 1급 문서로 다루는 방향으로 정의되어 있다.[2][3][4]

## 1. 지금 구현이 무엇을 하는가

현재 구현은 세 가지 핵심 도메인 객체를 DB에 저장하고 다시 읽어오는 첫 번째 persistence 레이어라고 볼 수 있다.[1] `Vault`는 문서 저장소 단위이고, `Document`는 실제 문서 메타데이터이며, `DocumentVersion`은 문서 내용의 스냅샷이다.[2][4][1]

Websidian 문서 기준으로 `Document`는 `slug`, `title`, `documentType`, `status`, `currentVersionId`를 가지며, `DocumentVersion`은 `versionNo`, `sourceType`, `contentSnapshot`을 가진다.[2][4] 지금 코드도 이 구조를 거의 그대로 반영하고 있어서, “문서 구조 문서 → DB 스키마 → JPA 엔티티”가 비교적 잘 이어지고 있다.[4][1]

## 2. Spring Boot를 왜 쓰는가

Spring Boot는 Java로 웹 서버와 비즈니스 로직을 빠르게 만들 수 있도록 기본 설정과 라이브러리 조합을 제공하는 프레임워크다.[5] Websidian에서는 컨트롤러, 서비스, 리포지토리, JPA, Flyway 같은 백엔드 구성 요소를 Spring Boot 위에서 묶어서 사용하려는 구조를 잡고 있다.[5][6]

처음에는 Spring Boot를 “서버를 띄우는 큰 틀”이라고 이해하면 충분하다. 그 안에서 JPA는 DB와 연결되는 부분이고, Flyway는 스키마 버전 관리, Repository는 조회/저장 창구, Entity는 DB 테이블과 연결된 객체라고 이해하면 된다.[5][1]

## 3. JPA와 Entity 개념

JPA는 Java 객체와 관계형 DB 테이블을 연결해 주는 표준이다.[1] 쉽게 말하면, Java 클래스 하나를 테이블 하나처럼 다루게 해 주고, 객체를 저장하면 SQL `INSERT`나 `UPDATE`가 실행되도록 도와주는 계층이다.[1]

예를 들어 `Document` 클래스에 `@Entity`와 `@Table(name = "documents")`를 붙이면, 이 클래스는 `documents` 테이블과 연결된다.[1] `@Id`, `@Column`, `@ManyToOne`, `@OneToMany` 같은 어노테이션은 “이 필드가 PK인지”, “어떤 컬럼인지”, “다른 테이블과 어떤 관계인지”를 JPA에게 알려주는 표식이다.[1]

### 자주 보이는 어노테이션

- `@Entity`: 이 클래스는 JPA가 관리하는 엔티티라는 뜻이다.[1]
- `@Table`: 실제 DB 테이블 이름과 제약조건을 지정한다.[1]
- `@Id`: 기본 키(primary key) 필드다.[1]
- `@GeneratedValue`, `@UuidGenerator`: ID를 자동 생성한다.[1]
- `@Column`: 컬럼명, null 허용 여부, 길이 같은 속성을 지정한다.[1]
- `@ManyToOne`, `@OneToMany`: 테이블 관계를 표현한다.[1]
- `@JoinColumn`: 외래 키(FK) 컬럼을 지정한다.[1]
- `@Convert`: enum 같은 값을 DB 문자열과 변환할 때 사용한다.[1]

## 4. 현재 도메인 구조 이해

Websidian의 핵심 관계는 `Vault 1 : N Document`, `Document 1 : N DocumentVersion`이다.[2][4] 또한 `Vault`는 하나의 `entryDocument`를 가질 수 있고, `Document`는 현재 대표 버전을 가리키는 `currentVersion`을 가질 수 있다.[2][4][1]

이 구조는 파일 시스템으로 비유하면 이해가 쉽다. `Vault`는 하나의 프로젝트 폴더이고, `Document`는 그 안의 문서 파일 메타데이터이며, `DocumentVersion`은 저장 이력이라고 보면 된다.[2][3][1]

### 코드에서 보이는 관계

| 관계 | 코드 표현 | 의미 |
|---|---|---|
| Vault → Documents | `@OneToMany(mappedBy = "vault")` [1] | 하나의 Vault에 여러 Document가 속한다.[1] |
| Document → Vault | `@ManyToOne` [1] | 각 Document는 하나의 Vault에 속한다.[1] |
| Document → Versions | `@OneToMany(mappedBy = "document")` [1] | 하나의 Document는 여러 Version을 가진다.[1] |
| DocumentVersion → Document | `@ManyToOne` [1] | 각 Version은 하나의 Document에 속한다.[1] |
| Vault → EntryDocument | `@ManyToOne` [1] | Vault의 진입 문서를 하나 지정할 수 있다.[2][1] |
| Document → CurrentVersion | `@ManyToOne` [1] | 문서의 현재 활성 버전을 하나 지정할 수 있다.[2][1] |

## 5. 관계형 DB에서 꼭 알아야 하는 것

관계형 DB는 데이터를 표 형태의 테이블로 저장하고, 테이블 사이를 키로 연결한다.[4][1] 여기서 가장 중요한 개념은 PK, FK, unique, check constraint다.[4][1]

### PK와 FK

- PK(Primary Key): 각 행을 유일하게 구분하는 값이다. 여기서는 `vaults.id`, `documents.id`, `document_versions.id`가 해당한다.[4][1]
- FK(Foreign Key): 다른 테이블의 PK를 참조하는 값이다. 예를 들어 `documents.vault_id`는 `vaults.id`를 참조한다.[4][1]
- FK가 있다는 것은 “이 문서는 반드시 어떤 Vault에 속해야 한다” 같은 규칙을 DB 차원에서 강제한다는 뜻이다.[4][1]

### Unique와 Check Constraint

- `documents_vault_slug_unique`: 같은 Vault 안에서는 같은 slug를 가진 문서를 둘 수 없다는 뜻이다.[1]
- `document_versions_document_version_unique`: 같은 Document 안에서는 같은 버전 번호를 둘 수 없다는 뜻이다.[1]
- `documents_status_check`, `documents_type_check`: status나 type에 허용된 값만 들어가도록 막는다.[1]

이런 제약은 “실수해도 DB가 마지막 방어선 역할을 한다”는 점에서 매우 중요하다. 애플리케이션 코드에서 검증을 해도, 최종 무결성은 DB가 잡아주는 구조가 더 안전하다.[1]

## 6. Entity 안의 비즈니스 규칙

지금 구현에서 좋은 점은 단순 getter/setter만 있는 빈 객체가 아니라, 엔티티 안에 규칙이 들어 있다는 점이다.[1] 예를 들어 `Document.publish()`는 `currentVersion`이 없으면 예외를 던지고, `Vault.changeEntryDocument()`는 같은 Vault 소속이면서 Published 상태인 문서만 entry document로 허용한다.[1]

이런 방식은 “상태를 아무 데서나 바꾸지 않고, 규칙을 통과한 경우만 상태 변경”하도록 만든다. 처음에는 불편해 보여도 프로젝트가 커질수록 훨씬 안전해진다.[1][3]

### 현재 코드에서 보이는 규칙

- `Document`는 생성 시 `slug`, `title`, `documentType`이 비어 있으면 안 된다.[1]
- `Document`는 기본 상태가 `DRAFT`다.[1][3]
- `Document.publish()`는 현재 버전이 있을 때만 가능하다.[1][3]
- `Vault.changeEntryDocument()`는 같은 Vault 소속이면서 `PUBLISHED` 상태인 문서만 허용한다.[1]
- `DocumentVersion`의 `versionNo`는 1 이상이어야 한다.[1]

## 7. enum과 Converter를 왜 쓰는가

코드에서는 `DocumentType`, `DocumentStatus`, `Visibility`를 enum으로 만들고, 각각 converter를 통해 DB 문자열로 바꾸고 있다.[1] 예를 들어 Java에서는 `DocumentStatus.DRAFT`로 다루지만, DB에는 `draft` 문자열로 저장된다.[1]

이 방식의 장점은 코드에서 오타를 줄이고, 허용된 값 집합을 명확히 표현할 수 있다는 점이다. 대신 enum 값을 바꾸면 DB 값과 migration, check constraint까지 같이 맞춰야 하므로, enum은 가볍게 바꾸지 않는 게 좋다.[1]

## 8. Soft Delete란 무엇인가

`Vault`와 `Document`에는 `deleted_at` 컬럼이 있고, 실제 삭제 대신 삭제 시각만 기록하는 soft delete 방식을 사용한다.[1] 코드에서는 `@SQLDelete`로 delete 시 `UPDATE ... SET deleted_at = CURRENT_TIMESTAMP`가 실행되게 하고, `@SQLRestriction("deleted_at IS NULL")`로 기본 조회에서 삭제된 데이터를 숨기고 있다.[1]

soft delete의 장점은 복구 가능성과 운영 안정성이다. 특히 문서 시스템에서는 사용자가 실수로 삭제했을 때 복구 여지가 중요하므로, Websidian 같은 서비스에 잘 맞는 선택이다.[3][1]

### soft delete에서 주의할 점

- DB에서 실제 row가 사라지는 것은 아니다.[1]
- 기본 조회에서는 안 보이지만, 데이터는 남아 있다.[1]
- “삭제된 문서도 관리자 화면에서 봐야 하는가” 같은 요구가 생기면 조회 전략을 다시 고민해야 한다.[1]
- unique 제약과 soft delete가 같이 있을 때는, 삭제된 데이터가 여전히 unique 충돌을 일으킬 수 있는지 설계를 확인해야 한다.[1]

## 9. Cascade와 연관관계 관리

`Vault.documents`와 `Document.versions`에는 `cascade = CascadeType.ALL`이 들어 있다.[1] 이는 부모 엔티티를 저장할 때 자식 엔티티도 함께 저장되도록 해 주는 옵션이다.[1]

예를 들어 `documentRepository.saveAndFlush(document)`를 할 때, 그 안에 연결된 `DocumentVersion`도 같이 저장된다.[1] 덕분에 서비스 코드가 단순해지지만, 반대로 연관관계가 잘못 잡혀 있으면 의도치 않은 저장이나 삭제가 발생할 수 있어서 관계 메서드를 신중하게 설계해야 한다.[1]

### 왜 `addDocument`, `addVersion` 같은 메서드가 필요한가

양방향 관계에서는 한쪽만 바꾸면 메모리 안의 객체 상태가 어긋날 수 있다.[1] 그래서 `vault.addDocument(document)`가 `documents` 리스트에 추가하는 동시에 `document.assignVault(this)`도 호출하고, `document.addVersion(version)`도 `version.assignDocument(this)`를 같이 호출한다.[1]

즉, 이 메서드들은 단순 편의 메서드가 아니라 “양쪽 관계를 항상 함께 맞추는 규칙 메서드”라고 이해하면 된다.[1]

## 10. FetchType.LAZY를 처음에 어떻게 이해하면 좋은가

`@ManyToOne(fetch = FetchType.LAZY)`는 연관된 객체를 바로 읽지 않고, 실제로 필요할 때 나중에 읽는 전략이다.[1] 처음에는 “성능을 위해 미뤄서 가져오는 옵션” 정도로 이해하면 된다.[1]

예를 들어 `Document`를 조회했다고 해서 항상 `Vault` 전체를 같이 읽을 필요는 없다. 이런 기본 전략은 조회가 많아질수록 중요해지지만, 나중에 JSON 직렬화나 트랜잭션 범위와 엮여 문제를 만들 수 있으니, 우선은 “기본은 늦게 로딩, 꼭 필요하면 fetch 전략을 따로 설계한다” 정도로 기억하면 충분하다.[1][5]

## 11. Repository는 어떤 역할인가

현재 구조에서 `DocumentRepository`, `VaultRepository`, `DocumentVersionRepository`는 domain 계층의 저장소 인터페이스이고, `JpaDocumentRepository` 같은 구현체가 infrastructure 계층에 있다.[1][5] 이 구조는 Websidian 문서의 모듈 구조 방향과도 맞는다.[5]

이 패턴의 핵심은 도메인이 “어떻게 저장하는지”보다 “무엇을 저장/조회할 수 있어야 하는지”만 알게 만드는 것이다. 즉, 도메인은 JPA에 직접 의존하지 않고, infrastructure가 JPA를 이용해 실제 저장을 처리한다.[5][1]

### 지금 repository 메서드에서 배울 수 있는 것

- `findByVaultIdAndSlug(...)`: 특정 Vault 안에서 slug로 문서를 찾는다.[1]
- `findByVaultId(...)`: Vault 소속 문서 목록을 가져온다.[1]
- `findByVaultIdAndStatus(...)`: 상태별 문서 목록을 가져온다.[1]
- `findTopByDocumentIdOrderByVersionNoDesc(...)`: 가장 최신 버전을 가져온다.[1]

Spring Data JPA는 메서드 이름만 보고도 쿼리를 생성할 수 있다. 처음에는 마법처럼 보이지만, 사실은 “이름 규칙 기반 자동 쿼리 생성”이라고 이해하면 된다.[1]

## 12. `saveAndFlush()`는 왜 쓰는가

테스트 코드에서 `saveAndFlush()`를 많이 쓰는 이유는 DB에 바로 반영해서 제약조건 위반이나 실제 저장 결과를 즉시 확인하기 위해서다.[1] `save()`만 쓰면 SQL 실행 시점이 조금 뒤로 밀릴 수 있어서, 테스트에서는 `flush()`로 강제로 반영하는 경우가 많다.[1]

처음에는 `save()`와 `saveAndFlush()`의 차이를 엄청 깊게 파지 않아도 된다. “테스트에서 진짜 DB 제약조건을 바로 확인하고 싶을 때는 `saveAndFlush()`가 유용하다” 정도면 충분하다.[1]

## 13. Flyway migration은 왜 중요한가

현재 설정은 `spring.jpa.hibernate.ddl-auto=validate`이고, 스키마 생성/변경은 Flyway migration이 담당한다.[1] 이 뜻은 “엔티티가 알아서 테이블을 만들게 하지 않고, SQL 파일로 스키마 변경 이력을 관리한다”는 의미다.[1]

이 방식은 처음엔 번거롭지만 훨씬 안전하다. 특히 Websidian처럼 나중에 PostgreSQL, MinIO, 버전 관리, 링크, 파일 구조가 점점 늘어날 프로젝트에서는 “언제 어떤 스키마가 추가/변경됐는지”를 SQL 파일로 남기는 편이 훨씬 관리하기 좋다.[1]

### 현재 migration 파일에서 중요한 포인트

- `vaults`, `documents`, `document_versions` 테이블을 만든다.[1]
- UUID 기본값, FK, unique, check constraint, index를 같이 정의한다.[1]
- `updated_at` 자동 갱신용 trigger를 만든다.[1]
- 순환 참조 때문에 `documents.current_version_id`, `vaults.entry_document_id`는 뒤에서 `ALTER TABLE`로 FK를 추가한다.[1]

여기서 특히 중요한 학습 포인트는 “현실 DB 설계는 순환 참조나 생성 순서 때문에 한 번에 다 만들기 어렵고, 나눠서 만든다”는 점이다.[1]

## 14. 테스트에서 무엇을 검증하고 있는가

현재 `DocumentPersistenceTest`는 단순히 저장이 되는지만 보는 것이 아니라, 도메인 규칙과 DB 제약을 함께 확인하고 있다.[1] 이건 아주 좋은 출발점이다.[1]

### 현재 테스트가 검증하는 것

- Vault, Document, DocumentVersion를 저장 후 다시 조회할 수 있는가.[1]
- 최신 버전 조회가 version 번호 기준으로 올바른가.[1]
- Published 문서만 entry document로 지정 가능한가.[1]
- 같은 Vault 안에서 slug 중복이 DB에서 막히는가.[1]
- 같은 Document 안에서 version 번호 중복이 DB에서 막히는가.[1]

이 테스트들은 “코드 규칙”과 “DB 규칙”이 둘 다 작동하는지 같이 보여준다. 백엔드에서는 서비스 메서드만 보는 테스트보다 이런 persistence 테스트가 꽤 중요하다.[1]

## 15. 지금 코드에서 기억할 규칙

### 도메인 규칙

- Vault는 여러 Document를 가진다.[2][1]
- Document는 반드시 하나의 Vault에 속한다.[4][1]
- Document는 여러 Version을 가질 수 있다.[2][1]
- Document를 publish하려면 current version이 필요하다.[3][1]
- Vault의 entry document는 published 문서여야 한다.[3][1]

### DB 규칙

- Vault slug는 전역에서 유일하다.[1]
- Document slug는 같은 Vault 안에서만 유일하다.[1]
- Version 번호는 같은 Document 안에서만 유일하다.[1]
- status, type, visibility는 허용된 문자열만 저장된다.[1]
- 삭제는 hard delete가 아니라 soft delete다.[1]

### 구현 규칙

- 연관관계는 `addDocument`, `addVersion` 같은 메서드로 맞춘다.[1]
- enum은 converter를 통해 DB 문자열과 매핑한다.[1]
- 스키마 변경은 JPA 자동 생성이 아니라 Flyway migration으로 관리한다.[1]
- repository 인터페이스는 domain에 두고, JPA 구현은 infrastructure에 둔다.[5][1]

## 16. 처음 배우는 입장에서 헷갈리기 쉬운 점

### 1) 객체와 테이블은 비슷하지만 완전히 같지는 않다

Java에서는 객체 참조로 관계를 표현하지만, DB에서는 FK 값으로 관계를 표현한다.[1] JPA는 그 사이를 연결해 주는 도구일 뿐이고, 결국 DB에서는 SQL과 제약조건이 실제 진실이다.[1]

### 2) 코드에서 막는 것과 DB가 막는 것은 역할이 다르다

생성자 검증이나 `publish()` 같은 메서드는 비즈니스 규칙을 코드에서 막는 것이다.[1] 반면 unique, FK, check constraint는 데이터 무결성을 DB에서 막는 것이다.[1] 둘 중 하나만 있으면 부족한 경우가 많다.[1]

### 3) JPA가 자동으로 다 해 주는 것은 아니다

JPA가 저장과 조회를 편하게 해 주지만, 관계 설계, fetch 전략, 제약조건, migration 설계는 여전히 개발자가 직접 판단해야 한다.[5][1] 그래서 “JPA를 쓰면 DB를 몰라도 된다”가 아니라, 오히려 기본 DB 개념을 같이 알아야 안정적으로 쓸 수 있다.[1]

## 17. Websidian에서 이 구현이 갖는 의미

이 구현은 Websidian에서 문서를 단순 파일이 아니라 “상태와 버전과 진입점 규칙을 가진 도메인 객체”로 다루기 시작했다는 의미가 있다.[2][3][1] 특히 Markdown뿐 아니라 HTML도 같은 `DocumentType` 체계 안에서 다루는 점은 Websidian의 핵심 방향과 맞닿아 있다.[2][3][1]

아직 File, Link, Tag, Permission 같은 나머지 모델은 붙지 않았지만, 지금의 Vault-Document-Version 구조는 이후 그래프, 백링크, 검색, 퍼블리시 흐름의 기반이 된다.[2][4]

## 18. 다음에 공부하면 좋은 순서

학습 순서는 아래처럼 가는 것이 좋다.[5][6][1]

1. Spring Boot의 계층 구조 이해: controller, application/service, domain, infrastructure 구분.[5]
2. JPA 기본: entity, PK/FK, 연관관계, cascade, lazy loading.[1]
3. DB 기본: unique, check constraint, index, transaction, migration.[4][1]
4. Websidian API 흐름: Vault 생성, Document 생성, Version 추가, Publish API 설계.[6][1]

특히 지금 단계에서는 새로운 기능을 많이 붙이기보다, 현재 코드가 왜 이렇게 생겼는지 이해하는 쪽이 더 중요하다. 이 기반을 이해하면 이후 controller나 service를 붙일 때 훨씬 덜 헷갈린다.[5][1]

## 19. 이 메모를 어디에 두면 좋은가

이 문서는 Websidian의 공식 설계 문서라기보다 개인 학습용 정리이므로 `docs/learning/`에 두는 것이 가장 자연스럽다.[2][5] 반면 “이번 구현에서 어떤 결정을 했는지”를 남기고 싶다면 `docs/working/`에 작업 메모를 따로 두고, 최종적으로 확정된 구조만 `docs/backend/`나 `docs/database/`에 반영하는 흐름이 좋다.[5]

추천 파일명 예시는 아래와 같다.

- `docs/learning/spring-boot-jpa-document-domain-basics.md`
- `docs/learning/websidian-document-persistence-first-cut.md`

## 20. 개인 메모용 한 줄 정리

Spring Boot는 백엔드 애플리케이션의 큰 틀이고, JPA는 Java 객체와 DB 테이블을 연결해 주는 도구이며, 현재 Websidian 구현은 Vault-Document-Version 구조를 DB에 안전하게 저장하기 위한 첫 persistence 레이어다.[5][1]