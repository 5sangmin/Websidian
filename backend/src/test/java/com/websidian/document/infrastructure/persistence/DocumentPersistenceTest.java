package com.websidian.document.infrastructure.persistence;

import com.websidian.document.domain.Document;
import com.websidian.document.domain.DocumentStatus;
import com.websidian.document.domain.DocumentType;
import com.websidian.document.domain.DocumentVersion;
import com.websidian.document.domain.Visibility;
import com.websidian.document.domain.Vault;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class DocumentPersistenceTest {

    @Autowired
    private JpaVaultRepository vaultRepository;

    @Autowired
    private JpaDocumentRepository documentRepository;

    @Autowired
    private JpaDocumentVersionRepository documentVersionRepository;

    @Test
    @DisplayName("Vault, Document, DocumentVersion를 저장하고 다시 조회할 수 있다")
    void saveAndLoadDocumentAggregate() {
        Vault vault = new Vault(
                "demo-vault",
                "Demo Vault",
                "Demo vault description",
                Visibility.PRIVATE
        );
        vault.setOwnerId(UUID.randomUUID());

        Vault savedVault = vaultRepository.save(vault);

        Document document = new Document("main", "Main Document", DocumentType.MARKDOWN);
        document.setCreatedBy(UUID.randomUUID());
        savedVault.addDocument(document);

        DocumentVersion version1 = new DocumentVersion(
                1,
                DocumentType.MARKDOWN,
                "# Hello Websidian",
                "checksum-v1"
        );
        version1.setCreatedBy(UUID.randomUUID());

        document.createAndSetCurrentVersion(version1);

        documentRepository.saveAndFlush(document);

        Optional<Document> found = documentRepository.findByVaultIdAndSlug(savedVault.getId(), "main");

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Main Document");
        assertThat(found.get().getStatus()).isEqualTo(DocumentStatus.DRAFT);
        assertThat(found.get().getCurrentVersion()).isNotNull();
        assertThat(found.get().getCurrentVersion().getVersionNo()).isEqualTo(1);
        assertThat(found.get().getCurrentVersion().getContentSnapshot()).isEqualTo("# Hello Websidian");
    }

    @Test
    @DisplayName("문서 버전 조회 시 가장 최신 version_no를 가져올 수 있다")
    void findLatestVersion() {
        Vault vault = new Vault(
                "demo-vault-2",
                "Demo Vault 2",
                "Demo vault 2 description",
                Visibility.PRIVATE
        );
        vault.setOwnerId(UUID.randomUUID());
        Vault savedVault = vaultRepository.saveAndFlush(vault);

        Document document = new Document("readme", "README", DocumentType.MARKDOWN);
        savedVault.addDocument(document);

        DocumentVersion version1 = new DocumentVersion(
                1,
                DocumentType.MARKDOWN,
                "# v1",
                "checksum-v1"
        );

        DocumentVersion version2 = new DocumentVersion(
                2,
                DocumentType.MARKDOWN,
                "# v2",
                "checksum-v2"
        );

        document.addVersion(version1);
        document.addVersion(version2);
        document.changeCurrentVersion(version2);

        Document savedDocument = documentRepository.saveAndFlush(document);

        Optional<DocumentVersion> latest =
                documentVersionRepository.findTopByDocumentIdOrderByVersionNoDesc(savedDocument.getId());

        List<DocumentVersion> versions =
                documentVersionRepository.findByDocumentIdOrderByVersionNoDesc(savedDocument.getId());

        assertThat(latest).isPresent();
        assertThat(latest.get().getVersionNo()).isEqualTo(2);
        assertThat(latest.get().getContentSnapshot()).isEqualTo("# v2");

        assertThat(versions).hasSize(2);
        assertThat(versions.get(0).getVersionNo()).isEqualTo(2);
        assertThat(versions.get(1).getVersionNo()).isEqualTo(1);
    }

    @Test
    @DisplayName("Published 상태의 문서만 Vault entry document로 지정할 수 있다")
    void onlyPublishedDocumentCanBeEntryDocument() {
        Vault vault = new Vault(
                "demo-vault-3",
                "Demo Vault 3",
                "Demo vault 3 description",
                Visibility.PRIVATE
        );
        vault.setOwnerId(UUID.randomUUID());

        Document draftDocument = new Document("draft-main", "Draft Main", DocumentType.MARKDOWN);
        vault.addDocument(draftDocument);

        assertThatThrownBy(() -> vault.changeEntryDocument(draftDocument))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Entry document must be published.");

        DocumentVersion version1 = new DocumentVersion(
                1,
                DocumentType.MARKDOWN,
                "# Published Main",
                "checksum-published-v1"
        );
        draftDocument.createAndSetCurrentVersion(version1);
        draftDocument.publish();

        vault.changeEntryDocument(draftDocument);

        Vault savedVault = vaultRepository.saveAndFlush(vault);

        Optional<Vault> found = vaultRepository.findBySlug("demo-vault-3");

        assertThat(found).isPresent();
        assertThat(found.get().getEntryDocument()).isNotNull();
        assertThat(found.get().getEntryDocument().getSlug()).isEqualTo("draft-main");
        assertThat(found.get().getEntryDocument().getStatus()).isEqualTo(DocumentStatus.PUBLISHED);
    }

    @Test
    @DisplayName("같은 Vault 안에서는 동일한 slug의 문서를 저장할 수 없다")
    void cannotSaveDuplicateSlugInSameVault() {
        Vault vault = new Vault(
                "demo-vault-4",
                "Demo Vault 4",
                "Demo vault 4 description",
                Visibility.PRIVATE
        );
        vault.setOwnerId(UUID.randomUUID());
        Vault savedVault = vaultRepository.saveAndFlush(vault);

        Document first = new Document("main", "Main Document", DocumentType.MARKDOWN);
        savedVault.addDocument(first);
        first.createAndSetCurrentVersion(new DocumentVersion(
                1,
                DocumentType.MARKDOWN,
                "# first",
                "checksum-first"
        ));
        documentRepository.saveAndFlush(first);

        Document second = new Document("main", "Another Main Document", DocumentType.MARKDOWN);
        savedVault.addDocument(second);
        second.createAndSetCurrentVersion(new DocumentVersion(
                1,
                DocumentType.MARKDOWN,
                "# second",
                "checksum-second"
        ));

        assertThatThrownBy(() -> documentRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    @DisplayName("같은 Document 안에서는 동일한 version_no를 저장할 수 없다")
    void cannotSaveDuplicateVersionNoInSameDocument() {
        Vault vault = new Vault(
                "demo-vault-5",
                "Demo Vault 5",
                "Demo vault 5 description",
                Visibility.PRIVATE
        );
        vault.setOwnerId(UUID.randomUUID());
        Vault savedVault = vaultRepository.saveAndFlush(vault);

        Document document = new Document("guide", "Guide", DocumentType.MARKDOWN);
        savedVault.addDocument(document);

        DocumentVersion version1 = new DocumentVersion(
                1,
                DocumentType.MARKDOWN,
                "# v1-first",
                "checksum-v1-first"
        );
        DocumentVersion duplicateVersion1 = new DocumentVersion(
                1,
                DocumentType.MARKDOWN,
                "# v1-duplicate",
                "checksum-v1-duplicate"
        );

        document.addVersion(version1);
        document.addVersion(duplicateVersion1);
        document.changeCurrentVersion(version1);

        assertThatThrownBy(() -> documentRepository.saveAndFlush(document))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}