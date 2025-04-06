package com.study.mypizza.order.role;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {
    ADMIN("ROLE_ADMIN,ROLE_CUSTOMER"),
    VIP_CUSTOMER("ROLE_VIP_CUSTOMER,ROLE_CUSTOMER"),
    CUSTOMER("ROLE_CUSTOMER");

    private final String roles;

    // DB에 User Role 정보가 enum 인스턴스들의 이름 그대로 저장되어 있고,
    // 해당 enum클래스의 필드인 roles를 가져오기 위한 함수
    public static String getIncludingRoles(String role){
        return Role.valueOf(role).getRoles();
    }

    // 권한들이 모여있는 String에 추가하고 싶은 Role의 roles들을 추가하여
    // 해당 권한들을 String으로 반환하는 함수
    public static String addRole(Role role, String addRole){
        String priorRoles = role.getRoles();
        priorRoles += ","+addRole;
        return priorRoles;
    }
    // 권한들이 모여있는 String에 추가하고 싶은 Role의 roles들을 추가하여
    // 해당 권한들을 String으로 반환하는 함수
    public static String addRole(String roles, Role role){
        return roles + "," + role.getRoles();

    }
}
