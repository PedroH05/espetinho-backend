CREATE TABLE verification_tokens (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    email VARCHAR(180) NOT NULL,
    type VARCHAR(40) NOT NULL,
    code_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    used_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT verification_tokens_type_check CHECK (type IN ('EMAIL_VERIFICATION', 'EMAIL_CHANGE', 'PASSWORD_RESET'))
);

CREATE INDEX idx_verification_tokens_email_type ON verification_tokens (email, type);
CREATE INDEX idx_verification_tokens_user_id ON verification_tokens (user_id);
CREATE INDEX idx_verification_tokens_expires_at ON verification_tokens (expires_at);
