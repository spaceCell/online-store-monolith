CREATE TABLE IF NOT EXISTS products (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description CLOB,
    price DECIMAL(19,2) NOT NULL,
    stock_quantity INT NOT NULL
);