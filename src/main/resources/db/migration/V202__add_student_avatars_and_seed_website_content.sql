-- Add student2_avatar and student3_avatar to homepage_content
ALTER TABLE homepage_content
    ADD COLUMN student2_avatar VARCHAR(500),
    ADD COLUMN student3_avatar VARCHAR(500);

-- Seed initial homepage content
INSERT INTO homepage_content (
    id,
    badge_title,
    headline_1,
    headline_2,
    description,
    cta_btn1_name,
    cta_btn1_url,
    cta_btn2_name,
    cta_btn2_url,
    main_image_url,
    stat1_number,
    stat1_desc,
    stat2_number,
    stat2_desc,
    student1_avatar,
    student2_avatar,
    student3_avatar,
    student_stats_desc,
    updated_by,
    created_at,
    updated_at
) VALUES (
    'c1111111-1111-1111-1111-111111111101',
    'Luyện thi ĐGNL - CUS',
    'Cơ hội do bạn quyết —',
    'Tương lai do bạn chọn!',
    'Đội ngũ giảng viên chuyên gia, lộ trình cá nhân hóa và hơn 3.400 học viên đã đỗ vào các trường đại học hàng đầu Việt Nam.',
    'Bắt đầu ngay',
    '/register',
    'Xem khóa học',
    '/courses',
    NULL,
    '29 / 30',
    'Điểm thi cao nhất 2024',
    '96%',
    'đạt điểm mục tiêu',
    NULL,
    NULL,
    NULL,
    '3.400+ học viên đã đỗ vào các trường đại học hàng đầu Việt Nam',
    'b1111111-1111-1111-1111-111111111101',
    NOW(),
    NOW()
);

-- Seed initial footer content
INSERT INTO footer_content (
    id,
    company_name,
    address,
    facebook_url,
    instagram_url,
    youtube_url,
    tiktok_url,
    phone,
    email,
    website,
    working_hours,
    copyright_text,
    privacy_url,
    terms_url,
    updated_by,
    created_at,
    updated_at
) VALUES (
    'd1111111-1111-1111-1111-111111111101',
    'CÔNG TY TNHH ĐÀO TẠO PHÁT TRIỂN CUS',
    '479 Mã Lò, phường Bình Hưng Hoà A, quận Bình Tân, TP.HCM',
    'https://facebook.com/luyenthicungcus',
    'https://instagram.com/...',
    'https://youtube.com/...',
    'https://tiktok.com/...',
    '036 217 4805',
    'luyenthicungcus@gmail.com',
    'www.luyenthicungcus.com.vn',
    'T2-T7: 7:30–21:00',
    '© 2025 Công ty TNHH Đào Tạo Phát Triển CUS. Bảo lưu mọi quyền.',
    '/privacy',
    '/terms',
    'b1111111-1111-1111-1111-111111111101',
    NOW(),
    NOW()
);

-- Seed initial footer links
INSERT INTO footer_links (id, footer_id, category, label, url, sort_order, created_at, updated_at)
VALUES
    ('e1111111-1111-1111-1111-111111111101', 'd1111111-1111-1111-1111-111111111101', 'PROGRAM', 'V-ACT', '/courses/v-act', 0, NOW(), NOW()),
    ('e1111111-1111-1111-1111-111111111102', 'd1111111-1111-1111-1111-111111111101', 'PROGRAM', 'V-SAT', '/courses/v-sat', 1, NOW(), NOW()),
    ('e1111111-1111-1111-1111-111111111103', 'd1111111-1111-1111-1111-111111111101', 'PROGRAM', 'HSA', '/courses/hsa', 2, NOW(), NOW()),
    ('e1111111-1111-1111-1111-111111111104', 'd1111111-1111-1111-1111-111111111101', 'ABOUT', 'Giới thiệu', '/about', 0, NOW(), NOW()),
    ('e1111111-1111-1111-1111-111111111105', 'd1111111-1111-1111-1111-111111111101', 'ABOUT', 'Đội ngũ giảng viên', '/instructors', 1, NOW(), NOW()),
    ('e1111111-1111-1111-1111-111111111106', 'd1111111-1111-1111-1111-111111111101', 'ABOUT', 'Tuyển dụng', '/careers', 2, NOW(), NOW());
