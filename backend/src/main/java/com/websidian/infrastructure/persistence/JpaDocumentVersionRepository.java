package com.websidian.document.infrastructure.persistence;

import com.websidian.document.domain.DocumentVersion;
import com.websidian.document.domain.DocumentVersionRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA 기반 DocumentVersion persistence adapter.
 *
 * <p>이 인터페이스는 document version 조회/저장을 담당하는
 * domain port {@link DocumentVersionRepository}의 JPA 구현이다.</p>
 *
 * <p>현재는 문서별 버전 조회와 최신 버전 조회 정도만 제공한다.
 * 이후 버전 비교, 페이징, 특정 상태 기준 조회가 필요해지면
 * custom query 또는 전용 조회 repository로 분리할 수 있다.</p>
 */
public interface JpaDocumentVersionRepository extends JpaRepository<DocumentVersion, UUID>, DocumentVersionRepository {

    @Override
    Optional<DocumentVersion> findByDocumentIdAndVersionNo(UUID documentId, Integer versionNo);

    @Override
    List<DocumentVersion> findByDocumentIdOrderByVersionNoDesc(UUID documentId);

    @Override
    Optional<DocumentVersion> findTopByDocumentIdOrderByVersionNoDesc(UUID documentId);
}