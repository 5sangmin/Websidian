package com.websidian.document.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Entity
@Table(
        name = "documents",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "documents_vault_slug_unique",
                        columnNames = {"vault_id", "slug"}
                )
        }
)
@SQLDelete(sql = "UPDATE documents SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Document {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "vault_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "documents_vault_fk")
    )
    private Vault vault;

    @Column(name = "slug", nullable = false, length = 255)
    private String slug;

    @Column(name = "title", nullable = false, length = 1000)
    private String title;

    @Convert(converter = DocumentTypeConverter.class)
    @Column(name = "document_type", nullable = false, length = 50)
    private DocumentType documentType;

    @Convert(converter = DocumentStatusConverter.class)
    @Column(name = "status", nullable = false, length = 50)
    private DocumentStatus status = DocumentStatus.DRAFT;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "current_version_id",
            foreignKey = @ForeignKey(name = "documents_current_version_fk")
    )
    private DocumentVersion currentVersion;

    @Column(name = "created_by")
    private UUID createdBy;

    @OneToMany(mappedBy = "document", cascade = CascadeType.ALL, orphanRemoval = false)
    @OrderBy("versionNo DESC")
    private List<DocumentVersion> versions = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected Document() {
    }

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

    void assignVault(Vault vault) {
        this.vault = vault;
    }

    public void addVersion(DocumentVersion version) {
        if (version == null) {
            throw new IllegalArgumentException("version must not be null");
        }

        if (!this.versions.contains(version)) {
            this.versions.add(version);
        }

        version.assignDocument(this);
    }

    public void removeVersion(DocumentVersion version) {
        if (version == null) {
            return;
        }

        this.versions.remove(version);
        version.assignDocument(null);

        if (this.currentVersion != null && this.currentVersion.equals(version)) {
            this.currentVersion = null;
        }
    }

    public void changeCurrentVersion(DocumentVersion version) {
        if (version != null && version.getDocument() != this) {
            throw new IllegalArgumentException("Current version must belong to the same document.");
        }
        this.currentVersion = version;
    }

    public void createAndSetCurrentVersion(DocumentVersion version) {
        addVersion(version);
        changeCurrentVersion(version);
    }

    public void changeTitle(String title) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("title must not be blank");
        }
        this.title = title;
    }

    public void publish() {
        if (this.currentVersion == null) {
            throw new IllegalStateException("Document must have current version before publish.");
        }
        this.status = DocumentStatus.PUBLISHED;
    }

    public void archive() {
        this.status = DocumentStatus.ARCHIVED;
    }

    public void revertToDraft() {
        this.status = DocumentStatus.DRAFT;
    }

    public UUID getId() {
        return id;
    }

    public Vault getVault() {
        return vault;
    }

    public String getSlug() {
        return slug;
    }

    public String getTitle() {
        return title;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public DocumentStatus getStatus() {
        return status;
    }

    public DocumentVersion getCurrentVersion() {
        return currentVersion;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public List<DocumentVersion> getVersions() {
        return Collections.unmodifiableList(versions);
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }

    public OffsetDateTime getDeletedAt() {
        return deletedAt;
    }
}