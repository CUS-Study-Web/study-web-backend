-- 1. Drop is_draft column from assessments (replaced by status enum DRAFT/PUBLISHED)
ALTER TABLE assessments DROP COLUMN is_draft;

-- 2. Replace lesson_id FK with subject_id FK
--    Homework now links to subject, Exam links to course (both nullable)
ALTER TABLE assessments DROP COLUMN lesson_id;
ALTER TABLE assessments ADD COLUMN subject_id UUID REFERENCES subjects(id) ON DELETE SET NULL;

-- 3. Drop old lesson index, create new subject index
DROP INDEX IF EXISTS idx_assessments_lesson;
CREATE INDEX idx_assessments_subject ON assessments(subject_id);

-- 4. Change question_type default from 'MULTIPLE_CHOICE' to 'SINGLE_CHOICE'
ALTER TABLE answer_keys ALTER COLUMN question_type SET DEFAULT 'SINGLE_CHOICE';

-- 5. Update any existing rows that still have the old default value
UPDATE answer_keys SET question_type = 'SINGLE_CHOICE' WHERE question_type = 'MULTIPLE_CHOICE';
