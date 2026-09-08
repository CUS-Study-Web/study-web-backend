-- 1. Add status column to flashcard_topics
ALTER TABLE flashcard_topics
    ADD COLUMN status VARCHAR(20) NOT NULL DEFAULT 'DRAFT';

-- 2. Rename progress_percent to learned_words in user_topic_progress
ALTER TABLE user_topic_progress
    RENAME COLUMN progress_percent TO learned_words;
