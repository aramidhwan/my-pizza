package com.study.mypizza.order.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
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

    @Override
    @JsonIgnore
    public String getAuthority() {
        return this.role;
    }
}