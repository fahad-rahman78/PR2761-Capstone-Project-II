-- ===================================================================
--  hardening.sql  -  Optional database-level safety-net.
--
--  This adds the EXCLUDE constraint from the design document. It is an
--  EXTRA layer on top of the application's advisory-lock logic: even if
--  the Java code had a bug, PostgreSQL itself would refuse to insert a
--  second overlapping CONFIRMED booking on the same resource.
--
--  IMPORTANT - capacity note:
--  This constraint enforces "no overlap at all", i.e. it assumes every
--  resource has capacity = 1. If you use resources with capacity > 1
--  (like the "Group Study Pod" in the seed data), DO NOT apply this
--  constraint to them - it would wrongly reject the 2nd valid booking.
--  In that case rely on the application's capacity check instead.
--
--  Apply it (capacity-1 deployments only) with:
--      psql -U postgres -d booking_db -f db/hardening.sql
-- ===================================================================

-- btree_gist lets a GiST index mix an equality column (resource_id) with
-- a range-overlap column (the time range) in one EXCLUDE constraint.
CREATE EXTENSION IF NOT EXISTS btree_gist;

ALTER TABLE bookings
    DROP CONSTRAINT IF EXISTS no_double_booking;

ALTER TABLE bookings
    ADD CONSTRAINT no_double_booking
    EXCLUDE USING GIST (
        resource_id WITH =,
        tsrange(start_time, end_time) WITH &&
    )
    WHERE (status = 'CONFIRMED');

-- The && operator means "overlaps". The constraint therefore reads:
-- "for any two CONFIRMED bookings on the same resource, their time
--  ranges must NOT overlap" - and PostgreSQL enforces it on every INSERT.
