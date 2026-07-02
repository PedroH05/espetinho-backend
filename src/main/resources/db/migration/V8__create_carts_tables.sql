CREATE TABLE carts (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id) ON DELETE CASCADE,
    guest_id UUID,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT carts_owner_check CHECK (
        (user_id IS NOT NULL AND guest_id IS NULL)
        OR (user_id IS NULL AND guest_id IS NOT NULL)
    )
);

CREATE TABLE cart_items (
    id UUID PRIMARY KEY,
    cart_id UUID NOT NULL REFERENCES carts(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id),
    quantity INTEGER NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT cart_items_quantity_check CHECK (quantity > 0),
    CONSTRAINT cart_items_unit_price_check CHECK (unit_price >= 0),
    CONSTRAINT cart_items_cart_product_unique UNIQUE (cart_id, product_id)
);

CREATE UNIQUE INDEX idx_carts_active_user
ON carts (user_id)
WHERE active = TRUE AND user_id IS NOT NULL;

CREATE UNIQUE INDEX idx_carts_active_guest
ON carts (guest_id)
WHERE active = TRUE AND guest_id IS NOT NULL;

CREATE INDEX idx_cart_items_cart_id ON cart_items (cart_id);
CREATE INDEX idx_cart_items_product_id ON cart_items (product_id);
