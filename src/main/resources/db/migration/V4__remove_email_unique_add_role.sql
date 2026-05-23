ALTER TABLE users DROP CONSTRAINT uc_users_email;

ALTER TABLE users ADD COLUMN  role VARCHAR(20) NOT NULL DEFAULT  'USER';

INSERT INTO users (
    id, email, nickname, password,
    provider, role, location_share_scope, status
) VALUES (
             gen_random_uuid(),
             'admin@heatrun.com',
             'HeatRun관리자',
             '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy',
             'LOCAL',
             'ADMIN',
             'PRIVATE',
             'ACTIVE'
);