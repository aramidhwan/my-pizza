package com.study.mypizza.order.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import java.util.*;

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

    private Collection<? extends GrantedAuthority> authorities;

    public CustomerDto(String email, String password, Collection<? extends GrantedAuthority> authorities) {
        this.email = email;
        this.password = password ;
        this.authorities = authorities ;
    }

//    @Override
//    public Collection<GrantedAuthority> getAuthorities() {
//        if ( this.authorities == null ) return null ;
//
//        ObjectMapper mapper = new ObjectMapper();
//        Collection<GrantedAuthority> collectors = new HashSet<>();
//        Iterator<? extends GrantedAuthority> iterator = authorities.iterator();
//
//        AuthorityDto authorityDto = null ;
//        while (iterator.hasNext()) {
//            authorityDto = mapper.convertValue(iterator.next(), AuthorityDto.class);
//            collectors.add(authorityDto);
//        }
//        return collectors;
//    }

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
