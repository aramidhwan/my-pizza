package com.study.mypizza.customercenter.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;

@Entity
@Table(name="MyPage_table")
@Data
public class MyPage {

    @Id
    @GeneratedValue(strategy=GenerationType.IDENTITY)
    private Long myPageId;
    private Long orderId;
    private String pizzaNm;
    private Integer qty;
    private String status;
    private Date orderDt;
    private Long customerId;

}
