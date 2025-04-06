package com.study.mypizza.common.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.mypizza.common.entity.Authority;
import com.study.mypizza.common.entity.Customer;
import jakarta.persistence.Column;
import jakarta.persistence.Transient;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Data
@Slf4j
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustomerDto implements UserDetails {
    private int customerNo;

    private String customerId;

    private String customerName;

    private String email;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String verifyPassword;

    private boolean activated;

    private String extraRoles;

    private Collection<? extends AuthorityDto> authorities;

    public CustomerDto(String email, String password, Collection<? extends AuthorityDto> authorities) {
        this.email = email;
        this.password = password ;
        this.authorities = authorities ;
    }

    public CustomerDto(String email, String password, String roles) {
        this.email = email;
        this.password = password ;
        this.authorities = createAuthorities(roles) ;
    }

    private Collection<AuthorityDto> createAuthorities(String roles){
        Collection<AuthorityDto> authorities = new ArrayList<>();

        for(String role : roles.split(",")){
            if (!StringUtils.hasText(role)) continue;
            authorities.add(new AuthorityDto(role));
        }
        return authorities;
    }

    @Override
    public Collection<? extends AuthorityDto> getAuthorities() {
        return authorities;
    }

    public static CustomerDto of(Customer customer) {
        return CustomerDto.builder()
                .customerNo(customer.getCustomerNo())
                .customerId(customer.getCustomerId())
                .customerName(customer.getCustomerName())
                .email(customer.getEmail())
//                .password(customer.getPassword())
                .activated(customer.isActivated())
                .extraRoles(customer.getExtraRoles())
                .authorities(customer.getAuthorities().stream().map(AuthorityDto::of).toList())
                .build();
    }

    @Override
    public String getUsername() {
        return this.email;
    }

    @Override
    public String getPassword() {
        return this.password;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true ;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true ;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true ;
    }

    @Override
    public boolean isEnabled() {
        return true ;
    }

}
