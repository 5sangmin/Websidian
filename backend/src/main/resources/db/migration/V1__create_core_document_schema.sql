-- =====================================================
-- Websidian V1 Migration: Core Document Schema
-- 
-- Purpose:
--   First cut persistence layer for Document and Version management.
--   This migration creates the minimal tables needed to test:
--   - Document metadata persistence
--   - Version history tracking
--   - Soft delete handling
--   - Vault-Document relationship
--
-- Scope:
--   - vaults
--   - documents
--   - document_versions
--
-- Excluded from V1 (to be added in V2+):
--   - users (created_by/owner_id left as UUID for now, nullable)
--   - files
--   - document_links
--   - tags
--   - permissions
-- =====================================================

-- =====================================================
-- Extension: UUID support
-- =====================================================
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

-- =====================================================
-- Table: vaults
-- =====================================================
CREATE TABLE vaults (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    owner_id UUID NULL,  -- FK to users (not created yet), nullable for first cut
    slug VARCHAR(255) NOT NULL,
    name VARCHAR(500) NOT NULL,
    description TEXT NULL,
    visibility VARCHAR(50) NOT NULL DEFAULT 'private',
    entry_document_id UUID NULL,  -- FK to documents (self-referencing, set after document creation)
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL,

    CONSTRAINT vaults_slug_unique UNIQUE (slug),
    CONSTRAINT vaults_visibility_check CHECK (visibility IN ('public', 'private'))
);

COMMENT ON TABLE vaults IS 'Top-level document repository container';
COMMENT ON COLUMN vaults.owner_id IS 'User who owns this vault (nullable for first cut, FK to users will be added later)';
COMMENT ON COLUMN vaults.entry_document_id IS 'Default entry document for this vault (nullable, FK will be added later)';
COMMENT ON COLUMN vaults.deleted_at IS 'Soft delete timestamp (NULL = active, NOT NULL = deleted)';

-- =====================================================
-- Table: documents
-- =====================================================
CREATE TABLE documents (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    vault_id UUID NOT NULL,
    slug VARCHAR(255) NOT NULL,
    title VARCHAR(1000) NOT NULL,
    document_type VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL DEFAULT 'draft',
    current_version_id UUID NULL,  -- FK to document_versions (circular dependency, set after version creation)
    created_by UUID NULL,  -- FK to users (not created yet), nullable for first cut
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at TIMESTAMPTZ NULL,

    CONSTRAINT documents_vault_fk FOREIGN KEY (vault_id) REFERENCES vaults(id) ON DELETE CASCADE,
    CONSTRAINT documents_vault_slug_unique UNIQUE (vault_id, slug),
    CONSTRAINT documents_type_check CHECK (document_type IN ('markdown', 'html')),
    CONSTRAINT documents_status_check CHECK (status IN ('draft', 'published', 'archived'))
);

COMMENT ON TABLE documents IS 'Core document entity (both Markdown and HTML documents)';
COMMENT ON COLUMN documents.current_version_id IS 'Points to the current active version (FK will be added after document_versions table)';
COMMENT ON COLUMN documents.status IS 'Lifecycle stage: draft, published, archived (independent of deleted_at)';
COMMENT ON COLUMN documents.deleted_at IS 'Soft delete timestamp (NULL = active, NOT NULL = deleted)';

-- =====================================================
-- Table: document_versions
-- =====================================================
CREATE TABLE document_versions (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    document_id UUID NOT NULL,
    version_no INTEGER NOT NULL,
    source_type VARCHAR(50) NOT NULL,
    content_snapshot TEXT NOT NULL,
    checksum VARCHAR(64) NULL,  -- SHA-256 or similar, optional for first cut
    created_by UUID NULL,  -- FK to users (not created yet), nullable for first cut
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT document_versions_document_fk FOREIGN KEY (document_id) REFERENCES documents(id) ON DELETE CASCADE,
    CONSTRAINT document_versions_document_version_unique UNIQUE (document_id, version_no),
    CONSTRAINT document_versions_source_type_check CHECK (source_type IN ('markdown', 'html')),
    CONSTRAINT document_versions_version_no_positive CHECK (version_no > 0)
);

COMMENT ON TABLE document_versions IS 'Version history for documents (immutable snapshots)';
COMMENT ON COLUMN document_versions.version_no IS 'Incremental version number within a document (starts at 1)';
COMMENT ON COLUMN document_versions.content_snapshot IS 'Full content snapshot at this version (DB-stored for first cut, MinIO migration TBD)';

-- =====================================================
-- Deferred FK: documents.current_version_id -> document_versions.id
-- =====================================================
ALTER TABLE documents
    ADD CONSTRAINT documents_current_version_fk
    FOREIGN KEY (current_version_id) REFERENCES document_versions(id) ON DELETE SET NULL;

COMMENT ON CONSTRAINT documents_current_version_fk ON documents IS 'Points to the active version (application layer ensures same document_id)';

-- =====================================================
-- Deferred FK: vaults.entry_document_id -> documents.id
-- =====================================================
ALTER TABLE vaults
    ADD CONSTRAINT vaults_entry_document_fk
    FOREIGN KEY (entry_document_id) REFERENCES documents(id) ON DELETE SET NULL;

COMMENT ON CONSTRAINT vaults_entry_document_fk ON vaults IS 'Default entry document (application layer ensures same vault_id)';

-- =====================================================
-- Indexes: Query optimization
-- =====================================================

-- Vault lookup by slug
CREATE INDEX idx_vaults_slug ON vaults(slug);

-- Document lookup by vault and slug
CREATE INDEX idx_documents_vault_slug ON documents(vault_id, slug);

-- Document filtering by vault and status (for listing active documents)
CREATE INDEX idx_documents_vault_status ON documents(vault_id, status) WHERE deleted_at IS NULL;

-- Version lookup by document (most recent first)
CREATE INDEX idx_document_versions_document_version ON document_versions(document_id, version_no DESC);

-- Soft delete filtering (active vaults)
CREATE INDEX idx_vaults_active ON vaults(id) WHERE deleted_at IS NULL;

-- Soft delete filtering (active documents)
CREATE INDEX idx_documents_active ON documents(id) WHERE deleted_at IS NULL;

-- =====================================================
-- Trigger: Auto-update updated_at on vaults
-- =====================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER vaults_updated_at
BEFORE UPDATE ON vaults
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- Trigger: Auto-update updated_at on documents
-- =====================================================
CREATE TRIGGER documents_updated_at
BEFORE UPDATE ON documents
FOR EACH ROW
EXECUTE FUNCTION update_updated_at_column();

-- =====================================================
-- End of V1 Migration
-- =====================================================