-- Migrate existing flashcard progress statuses to new enum values
UPDATE user_flashcard_progress
SET status = 'REMEMBERED'
WHERE status = 'REMEMBER';

UPDATE user_flashcard_progress
SET status = 'NOT_STUDIED'
WHERE status = 'STUDY';

ALTER TABLE user_flashcard_progress
    ALTER COLUMN status SET DEFAULT 'NOT_STUDIED';
