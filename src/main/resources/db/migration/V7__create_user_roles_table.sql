CREATE TABLE user_roles (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    role VARCHAR(40) NOT NULL,
    PRIMARY KEY (user_id, role),
    CONSTRAINT user_roles_role_check CHECK (
        role IN (
            'CLIENT',
            'STAFF',
            'ADMIN',
            'CASHIER',
            'WAITER',
            'PRODUCTS_MANAGER',
            'ORDERS_MANAGER',
            'FINANCE',
            'CHAT_OPERATOR'
        )
    )
);

INSERT INTO user_roles (user_id, role)
SELECT id, role
FROM users
ON CONFLICT (user_id, role) DO NOTHING;

ALTER TABLE users
ALTER COLUMN role DROP NOT NULL;

CREATE INDEX idx_user_roles_role ON user_roles (role);
