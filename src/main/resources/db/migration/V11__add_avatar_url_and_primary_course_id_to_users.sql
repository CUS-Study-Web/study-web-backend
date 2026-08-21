ALTER TABLE users
    ADD COLUMN avatar_url VARCHAR(500),
    ADD COLUMN primary_course_id UUID REFERENCES courses(id) ON DELETE SET NULL;
