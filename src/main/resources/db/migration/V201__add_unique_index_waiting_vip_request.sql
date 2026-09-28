CREATE UNIQUE INDEX idx_unique_waiting_vip_request
ON vip_requests(user_id)
WHERE status = 'WAITING';
