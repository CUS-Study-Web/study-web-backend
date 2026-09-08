-- Seed Course
INSERT INTO courses (id, title, subtitle, description, badge_title, created_at, updated_at) 
VALUES ('c1000000-0000-0000-0000-000000000000', 'Khóa học THPT Quốc Gia', 'Luyện thi Đại học', 'Khóa học giúp các bạn học sinh nắm vững kiến thức', 'HOT', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Subject
INSERT INTO subjects (id, course_id, title, max_scores, num_lessons, duration_hour, created_at, updated_at)
VALUES ('51000000-0000-0000-0000-000000000000', 'c1000000-0000-0000-0000-000000000000', 'Toán Học', 10, 50, 120.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO subjects (id, course_id, title, max_scores, num_lessons, duration_hour, created_at, updated_at)
VALUES ('52000000-0000-0000-0000-000000000000', 'c1000000-0000-0000-0000-000000000000', 'Vật Lý', 10, 45, 100.0, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Teacher Profiles
INSERT INTO teacher_profiles (id, name, subject, description, avatar_url, created_at, updated_at)
VALUES ('f1000000-0000-0000-0000-000000000000', 'Nguyễn Văn A', 'TOÁN HỌC', 'Giáo viên chuyên toán 10 năm kinh nghiệm', 'https://example.com/avatar1.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO teacher_profiles (id, name, subject, description, avatar_url, created_at, updated_at)
VALUES ('f2000000-0000-0000-0000-000000000000', 'Trần Thị B', 'VẬT LÝ', 'Giáo viên chuyên lý', 'https://example.com/avatar2.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Leaderboard
INSERT INTO leaderboard (id, student_name, course_id, achievement, avatar_url, sum_score, university, created_at, updated_at)
VALUES ('e1000000-0000-0000-0000-000000000000', 'Phạm Minh C', 'c1000000-0000-0000-0000-000000000000', 'Thủ khoa khối A', 'https://example.com/student1.jpg', 29.5, 'Đại học Bách Khoa', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO leaderboard (id, student_name, course_id, achievement, avatar_url, sum_score, university, created_at, updated_at)
VALUES ('e2000000-0000-0000-0000-000000000000', 'Lê Thu D', 'c1000000-0000-0000-0000-000000000000', 'Á khoa khối A', 'https://example.com/student2.jpg', 28.5, 'Đại học Ngoại Thương', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Achievement Scores
INSERT INTO achievement_scores (id, exam_subject_id, achievement_id, score, created_at, updated_at)
VALUES ('a1000000-0000-0000-0000-000000000000', '51000000-0000-0000-0000-000000000000', 'e1000000-0000-0000-0000-000000000000', 10, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO achievement_scores (id, exam_subject_id, achievement_id, score, created_at, updated_at)
VALUES ('a2000000-0000-0000-0000-000000000000', '52000000-0000-0000-0000-000000000000', 'e1000000-0000-0000-0000-000000000000', 9, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Seed Reviews
INSERT INTO reviews (id, student_name, course_id, comment, time_text, avatar_url, created_at, updated_at)
VALUES ('d1000000-0000-0000-0000-000000000000', 'Học sinh E', 'c1000000-0000-0000-0000-000000000000', 'Khóa học rất hay và bổ ích, giúp em đạt điểm cao.', '1 tháng trước', 'https://example.com/student3.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
INSERT INTO reviews (id, student_name, course_id, comment, time_text, avatar_url, created_at, updated_at)
VALUES ('d2000000-0000-0000-0000-000000000000', 'Học sinh F', 'c1000000-0000-0000-0000-000000000000', 'Thầy cô giảng bài rất dễ hiểu!', '2 tuần trước', 'https://example.com/student4.jpg', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
