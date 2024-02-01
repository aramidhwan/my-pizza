package com.study.mypizza.store.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name="store_table")
@Data
public class Store {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long storeId;
    private String regionNm;
    private Boolean openYN;
}
