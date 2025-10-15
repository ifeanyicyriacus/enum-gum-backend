
CREATE TABLE IF NOT EXISTS users (
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    email       VARCHAR(320) NOT NULL UNIQUE,
    password    VARCHAR(255) NOT NULL,
    verified    BOOLEAN NOT NULL
);

CREATE TABLE IF NOT EXISTS organisations (
    id          UUID PRIMARY KEY,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    name        VARCHAR(120) NOT NULL,
    description VARCHAR(1000),
    industry    VARCHAR(255),
    logo_url    VARCHAR(255),
    website     VARCHAR(255),
    plan        VARCHAR(20) DEFAULT 'FREE' NOT NULL
);

CREATE TABLE IF NOT EXISTS memberships (
    created_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    org_id     UUID NOT NULL,
    user_id    UUID NOT NULL,
    role       VARCHAR(20) NOT NULL,
    PRIMARY KEY (org_id, user_id),
    FOREIGN KEY (org_id)  REFERENCES organisations ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users         ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS verification_tokens (
    used         BOOLEAN NOT NULL,
    created_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    expires_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    id           UUID PRIMARY KEY,
    email        VARCHAR(255) NOT NULL,
    token        VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS refresh_tokens (
    used        BOOLEAN NOT NULL,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    expires_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    family      UUID NOT NULL,
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL,
    token       VARCHAR(255) NOT NULL UNIQUE,
    FOREIGN KEY (user_id) REFERENCES users ON DELETE CASCADE
);

-- hot-path indexes
CREATE INDEX IF NOT EXISTS idx_users_email              ON users(email);
CREATE INDEX IF NOT EXISTS idx_verification_token       ON verification_tokens(token);
CREATE INDEX IF NOT EXISTS idx_verification_email       ON verification_tokens(email);
CREATE INDEX IF NOT EXISTS idx_refresh_token            ON refresh_tokens(token);
CREATE INDEX IF NOT EXISTS idx_refresh_family           ON refresh_tokens(family);