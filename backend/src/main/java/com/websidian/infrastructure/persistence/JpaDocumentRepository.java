package com.websidian.document.infrastructure.persistence;

import com.websidian.document.domain.Document;
import com.websidian.document.domain.DocumentRepository;
import com.websidian.document.domain.DocumentStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA 기반 Document persistence adapter.
 *
 * <p>이 인터페이스는 document 도메인의 repository port인 {@link DocumentRepository}를
 * Spring Data JPA로 구현한다.</p>
 *
 * <p>현재는 query method 기반으로 단순하게 유지하고,
 * 복잡한 조회 조건이나 fetch 전략이 필요해지면 custom repository 또는 별도 adapter 클래스로 확장한다.</p>
 */
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