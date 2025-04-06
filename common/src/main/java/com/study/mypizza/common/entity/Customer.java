package com.study.mypizza.common.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.util.Collection;

@Entity
@Table(name="t_customer")
@ToString
@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Customer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="customer_no")
    private int customerNo;

    @Column(length = 100, nullable = false)
    private String customerId;

    @Column(length = 100, nullable = false)
    private String customerName;

    @Column(length = 200, nullable = false)
    private String email;

    @Column(length = 300, nullable = false)
    @NotEmpty(message = "{password.notempty}")
    private String password;

//    @NotEmpty(message = "{verifyPassword.notempty}")
    @Transient
    private String verifyPassword;

    @Column(nullable = false)
    private boolean activated;

    @Column(length = 1000, nullable = true)
    private String extraRoles;

    @ManyToMany
    @JoinTable(
            name = "t_customer_authority",        // Table명
            joinColumns = {@JoinColumn(name = "customer_no", referencedColumnName = "customer_no")},
            inverseJoinColumns = {@JoinColumn(name = "authority_name", referencedColumnName = "authority_name")})
    @ToString.Exclude       // ToString is not recommanded for lazy loading field
    private Collection<Authority> authorities;
}
