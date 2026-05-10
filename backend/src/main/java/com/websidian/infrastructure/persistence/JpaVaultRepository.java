package com.websidian.document.infrastructure.persistence;

import com.websidian.document.domain.Vault;
import com.websidian.document.domain.VaultRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA 기반 Vault persistence adapter.
 *
 * <p>이 인터페이스는 vault aggregate의 repository port인 {@link VaultRepository}를
 * Spring Data JPA로 연결하는 infrastructure 계층 구현이다.</p>
 *
 * <p>초기 MVP에서는 slug 기반 조회와 존재 여부 확인 정도만 지원한다.
 * 이후 owner 기준 목록 조회, visibility 기준 공개 vault 탐색 등이 필요해지면
 * query method 또는 custom repository를 확장한다.</p>
 */
public interface JpaVaultRepository extends JpaRepository<Vault, UUID>, VaultRepository {

    @Override
    Optional<Vault> findBySlug(String slug);

    @Override
    boolean existsBySlug(String slug);
}