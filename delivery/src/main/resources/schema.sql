DROP TABLE IF EXISTS t_delivery;

CREATE TABLE t_delivery (
  `delivery_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint DEFAULT NULL,
  `store_id` bigint DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `owner_no` int NOT NULL,
  `create_dt` timestamp NULL DEFAULT NULL,
  `update_dt` timestamp NULL DEFAULT NULL,
  KEY `idx_delivery_01` (`order_id`), -- order_id 인덱스 추가
  KEY `idx_delivery_02` (`create_dt`), -- order_id 인덱스 추가
  PRIMARY KEY (`delivery_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;