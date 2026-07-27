DROP TABLE IF EXISTS t_orderdetail;
DROP TABLE IF EXISTS t_order;
DROP TABLE IF EXISTS t_item;

CREATE TABLE t_order (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `customer_no` int NOT NULL,
  `store_id` bigint DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `status_info` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `region_nm` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_price` int NOT NULL,
  `create_dt` timestamp NULL DEFAULT NULL,
  `update_dt` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE t_item (
  `item_id` bigint NOT NULL AUTO_INCREMENT,
  `item_nm` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `item_group` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `price_per_one` int NOT NULL,
  `regist_dt` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`item_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE t_orderdetail (
  `order_detail_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint DEFAULT NULL,
  `item_id` bigint DEFAULT NULL,
  `qty` int NOT NULL,
  `price_per_one` int NOT NULL,
  PRIMARY KEY (`order_detail_id`),
  KEY `fk_orderdetail_item` (`item_id`),
  KEY `fk_orderdetail_order` (`order_id`),
  CONSTRAINT `fk_orderdetail_item` FOREIGN KEY (`item_id`) REFERENCES `t_item` (`item_id`),
  CONSTRAINT `fk_orderdetail_order` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
