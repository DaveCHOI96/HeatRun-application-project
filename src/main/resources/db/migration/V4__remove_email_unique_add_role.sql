ALTER TABLE users DROP CONSTRAINT uc_users_email;

ALTER TABLE users ADD COLUMN  role VARCHAR(20) NOT NULL DEFAULT  'USER';

-- provider + provider_id 복합 unique 추가 (동시 가입 방지)
ALTER TABLE users
    ADD CONSTRAINT uk_users_provider_provider_id
        UNIQUE (provider, provider_id);

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