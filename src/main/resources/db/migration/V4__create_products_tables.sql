CREATE TABLE products (
    id UUID PRIMARY KEY,
    name VARCHAR(120) NOT NULL,
    description VARCHAR(1000) NOT NULL,
    price NUMERIC(10, 2) NOT NULL,
    category VARCHAR(80) NOT NULL,
    available BOOLEAN NOT NULL DEFAULT TRUE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    stock_quantity INTEGER,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT products_price_check CHECK (price >= 0),
    CONSTRAINT products_stock_quantity_check CHECK (stock_quantity IS NULL OR stock_quantity >= 0)
);

CREATE TABLE product_images (
    product_id UUID NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    display_order INTEGER NOT NULL,
    image_url VARCHAR(500) NOT NULL,
    PRIMARY KEY (product_id, display_order)
);

CREATE INDEX idx_products_category ON products (category);
CREATE INDEX idx_products_available ON products (available);
CREATE INDEX idx_products_active ON products (active);
