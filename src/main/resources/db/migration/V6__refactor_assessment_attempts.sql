-- Remove denormalized fields from assessment_attempts
ALTER TABLE assessment_attempts
DROP COLUMN score,
DROP COLUMN num_correct,
DROP COLUMN num_wrong;

-- Remove denormalized fields from assessment_attempt_details
ALTER TABLE assessment_attempt_details
DROP COLUMN correct_answer,
DROP COLUMN is_correct;
