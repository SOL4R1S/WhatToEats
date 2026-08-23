-- WhatToEat 성장 대비 인덱스 추가
-- Flyway V2: 식당 이름/주소 LIKE 검색용 인덱스
-- (feed_like/feed_comment/follow 인덱스는 V1에 이미 존재 → 중복 생성 금지)
-- (컬럼 변경 없음, 인덱스만 추가 → 기존 데이터 무손실)

-- 식당 이름/주소 LIKE 검색 (DB 저장 식당 검색 기능)
CREATE INDEX idx_restaurant_name ON restaurant (name);
CREATE INDEX idx_restaurant_address ON restaurant (address);
