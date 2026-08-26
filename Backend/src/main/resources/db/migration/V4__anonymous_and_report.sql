-- Flyway V4: 그린메일(익명 계정) 모델 전환 + 신고/검열 시스템
-- 1) users: 익명 가입 지원 (email nullable, provider에 ANONYMOUS 추가)
-- 2) report: 신고 엔티티 (피드/댓글/리스트 대상, 중복방지 유니크키)
--    신고는 전부 PENDING 대기열에 적재되고, 헤르메스 에이전트가 주기적으로
--    검토하여 소프트삭제(RESOLVED)/기각(REJECTED)을 판정한다.
--    rule_score/matched_rules는 자동 삭제에 쓰지 않고 에이전트의 검토
--    우선순위·판단 근거로 참고하는 메타데이터다.
-- 3) 소프트삭제: feeds/restaurant_list에 deleted_at 추가 (롤백 가능한 삭제)

-- 1. users.email nullable (익명 계정은 이메일 없음)
ALTER TABLE `users` MODIFY `email` varchar(100) NULL;

-- MySQL 8.0.13+ : ALTER로 enum 값 추가 (재작성 없음)
ALTER TABLE `users` MODIFY `provider` enum('KAKAO','LOCAL','ANONYMOUS') NOT NULL;

-- 2. 신고 테이블
CREATE TABLE `report` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  -- 신고 대상
  `target_type` enum('FEED','COMMENT','RESTAURANT_LIST') NOT NULL,
  `feed_id` bigint DEFAULT NULL,
  `comment_id` bigint DEFAULT NULL,
  `restaurant_list_id` bigint DEFAULT NULL,
  -- 신고자 / 피신고자
  `reporter_id` bigint NOT NULL,
  `reported_user_id` bigint DEFAULT NULL,
  -- 분류 및 내용
  `reason` enum('SPAM','ABUSE','PORNOGRAPHY','FRAUD','COPYRIGHT','OTHER') NOT NULL,
  `detail` varchar(500) DEFAULT NULL,
  -- 검증 상태: PENDING(대기열) → 에이전트가 주기적으로 검토
  --   RESOLVED(삭제 확정) / REJECTED(기각) / RESTORED(롤백 복원)
  `status` enum('PENDING','RESOLVED','REJECTED','RESTORED') NOT NULL DEFAULT 'PENDING',
  -- 룰 기반 증불 점수와 판정 근거: 자동 삭제에는 사용하지 않고,
  -- 에이전트가 검토 우선순위와 판단 근거로 참고하는 메타데이터.
  `rule_score` int NOT NULL DEFAULT 0,
  `matched_rules` varchar(500) DEFAULT NULL,
  -- 처리 결과 메모 (에이전트 판정 사유 등)
  `resolution_note` varchar(500) DEFAULT NULL,
  `resolved_at` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_report_status` (`status`),
  KEY `idx_report_reporter` (`reporter_id`),
  -- 동일인이 동일 대상을 재신고 방지 (NULL 포함 조합도 MySQL 유니크키로 중복 차단 가능)
  UNIQUE KEY `uk_report_dedup` (`reporter_id`,`target_type`,`feed_id`,`comment_id`,`restaurant_list_id`),
  CONSTRAINT `fk_report_feed` FOREIGN KEY (`feed_id`) REFERENCES `feeds` (`id`),
  CONSTRAINT `fk_report_comment` FOREIGN KEY (`comment_id`) REFERENCES `feed_comment` (`id`),
  CONSTRAINT `fk_report_restaurant_list` FOREIGN KEY (`restaurant_list_id`) REFERENCES `restaurant_list` (`id`),
  CONSTRAINT `fk_report_reporter` FOREIGN KEY (`reporter_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- 3. 소프트삭제 컬럼 (NULL = 정상, NOT NULL = 숨김 처리된 시각)
ALTER TABLE `feeds` ADD COLUMN `deleted_at` datetime(6) DEFAULT NULL;
ALTER TABLE `restaurant_list` ADD COLUMN `deleted_at` datetime(6) DEFAULT NULL;
ALTER TABLE `feed_comment` ADD COLUMN `deleted_at` datetime(6) DEFAULT NULL;

-- 목록 조회 쿼리에서 deleted_at IS NULL 조건이 자주 붙으므로 커버 인덱스
ALTER TABLE `feeds` ADD INDEX `idx_feeds_deleted_at` (`deleted_at`);
ALTER TABLE `restaurant_list` ADD INDEX `idx_restaurant_list_deleted_at` (`deleted_at`);
ALTER TABLE `feed_comment` ADD INDEX `idx_feed_comment_deleted_at` (`deleted_at`);
