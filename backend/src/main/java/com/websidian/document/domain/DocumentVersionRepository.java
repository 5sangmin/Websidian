package com.websidian.document.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface DocumentVersionRepository {

    Optional<DocumentVersion> findById(UUID id);

    Optional<DocumentVersion> findByDocumentIdAndVersionNo(UUID documentId, Integer versionNo);

    List<DocumentVersion> findByDocumentIdOrderByVersionNoDesc(UUID documentId);

    Optional<DocumentVersion> findTopByDocumentIdOrderByVersionNoDesc(UUID documentId);
}