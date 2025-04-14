-- --------------------------------------------------------
-- 호스트:                          127.0.0.1
-- 서버 버전:                        8.0.40 - MySQL Community Server - GPL
-- 서버 OS:                        Win64
-- HeidiSQL 버전:                  12.8.0.6908
-- --------------------------------------------------------

/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET NAMES utf8 */;
/*!50503 SET NAMES utf8mb4 */;
/*!40103 SET @OLD_TIME_ZONE=@@TIME_ZONE */;
/*!40103 SET TIME_ZONE='+00:00' */;
/*!40014 SET @OLD_FOREIGN_KEY_CHECKS=@@FOREIGN_KEY_CHECKS, FOREIGN_KEY_CHECKS=0 */;
/*!40101 SET @OLD_SQL_MODE=@@SQL_MODE, SQL_MODE='NO_AUTO_VALUE_ON_ZERO' */;
/*!40111 SET @OLD_SQL_NOTES=@@SQL_NOTES, SQL_NOTES=0 */;


-- my-pizza 데이터베이스 구조 내보내기
CREATE DATABASE IF NOT EXISTS `my-pizza` /*!40100 DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci */ /*!80016 DEFAULT ENCRYPTION='N' */;
USE `my-pizza`;
CREATE USER 'admin'@'%' IDENTIFIED BY 'admin';
GRANT ALL ON `my-pizza`.* TO admin@'%';

-- 테이블 my-pizza.t_authority 구조 내보내기
CREATE TABLE IF NOT EXISTS `t_authority` (
  `authority_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`authority_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 my-pizza.t_authority:~5 rows (대략적) 내보내기
INSERT INTO `t_authority` (`authority_name`) VALUES
	('ROLE_ADMIN'),
	('ROLE_CUSTOMER'),
	('ROLE_DELIVERY_ADMIN'),
	('ROLE_STORE_ADMIN'),
	('ROLE_VIP_CUSTOMER');

-- 테이블 my-pizza.t_customer 구조 내보내기
CREATE TABLE IF NOT EXISTS `t_customer` (
  `customer_no` int NOT NULL AUTO_INCREMENT,
  `customer_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `customer_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `email` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `password` varchar(300) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL,
  `activated` bit(1) NOT NULL,
  `extra_roles` varchar(1000) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  PRIMARY KEY (`customer_no`)
) ENGINE=InnoDB AUTO_INCREMENT=21 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 my-pizza.t_customer:~14 rows (대략적) 내보내기
INSERT INTO `t_customer` (`customer_no`, `customer_id`, `customer_name`, `email`, `password`, `activated`, `extra_roles`) VALUES
	(3, 'aramidhwan', '아라미드', 'aramidhwan@naver.com', '$2a$10$5fJg/YSIIyg4QHm08s.PwuxqMdSDxfSrEgO299PEnkzz6rzYwGjnu', b'1', NULL),
	(6, 'aramidchul', 'aramidchul', 'aramidchul@naver.com', '$2a$10$CToPTFRdJaMGp6Bm/U5V5e4m0TBmTgEcwzXx9iLOHDx.GryPwl1UK', b'1', NULL),
	(7, 'aramidkim', 'aramidkim', 'aramidkim@naver.com', '$2a$10$EHfgqaWxVkxQahSOmmaIfe1EK/sz8HjRi982dh6S4y4ppzXT7LNjW', b'1', 'STORE_ID=1'),
	(10, 'aramidnam', 'aramidnam', 'aramidnam@naver.com', '$2a$10$XmzjLZTw6jIUy0PG0YaRpOqNk/0EZJpZU2.47hadGDKlWoFMOQUya', b'1', NULL),
	(11, 'aramid01', 'aramid01', 'aramid01@naver.com', '$2a$10$gTxdXMwQy3TnJ85UQpvBjO0PMMcqx1JAyYmS.zzvItFsswcO44GLi', b'1', NULL),
	(12, 'aramid02', 'aramid02', 'aramid02@naver.com', '$2a$10$S0oXCWLvx2mYpy.Rv82tGunngYK5p7MgqzRxRgESKwW79wI80mq56', b'1', NULL),
	(13, 'aramid03', 'aramid03', 'aramid03@naver.com', '$2a$10$wM4tlA.9cBIsagih291gdu3Sfp.Ypi63J/XT5Y.6.wQNxgxZyzHJy', b'1', NULL),
	(14, 'aramid04', 'aramid04', 'aramid04@naver.com', '$2a$10$AHDy/ac4cX9NULlovwdv2OtmdS8kDqmd0N9afDSDhl5Rn3SsRlQ9m', b'1', NULL),
	(15, 'aramid05', 'aramid05', 'aramid05@naver.com', '$2a$10$UDfbwu8WJiTGxBrQ5Orcv.WKdOU7SqUqhsVOdAv2f1SZTFVQ4k1he', b'1', NULL),
	(16, 'aramid06', 'aramid06', 'aramid06@naver.com', '$2a$10$BOR6RsxXEulnz3/pzxqqiOKKyE3xDQPwlB83NU1yzZYlvgPb0dOf2', b'1', NULL),
	(17, 'aramid07', 'aramid07', 'aramid07@naver.com', '$2a$10$MyHdnt5qUZMUeS2toAemx.8hzvBjEfuZz.l9HUlKkSIsx7VIaoNSG', b'1', NULL),
	(18, 'aramid08', 'aramid08', 'aramid08@naver.com', '$2a$10$xyoygoKlbLb5yK/nogDz/eXrkSt7He8gnFIgiuzDey30bGLsJrYX6', b'1', NULL),
	(19, 'aramid09', 'aramid09', 'aramid09@naver.com', '$2a$10$HmhRXuxKLuhjbhfOEQl/C.SQ.q7FnVPuFFA2tPuQjTSnogC/M8Tmq', b'1', NULL),
	(20, 'aramid10', 'aramid10', 'aramid10@naver.com', '$2a$10$9AivCCOuthPABeW6qs/Pdu6gGnHoxgwTHczbhdyhtiPbCUNs..UHi', b'1', NULL);

-- 테이블 my-pizza.t_customer_authority 구조 내보내기
CREATE TABLE IF NOT EXISTS `t_customer_authority` (
  `customer_no` int NOT NULL,
  `authority_name` varchar(50) COLLATE utf8mb4_unicode_ci NOT NULL,
  PRIMARY KEY (`customer_no`,`authority_name`),
  KEY `FKtmaj6f4y7ig4q3rmd518au8ke` (`authority_name`),
  CONSTRAINT `FK7hoxi1jrqolgibyl3panktk2c` FOREIGN KEY (`customer_no`) REFERENCES `t_customer` (`customer_no`),
  CONSTRAINT `FKtmaj6f4y7ig4q3rmd518au8ke` FOREIGN KEY (`authority_name`) REFERENCES `t_authority` (`authority_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 my-pizza.t_customer_authority:~18 rows (대략적) 내보내기
INSERT INTO `t_customer_authority` (`customer_no`, `authority_name`) VALUES
	(3, 'ROLE_ADMIN'),
	(3, 'ROLE_CUSTOMER'),
	(6, 'ROLE_CUSTOMER'),
	(7, 'ROLE_CUSTOMER'),
	(10, 'ROLE_CUSTOMER'),
	(11, 'ROLE_CUSTOMER'),
	(12, 'ROLE_CUSTOMER'),
	(13, 'ROLE_CUSTOMER'),
	(14, 'ROLE_CUSTOMER'),
	(15, 'ROLE_CUSTOMER'),
	(16, 'ROLE_CUSTOMER'),
	(17, 'ROLE_CUSTOMER'),
	(18, 'ROLE_CUSTOMER'),
	(19, 'ROLE_CUSTOMER'),
	(20, 'ROLE_CUSTOMER'),
	(7, 'ROLE_DELIVERY_ADMIN'),
	(7, 'ROLE_STORE_ADMIN'),
	(3, 'ROLE_VIP_CUSTOMER');

-- 테이블 my-pizza.t_delivery 구조 내보내기
CREATE TABLE IF NOT EXISTS `t_delivery` (
  `delivery_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint DEFAULT NULL,
  `store_id` bigint DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `owner_no` int NOT NULL,
  `create_dt` timestamp NULL DEFAULT NULL,
  `update_dt` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`delivery_id`)
) ENGINE=InnoDB AUTO_INCREMENT=14797 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 my-pizza.t_delivery:~1,130 rows (대략적) 내보내기

-- 테이블 my-pizza.t_item 구조 내보내기
CREATE TABLE IF NOT EXISTS `t_item` (
  `item_id` bigint NOT NULL AUTO_INCREMENT,
  `item_group` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `item_nm` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `price_per_one` int DEFAULT NULL,
  `regist_dt` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`item_id`)
) ENGINE=InnoDB AUTO_INCREMENT=13 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 my-pizza.t_item:~12 rows (대략적) 내보내기
INSERT INTO `t_item` (`item_id`, `item_group`, `item_nm`, `price_per_one`, `regist_dt`) VALUES
	(1, '피자', '페파로니피자', 6000, '2024-04-02 12:05:10.887000'),
	(2, '피자', '치즈피자', 4000, '2024-04-02 12:05:10.998000'),
	(3, '피자', '고구마피자', 6000, '2024-04-02 12:05:11.001000'),
	(4, '스파게티', '볼레로스파게티', 4000, '2024-04-02 12:05:11.003000'),
	(5, '스파게티', '까르보나라스파게티', 4000, '2024-04-02 12:05:11.006000'),
	(6, '스파게티', '알리오스파게티', 4000, '2024-04-02 12:05:11.008000'),
	(7, '분식', '로제떡볶이', 3000, '2024-04-02 12:05:11.012000'),
	(8, '분식', '소떡소떡', 2500, '2024-04-02 12:05:11.014000'),
	(9, '분식', '쫄깃닭꼬치', 2500, '2024-04-02 12:05:11.018000'),
	(10, '음료', '펩시콜라', 1000, '2024-04-02 12:05:11.023000'),
	(11, '음료', '코카콜라', 1000, '2024-04-02 12:05:11.028000'),
	(12, '음료', '환타', 1000, '2024-04-02 12:05:11.032000');

-- 테이블 my-pizza.t_mypage 구조 내보내기
CREATE TABLE IF NOT EXISTS `t_mypage` (
  `my_page_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint DEFAULT NULL,
  `customer_no` int NOT NULL,
  `store_id` bigint DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `region_nm` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_price` int DEFAULT NULL,
  `create_dt` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`my_page_id`)
) ENGINE=InnoDB AUTO_INCREMENT=23486 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 my-pizza.t_mypage:~1,132 rows (대략적) 내보내기

-- 테이블 my-pizza.t_mypage_order_detail 구조 내보내기
CREATE TABLE IF NOT EXISTS `t_mypage_order_detail` (
  `order_detail_id` bigint NOT NULL,
  `order_id` bigint NOT NULL,
  `item_id` bigint NOT NULL,
  `qty` int DEFAULT NULL,
  `price_per_one` int DEFAULT NULL,
  PRIMARY KEY (`order_detail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 my-pizza.t_mypage_order_detail:~2,265 rows (대략적) 내보내기

-- 테이블 my-pizza.t_order 구조 내보내기
CREATE TABLE IF NOT EXISTS `t_order` (
  `order_id` bigint NOT NULL AUTO_INCREMENT,
  `store_id` bigint DEFAULT NULL,
  `region_nm` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `total_price` int DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `status_info` varchar(500) COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_dt` datetime(6) DEFAULT NULL,
  `update_dt` datetime(6) DEFAULT NULL,
  `customer_no` int DEFAULT NULL,
  PRIMARY KEY (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=25072 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 my-pizza.t_order:~1,132 rows (대략적) 내보내기

-- 테이블 my-pizza.t_orderdetail 구조 내보내기
CREATE TABLE IF NOT EXISTS `t_orderdetail` (
  `order_detail_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint DEFAULT NULL,
  `item_id` bigint DEFAULT NULL,
  `price_per_one` int DEFAULT NULL,
  `qty` int DEFAULT NULL,
  PRIMARY KEY (`order_detail_id`),
  KEY `fk_orderdetail_item_id` (`item_id`),
  KEY `fk_orderdetail_order_id` (`order_id`),
  CONSTRAINT `fk_orderdetail_item_id` FOREIGN KEY (`item_id`) REFERENCES `t_item` (`item_id`),
  CONSTRAINT `fk_orderdetail_order_id` FOREIGN KEY (`order_id`) REFERENCES `t_order` (`order_id`)
) ENGINE=InnoDB AUTO_INCREMENT=49874 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 my-pizza.t_orderdetail:~2,265 rows (대략적) 내보내기

-- 테이블 my-pizza.t_store 구조 내보내기
CREATE TABLE IF NOT EXISTS `t_store` (
  `store_id` bigint NOT NULL AUTO_INCREMENT,
  `store_nm` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `region_nm` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `addr` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `openyn` bit(1) NOT NULL,
  `create_dt` timestamp NULL DEFAULT NULL,
  `update_dt` timestamp NULL DEFAULT NULL,
  `owner_no` int DEFAULT NULL,
  PRIMARY KEY (`store_id`)
) ENGINE=InnoDB AUTO_INCREMENT=9009 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 my-pizza.t_store:~11 rows (대략적) 내보내기
INSERT INTO `t_store` (`store_id`, `store_nm`, `region_nm`, `addr`, `openyn`, `create_dt`, `update_dt`, `owner_no`) VALUES
	(1, '강남1호점', '강남구', '강남구 테헤란로 178-21', b'0', NULL, '2025-03-19 22:39:00', 0),
	(2, '강남2호점', '강남구', '강남구 삼성로 382', b'1', NULL, NULL, 7),
	(3, '강남3호점', '강남구', '강남구 개포동 42-3', b'1', NULL, NULL, 7),
	(4, '서초1호점', '서초구', '강남구 서초로 178-1', b'0', NULL, NULL, 0),
	(5, '종로1호점', '종로구', '강남구 종로1로 178-1', b'1', NULL, NULL, 0),
	(6, '종로2호점', '종로구', '강남구 종로2로 22-2', b'0', NULL, NULL, 0),
	(7, '이태원1호점', '중구', '중구 이태원로 178-1', b'0', NULL, NULL, 0),
	(13, '이태원2호점', '중구', '중구 이태원로 178-2', b'0', NULL, NULL, 0),
	(39, '인천1호점', '인천시', '인천시 계양구 8-1', b'1', NULL, NULL, 0),
	(9007, '강남4호점', '강남구', '강남구 테헤란로 178-12', b'0', '2025-03-03 17:46:11', '2025-03-19 22:39:06', 0),
	(9008, '종로4호점', '종로구', '서울시 종로1가 13-91', b'1', '2025-03-09 16:01:40', '2025-03-09 16:01:40', 0);

-- 테이블 my-pizza.t_store_order 구조 내보내기
CREATE TABLE IF NOT EXISTS `t_store_order` (
  `store_order_id` bigint NOT NULL AUTO_INCREMENT,
  `order_id` bigint DEFAULT NULL,
  `store_id` bigint DEFAULT NULL,
  `status` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci DEFAULT NULL,
  `create_dt` timestamp NULL DEFAULT NULL,
  `update_dt` timestamp NULL DEFAULT NULL,
  PRIMARY KEY (`store_order_id`),
  KEY `fk_store_order_store_id` (`store_id`),
  CONSTRAINT `fk_store_order_store_id` FOREIGN KEY (`store_id`) REFERENCES `t_store` (`store_id`)
) ENGINE=InnoDB AUTO_INCREMENT=24572 DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 my-pizza.t_store_order:~1,132 rows (대략적) 내보내기

-- 테이블 my-pizza.t_store_order_detail 구조 내보내기
CREATE TABLE IF NOT EXISTS `t_store_order_detail` (
  `order_detail_id` bigint NOT NULL,
  `order_id` bigint DEFAULT NULL,
  `item_id` bigint DEFAULT NULL,
  `qty` int DEFAULT NULL,
  `price_per_one` int DEFAULT NULL,
  PRIMARY KEY (`order_detail_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 테이블 데이터 my-pizza.t_store_order_detail:~2,265 rows (대략적) 내보내기

/*!40103 SET TIME_ZONE=IFNULL(@OLD_TIME_ZONE, 'system') */;
/*!40101 SET SQL_MODE=IFNULL(@OLD_SQL_MODE, '') */;
/*!40014 SET FOREIGN_KEY_CHECKS=IFNULL(@OLD_FOREIGN_KEY_CHECKS, 1) */;
/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40111 SET SQL_NOTES=IFNULL(@OLD_SQL_NOTES, 1) */;
