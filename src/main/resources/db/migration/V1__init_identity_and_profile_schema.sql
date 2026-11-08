-- =============================================================================
-- V1: Identity, Consent, Profiles, and Versioned Measurement Schema
-- =============================================================================

CREATE TABLE users (
    id VARCHAR(64) PRIMARY KEY,
    email VARCHAR(255) UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    auth_provider VARCHAR(50) NOT NULL DEFAULT 'LOCAL',
    role VARCHAR(50) NOT NULL DEFAULT 'USER',
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE user_sessions (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    refresh_token_hash VARCHAR(255) NOT NULL,
    expires_at TIMESTAMP WITH TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE consent_records (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
    analytics_consent BOOLEAN NOT NULL DEFAULT TRUE,
    personalized_recommendations_consent BOOLEAN NOT NULL DEFAULT TRUE,
    image_measurement_consent BOOLEAN NOT NULL DEFAULT FALSE,
    marketing_consent BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE TABLE profiles (
    id VARCHAR(64) PRIMARY KEY,
    user_id VARCHAR(64) NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(120) NOT NULL,
    relationship VARCHAR(50) NOT NULL DEFAULT 'SELF',
    gender_category VARCHAR(50) NOT NULL DEFAULT 'MENS',
    preferred_unit VARCHAR(16) NOT NULL DEFAULT 'CM',
    region VARCHAR(16) NOT NULL DEFAULT 'IN',
    default_fit_preference VARCHAR(32) NOT NULL DEFAULT 'REGULAR',
    is_primary BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX idx_profiles_user_id ON profiles(user_id);

CREATE TABLE measurement_sets (
    id VARCHAR(64) PRIMARY KEY,
    profile_id VARCHAR(64) NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    version INT NOT NULL,
    original_unit VARCHAR(16) NOT NULL,
    source VARCHAR(32) NOT NULL,
    status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    measured_at TIMESTAMP WITH TIME ZONE NOT NULL,
    notes VARCHAR(500),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_measurement_sets_profile_version UNIQUE (profile_id, version)
);

CREATE INDEX idx_measurement_sets_profile_status ON measurement_sets(profile_id, status);

CREATE TABLE measurement_values (
    id VARCHAR(64) PRIMARY KEY,
    measurement_set_id VARCHAR(64) NOT NULL REFERENCES measurement_sets(id) ON DELETE CASCADE,
    dimension_code VARCHAR(64) NOT NULL,
    value_cm NUMERIC(8,2) NOT NULL,
    original_value NUMERIC(8,2) NOT NULL,
    original_unit VARCHAR(16) NOT NULL,
    source VARCHAR(32) NOT NULL,
    confidence NUMERIC(4,2) NOT NULL DEFAULT 1.00,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    CONSTRAINT uq_measurement_values_set_dimension UNIQUE (measurement_set_id, dimension_code)
);

CREATE INDEX idx_measurement_values_set_id ON measurement_values(measurement_set_id);

CREATE TABLE audit_events (
    id VARCHAR(64) PRIMARY KEY,
    actor_id VARCHAR(64) NOT NULL,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(64) NOT NULL,
    entity_id VARCHAR(64) NOT NULL,
    details_json TEXT,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL
);
