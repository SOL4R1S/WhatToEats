-- WhatToEat 검색엔진 고도화: 한글 부분일치 검색용 FULLTEXT 인덱스
-- Flyway V3: ngram 파서(2글자 토큰) 기반 FULLTEXT 인덱스 추가
-- (V2의 B-TREE name/address 인덱스는 접두어 검색용으로 유지,
--  FULLTEXT는 %키워드% 부분일치 검색을 인덱스로 처리)
--
-- 주의: NATURAL LANGUAGE MODE는 한글 3글자 검색어를 토큰으로 못 쪼개 0건 반환.
--       반드시 BOOLEAN MODE로 검색할 것 (repository 쿼리에서 처리).

CREATE FULLTEXT INDEX idx_restaurant_name_ft ON restaurant (name) WITH PARSER ngram;
