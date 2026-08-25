-- Seed a new normal learner
INSERT INTO users (
    id,
    gmail,
    name,
    password,
    role,
    tier,
    status,
    course_id,
    created_at,
    updated_at
) VALUES (
    'd1111111-1111-1111-1111-111111111103',
    'newlearner@gm.cus.vn',
    'New Learner',
    '$2a$10$CmiIX16Mxc0cihxl33133.zm2TvSshAFouw9Bs.b5WZKtnpuBbldK',
    'LEARNER',
    'NORMAL',
    'ACTIVE',
    'a1111111-1111-1111-1111-111111111111',
    NOW(),
    NOW()
);

-- Seed VIP requests: 1 for current normal learner, 1 for new learner
INSERT INTO vip_requests (
    id,
    user_id,
    status,
    note,
    request_date,
    created_at,
    updated_at
) VALUES (
    'f1111111-1111-1111-1111-111111111101',
    'd1111111-1111-1111-1111-111111111102',
    'WAITING',
    'Nâng cấp tài khoản VIP cho normal learner',
    NOW(),
    NOW(),
    NOW()
), (
    'f1111111-1111-1111-1111-111111111102',
    'd1111111-1111-1111-1111-111111111103',
    'WAITING',
    'Nâng cấp tài khoản VIP cho new learner',
    NOW(),
    NOW(),
    NOW()
);
