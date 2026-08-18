-- Seed Course V-ACT
INSERT INTO courses (id, title, subtitle, badge_title, description, thumbnail_url, created_at, updated_at)
VALUES (
    'a1111111-1111-1111-1111-111111111111',
    'V-ACT',
    'Đánh giá Năng lực — ĐHQG TP.HCM',
    'ĐGNL TP.HCM',
    'Kỳ thi đánh giá toàn diện các năng lực: ngôn ngữ, tư duy logic, giải quyết vấn đề trên giấy, do Đại học Quốc gia TP.HCM tổ chức để...',
    'https://images.unsplash.com/photo-1523240795612-9a054b0db644',
    NOW(),
    NOW()
);

-- Seed 8 Subjects for V-ACT Course
INSERT INTO subjects (id, course_id, title, duration_hour, num_lessons, created_at, updated_at)
VALUES 
    ('b1111111-1111-1111-1111-111111111101', 'a1111111-1111-1111-1111-111111111111', 'Tiếng Việt', 30.00, 40, NOW(), NOW()),
    ('b1111111-1111-1111-1111-111111111102', 'a1111111-1111-1111-1111-111111111111', 'Tiếng Anh', 42.00, 56, NOW(), NOW()),
    ('b1111111-1111-1111-1111-111111111103', 'a1111111-1111-1111-1111-111111111111', 'Toán học', 42.00, 56, NOW(), NOW()),
    ('b1111111-1111-1111-1111-111111111104', 'a1111111-1111-1111-1111-111111111111', 'Vật lý', 28.00, 36, NOW(), NOW()),
    ('b1111111-1111-1111-1111-111111111105', 'a1111111-1111-1111-1111-111111111111', 'Hóa học', 28.00, 36, NOW(), NOW()),
    ('b1111111-1111-1111-1111-111111111106', 'a1111111-1111-1111-1111-111111111111', 'Sinh học', 22.00, 28, NOW(), NOW()),
    ('b1111111-1111-1111-1111-111111111107', 'a1111111-1111-1111-1111-111111111111', 'Lịch sử', 20.00, 24, NOW(), NOW()),
    ('b1111111-1111-1111-1111-111111111108', 'a1111111-1111-1111-1111-111111111111', 'Địa lý', 20.00, 24, NOW(), NOW());

-- Seed 8 Lessons for Tiếng Việt Subject
INSERT INTO lessons (id, subject_id, order_num, title, duration_min, youtube_url, access, created_at, updated_at)
VALUES 
    ('c1111111-1111-1111-1111-111111111101', 'b1111111-1111-1111-1111-111111111101', 1, 'Giới thiệu kỳ thi V-ACT & cấu trúc đề', 42, 'https://www.youtube.com/', 'PUBLIC', NOW(), NOW()),
    ('c1111111-1111-1111-1111-111111111102', 'b1111111-1111-1111-1111-111111111101', 2, 'Tư duy ngôn ngữ – Đọc hiểu nhanh', 55, 'https://www.youtube.com/', 'PUBLIC', NOW(), NOW()),
    ('c1111111-1111-1111-1111-111111111103', 'b1111111-1111-1111-1111-111111111101', 3, 'Tư duy logic – Suy luận và phân tích', 68, 'https://www.youtube.com/', 'VIP', NOW(), NOW()),
    ('c1111111-1111-1111-1111-111111111104', 'b1111111-1111-1111-1111-111111111101', 4, 'Toán học ứng dụng – Đại số và xác suất', 72, 'https://www.youtube.com/', 'VIP', NOW(), NOW()),
    ('c1111111-1111-1111-1111-111111111105', 'b1111111-1111-1111-1111-111111111101', 5, 'Khoa học tự nhiên – Vật lý & Hóa học', 65, 'https://www.youtube.com/', 'VIP', NOW(), NOW()),
    ('c1111111-1111-1111-1111-111111111106', 'b1111111-1111-1111-1111-111111111101', 6, 'Khoa học xã hội – Lịch sử & Địa lý', 58, 'https://www.youtube.com/', 'VIP', NOW(), NOW()),
    ('c1111111-1111-1111-1111-111111111107', 'b1111111-1111-1111-1111-111111111101', 7, 'Kỹ năng làm bài thi trên giấy hiệu quả', 38, 'https://www.youtube.com/', 'VIP', NOW(), NOW()),
    ('c1111111-1111-1111-1111-111111111108', 'b1111111-1111-1111-1111-111111111101', 8, 'Ôn tập tổng hợp & đề thi thử toàn diện', 90, 'https://www.youtube.com/', 'VIP', NOW(), NOW());

