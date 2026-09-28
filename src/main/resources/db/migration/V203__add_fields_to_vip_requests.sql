ALTER TABLE vip_requests
    ADD COLUMN name VARCHAR(150),
    ADD COLUMN email VARCHAR(150),
    ADD COLUMN phone VARCHAR(20),
    ADD COLUMN birth DATE,
    ADD COLUMN evidence_url VARCHAR(500);

UPDATE vip_requests vr
SET
    name = u.name,
    email = u.gmail,
    phone = u.phone,
    birth = u.birth
FROM users u
WHERE vr.user_id = u.id;
