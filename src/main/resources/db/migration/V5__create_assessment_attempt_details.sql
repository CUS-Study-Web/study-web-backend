CREATE TABLE assessment_attempt_details (
    id UUID PRIMARY KEY,
    attempt_id UUID NOT NULL REFERENCES assessment_attempts(id) ON DELETE CASCADE,
    question_number INT NOT NULL,
    selected_answer VARCHAR(10),
    correct_answer VARCHAR(10) NOT NULL,
    is_correct BOOLEAN NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_attempt_details_attempt ON assessment_attempt_details(attempt_id);
