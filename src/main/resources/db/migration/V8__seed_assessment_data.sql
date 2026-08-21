-- Seed Homework for V-ACT Course (Course ID: a1111111-1111-1111-1111-111111111111)
-- Linked to Subject "Tiếng Việt" (Subject ID: b1111111-1111-1111-1111-111111111101)
INSERT INTO assessments (id, course_id, subject_id, title, duration_min, num_questions, max_score, file_type, file_url, access, assessment_type, status, published_at, created_at, updated_at)
VALUES 
    ('d1111111-1111-1111-1111-111111111101', 'a1111111-1111-1111-1111-111111111111', 'b1111111-1111-1111-1111-111111111101', 'Bài tập Tư duy logic — Tuần 1', 60, 5, 100, 'PDF', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'PUBLIC', 'HOMEWORK', 'PUBLISHED', NOW(), NOW(), NOW()),
    ('d1111111-1111-1111-1111-111111111102', 'a1111111-1111-1111-1111-111111111111', 'b1111111-1111-1111-1111-111111111101', 'Bài tập Toán ứng dụng — Tuần 2', 60, 40, 100, 'PDF', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'VIP', 'HOMEWORK', 'PUBLISHED', NOW(), NOW(), NOW()),
    ('d1111111-1111-1111-1111-111111111103', 'a1111111-1111-1111-1111-111111111111', 'b1111111-1111-1111-1111-111111111101', 'Bài tập Toán tư duy logic — Tuần 3', 60, 40, 100, 'PDF', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'VIP', 'HOMEWORK', 'PUBLISHED', NOW(), NOW(), NOW()),
    ('d1111111-1111-1111-1111-111111111104', 'a1111111-1111-1111-1111-111111111111', 'b1111111-1111-1111-1111-111111111101', 'Bài tập Ngữ văn nghị luận xã hội', 60, 40, 100, 'PDF', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'VIP', 'HOMEWORK', 'PUBLISHED', NOW(), NOW(), NOW());

-- Seed Exams for V-ACT Course
INSERT INTO assessments (id, course_id, subject_id, title, duration_min, num_questions, max_score, file_type, file_url, access, assessment_type, status, published_at, created_at, updated_at)
VALUES 
    ('e1111111-1111-1111-1111-111111111101', 'a1111111-1111-1111-1111-111111111111', NULL, 'Đề thi thử V-ACT — Mã đề 001', 150, 5, 120, 'PDF', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'PUBLIC', 'EXAM', 'PUBLISHED', NOW(), NOW(), NOW()),
    ('e1111111-1111-1111-1111-111111111102', 'a1111111-1111-1111-1111-111111111111', NULL, 'Đề thi thử V-ACT — Mã đề 002', 150, 120, 120, 'PDF', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'VIP', 'EXAM', 'PUBLISHED', NOW(), NOW(), NOW()),
    ('e1111111-1111-1111-1111-111111111103', 'a1111111-1111-1111-1111-111111111111', NULL, 'Đề ôn tập tổng hợp V-ACT 2024', 150, 120, 120, 'PDF', 'https://www.w3.org/WAI/ER/tests/xhtml/testfiles/resources/pdf/dummy.pdf', 'VIP', 'EXAM', 'PUBLISHED', NOW(), NOW(), NOW());

-- Seed Answer Keys for Homework 1 (id: d1111111-1111-1111-1111-111111111101) - 5 questions
INSERT INTO answer_keys (id, exam_id, question_number, question_type, correct_answer, created_at, updated_at)
VALUES 
    (gen_random_uuid(), 'd1111111-1111-1111-1111-111111111101', 1, 'SINGLE_CHOICE', 'A', NOW(), NOW()),
    (gen_random_uuid(), 'd1111111-1111-1111-1111-111111111101', 2, 'SINGLE_CHOICE', 'B', NOW(), NOW()),
    (gen_random_uuid(), 'd1111111-1111-1111-1111-111111111101', 3, 'SINGLE_CHOICE', 'C', NOW(), NOW()),
    (gen_random_uuid(), 'd1111111-1111-1111-1111-111111111101', 4, 'SINGLE_CHOICE', 'D', NOW(), NOW()),
    (gen_random_uuid(), 'd1111111-1111-1111-1111-111111111101', 5, 'SINGLE_CHOICE', 'A', NOW(), NOW());

-- Seed Answer Keys for Exam 1 (id: e1111111-1111-1111-1111-111111111101) - 5 questions
INSERT INTO answer_keys (id, exam_id, question_number, question_type, correct_answer, created_at, updated_at)
VALUES 
    (gen_random_uuid(), 'e1111111-1111-1111-1111-111111111101', 1, 'SINGLE_CHOICE', 'C', NOW(), NOW()),
    (gen_random_uuid(), 'e1111111-1111-1111-1111-111111111101', 2, 'SINGLE_CHOICE', 'D', NOW(), NOW()),
    (gen_random_uuid(), 'e1111111-1111-1111-1111-111111111101', 3, 'SINGLE_CHOICE', 'A', NOW(), NOW()),
    (gen_random_uuid(), 'e1111111-1111-1111-1111-111111111101', 4, 'SINGLE_CHOICE', 'B', NOW(), NOW()),
    (gen_random_uuid(), 'e1111111-1111-1111-1111-111111111101', 5, 'SINGLE_CHOICE', 'C', NOW(), NOW());
