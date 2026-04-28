-- =====================================================
-- HeatRun 초기 스키마
-- V1__init_schema.sql
-- =====================================================

-- PostGIS 확장 활성화
CREATE EXTENSION IF NOT EXISTS postgis;

-- =====================================================
-- users
-- =====================================================
CREATE TABLE users
(
    id          UUID          NOT NULL DEFAULT gen_random_uuid(),
    email       VARCHAR(100)  NOT NULL,
    nickname    VARCHAR(30)   NOT NULL

)