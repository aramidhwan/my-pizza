DROP TABLE IF EXISTS t_store_orderdetail;
DROP TABLE IF EXISTS t_store_order;
DROP TABLE IF EXISTS t_store;

CREATE TABLE t_store (
  `store_id` bigint NOT NULL AUTO_INCREMENT,
  `store_nm` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `owner_no` int NOT NULL,
  `region_nm` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `open_yn` bit(1) NOT NULL,
  `addr` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_dt` timestamp NOT NULL,
  `update_dt` timestamp NOT NULL,
  PRIMARY KEY (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE t_store_order (
  `store_order_id` bigint NOT NULL AUTO_INCREMENT,
  `store_id` bigint DEFAULT NULL,
  `order_id` bigint DEFAULT NULL,
  `status` varchar(255) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_dt` timestamp NOT NULL,
  `update_dt` timestamp NOT NULL,
  PRIMARY KEY (`store_order_id`),
  KEY `fk_storeOrder_store` (`store_id`),
  CONSTRAINT `fk_storeOrder_store` FOREIGN KEY (`store_id`) REFERENCES `t_store` (`store_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE t_store_orderdetail (
  `order_detail_id` bigint NOT NULL,
  `store_order_id` bigint DEFAULT NULL,
  `item_id` bigint DEFAULT NULL,
  `qty` int DEFAULT NULL,
  `price_per_one` int DEFAULT NULL,
  PRIMARY KEY (`order_detail_id`),
  KEY `fk_storeOrderDetail_storeOrder` (`store_order_id`),
  CONSTRAINT `fk_storeOrderDetail_storeOrder` FOREIGN KEY (`store_order_id`) REFERENCES `t_store_order` (`store_order_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;