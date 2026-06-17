CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE categories (
    id UUID PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    slug VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    display_order INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_categories_slug ON categories (slug);
CREATE INDEX idx_categories_active ON categories (active);

WITH distinct_categories AS (
    SELECT DISTINCT TRIM(category) AS name
    FROM products
    WHERE category IS NOT NULL
      AND TRIM(category) <> ''
),
numbered_categories AS (
    SELECT
        name,
        LOWER(REGEXP_REPLACE(name, '[^[:alnum:]]+', '-', 'g')) AS slug,
        (ROW_NUMBER() OVER (ORDER BY name))::INTEGER AS display_order
    FROM distinct_categories
)
INSERT INTO categories (id, name, slug, active, display_order)
SELECT gen_random_uuid(), name, slug, TRUE, display_order
FROM numbered_categories
ON CONFLICT (name) DO NOTHING;

ALTER TABLE products
ADD COLUMN category_id UUID;

UPDATE products
SET category_id = categories.id
FROM categories
WHERE categories.name = products.category;

ALTER TABLE products
ALTER COLUMN category_id SET NOT NULL;

ALTER TABLE products
ADD CONSTRAINT fk_products_category
FOREIGN KEY (category_id) REFERENCES categories(id);

DROP INDEX IF EXISTS idx_products_category;

CREATE INDEX idx_products_category_id ON products (category_id);

ALTER TABLE products
DROP COLUMN category;
