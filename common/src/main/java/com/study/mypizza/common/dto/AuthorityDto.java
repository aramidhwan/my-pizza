package com.study.mypizza.common.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.study.mypizza.common.entity.Authority;
import com.study.mypizza.common.entity.Customer;
import lombok.*;
import org.springframework.beans.BeanUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.Assert;

@Data
@Builder
@NoArgsConstructor
public class AuthorityDto implements GrantedAuthority {
    private String role;

    public AuthorityDto(String role) {
        Assert.hasText(role, "### A granted authority textual representation is required");
        this.role = role ;
    }

    public Authority toAuthority() {
        Authority authority = new Authority() ;
        BeanUtils.copyProperties(this, authority);
        return authority;
    }

    @Override
    @JsonIgnore
    public String getAuthority() {
        return this.role;
    }

    public Authority toEntity() {
        return Authority.builder()
                .authorityName(role)
                .build();
    }

    public static AuthorityDto of(Authority authority) {
        return AuthorityDto.builder()
                .role(authority.getAuthorityName())
                .build();
    }
}