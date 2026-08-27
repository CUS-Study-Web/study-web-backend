CREATE TABLE documents (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    doc_type VARCHAR(50) NOT NULL DEFAULT 'THEORY',
    file_type VARCHAR(20) NOT NULL,
    file_url VARCHAR(500) NOT NULL,
    num_pages INT DEFAULT 0,
    description TEXT,
    download_count INT NOT NULL DEFAULT 0,
    youtube_url TEXT,
    access_tier VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
