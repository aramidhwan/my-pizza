const JwtAuth = {
    OK: "OK",
    JWT_EXPIRED: "JWT_EXPIRED",
    JWT_REGENERATED: "JWT_REGENERATED",
    NO_JWT: "NO_JWT",
    WRONG_JWT: "WRONG_JWT",
    REFRESH_EXPIRED: "REFRESH_EXPIRED",
    WRONG_REFRESH: "WRONG_REFRESH"
} ;

const Roles = {
    ROLE_ADMIN: "ROLE_ADMIN",
    ROLE_CUSTOMER: "ROLE_CUSTOMER",
    ROLE_VIP_CUSTOMER: "ROLE_VIP_CUSTOMER",
    ROLE_STORE_ADMIN: "ROLE_STORE_ADMIN",
    ROLE_DELIVERY_ADMIN: "ROLE_DELIVERY_ADMIN"
} ;

const OrderStatus = {
    ORDERED: "주문신청(Ordered)",
    ORDER_CANCELLED: "주문취소(OrderCancelled)",
    ORDER_REJECTED: "주문거절(OrderRejected)",
    ORDER_ACCEPTED: "주문접수(OrderAccepted)",
    COOKED: "조리완료(Cooked)",
    DELIVERY_ACCEPTED: "배달접수(DeliveryAccepted)",
    DELIVERY_STARTED: "배달시작(DeliveryStarted)",
    DELIVERED: "배달완료(Delivered)"
} ;

const global_loginPageUrl = '/common-service/html/auth/loginPage';

function callFetchApi(url, queryString, params, callBackFunctionName) {
    fetch((queryString==null)? url:url+"?"+queryString, params)
    .then(response => {
        if ( !response.ok ) {
            if ( response.status == 401 ) {
                // 401 : JWT 토큰이 없거나 잘못된 경우
                // 401 : JWT 토큰이 있는데 만료된 경우
                // Do Nothing Here!
            } else {
                throw new Error("❌ " + response.status + ' 에러가 발생했습니다.') ;
            }
        }
        return response.json() ;
    }) // 서버 응답을 JSON 형식으로 변환합니다.
    .then(json => {
        if ( json.BIZ_SUCCESS == "0" ) {
            if ( json.msg != null ) {
                alert(json.msg) ;
            }
            if ( callBackFunctionName != null ) {
                if (typeof window[callBackFunctionName] === "function") {
                    window[callBackFunctionName](json);
                } else {
                    alert("❌ 해당 함수가 존재하지 않습니다. ["+callBackFunctionName+"()]");
                }
            }
        // 비즈니스 Exception 인 경우
        } else if ( json.BIZ_SUCCESS == "2" ) {
            if ( json.msg != null ) {
                alert(json.msg) ;
            }
            return false ;
        // JWT 인증 관련 사항
        } else if ( json.BIZ_SUCCESS == "1" ) {
            if ( json.jwtAuth == JwtAuth.JWT_REGENERATED) {
                // RefreshToken 처리
                callFetchApi(url, queryString, params, callBackFunctionName) ;
            } else if (json.jwtAuth == JwtAuth.NO_JWT
                    || json.jwtAuth == JwtAuth.WRONG_JWT
                    || json.jwtAuth == JwtAuth.REFRESH_EXPIRED
                    || json.jwtAuth == JwtAuth.WRONG_REFRESH
                    ) {
                if (confirm("⚠️ 로그인이 필요합니다. 로그인 페이지로 이동합니다.\n로그인 후 자동으로 본 페이지로 다시 이동합니다.") ) {
                    window.location.href = global_loginPageUrl;
                } else {
                    // Do Nothing Here!
                }
            } else {
                alert("❌ 관리자에게 문의하십시요.\n예상치 못한 상태, jwtAuth : " + json.jwtAuth + ", " + json.msg);
            }
        // 시스템 오류
        } else if ( json.BIZ_SUCCESS == "9" ) {
            alert("❌ 관리자에게 문의하십시요.\n예상치 못한 상태, json.BIZ_SUCCESS : " + json.BIZ_SUCCESS + ", " + json.msg);
        } else {
            alert("❌ 관리자에게 문의하십시요.\n예상치 못한 상태, json.BIZ_SUCCESS : " + json.BIZ_SUCCESS + ", " + json.msg);
        }
    })
    .catch(error => {
        alert(error.message) ;
    })
    ;
}