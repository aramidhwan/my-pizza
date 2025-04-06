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
public class LoginDto implements UserDetails {
    private String redirectUrl ;
    private String email;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String password;
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private String verifyPassword;
    private Collection<? extends GrantedAuthority> authorities;

//    public LoginDto(String email, String password, Collection<? extends GrantedAuthority> authorities) {
//        this.email = email;
//        this.password = password ;
//        this.authorities = authorities ;
//    }
//
//    public LoginDto(String email, String password, String roles) {
//        this.email = email;
//        this.password = password ;
//        this.authorities = createAuthorities(roles) ;
//    }

//    public void setAuthorityDtos(Set<AuthorityDto> authorities) {
//        this.authorities = authorities ;
//    }
//
//    private Collection<AuthorityDto> createAuthorities(String roles){
//        Collection<AuthorityDto> authorities = new ArrayList<>();
//
//        for(String role : roles.split(",")){
//            if (!StringUtils.hasText(role)) continue;
//            authorities.add(new AuthorityDto(role));
//        }
//        return authorities;
//    }

//    private Collection<AuthorityDto> convertToAuthrityDto(Collection<Authority> authorities) {
//        Collection<AuthorityDto> authorityDtos = new ArrayList<>();
//        for (Authority authority:authorities) {
//            authorityDtos.add(new AuthorityDto(authority.getAuthorityName())) ;
//        }
//        return authorityDtos ;
//    }

//    public void setAuthorities(Collection<Authority> authorities) {
//        Set<AuthorityDto> tmpAuthorities = new HashSet<>() ;
//        ObjectMapper mapper = new ObjectMapper() ;
//        for (Authority authority : authorities) {
//            tmpAuthorities.add(new AuthorityDto(authority.getAuthorityName())) ;
//        }
//        setAuthorityDtos(tmpAuthorities);
//    }

//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return authorities;
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
