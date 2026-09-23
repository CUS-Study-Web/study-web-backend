CREATE TABLE register_forms (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    phone_numer VARCHAR(50) NOT NULL,
    email VARCHAR(255) NOT NULL,
    subject VARCHAR(255),
    note TEXT,
    registered_date DATE NOT NULL DEFAULT CURRENT_DATE,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE INDEX idx_register_forms_email ON register_forms(email);
CREATE INDEX idx_register_forms_registered_date ON register_forms(registered_date);
CREATE INDEX idx_register_forms_created_at ON register_forms(created_at);
