-- Recreate user_course_progress table that was dropped in V20.

CREATE TABLE user_course_progress (
    id               UUID        PRIMARY KEY,
    user_id          UUID        NOT NULL REFERENCES users(id)   ON DELETE CASCADE,
    course_id        UUID        NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    progress_percent INT         NOT NULL DEFAULT 0,
    created_at       TIMESTAMP   NOT NULL,
    updated_at       TIMESTAMP   NOT NULL,
    CONSTRAINT uq_user_course UNIQUE (user_id, course_id)
);

CREATE INDEX idx_user_course_progress_user
    ON user_course_progress(user_id);
