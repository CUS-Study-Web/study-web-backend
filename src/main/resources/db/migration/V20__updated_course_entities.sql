-- Align course and progress tables with the updated JPA entities.

ALTER TABLE courses
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

CREATE TABLE user_subject_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    progress_percent INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_subject UNIQUE (user_id, subject_id)
);

CREATE INDEX idx_user_subject_progress_user
    ON user_subject_progress(user_id);
