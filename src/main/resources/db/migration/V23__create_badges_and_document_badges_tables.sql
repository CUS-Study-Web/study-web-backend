-- 1. Create badges table
CREATE TABLE badges (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    created_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_badges_name ON badges(name);

-- 2. Create document_badges table
CREATE TABLE document_badges (
    id UUID PRIMARY KEY,
    badge_id UUID NOT NULL REFERENCES badges(id) ON DELETE CASCADE,
    document_id UUID NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT uq_document_badge UNIQUE (document_id, badge_id)
);

CREATE INDEX idx_document_badges_document ON document_badges(document_id);
CREATE INDEX idx_document_badges_badge ON document_badges(badge_id);
