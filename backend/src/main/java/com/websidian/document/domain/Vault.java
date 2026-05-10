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
        name = "vaults",
        uniqueConstraints = {
                @UniqueConstraint(name = "vaults_slug_unique", columnNames = "slug")
        }
)
@SQLDelete(sql = "UPDATE vaults SET deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
@SQLRestriction("deleted_at IS NULL")
public class Vault {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "owner_id")
    private UUID ownerId;

    @Column(name = "slug", nullable = false, length = 255)
    private String slug;

    @Column(name = "name", nullable = false, length = 500)
    private String name;

    @Column(name = "description")
    private String description;

    @Convert(converter = VisibilityConverter.class)
    @Column(name = "visibility", nullable = false, length = 50)
    private Visibility visibility = Visibility.PRIVATE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "entry_document_id",
            foreignKey = @ForeignKey(name = "vaults_entry_document_fk")
    )
    private Document entryDocument;

    @OneToMany(mappedBy = "vault", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Document> documents = new ArrayList<>();

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime updatedAt;

    @Column(name = "deleted_at")
    private OffsetDateTime deletedAt;

    protected Vault() {
    }

    public Vault(String slug, String name, String description, Visibility visibility) {
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.visibility = visibility == null ? Visibility.PRIVATE : visibility;
    }

    public void addDocument(Document document) {
        if (document == null) {
            throw new IllegalArgumentException("document must not be null");
        }

        if (!this.documents.contains(document)) {
            this.documents.add(document);
        }

        document.assignVault(this);
    }

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

    public void changeName(String name) {
        this.name = name;
    }

    public void changeDescription(String description) {
        this.description = description;
    }

    public void changeVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public UUID getId() {
        return id;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(UUID ownerId) {
        this.ownerId = ownerId;
    }

    public String getSlug() {
        return slug;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public Visibility getVisibility() {
        return visibility;
    }

    public Document getEntryDocument() {
        return entryDocument;
    }

    public List<Document> getDocuments() {
        return Collections.unmodifiableList(documents);
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