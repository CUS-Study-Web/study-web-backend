-- Insert the pricing page settings first
INSERT INTO pricing_page_content (
    id,
    normal_pkg_name,
    normal_pkg_price,
    normal_pkg_desc,
    normal_btn_text,
    vip_pkg_tag,
    vip_pkg_name,
    vip_pkg_price,
    vip_pkg_billing_period,
    vip_pkg_desc,
    vip_btn_text,
    created_at,
    updated_at
) VALUES (
    '87c8008e-5b23-455c-a5b6-7eb8d79cb8e9', 
    'Tài khoản Thường',
    'Miễn phí',
    'Phù hợp để khám phá nền tảng CUS với các tính năng cơ bản.',
    'Đang sử dụng',
    '+ Phổ biến',
    'Tài khoản VIP',
    '199.000 đ',
    'tháng',
    'Đầy đủ tính năng, không giới hạn truy cập toàn bộ nội dung và đề thi.',
    'Nâng cấp ngay +',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

-- Insert VIP features
INSERT INTO vip_features (
    id,
    setting_id,
    feature_name,
    icon_normal_access,
    normal_access,
    normal_has_icon,
    icon_vip_access,
    vip_access,
    vip_has_icon,
    created_at,
    updated_at
) VALUES 
(
    gen_random_uuid(),
    '87c8008e-5b23-455c-a5b6-7eb8d79cb8e9',
    'Làm đề thi',
    'CHECKED',
    'Giới hạn 3 đề mỗi ngày',
    true,
    'CHECKED',
    'Không giới hạn',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
),
(
    gen_random_uuid(),
    '87c8008e-5b23-455c-a5b6-7eb8d79cb8e9',
    'Xem video bài giảng',
    'NON_EXIST',
    'Không khả dụng',
    false,
    'CHECKED',
    'Toàn bộ thư viện video',
    true,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);
