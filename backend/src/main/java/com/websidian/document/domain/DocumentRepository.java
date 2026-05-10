package com.websidian.document.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentRepository {

    Optional<Document> findById(UUID id);

    Optional<Document> findByVaultIdAndSlug(UUID vaultId, String slug);

    List<Document> findByVaultId(UUID vaultId);

    List<Document> findByVaultIdAndStatus(UUID vaultId, DocumentStatus status);

    boolean existsByVaultIdAndSlug(UUID vaultId, String slug);
}