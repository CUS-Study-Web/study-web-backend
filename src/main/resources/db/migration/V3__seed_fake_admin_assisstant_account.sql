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
    'b1111111-1111-1111-1111-111111111101', 
    'admin@gm.cus.vn', 
    'Admin User', 
    '$2a$10$CmiIX16Mxc0cihxl33133.zm2TvSshAFouw9Bs.b5WZKtnpuBbldK', 
    'ADMIN', 
    'VIP', 
    'ACTIVE', 
    NOW(), 
    NOW()
), (
    'b1111111-1111-1111-1111-111111111111', 
    'assistant@gm.cus.vn', 
    'Assistant User', 
    '$2a$10$CmiIX16Mxc0cihxl33133.zm2TvSshAFouw9Bs.b5WZKtnpuBbldK', 
    'ASSISTANT', 
    'NORMAL', 
    'ACTIVE', 
    NOW(), 
    NOW()
);
