-- ===================================================================
--  schema.sql  -  Full PostgreSQL schema for the Booking System.
--
--  You do NOT have to run this by hand: when the application starts,
--  Hibernate creates these tables automatically from the entity
--  classes (spring.jpa.hibernate.ddl-auto=update).
--
--  This file is provided so the schema can be read, reviewed, or set
--  up manually if you prefer. Run it with:
--      psql -U postgres -d booking_db -f db/schema.sql
-- ===================================================================

-- Table: users
CREATE TABLE IF NOT EXISTS users (
    user_id     BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    email       VARCHAR(150) NOT NULL UNIQUE,
    created_at  TIMESTAMP DEFAULT NOW()
);

-- Table: resources
CREATE TABLE IF NOT EXISTS resources (
    resource_id BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    type        VARCHAR(50)  NOT NULL,
    capacity    INTEGER      NOT NULL DEFAULT 1
);

-- Table: bookings  (the core table)
CREATE TABLE IF NOT EXISTS bookings (
    booking_id  BIGSERIAL PRIMARY KEY,
    user_id     BIGINT NOT NULL REFERENCES users(user_id),
    resource_id BIGINT NOT NULL REFERENCES resources(resource_id),
    start_time  TIMESTAMP NOT NULL,
    end_time    TIMESTAMP NOT NULL,
    status      VARCHAR(20) NOT NULL DEFAULT 'CONFIRMED',
    created_at  TIMESTAMP DEFAULT NOW(),
    CONSTRAINT chk_time_order CHECK (start_time < end_time)
);

-- An index that makes the "find overlapping bookings" query fast.
CREATE INDEX IF NOT EXISTS idx_bookings_resource_time
    ON bookings (resource_id, start_time, end_time)
    WHERE status = 'CONFIRMED';
