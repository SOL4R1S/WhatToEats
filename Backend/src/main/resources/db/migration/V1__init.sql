-- WhatToEat 초기 스키마 (운영 MySQL 8.0 기준)
-- Flyway V1: 신규 DB는 이 파일로 전체 생성, 운영 DB는 baseline(1)로 스킵됨

CREATE TABLE `users` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `email` varchar(100) NOT NULL,
  `kakao_id` varchar(255) DEFAULT NULL,
  `login_id` varchar(50) DEFAULT NULL,
  `nickname` varchar(100) NOT NULL,
  `password` varchar(255) DEFAULT NULL,
  `profile_image` varchar(500) DEFAULT NULL,
  `provider` enum('KAKAO','LOCAL') NOT NULL,
  `role` enum('ADMIN','USER') NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKk4ycaj27putgcujmehwbsrmmc` (`kakao_id`),
  UNIQUE KEY `UKi3xs7wmfu2i3jt079uuetycit` (`login_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `restaurant` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `address` varchar(300) NOT NULL,
  `category` enum('ASIAN','BAR','BUFFET','CAFE','CHICKEN','CHINESE','ETC','FASTFOOD','FUSION','JAPANESE','KOREAN','SHABU','SNACK','WESTERN') NOT NULL,
  `kakao_place_id` varchar(255) NOT NULL,
  `lat` double NOT NULL,
  `lng` double NOT NULL,
  `name` varchar(200) NOT NULL,
  `phone` varchar(50) DEFAULT NULL,
  `region1` varchar(50) NOT NULL,
  `region2` varchar(50) NOT NULL,
  `region3` varchar(50) DEFAULT NULL,
  `region4` varchar(50) DEFAULT NULL,
  `road_address` varchar(300) DEFAULT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `UKs7ip0f58cyfmjdx9icproi6qf` (`kakao_place_id`),
  KEY `idx_restaurant_category` (`category`),
  KEY `idx_restaurant_region1` (`region1`),
  KEY `idx_restaurant_region2` (`region2`),
  KEY `idx_restaurant_region3` (`region3`),
  KEY `idx_restaurant_region4` (`region4`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feeds` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `content` varchar(1000) NOT NULL,
  `image_url` varchar(255) DEFAULT NULL,
  `like_count` int NOT NULL,
  `mood_tag` enum('DATE','FAMILY','FRIENDS','GROUP','NIGHT','SOLO') DEFAULT NULL,
  `updated_at` datetime(6) NOT NULL,
  `restaurant_id` bigint DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_feed_user_id` (`user_id`),
  KEY `idx_feed_restaurant_id` (`restaurant_id`),
  CONSTRAINT `FKa4nmt7wyx9clm9okj61dgd1tw` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKm2f54hpgiuawcomg7iscjf10e` FOREIGN KEY (`restaurant_id`) REFERENCES `restaurant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feed_comment` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `content` varchar(500) NOT NULL,
  `feed_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_feed_comment_feed_id` (`feed_id`),
  KEY `idx_feed_comment_user_id` (`user_id`),
  CONSTRAINT `FKmsikihu0q2sdkwyvp0gojgfj3` FOREIGN KEY (`feed_id`) REFERENCES `feeds` (`id`),
  CONSTRAINT `FKqpir1qjx3arwurump725gf24n` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `feed_like` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `feed_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_feed_like_feed_user` (`feed_id`,`user_id`),
  KEY `idx_feed_like_feed_id` (`feed_id`),
  KEY `idx_feed_like_user_id` (`user_id`),
  CONSTRAINT `FKht7sg59oaw8w3bcrbgbeip8u7` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKsybvsbssnb8q7nqng5gvht442` FOREIGN KEY (`feed_id`) REFERENCES `feeds` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `follow` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `follower_id` bigint NOT NULL,
  `following_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_follow_follower_following` (`follower_id`,`following_id`),
  KEY `idx_follow_follower_id` (`follower_id`),
  KEY `idx_follow_following_id` (`following_id`),
  CONSTRAINT `FK9oqsjovu9bl95dwt8ibiy2oey` FOREIGN KEY (`following_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKjikg34txcxnhcky26w14fvfcc` FOREIGN KEY (`follower_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `restaurant_list` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `description` varchar(500) NOT NULL,
  `mood_tag` enum('DATE','FAMILY','FRIENDS','GROUP','NIGHT','SOLO') DEFAULT NULL,
  `title` varchar(200) NOT NULL,
  `updated_at` datetime(6) DEFAULT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_restaurant_list_user_id` (`user_id`),
  CONSTRAINT `FK12x0hxnw1y4mol8djklshx49w` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `restaurant_list_item` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `memo` varchar(200) DEFAULT NULL,
  `order_index` int NOT NULL,
  `restaurant_id` bigint NOT NULL,
  `list_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_restaurant_list_item_list_restaurant` (`list_id`,`restaurant_id`),
  KEY `idx_restaurant_list_item_list_id` (`list_id`),
  KEY `idx_restaurant_list_item_restaurant_id` (`restaurant_id`),
  CONSTRAINT `FK1tun7l5cbu511t66s1mcsxp9a` FOREIGN KEY (`list_id`) REFERENCES `restaurant_list` (`id`),
  CONSTRAINT `FKq3r80gmjxkaxa15hm8328kmoc` FOREIGN KEY (`restaurant_id`) REFERENCES `restaurant` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `saved_restaurant_list` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `restaurant_list_id` bigint NOT NULL,
  `user_id` bigint NOT NULL,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_saved_restaurant_list_user_list` (`user_id`,`restaurant_list_id`),
  KEY `FK37ta5j1u1n7yrd0q9tkx5vlb1` (`restaurant_list_id`),
  CONSTRAINT `FK37ta5j1u1n7yrd0q9tkx5vlb1` FOREIGN KEY (`restaurant_list_id`) REFERENCES `restaurant_list` (`id`),
  CONSTRAINT `FKpyrfbu5k57nu2nywm6npousm3` FOREIGN KEY (`user_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

CREATE TABLE `notification` (
  `id` bigint NOT NULL AUTO_INCREMENT,
  `created_at` datetime(6) DEFAULT NULL,
  `is_read` bit(1) NOT NULL,
  `message` varchar(500) NOT NULL,
  `type` enum('FEED_COMMENT','FEED_LIKE','FOLLOW','LIST_SHARE','NEW_FEED') NOT NULL,
  `actor_id` bigint NOT NULL,
  `feed_id` bigint DEFAULT NULL,
  `receiver_id` bigint NOT NULL,
  `restaurant_list_id` bigint DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_notification_receiver_id` (`receiver_id`),
  KEY `idx_notification_receiver_id_id` (`receiver_id`,`id`),
  KEY `idx_notification_dedup` (`receiver_id`,`actor_id`,`type`,`feed_id`),
  KEY `FKqhkbe4e4w4pom5rn4qm6h3imb` (`actor_id`),
  KEY `FKayqxjjgu2gops30mh3lyjhihx` (`feed_id`),
  KEY `FKa18oh2c43yiyk4hytbpo9grpq` (`restaurant_list_id`),
  CONSTRAINT `FKa18oh2c43yiyk4hytbpo9grpq` FOREIGN KEY (`restaurant_list_id`) REFERENCES `restaurant_list` (`id`),
  CONSTRAINT `FKayqxjjgu2gops30mh3lyjhihx` FOREIGN KEY (`feed_id`) REFERENCES `feeds` (`id`),
  CONSTRAINT `FKdammjl0v5xfaegi926ugx6254` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`id`),
  CONSTRAINT `FKqhkbe4e4w4pom5rn4qm6h3imb` FOREIGN KEY (`actor_id`) REFERENCES `users` (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
