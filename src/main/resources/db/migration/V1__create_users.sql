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
    password VARCHAR(255) NOT NULL
);
