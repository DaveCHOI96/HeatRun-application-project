ALTER TABLE users ADD COLUMN fcm_token VARCHAR(500);
COMMENT ON COLUMN users.fcm_token IS 'Firebase FCM 디바이스 토큰 - 앱 최초 실행 시 발급';