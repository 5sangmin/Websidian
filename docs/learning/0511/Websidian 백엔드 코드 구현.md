# Websidian 백엔드 코드 구현 학습 노트

이 문서는 Websidian의 현재 `Vault`, `Document`, `DocumentVersion` 구현을 기준으로, 개념 설명보다 **코드가 왜 이렇게 생겼는지**, **어떤 규칙을 어디에 넣었는지**, **실제로 구현할 때 어떤 룰로 작성하면 좋은지**를 정리한 개인 학습용 메모다.[1][2] 현재 구현은 Websidian의 document domain과 persistence 첫 단계로, Vault-Document-Version 구조와 상태 규칙을 JPA 엔티티와 migration으로 연결하고 있다.[3][4][5][1]

## 1. 이 문서를 보는 관점

처음에는 이 코드를 “DB 테이블을 Java 클래스로 옮긴 것”이라고만 보면 절반만 이해한 것이다.[1] 실제로는 엔티티가 단순 데이터 통이 아니라, **상태 변경 규칙을 스스로 지키는 객체**가 되도록 작성되어 있다.[1]

즉, 이 코드에서 봐야 할 핵심은 세 가지다.[1]

- 어떤 필드가 DB 컬럼과 연결되는가.[1]
- 어떤 메서드가 연관관계를 안전하게 맞추는가.[1]
- 어떤 메서드가 비즈니스 규칙을 강제하는가.[1][4]

## 2. 현재 패키지 구조에서 읽는 법

현재 구조는 `domain`에 엔티티와 repository port를 두고, `infrastructure.persistence`에 JPA repository adapter를 두는 형태다.[1][2] 이건 Websidian의 backend module structure 문서에서 제안한 `api / application / domain / infrastructure` 분리 방향과 맞는다.[2]

### 현재 코드 위치

```text
backend/src/main/java/com/websidian/document/
├─ domain/
│  ├─ Document.java
│  ├─ DocumentVersion.java
│  ├─ Vault.java
│  ├─ DocumentRepository.java
│  ├─ DocumentVersionRepository.java
│  ├─ VaultRepository.java
│  ├─ DocumentStatus.java
│  ├─ DocumentType.java
│  └─ Visibility.java
└─ infrastructure/persistence/
   ├─ JpaDocumentRepository.java
   ├─ JpaDocumentVersionRepository.java
   └─ JpaVaultRepository.java
```

이 구조의 의도는 “도메인은 저장 방식의 세부 구현을 몰라도 되게 한다”는 것이다.[2][1] 나중에 JPA가 아닌 다른 저장소를 쓰더라도, domain 쪽 코드를 크게 흔들지 않으려는 설계다.[2]

## 3. 엔티티를 단순 setter 덩어리로 만들지 않은 이유

현재 엔티티에는 공개 setter가 거의 없고, 대신 의미 있는 메서드가 있다.[1] 예를 들어 `changeTitle()`, `publish()`, `changeEntryDocument()`, `addVersion()` 같은 메서드는 단순 값 변경이 아니라 **규칙이 포함된 변경 동작**이다.[1]

이 방식의 장점은 “아무 곳에서나 이상한 상태를 만들기 어렵다”는 점이다. 예를 들어 외부 코드가 `document.setStatus(PUBLISHED)`처럼 바로 바꾸지 못하고, 반드시 `publish()`를 통과해야 하므로 현재 버전 존재 여부를 체크할 수 있다.[1]

### 나쁜 예: 상태를 아무 데서나 바꾸는 경우

```java
public class Document {
    private DocumentStatus status;
    private DocumentVersion currentVersion;

    public void setStatus(DocumentStatus status) {
        this.status = status;
    }
}
```

이렇게 만들면 서비스 코드 어디서든 `PUBLISHED`로 바꿀 수 있어서, `currentVersion == null`인데도 발행된 문서가 생길 수 있다.[1] 즉, 엔티티 스스로 규칙을 지키지 못한다.[1]

### 현재 코드의 방식

```java
public void publish() {
    if (this.currentVersion == null) {
        throw new IllegalStateException("Document must have current version before publish.");
    }
    this.status = DocumentStatus.PUBLISHED;
}
```

이 구현은 publish라는 행위를 메서드로 표현하고, 그 안에서 상태 전이 규칙을 검사한다.[1][4] 처음 배울 때는 “setter 대신 의미 있는 동사 메서드로 상태를 바꾼다”라고 기억하면 좋다.[1]

## 4. 생성자 검증은 첫 번째 방어선이다

`Document`, `DocumentVersion` 생성자에는 null/blank 검사와 기본값 설정이 들어 있다.[1] 이건 엔티티가 처음부터 말이 안 되는 상태로 만들어지지 않게 하는 가장 앞쪽 방어선이다.[1]

### 현재 구현 예시

```java
public Document(String slug, String title, DocumentType documentType) {
    if (slug == null || slug.isBlank()) {
        throw new IllegalArgumentException("slug must not be blank");
    }
    if (title == null || title.isBlank()) {
        throw new IllegalArgumentException("title must not be blank");
    }
    if (documentType == null) {
        throw new IllegalArgumentException("documentType must not be null");
    }

    this.slug = slug;
    this.title = title;
    this.documentType = documentType;
    this.status = DocumentStatus.DRAFT;
}
```

여기서 배울 수 있는 룰은 두 가지다.[1]

- 생성자에서 “필수값”은 반드시 검증한다.[1]
- 기본 상태는 생성 시점에 명확히 정한다. 여기서는 `DRAFT`가 기본값이다.[1][4]

즉, 엔티티를 만들자마자 이미 규칙을 한 번 통과한 상태가 되게 만드는 것이다.[1]

## 5. 양방향 연관관계는 한쪽만 바꾸면 안 된다

JPA에서 가장 많이 헷갈리는 부분 중 하나가 양방향 관계다.[1] 현재 구현에서는 `Vault`와 `Document`, `Document`와 `DocumentVersion`이 양방향으로 연결되어 있다.[1]

문제는 리스트에만 추가하거나, 반대편 참조만 바꾸면 메모리 안 객체 상태가 어긋날 수 있다는 점이다.[1] 그래서 현재 코드는 **항상 양쪽을 같이 맞추는 메서드**를 둔다.[1]

### 현재 구현 예시: Vault와 Document

```java
public void addDocument(Document document) {
    if (document == null) {
        throw new IllegalArgumentException("document must not be null");
    }

    if (!this.documents.contains(document)) {
        this.documents.add(document);
    }

    document.assignVault(this);
}
```

```java
void assignVault(Vault vault) {
    this.vault = vault;
}
```

`addDocument()`는 컬렉션에도 넣고, 반대쪽 참조도 맞춘다.[1] 이때 `assignVault()`를 package-private으로 숨겨 둔 점이 중요하다. 외부에서 아무 코드나 `assignVault()`를 호출하지 못하게 해서, 관계 변경의 진입점을 제한하고 있다.[1]

### 같은 패턴: Document와 Version

```java
public void addVersion(DocumentVersion version) {
    if (version == null) {
        throw new IllegalArgumentException("version must not be null");
    }

    if (!this.versions.contains(version)) {
        this.versions.add(version);
    }

    version.assignDocument(this);
}
```

```java
void assignDocument(Document document) {
    this.document = document;
}
```

학습 포인트는 간단하다.[1]

- 양방향 관계는 한 메서드 안에서 같이 맞춘다.[1]
- 반대편 setter는 public으로 열지 않는다.[1]
- 연관관계의 “정식 진입점”을 한 군데로 모은다.[1]

## 6. 현재 코드에서 중요한 규칙 메서드들

### 6-1. `changeCurrentVersion()`

```java
public void changeCurrentVersion(DocumentVersion version) {
    if (version != null && version.getDocument() != this) {
        throw new IllegalArgumentException("Current version must belong to the same document.");
    }
    this.currentVersion = version;
}
```

이 메서드는 현재 버전이 **반드시 같은 문서 소속의 버전이어야 한다**는 규칙을 강제한다.[1] 즉, 다른 문서의 버전을 실수로 현재 버전으로 꽂는 것을 막는다.[1]

### 6-2. `createAndSetCurrentVersion()`

```java
public void createAndSetCurrentVersion(DocumentVersion version) {
    addVersion(version);
    changeCurrentVersion(version);
}
```

이 메서드는 자주 같이 일어나는 두 동작을 한 번에 묶는다.[1] “버전 추가”와 “현재 버전 지정”이 함께 자주 발생하므로, 아예 안전한 조합 메서드로 묶은 것이다.[1]

### 6-3. `changeEntryDocument()`

```java
public void changeEntryDocument(Document entryDocument) {
    if (entryDocument != null) {
        if (entryDocument.getVault() != this) {
            throw new IllegalArgumentException("Entry document must belong to the same vault.");
        }
        if (entryDocument.getStatus() != DocumentStatus.PUBLISHED) {
            throw new IllegalArgumentException("Entry document must be published.");
        }
    }
    this.entryDocument = entryDocument;
}
```

이 메서드는 두 가지를 확인한다.[1]

- 그 문서가 정말 같은 Vault 소속인가.[1]
- 그 문서가 Published 상태인가.[1][4]

이런 검사는 서비스 계층에서만 해도 되지 않나 싶을 수 있지만, 엔티티가 직접 알고 있는 규칙이라면 엔티티 안에서도 막는 편이 더 안전하다.[1]

## 7. getter는 열어도, 컬렉션은 직접 수정 못 하게 막고 있다

현재 `getDocuments()`와 `getVersions()`는 `Collections.unmodifiableList(...)`를 반환한다.[1] 이건 외부 코드가 `getVersions().add(...)`처럼 직접 내부 리스트를 수정하지 못하게 하려는 의도다.[1]

### 현재 구현 예시

```java
public List<DocumentVersion> getVersions() {
    return Collections.unmodifiableList(versions);
}
```

이 패턴의 의미는 “조회는 허용하지만 수정은 정식 메서드로만 하라”는 것이다.[1] 즉, 수정은 `addVersion()`, `removeVersion()` 같은 엔티티 메서드를 통해서만 이루어지게 만든다.[1]

## 8. enum + converter 조합은 코드와 DB를 분리한다

현재 구현에서는 enum을 그대로 DB에 저장하지 않고 converter를 통해 소문자 문자열로 저장한다.[1] 예를 들어 Java에서는 `DocumentStatus.PUBLISHED`지만, DB에는 `published`가 저장된다.[1]

### 현재 구현 예시

```java
@Convert(converter = DocumentStatusConverter.class)
@Column(name = "status", nullable = false, length = 50)
private DocumentStatus status = DocumentStatus.DRAFT;
```

```java
@Override
public String convertToDatabaseColumn(DocumentStatus attribute) {
    if (attribute == null) {
        return null;
    }
    return attribute.name().toLowerCase();
}
```

```java
@Override
public DocumentStatus convertToEntityAttribute(String dbData) {
    if (dbData == null || dbData.isEmpty()) {
        return null;
    }
    return DocumentStatus.valueOf(dbData.toUpperCase());
}
```

이 방식은 코드에서 enum 타입 안정성을 유지하면서, DB에는 읽기 쉬운 문자열을 남긴다.[1] 대신 enum 값 이름을 바꾸면 migration의 check constraint와 DB 값도 같이 맞춰야 하므로, enum은 설계 의도가 명확할 때만 수정하는 게 좋다.[1]

## 9. soft delete 구현은 어노테이션 세 개로 끝나지 않는다

현재 `Vault`와 `Document`는 soft delete를 사용한다.[1] 코드에서는 `@SQLDelete`와 `@SQLRestriction`으로 구현되어 있지만, 중요한 것은 이 어노테이션이 **행동 규칙**까지 암묵적으로 바꾼다는 점이다.[1]

### 현재 구현 예시

```java
@SQLDelete(sql = "UPDATE documents SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Document {
    // ...
}
```

이 설정으로 인해 `delete()`를 호출해도 실제 row 삭제가 아니라 `deleted_at`만 채워진다.[1] 또한 일반 조회에서는 삭제된 데이터가 자동으로 숨겨진다.[1]

### 구현할 때 기억할 룰

- soft delete를 쓸 때는 “삭제 후 복구 가능성”을 염두에 둔다.[4][1]
- 조회가 안 보이는 것과 실제 row 삭제는 다르다.[1]
- unique 제약, 관리자 조회, 복구 기능까지 같이 생각해야 한다.[1]

## 10. `orphanRemoval = false`와 remove 메서드의 함정

현재 코드에는 `orphanRemoval = false`가 들어 있고, `removeDocument()`나 `removeVersion()`에서 연관관계를 끊는 코드가 있다.[1] 그런데 이 부분은 구현상 주의가 필요하다.[1]

### 현재 코드 예시

```java
public void removeDocument(Document document) {
    if (document == null) {
        return;
    }

    this.documents.remove(document);
    document.assignVault(null);

    if (this.entryDocument != null && this.entryDocument.equals(document)) {
        this.entryDocument = null;
    }
}
```

`documents.vault_id`는 DB에서 `NOT NULL`이므로, 단순히 `assignVault(null)`로 끊어 버리면 저장 시점에 제약 위반이 날 수 있다.[1] 즉, 코드상으로는 메서드가 있어도 도메인 규칙과 DB 제약을 함께 보면 **사실상 안전하지 않은 remove 구현**일 수 있다.[1]

### 학습용 추천 개선 예시

```java
public void removeDocument(Document document) {
    throw new UnsupportedOperationException(
            "Document removal from vault is not supported. Use document soft delete flow instead."
    );
}
```

```java
public void removeVersion(Document version) {
    throw new UnsupportedOperationException(
            "Removing persisted versions is not supported in current MVP."
    );
}
```

처음 학습할 때 중요한 포인트는 이것이다.[1]

- “메서드가 있다”와 “안전하게 동작한다”는 다르다.[1]
- 엔티티 메서드는 DB 제약까지 함께 고려해야 한다.[1]
- soft delete 구조라면 제거보다 상태 변경 흐름이 더 맞을 수 있다.[4][1]

## 11. repository 인터페이스와 JPA 구현을 분리한 이유

현재는 domain에 `DocumentRepository` 인터페이스가 있고, infrastructure에 `JpaDocumentRepository`가 있다.[1][2] 이것은 domain이 “저장 기술”보다 “필요한 저장 기능”에 집중하도록 만드는 방식이다.[2]

### domain port

```java
public interface DocumentRepository {
    Optional<Document> findById(UUID id);
    Optional<Document> findByVaultIdAndSlug(UUID vaultId, String slug);
    List<Document> findByVaultId(UUID vaultId);
    List<Document> findByVaultIdAndStatus(UUID vaultId, DocumentStatus status);
    boolean existsByVaultIdAndSlug(UUID vaultId, String slug);
}
```

### JPA adapter

```java
public interface JpaDocumentRepository
        extends JpaRepository<Document, UUID>, DocumentRepository {

    @Override
    Optional<Document> findByVaultIdAndSlug(UUID vaultId, String slug);

    @Override
    List<Document> findByVaultId(UUID vaultId);
}
```

학습 포인트는 다음과 같다.[1][2]

- domain은 “무엇이 필요하다”를 말한다.[2][1]
- infrastructure는 “JPA로 어떻게 구현한다”를 담당한다.[2][1]
- 서비스 계층은 domain repository 인터페이스에 의존하게 설계하는 것이 자연스럽다.[2]

참고로 지금처럼 `JpaDocumentRepository`에서 메서드를 다시 적는 방식은 명시성은 있지만, 단순 query method라면 중복 선언이 없어도 동작할 수 있다.[1] 다만 학습 초반에는 “어떤 메서드가 실제 저장소 계약인지”를 눈에 보이게 적어 두는 것도 이해에는 도움이 된다.[1]

## 12. 테스트 코드는 구현 규칙을 문서처럼 보여준다

현재 `DocumentPersistenceTest`는 단순 저장 성공 여부만 보는 테스트가 아니라, 이 도메인의 규칙이 무엇인지 그대로 보여주는 테스트다.[1] 초보자 입장에서는 엔티티 코드만 보는 것보다 테스트를 같이 읽는 것이 훨씬 이해가 쉽다.[1]

### 예시 1: publish된 문서만 entry document 가능

```java
assertThatThrownBy(() -> vault.changeEntryDocument(draftDocument))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("Entry document must be published.");
```

이 테스트 한 줄은 도메인 룰을 아주 명확하게 보여 준다.[1] 즉, “entry document는 published여야 한다”는 규칙이 문서가 아니라 실행 코드로 남아 있다.[1]

### 예시 2: slug 중복은 DB가 막는다

```java
assertThatThrownBy(() -> documentRepository.saveAndFlush(second))
        .isInstanceOf(DataIntegrityViolationException.class);
```

이 코드는 애플리케이션이 실수해도 DB unique 제약이 마지막 방어선으로 동작함을 보여 준다.[1] 여기서 `saveAndFlush()`를 쓰는 이유는 SQL을 즉시 실행해서 제약 위반을 바로 확인하려는 것이다.[1]

### 예시 3: 최신 버전 조회 규칙

```java
Optional<DocumentVersion> latest =
        documentVersionRepository.findTopByDocumentIdOrderByVersionNoDesc(savedDocument.getId());
```

이 메서드 이름은 Spring Data JPA가 이름 규칙을 보고 쿼리를 만드는 방식의 예시다.[1] 처음에는 길고 어색해 보여도, “documentId로 찾고 versionNo 내림차순 정렬 후 맨 위 하나”라는 의미가 그대로 드러난다.[1]

## 13. migration SQL과 엔티티는 같이 읽어야 한다

JPA 엔티티만 보면 놓치기 쉬운 규칙이 migration SQL에는 더 명확하게 적혀 있다.[1] 예를 들어 null 허용 여부, FK 삭제 정책, unique 제약, check constraint, index는 DB가 실제로 강제하는 규칙이다.[1]

### migration에서 확인할 규칙 예시

```sql
CONSTRAINT documents_vault_slug_unique UNIQUE (vault_id, slug),
CONSTRAINT documents_type_check CHECK (document_type IN ('markdown', 'html')),
CONSTRAINT documents_status_check CHECK (status IN ('draft', 'published', 'archived'))
```

이 규칙들은 Java 코드만으로는 완전히 보장되지 않는다.[1] 즉, 엔티티는 애플리케이션 규칙, migration은 데이터 무결성 규칙을 담당한다고 보면 이해가 쉽다.[1]

### 인덱스도 중요한 구현 요소다

```sql
CREATE INDEX idx_documents_vault_status
ON documents(vault_id, status) WHERE deleted_at IS NULL;
```

인덱스는 데이터를 더 빨리 찾기 위한 구조다.[1] 학습 초기에는 “조회 메서드가 자주 쓰일 패턴이면 인덱스로 받쳐 준다” 정도로 이해하면 충분하다.[1]

## 14. 지금 코드 기준으로 지키면 좋은 구현 룰

### 엔티티 작성 룰

- public setter를 남발하지 않는다.[1]
- 상태 변경은 `publish()`, `archive()` 같은 의미 있는 메서드로 만든다.[1][4]
- 생성자에서 필수값을 검증한다.[1]
- 컬렉션은 수정 불가능 뷰로 노출한다.[1]

### 연관관계 작성 룰

- 양방향 관계는 한 메서드에서 양쪽을 같이 맞춘다.[1]
- 반대편 직접 연결 메서드는 public으로 열지 않는다.[1]
- FK가 `NOT NULL`이면 단순 null 끊기 메서드는 조심한다.[1]

### DB 연동 룰

- enum 저장값은 converter와 check constraint를 같이 맞춘다.[1]
- 스키마 변경은 엔티티 수정만 하지 말고 migration도 같이 바꾼다.[1]
- 애플리케이션 검증과 DB 제약을 둘 다 둔다.[1]

### 테스트 작성 룰

- “성공 케이스”뿐 아니라 “깨지면 안 되는 규칙”도 테스트한다.[1]
- unique, FK, 상태 전이 같은 규칙은 예외 테스트로 남긴다.[1]
- persistence 테스트에서는 `saveAndFlush()`로 실제 DB 제약을 확인한다.[1]

## 15. 학습용으로 직접 해보면 좋은 작은 리팩터링

현재 구조를 더 잘 이해하려면 아래 정도를 직접 바꿔보는 것이 좋다.[1][2]

1. `removeDocument()`와 `removeVersion()`를 soft delete 정책에 맞게 막는 방향으로 수정해 보기.[1]
2. `changeName()`, `changeDescription()`에도 blank 검증을 넣을지 결정해 보기.[1]
3. `publish()` 후 `archive()`나 `revertToDraft()`에 추가 제약이 필요한지 도메인 규칙 관점에서 적어 보기.[4][1]
4. `CreateDocumentService` 초안을 만들어 엔티티 메서드만 호출하도록 조립해 보기.[2][6]

### 학습용 서비스 예시 초안

```java
package com.websidian.document.application;

import com.websidian.document.domain.Document;
import com.websidian.document.domain.DocumentRepository;
import com.websidian.document.domain.DocumentType;
import com.websidian.document.domain.DocumentVersion;
import com.websidian.document.domain.Vault;
import com.websidian.document.domain.VaultRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

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

    public UUID create(
            UUID vaultId,
            String slug,
            String title,
            String content,
            UUID actorId
    ) {
        Vault vault = vaultRepository.findById(vaultId)
                .orElseThrow(() -> new IllegalArgumentException("Vault not found"));

        if (documentRepository.existsByVaultIdAndSlug(vaultId, slug)) {
            throw new IllegalArgumentException("Document slug already exists in vault");
        }

        Document document = new Document(slug, title, DocumentType.MARKDOWN);
        document.setCreatedBy(actorId);

        DocumentVersion version = new DocumentVersion(
                1,
                DocumentType.MARKDOWN,
                content,
                null
        );
        version.setCreatedBy(actorId);

        vault.addDocument(document);
        document.createAndSetCurrentVersion(version);

        return documentRepository.save(document).getId();
    }
}
```

이 예시의 핵심은 서비스가 직접 필드를 만지지 않고, 엔티티의 규칙 메서드를 조합해서 use case를 만든다는 점이다.[2][1] 즉, 서비스는 orchestration, 엔티티는 규칙 보유자라는 역할 분리가 보인다.[2]

## 16. 이 문서를 어디에 두면 좋은가

이 문서는 개인 학습용이므로 `docs/learning/`에 두는 것이 가장 자연스럽다.[2] 반면 여기서 실제로 채택할 구현 원칙이 있다면, 확정된 내용만 따로 `docs/backend/module-structure.md`나 `docs/architecture/document-lifecycle.md`에 반영하는 편이 좋다.[2][4]

추천 파일명은 아래 둘 중 하나가 잘 맞는다.[2]

- `docs/learning/websidian-jpa-domain-implementation-notes.md`
- `docs/learning/document-domain-code-rules.md`

## 17. 짧은 기억 포인트

현재 Websidian backend 코드는 단순 CRUD 클래스 모음이 아니라, 엔티티 내부에 규칙을 넣고, JPA 관계 메서드로 객체 상태를 맞추고, DB migration으로 마지막 무결성을 보장하는 구조다.[1] 처음에는 어노테이션보다도 “어떤 메서드가 공식 변경 통로인지”, “어떤 규칙을 코드와 DB가 나눠서 지키는지”를 읽는 습관을 들이는 것이 더 중요하다.[1]



# Websidian 현재 도메인 구조와 어노테이션 정리

이 문서는 현재 Websidian backend의 `document` 도메인 구조를 처음 보는 기준으로 정리한 학습용 메모다.[1][2] 특히 **현재 어떤 파일이 어떤 역할을 하는지**, **엔티티 사이 관계가 어떻게 잡혀 있는지**, **JPA/Hibernate 어노테이션이 각각 무슨 의미인지**를 중심으로 설명한다.[1]

## 1. 현재 구조를 한 문장으로 보면

지금 구현은 `controller`나 `service` 없이, **도메인 엔티티와 DB 저장 구조를 먼저 만든 상태**라고 보면 된다.[1][2] 즉, `Vault`, `Document`, `DocumentVersion`라는 핵심 모델과, 이들을 PostgreSQL에 저장하기 위한 JPA 매핑이 먼저 만들어져 있다.[1][3]

Websidian의 백엔드 구조 문서 기준으로 보면, 현재는 `domain`과 `infrastructure.persistence`가 먼저 잡혀 있는 단계다.[2][1] 아직 `api(controller)`와 `application(service)` 계층은 붙지 않았고, 도메인 규칙과 persistence 검증 테스트가 먼저 구현되어 있다.[1]

## 2. 패키지 구조

현재 관련 코드는 크게 두 군데에 있다.[1]

```text
backend/src/main/java/com/websidian/document/
├─ domain/
│  ├─ Vault.java
│  ├─ Document.java
│  ├─ DocumentVersion.java
│  ├─ VaultRepository.java
│  ├─ DocumentRepository.java
│  ├─ DocumentVersionRepository.java
│  ├─ Visibility.java
│  ├─ DocumentType.java
│  ├─ DocumentStatus.java
│  ├─ VisibilityConverter.java
│  ├─ DocumentTypeConverter.java
│  └─ DocumentStatusConverter.java
└─ infrastructure/persistence/
   ├─ JpaVaultRepository.java
   ├─ JpaDocumentRepository.java
   └─ JpaDocumentVersionRepository.java
```

간단히 말하면 `domain`은 “무엇을 다루는가”를 담고 있고, `infrastructure.persistence`는 “그걸 DB에 어떻게 저장하는가”를 담고 있다.[2][1] 이건 Websidian이 지향하는 `api / application / domain / infrastructure` 구조의 일부가 구현된 상태다.[2]

## 3. 현재 도메인 모델의 중심 관계

현재 중심 관계는 아래처럼 이해하면 된다.[4][3][1]

- 하나의 `Vault`는 여러 개의 `Document`를 가진다.[1]
- 하나의 `Document`는 하나의 `Vault`에 속한다.[1]
- 하나의 `Document`는 여러 개의 `DocumentVersion`을 가진다.[1]
- 하나의 `DocumentVersion`은 하나의 `Document`에 속한다.[1]
- `Vault`는 대표 진입 문서인 `entryDocument`를 가질 수 있다.[1][4]
- `Document`는 현재 활성 버전인 `currentVersion`을 가질 수 있다.[1][4]

이걸 그림처럼 보면 아래와 비슷하다.[1]

```text
Vault
 ├─ documents: List<Document>
 └─ entryDocument: Document (optional)

Document
 ├─ vault: Vault
 ├─ versions: List<DocumentVersion>
 └─ currentVersion: DocumentVersion (optional)

DocumentVersion
 └─ document: Document
```

## 4. 파일별 역할

### 엔티티 파일

#### `Vault.java`

`Vault`는 Websidian에서 문서 저장소 단위를 나타내는 엔티티다.[1][4] `slug`, `name`, `description`, `visibility`, `entryDocument`, `documents` 같은 필드를 가진다.[1]

또한 단순 데이터만 있는 것이 아니라, `addDocument()`, `removeDocument()`, `changeEntryDocument()` 같은 도메인 규칙 메서드를 가진다.[1] 특히 `changeEntryDocument()`는 같은 Vault에 속하고 published 상태인 문서만 entry document로 허용한다.[1]

#### `Document.java`

`Document`는 실제 문서 메타데이터를 나타내는 엔티티다.[1][4] `slug`, `title`, `documentType`, `status`, `currentVersion`, `versions` 등을 가진다.[1]

여기에는 문서 상태 전이 규칙이 들어 있다.[1][5] 예를 들어 `publish()`는 current version이 있을 때만 가능하고, `changeCurrentVersion()`은 같은 문서 소속 버전만 현재 버전으로 허용한다.[1]

#### `DocumentVersion.java`

`DocumentVersion`은 문서 버전 스냅샷 엔티티다.[1][4] `versionNo`, `sourceType`, `contentSnapshot`, `checksum` 등을 가진다.[1]

즉, 문서의 현재 상태 그 자체라기보다 “특정 시점의 문서 내용 기록”에 가깝다.[1][3]

### enum 파일

#### `DocumentStatus.java`

문서 상태를 나타내는 enum이다.[1] 현재 값은 `DRAFT`, `PUBLISHED`, `ARCHIVED`다.[1][5]

#### `DocumentType.java`

문서 타입을 나타내는 enum이다.[1] 현재 값은 `MARKDOWN`, `HTML`이다.[1][4]

#### `Visibility.java`

Vault 공개 범위를 나타내는 enum이다.[1] 현재 값은 `PUBLIC`, `PRIVATE`다.[1]

### converter 파일

#### `DocumentStatusConverter.java`

`DocumentStatus` enum을 DB에 저장 가능한 문자열로 바꾸고, DB 문자열을 다시 enum으로 읽어 오는 클래스다.[1] 예를 들어 코드의 `PUBLISHED`를 DB의 `published`로 바꾼다.[1]

#### `DocumentTypeConverter.java`

`DocumentType` enum ↔ 문자열 변환을 담당한다.[1]

#### `VisibilityConverter.java`

`Visibility` enum ↔ 문자열 변환을 담당한다.[1]

### repository 인터페이스 파일

#### `VaultRepository.java`

Vault를 조회하거나 존재 여부를 확인하는 기능 계약이다.[1] 예를 들어 `findById`, `findBySlug`, `existsBySlug` 같은 메서드가 있다.[1]

#### `DocumentRepository.java`

Document 조회/존재 확인 기능 계약이다.[1] 예를 들어 `findByVaultIdAndSlug`, `findByVaultIdAndStatus`, `existsByVaultIdAndSlug`가 있다.[1]

#### `DocumentVersionRepository.java`

문서 버전 조회 기능 계약이다.[1] 예를 들어 특정 문서의 최신 버전 조회나, version 번호 기준 정렬 조회를 담당한다.[1]

### persistence 구현 파일

#### `JpaVaultRepository.java`

`VaultRepository`를 Spring Data JPA로 연결한 인터페이스다.[1] 실제 DB 저장과 조회는 이쪽이 담당한다.[1]

#### `JpaDocumentRepository.java`

`DocumentRepository`의 JPA 구현이다.[1]

#### `JpaDocumentVersionRepository.java`

`DocumentVersionRepository`의 JPA 구현이다.[1]

즉, repository 인터페이스는 “필요한 저장소 기능 계약”이고, `Jpa...Repository`는 “그 계약을 JPA로 실제 연결한 구현”이라고 보면 된다.[2][1]

## 5. 가장 먼저 알아야 할 어노테이션

### `@Entity`

`@Entity`는 “이 클래스는 JPA가 관리하는 엔티티다”라는 뜻이다.[1] 쉽게 말하면 DB 테이블과 연결되는 핵심 클래스라는 의미다.[1]

현재 `Vault`, `Document`, `DocumentVersion`에 붙어 있다.[1] 즉, 이 세 클래스는 모두 DB row로 저장될 수 있는 객체다.[1]

### `@Table`

`@Table`은 이 엔티티가 어떤 테이블과 연결되는지, 그리고 테이블 수준의 제약을 어떻게 둘지 정한다.[1]

예시:

```java
@Table(
        name = "documents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "documents_vault_slug_unique",
                        columnNames = {"vault_id", "slug"}
                )
        }
)
```

이 코드는 `Document`가 `documents` 테이블과 연결되고, 같은 Vault 안에서는 같은 slug를 허용하지 않는다는 뜻이다.[1]

## 6. ID 관련 어노테이션

### `@Id`

기본 키(primary key)를 뜻한다.[1] 테이블에서 각 row를 식별하는 값이다.[1]

### `@GeneratedValue`

ID를 애플리케이션이 직접 넣지 않아도 자동 생성하겠다는 뜻이다.[1]

### `@UuidGenerator`

ID 생성 전략으로 UUID를 사용하겠다는 의미다.[1] Websidian 현재 구현은 숫자 증가형 ID 대신 UUID를 사용한다.[1][3]

예시:

```java
@Id
@GeneratedValue
@UuidGenerator
@Column(name = "id", nullable = false, updatable = false)
private UUID id;
```

이 조합은 “이 필드는 PK이고, UUID로 자동 생성되며, 저장 후 수정되지 않는다”는 뜻이다.[1]

## 7. 컬럼 관련 어노테이션

### `@Column`

`@Column`은 필드와 DB 컬럼을 연결한다.[1] 이름, null 허용 여부, 길이, 수정 가능 여부 같은 세부 설정도 여기서 한다.[1]

예시:

```java
@Column(name = "title", nullable = false, length = 1000)
private String title;
```

이 코드는 `title`이 null이면 안 되고, 길이는 최대 1000이라는 의미다.[1]

또 다른 예시:

```java
@Column(name = "created_at", nullable = false, updatable = false, insertable = false)
private OffsetDateTime createdAt;
```

이건 보통 DB 기본값이 넣어 주는 컬럼이라서, 애플리케이션이 insert/update 시 직접 건드리지 않겠다는 뜻으로 읽으면 된다.[1]

## 8. 관계 어노테이션

### `@ManyToOne`

여러 개의 엔티티가 하나의 엔티티를 참조하는 관계다.[1] 예를 들어 여러 Document가 하나의 Vault에 속하므로 `Document.vault`는 `@ManyToOne`이다.[1]

예시:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(
        name = "vault_id",
        nullable = false,
        foreignKey = @ForeignKey(name = "documents_vault_fk")
)
private Vault vault;
```

이 코드는 아래 의미를 가진다.[1]

- Document는 하나의 Vault에 속한다.[1]
- FK 컬럼 이름은 `vault_id`다.[1]
- null이면 안 된다.[1]
- 필요할 때 늦게 로딩한다(`LAZY`).[1]

### `@OneToMany`

하나의 엔티티가 여러 엔티티를 가지는 관계다.[1] 예를 들어 하나의 Vault는 여러 Document를 가진다.[1]

예시:

```java
@OneToMany(mappedBy = "vault", cascade = CascadeType.ALL, orphanRemoval = false)
private List<Document> documents = new ArrayList<>();
```

여기서 읽어야 할 포인트는 아래와 같다.[1]

- `mappedBy = "vault"`: 관계의 주인은 반대편 `Document.vault`다.[1]
- `cascade = CascadeType.ALL`: 부모 작업이 자식에도 전파될 수 있다.[1]
- `orphanRemoval = false`: 컬렉션에서 빠졌다고 자동 삭제하지는 않는다.[1]

### `@JoinColumn`

FK 컬럼 이름과 FK 제약 이름을 지정한다.[1] 즉, 어떤 컬럼으로 다른 테이블을 참조하는지 알려 준다.[1]

예시:

```java
@JoinColumn(
        name = "current_version_id",
        foreignKey = @ForeignKey(name = "documents_current_version_fk")
)
```

이건 `documents.current_version_id`가 `document_versions.id`를 참조한다는 뜻이다.[1]

## 9. fetch 전략 관련 어노테이션 값

### `FetchType.LAZY`

`LAZY`는 연관 객체를 바로 읽지 않고, 실제로 필요할 때 읽는 전략이다.[1] 처음에는 “나중에 불러오는 방식” 정도로 이해하면 된다.[1]

예를 들어 Document를 읽을 때 Vault 전체를 무조건 같이 읽지 않고, 필요할 때만 가져오게 하려는 의도다.[1] 성능과 조회 범위를 조절하기 위해 자주 쓰인다.[1]

## 10. enum 변환 관련 어노테이션

### `@Convert`

필드 저장 시 변환기를 쓰겠다는 의미다.[1] enum을 문자열로 바꿔 DB에 저장할 때 자주 쓴다.[1]

예시:

```java
@Convert(converter = DocumentStatusConverter.class)
@Column(name = "status", nullable = false, length = 50)
private DocumentStatus status = DocumentStatus.DRAFT;
```

이 뜻은 코드에서는 enum으로 다루고, DB에는 converter가 처리한 문자열로 저장한다는 의미다.[1]

### `@Converter`

변환기 클래스라는 뜻이다.[1] 예를 들어 `DocumentStatusConverter`는 아래처럼 동작한다.[1]

```java
@Converter
public class DocumentStatusConverter implements AttributeConverter<DocumentStatus, String> {

    @Override
    public String convertToDatabaseColumn(DocumentStatus attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.name().toLowerCase();
    }

    @Override
    public DocumentStatus convertToEntityAttribute(String dbData) {
        if (dbData == null || dbData.isEmpty()) {
            return null;
        }
        return DocumentStatus.valueOf(dbData.toUpperCase());
    }
}
```

즉, Java enum과 DB 문자열을 오가는 번역기 역할이다.[1]

## 11. 컬렉션 정렬 관련 어노테이션

### `@OrderBy`

`@OrderBy("versionNo DESC")`는 연관 컬렉션을 가져올 때 버전 번호 내림차순으로 정렬하라는 뜻이다.[1]

예시:

```java
@OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = false)
@OrderBy("versionNo DESC")
private List<DocumentVersion> versions = new ArrayList<>();
```

이렇게 하면 `versions`를 읽을 때 최신 버전이 앞쪽에 오게 된다.[1]

## 12. soft delete 관련 Hibernate 어노테이션

### `@SQLDelete`

삭제 시 실제 `DELETE` 대신 다른 SQL을 실행하게 만든다.[1] 현재는 soft delete를 위해 사용한다.[1]

예시:

```java
@SQLDelete(sql = "UPDATE documents SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
```

즉, 삭제 요청이 들어오면 row를 지우지 않고 `deleted_at`만 채운다.[1]

### `@SQLRestriction`

조회 시 특정 조건을 자동으로 붙인다.[1] 현재는 `deleted_at IS NULL`이라서 soft delete된 데이터를 일반 조회에서 숨긴다.[1]

예시:

```java
@SQLRestriction("deleted_at IS NULL")
```

`Vault`와 `Document` 둘 다 이 패턴을 쓰고 있다.[1]

## 13. 생성자와 메서드도 구조의 일부다

현재 엔티티는 단순 필드 모음이 아니라, 도메인 규칙을 담는 메서드도 같이 가진다.[1] 이 부분이 중요하다.[1]

예를 들면 아래와 같다.[1]

- `Vault.addDocument()`: Vault와 Document 관계를 양쪽에서 같이 맞춘다.[1]
- `Vault.changeEntryDocument()`: 같은 Vault 소속이며 published인 문서만 entry document로 허용한다.[1]
- `Document.addVersion()`: Document와 Version 양방향 관계를 맞춘다.[1]
- `Document.changeCurrentVersion()`: 현재 버전이 반드시 자기 문서의 버전인지 확인한다.[1]
- `Document.publish()`: current version이 있을 때만 publish 가능하다.[1][5]

즉, 지금 도메인 코드는 “데이터 저장용 클래스”가 아니라 “상태와 규칙을 가진 클래스”에 더 가깝다.[1]

## 14. repository 인터페이스는 왜 domain에 있나

현재 repository 인터페이스는 domain에 있다.[1] 이것은 도메인이 “어떻게 저장하는지”보다 “어떤 저장 기능이 필요한지”를 먼저 정의하도록 하려는 구조다.[2][1]

예시:

```java
public interface DocumentRepository {

    Optional<Document> findById(UUID id);

    Optional<Document> findByVaultIdAndSlug(UUID vaultId, String slug);

    List<Document> findByVaultId(UUID vaultId);

    List<Document> findByVaultIdAndStatus(UUID vaultId, DocumentStatus status);

    boolean existsByVaultIdAndSlug(UUID vaultId, String slug);
}
```

이건 “Document 저장소라면 최소한 이런 기능은 있어야 한다”는 계약이다.[1] 실제 JPA 기술 세부사항은 domain이 아니라 infrastructure가 맡는다.[2][1]

## 15. JPA repository 구현은 무엇을 하는가

`JpaDocumentRepository` 같은 파일은 Spring Data JPA가 실제 저장/조회 코드를 연결하게 하는 인터페이스다.[1] 즉, 도메인 계약을 JPA라는 기술로 구현한 adapter다.[2][1]

예시:

```java
public interface JpaDocumentRepository extends JpaRepository<Document, UUID>, DocumentRepository {

    @Override
    Optional<Document> findByVaultIdAndSlug(UUID vaultId, String slug);

    @Override
    List<Document> findByVaultId(UUID vaultId);

    @Override
    List<Document> findByVaultIdAndStatus(UUID vaultId, DocumentStatus status);

    @Override
    boolean existsByVaultIdAndSlug(UUID vaultId, String slug);
}
```

즉, `JpaRepository`의 기본 CRUD 기능과 domain repository 계약을 같이 가지는 구조다.[1]

## 16. 지금 구조를 읽을 때 추천 순서

처음 읽을 때는 아래 순서가 가장 쉽다.[1][2]

1. `DocumentType`, `DocumentStatus`, `Visibility`부터 읽기.[1]
2. `Vault`, `Document`, `DocumentVersion` 필드와 관계 읽기.[1]
3. 각 엔티티의 메서드에서 규칙 읽기.[1]
4. `...Converter`로 enum 저장 방식 확인하기.[1]
5. `...Repository`로 어떤 조회가 필요한지 보기.[1]
6. `Jpa...Repository`로 JPA 연결 확인하기.[1]

이 순서로 보면 “값 종류 → 엔티티 구조 → 규칙 → 저장 방식”이 자연스럽게 이어진다.[1]

## 17. 한 줄 요약

현재 Websidian의 `document` 도메인 구조는 `Vault`, `Document`, `DocumentVersion`를 중심으로 한 JPA 엔티티 구조이며, enum/converter로 저장값을 맞추고, repository 인터페이스와 JPA adapter로 persistence를 분리한 상태다.[1][2]