-- V17: every account must have a role.
-- Accounts created before roles were assigned at signup have role = NULL, which gives them
-- Spring authority ROLE_USER and a 403 on /api/buyer/**. Make them BUYER (the new default).
-- ADMIN, FARMER and TRANSPORT accounts already have a role, so they are not touched.
UPDATE users SET role = 'BUYER' WHERE role IS NULL;
