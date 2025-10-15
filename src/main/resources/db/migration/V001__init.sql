-- V001__init.sql - Initial schema for Enum Organisation Platform

-- Create users table
CREATE TABLE users
(
    id         UUID                  DEFAULT RANDOM_UUID() PRIMARY KEY,
    email      VARCHAR(320) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    verified   BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create organisations table
CREATE TABLE organisations
(
    id          UUID                  DEFAULT RANDOM_UUID() PRIMARY KEY,
    name        VARCHAR(120) NOT NULL,
    logo_url    VARCHAR(255),
    website     VARCHAR(255),
    industry    VARCHAR(100),
    description TEXT,
    plan        VARCHAR(50)  NOT NULL DEFAULT 'FREE',
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create memberships table (with composite primary key)
CREATE TABLE memberships
(
    user_id    UUID        NOT NULL,
    org_id     UUID        NOT NULL,
    role       VARCHAR(50) NOT NULL,
    created_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, org_id),
    CONSTRAINT fk_membership_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_membership_org
        FOREIGN KEY (org_id)
            REFERENCES organisations (id)
            ON DELETE CASCADE
);

-- Create refresh_tokens table
CREATE TABLE refresh_tokens
(
    id         UUID                  DEFAULT RANDOM_UUID() PRIMARY KEY,
    token      VARCHAR(255) NOT NULL UNIQUE,
    user_id    UUID         NOT NULL,
    family     UUID         NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_token_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

-- Create verification_tokens table
CREATE TABLE verification_tokens
(
    id         UUID                  DEFAULT RANDOM_UUID() PRIMARY KEY,
    token      VARCHAR(255) NOT NULL UNIQUE,
    email      VARCHAR(320) NOT NULL,
    expires_at TIMESTAMP    NOT NULL,
    used       BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for performance
CREATE INDEX idx_users_email ON users (email);
CREATE INDEX idx_refresh_tokens_token ON refresh_tokens (token);
CREATE INDEX idx_refresh_tokens_family ON refresh_tokens (family);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens (user_id);
CREATE INDEX idx_verification_tokens_token ON verification_tokens (token);
CREATE INDEX idx_verification_tokens_email ON verification_tokens (email);
CREATE INDEX idx_verification_tokens_expires_at ON verification_tokens (expires_at);