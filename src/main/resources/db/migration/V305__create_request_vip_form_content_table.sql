CREATE TABLE request_vip_form_content (
    id UUID PRIMARY KEY,
    form_title VARCHAR(255),
    description TEXT,
    hotline VARCHAR(50),
    fanpage_link TEXT,
    bank_name VARCHAR(255),
    account_holder VARCHAR(255),
    account_number VARCHAR(100),
    transfer_content VARCHAR(255),
    account_holder_qr_url VARCHAR(500),
    updated_by UUID REFERENCES users(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);