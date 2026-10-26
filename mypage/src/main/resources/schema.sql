DROP TABLE IF EXISTS t_mypage_orderdetail;
DROP TABLE IF EXISTS t_mypage;
DROP TABLE IF EXISTS t_pending_event;

CREATE TABLE t_mypage (
  `my_page_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `customer_no` int NOT NULL,
  `store_id` bigint DEFAULT NULL,
  `delivery_id` bigint DEFAULT NULL,
  `status` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status_info` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `region_nm` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_price` int NOT NULL,
  `create_dt` timestamp NULL DEFAULT NULL,
  `update_dt` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`my_page_id`),
  UNIQUE KEY `UK_mypage_orderId` (`order_id`),
  KEY `idx_mypage_01` (customer_no, create_dt)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE t_mypage_orderdetail (
  `order_detail_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `qty` int NOT NULL,
  `price_per_one` int NOT NULL,
  PRIMARY KEY (`order_detail_id`),
  KEY `fk_mypageOrderDetail_mypage` (`order_id`),
  CONSTRAINT `fk_mypageOrderDetail_mypage` FOREIGN KEY (`order_id`) REFERENCES `t_mypage` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE t_pending_event (
  `event_id` bigint NOT NULL AUTO_INCREMENT,
  `msa_gb` varchar(30) COLLATE utf8mb4_unicode_ci NOT NULL,
  `order_id` bigint NOT NULL,
  `event_type` VARCHAR(30) NOT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `payload` text NOT NULL,
  `is_completed` bit(1) NOT NULL,
  `create_dt` timestamp NULL DEFAULT NULL,
  `update_dt` timestamp NULL DEFAULT NULL,
  KEY `idx_pendingEvent_01` (`order_id`, `msa_gb`, `is_completed`), -- order_id + msa_gb 인덱스 추가
  PRIMARY KEY (`event_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
