-- 1. Alter users table to align with ERD specification
CREATE TABLE users (
    id UUID PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    gmail VARCHAR(150) NOT NULL UNIQUE,
    name VARCHAR(150) NOT NULL,
    phone VARCHAR(20),
    birth DATE,
    gender VARCHAR(20),
    school VARCHAR(150),
    password VARCHAR(255) NOT NULL,
    role VARCHAR(20) NOT NULL DEFAULT 'LEARNER',
    tier VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    join_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    last_login TIMESTAMP
);
-- 2. VIP Requests
CREATE TABLE vip_requests (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'WAITING',
    request_date TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_vip_requests_user ON vip_requests(user_id);

-- 3. Courses
CREATE TABLE courses (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    subtitle VARCHAR(255),
    description TEXT,
    badge_title VARCHAR(100),
    thumbnail_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);

-- 4. Subjects
CREATE TABLE subjects (
    id UUID PRIMARY KEY,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    title VARCHAR(255) NOT NULL,
    max_scores INT DEFAULT 0,
    num_lessons INT DEFAULT 0,
    duration_hour DECIMAL(5,2) DEFAULT 0.00,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_subjects_course ON subjects(course_id);

-- 5. Lessons
CREATE TABLE lessons (
    id UUID PRIMARY KEY,
    subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    order_num INT NOT NULL DEFAULT 1,
    title VARCHAR(255) NOT NULL,
    youtube_url VARCHAR(500),
    duration_min INT DEFAULT 0,
    access VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_lessons_subject ON lessons(subject_id);

-- 6. Assessments (Homework / Exam)
CREATE TABLE assessments (
    id UUID PRIMARY KEY,
    course_id UUID REFERENCES courses(id) ON DELETE SET NULL,
    lesson_id UUID REFERENCES lessons(id) ON DELETE SET NULL,
    title VARCHAR(255) NOT NULL,
    duration_min INT DEFAULT 0,
    num_questions INT DEFAULT 0,
    max_score INT DEFAULT 100,
    file_type VARCHAR(20),
    file_url VARCHAR(500),
    access VARCHAR(20) NOT NULL DEFAULT 'PUBLIC',
    assessment_type VARCHAR(20) NOT NULL DEFAULT 'HOMEWORK',
    status VARCHAR(50) NOT NULL DEFAULT 'DRAFT',
    explanation_url VARCHAR(500),
    is_draft BOOLEAN NOT NULL DEFAULT true,
    published_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_assessments_course ON assessments(course_id);
CREATE INDEX idx_assessments_lesson ON assessments(lesson_id);

-- 7. Answer Keys
CREATE TABLE answer_keys (
    id UUID PRIMARY KEY,
    exam_id UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    question_number INT NOT NULL,
    question_type VARCHAR(50) DEFAULT 'MULTIPLE_CHOICE',
    correct_answer VARCHAR(10) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_answer_keys_exam ON answer_keys(exam_id);

-- 8. Assessment Attempts
CREATE TABLE assessment_attempts (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    exam_id UUID NOT NULL REFERENCES assessments(id) ON DELETE CASCADE,
    attempt_number INT NOT NULL DEFAULT 1,
    score DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    num_correct INT NOT NULL DEFAULT 0,
    num_wrong INT NOT NULL DEFAULT 0,
    duration_min INT NOT NULL DEFAULT 0,
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_assessment_attempts_user ON assessment_attempts(user_id);
CREATE INDEX idx_assessment_attempts_exam ON assessment_attempts(exam_id);

-- 9. Flashcard Topics
CREATE TABLE flashcard_topics (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    num_words INT NOT NULL DEFAULT 0,
    description TEXT,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    deleted_at TIMESTAMP 
);

-- 10. Flashcards
CREATE TABLE flashcards (
    id UUID PRIMARY KEY,
    topic_id UUID NOT NULL REFERENCES flashcard_topics(id) ON DELETE CASCADE,
    word VARCHAR(255) NOT NULL,
    meaning TEXT NOT NULL,
    pronunciation VARCHAR(255),
    part_of_speech VARCHAR(100),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL
);
CREATE INDEX idx_flashcards_topic ON flashcards(topic_id);

-- 11. User Flashcard Progress
CREATE TABLE user_flashcard_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    flashcard_id UUID NOT NULL REFERENCES flashcards(id) ON DELETE CASCADE,
    status VARCHAR(20) NOT NULL DEFAULT 'STUDY',
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_flashcard UNIQUE(user_id, flashcard_id)
);

-- 12. User Topic Progress
CREATE TABLE user_topic_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic_id UUID NOT NULL REFERENCES flashcard_topics(id) ON DELETE CASCADE,
    progress_percent INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_topic UNIQUE(user_id, topic_id)
);

-- 13. User Course Progress
CREATE TABLE user_course_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    progress_percent INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_course UNIQUE(user_id, course_id)
);

-- 14. User Lesson Progress
CREATE TABLE user_lesson_progress (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    lesson_id UUID NOT NULL REFERENCES lessons(id) ON DELETE CASCADE,
    is_clicked BOOLEAN NOT NULL DEFAULT false,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    CONSTRAINT uq_user_lesson UNIQUE(user_id, lesson_id)
);

-- 15. Leaderboard
CREATE TABLE leaderboard (
    id UUID PRIMARY KEY,
    student_name VARCHAR(150) NOT NULL,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    achievement VARCHAR(255),
    avatar_url VARCHAR(500),
    sum_score DECIMAL(5,2) NOT NULL DEFAULT 0.00,
    university VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_leaderboard_course ON leaderboard(course_id);

-- 16. Achievement Scores
CREATE TABLE achievement_scores (
    id UUID PRIMARY KEY,
    exam_subject_id UUID NOT NULL REFERENCES subjects(id) ON DELETE CASCADE,
    achievement_id UUID NOT NULL REFERENCES leaderboard(id) ON DELETE CASCADE,
    score INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_achievement_scores_leaderboard ON achievement_scores(achievement_id);

-- 17. Reviews
CREATE TABLE reviews (
    id UUID PRIMARY KEY,
    student_name VARCHAR(150) NOT NULL,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    comment TEXT NOT NULL,
    time_text VARCHAR(50),
    avatar_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_reviews_course ON reviews(course_id);

-- 18. Teacher Profiles
CREATE TABLE teacher_profiles (
    id UUID PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    description TEXT,
    avatar_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 19. Activity Logs
CREATE TABLE activity_logs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    action_type VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_activity_logs_user ON activity_logs(user_id);

-- 20. Daily System Stats
CREATE TABLE daily_system_stats (
    id UUID PRIMARY KEY,
    stat_date DATE NOT NULL,
    access_count INT NOT NULL DEFAULT 0,
    registration_count INT NOT NULL DEFAULT 0,
    vip_activation_count INT NOT NULL DEFAULT 0,
    login_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP
);
CREATE INDEX idx_daily_stats_date ON daily_system_stats(stat_date);

-- 21. Monthly System Stats
CREATE TABLE monthly_system_stats (
    id UUID PRIMARY KEY,
    month INT NOT NULL,
    year INT NOT NULL,
    access_count INT NOT NULL DEFAULT 0,
    registration_count INT NOT NULL DEFAULT 0,
    vip_activation_count INT NOT NULL DEFAULT 0,
    login_count INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    deleted_at TIMESTAMP,
    CONSTRAINT uq_monthly_stats_month_year UNIQUE(month, year)
);

-- 22. Homepage Content
CREATE TABLE homepage_content (
    id UUID PRIMARY KEY,
    badge_title VARCHAR(255),
    headline_1 VARCHAR(255),
    headline_2 VARCHAR(255),
    description TEXT,
    cta_btn1_name VARCHAR(100),
    cta_btn1_url VARCHAR(500),
    cta_btn2_name VARCHAR(100),
    cta_btn2_url VARCHAR(500),
    main_image_url VARCHAR(500),
    stat1_number VARCHAR(50),
    stat1_desc VARCHAR(255),
    stat2_number VARCHAR(50),
    stat2_desc VARCHAR(255),
    student1_avatar VARCHAR(500),
    student_stats_desc TEXT,
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 23. Footer Content
CREATE TABLE footer_content (
    id UUID PRIMARY KEY,
    company_name VARCHAR(255) NOT NULL,
    address TEXT,
    facebook_url VARCHAR(500),
    instagram_url VARCHAR(500),
    youtube_url VARCHAR(500),
    tiktok_url VARCHAR(500),
    phone VARCHAR(50),
    email VARCHAR(150),
    website VARCHAR(255),
    working_hours VARCHAR(100),
    copyright_text VARCHAR(255),
    privacy_url VARCHAR(500),
    terms_url VARCHAR(500),
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 24. Footer Links
CREATE TABLE footer_links (
    id UUID PRIMARY KEY,
    footer_id UUID NOT NULL REFERENCES footer_content(id) ON DELETE CASCADE,
    category VARCHAR(20) NOT NULL DEFAULT 'PROGRAM',
    label VARCHAR(150) NOT NULL,
    url VARCHAR(500) NOT NULL,
    sort_order INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_footer_links_footer ON footer_links(footer_id);

-- 25. Pricing Page Content
CREATE TABLE pricing_page_content (
    id UUID PRIMARY KEY,
    normal_pkg_name VARCHAR(255),
    normal_pkg_price VARCHAR(100),
    normal_pkg_desc TEXT,
    normal_btn_text VARCHAR(100),
    vip_pkg_tag VARCHAR(100),
    vip_pkg_name VARCHAR(255),
    vip_pkg_price VARCHAR(100),
    vip_pkg_billing_period VARCHAR(100),
    vip_pkg_desc TEXT,
    vip_btn_text VARCHAR(100),
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

-- 26. VIP Features
CREATE TABLE vip_features (
    id UUID PRIMARY KEY,
    setting_id UUID NOT NULL REFERENCES pricing_page_content(id) ON DELETE CASCADE,
    feature_name VARCHAR(255) NOT NULL,
    icon_normal_access VARCHAR(20) NOT NULL DEFAULT 'CHECKED',
    normal_access TEXT,
    icon_vip_access VARCHAR(20) NOT NULL DEFAULT 'CHECKED',
    vip_access TEXT,
    normal_has_icon BOOLEAN NOT NULL DEFAULT true,
    vip_has_icon BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);
CREATE INDEX idx_vip_features_setting ON vip_features(setting_id);

