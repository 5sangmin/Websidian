package com.websidian.document.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface VaultRepository {

    Optional<Vault> findById(UUID id);

    Optional<Vault> findBySlug(String slug);

    boolean existsBySlug(String slug);

    List<Vault> findAll();
}