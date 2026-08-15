-- Seed 2 Learner Users: 1 VIP and 1 Normal
INSERT INTO users (
    id, 
    gmail, 
    name, 
    password, 
    role, 
    tier, 
    status, 
    created_at, 
    updated_at
) VALUES (
    'd1111111-1111-1111-1111-111111111101', 
    'vipuser@gm.cus.vn', 
    'VIP Learner', 
    '$2a$10$CmiIX16Mxc0cihxl33133.zm2TvSshAFouw9Bs.b5WZKtnpuBbldK', 
    'LEARNER', 
    'VIP', 
    'ACTIVE', 
    NOW(), 
    NOW()
), (
    'd1111111-1111-1111-1111-111111111102', 
    'normaluser@gm.cus.vn', 
    'Normal Learner', 
    '$2a$10$CmiIX16Mxc0cihxl33133.zm2TvSshAFouw9Bs.b5WZKtnpuBbldK', 
    'LEARNER', 
    'NORMAL', 
    'ACTIVE', 
    NOW(), 
    NOW()
);
