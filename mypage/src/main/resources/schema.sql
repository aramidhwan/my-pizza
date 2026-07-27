DROP TABLE IF EXISTS t_mypage_orderdetail;
DROP TABLE IF EXISTS t_mypage;

CREATE TABLE t_mypage (
  `my_page_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint NOT NULL,
  `customer_no` int NOT NULL,
  `store_id` bigint DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status_info` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `region_nm` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `total_price` int NOT NULL,
  `create_dt` timestamp NULL DEFAULT NULL,
  `update_dt` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`my_page_id`),
  UNIQUE KEY `UK_mypage_orderId` (`order_id`)
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
