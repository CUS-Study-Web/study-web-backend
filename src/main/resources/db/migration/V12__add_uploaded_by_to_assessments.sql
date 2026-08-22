ALTER TABLE assessments
    ADD COLUMN uploaded_by_id UUID REFERENCES users(id) ON DELETE SET NULL;

CREATE INDEX idx_assessments_uploaded_by ON assessments(uploaded_by_id);
