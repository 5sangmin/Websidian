package com.websidian.document.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import org.hibernate.annotations.UuidGenerator;

import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(
        name = "document_versions",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "document_versions_document_version_unique",
                        columnNames = {"document_id", "version_no"}
                )
        }
)
public class DocumentVersion {

    @Id
    @GeneratedValue
    @UuidGenerator
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "document_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "document_versions_document_fk")
    )
    private Document document;

    @Column(name = "version_no", nullable = false)
    private Integer versionNo;

    @Convert(converter = DocumentTypeConverter.class)
    @Column(name = "source_type", nullable = false, length = 50)
    private DocumentType sourceType;

    @Column(name = "content_snapshot", nullable = false, columnDefinition = "TEXT")
    private String contentSnapshot;

    @Column(name = "checksum", length = 64)
    private String checksum;

    @Column(name = "created_by")
    private UUID createdBy;

    @Column(name = "created_at", nullable = false, updatable = false, insertable = false)
    private OffsetDateTime createdAt;

    protected DocumentVersion() {
    }

    public DocumentVersion(
            Integer versionNo,
            DocumentType sourceType,
            String contentSnapshot,
            String checksum
    ) {
        if (versionNo == null || versionNo < 1) {
            throw new IllegalArgumentException("versionNo must be greater than 0");
        }
        if (sourceType == null) {
            throw new IllegalArgumentException("sourceType must not be null");
        }
        if (contentSnapshot == null || contentSnapshot.isBlank()) {
            throw new IllegalArgumentException("contentSnapshot must not be blank");
        }

        this.versionNo = versionNo;
        this.sourceType = sourceType;
        this.contentSnapshot = contentSnapshot;
        this.checksum = checksum;
    }

    void assignDocument(Document document) {
        this.document = document;
    }

    public UUID getId() {
        return id;
    }

    public Document getDocument() {
        return document;
    }

    public Integer getVersionNo() {
        return versionNo;
    }

    public DocumentType getSourceType() {
        return sourceType;
    }

    public String getContentSnapshot() {
        return contentSnapshot;
    }

    public String getChecksum() {
        return checksum;
    }

    public UUID getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(UUID createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}