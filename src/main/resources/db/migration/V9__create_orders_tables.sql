CREATE TABLE orders (
    id UUID PRIMARY KEY,
    user_id UUID REFERENCES users(id),
    guest_id UUID,
    guest_name VARCHAR(120),
    guest_phone VARCHAR(30),
    guest_email VARCHAR(180),
    type VARCHAR(30) NOT NULL,
    status VARCHAR(40) NOT NULL,
    delivery_street VARCHAR(180),
    delivery_number VARCHAR(30),
    delivery_complement VARCHAR(120),
    delivery_neighborhood VARCHAR(120),
    notes VARCHAR(500),
    total_amount NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT orders_owner_check CHECK (
        (user_id IS NOT NULL AND guest_id IS NULL)
        OR (user_id IS NULL AND guest_id IS NOT NULL)
    ),
    CONSTRAINT orders_guest_customer_check CHECK (
        user_id IS NOT NULL
        OR (guest_name IS NOT NULL AND guest_phone IS NOT NULL)
    ),
    CONSTRAINT orders_type_check CHECK (type IN ('DELIVERY', 'PICKUP', 'DINE_IN')),
    CONSTRAINT orders_status_check CHECK (
        status IN (
            'AWAITING_PAYMENT',
            'PAYMENT_APPROVED',
            'PREPARING',
            'READY_FOR_PICKUP',
            'OUT_FOR_DELIVERY',
            'DELIVERED',
            'FINISHED',
            'CANCELLED'
        )
    ),
    CONSTRAINT orders_delivery_address_check CHECK (
        type <> 'DELIVERY'
        OR (
            delivery_street IS NOT NULL
            AND delivery_number IS NOT NULL
            AND delivery_neighborhood IS NOT NULL
        )
    ),
    CONSTRAINT orders_total_amount_check CHECK (total_amount >= 0)
);

CREATE TABLE order_items (
    id UUID PRIMARY KEY,
    order_id UUID NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    product_id UUID NOT NULL REFERENCES products(id),
    product_name VARCHAR(120) NOT NULL,
    unit_price NUMERIC(10, 2) NOT NULL,
    quantity INTEGER NOT NULL,
    subtotal NUMERIC(10, 2) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT order_items_quantity_check CHECK (quantity > 0),
    CONSTRAINT order_items_unit_price_check CHECK (unit_price >= 0),
    CONSTRAINT order_items_subtotal_check CHECK (subtotal >= 0)
);

CREATE INDEX idx_orders_user_id ON orders (user_id);
CREATE INDEX idx_orders_guest_id ON orders (guest_id);
CREATE INDEX idx_orders_status ON orders (status);
CREATE INDEX idx_orders_type ON orders (type);
CREATE INDEX idx_orders_created_at ON orders (created_at);
CREATE INDEX idx_order_items_order_id ON order_items (order_id);
CREATE INDEX idx_order_items_product_id ON order_items (product_id);
