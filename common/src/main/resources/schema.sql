DROP TABLE IF EXISTS t_customer_authority;
DROP TABLE IF EXISTS t_customer;
DROP TABLE IF EXISTS t_authority;

CREATE TABLE t_authority (
  `authority_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`authority_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE t_customer (
  `customer_no` int NOT NULL AUTO_INCREMENT,
  `customer_id` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `customer_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(200) COLLATE utf8mb4_unicode_ci NOT NULL,
  `password` varchar(300) COLLATE utf8mb4_unicode_ci NOT NULL,
  `activated` bit(1) NOT NULL,
  `extra_roles` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`customer_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE t_customer_authority (
  `customer_no` int NOT NULL,
  `authority_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  KEY `fk_customerauthority_authority` (`authority_name`),
  KEY `fk_customerauthority_customer` (`customer_no`),
  CONSTRAINT `fk_customerauthority_customer` FOREIGN KEY (`customer_no`) REFERENCES `t_customer` (`customer_no`),
  CONSTRAINT `fk_customerauthority_authority` FOREIGN KEY (`authority_name`) REFERENCES `t_authority` (`authority_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;