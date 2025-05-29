// 전역에 저장
window.calendarInstance = null;

$(
    function() {
        checkSignIn() ;
    }
);

function signIn(event) {
    event.preventDefault();
    window.location.href = global_loginPageUrl ;
}
function logout(event) {
    event.preventDefault();
    const url = '/common-service/api/auth/logout' ;
    const params = {
      method: 'GET',
      credentials: 'include'
    };
    const signYN  = document.getElementById("signYN") ;
    const liAdmin = document.getElementById("liAdmin") ;
    const liStore = document.getElementById("liStore") ;

    fetch(url, params)
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
    .then(data => {
        // 정상적 "로그아웃" 시 처리할 일들
        if ( data.BIZ_SUCCESS == "0" ) {
            signYN.innerText = '로그인' ;
            signYN.setAttribute("data-value", "N") ;
            signYN.removeEventListener("click", logout); // 기존 이벤트 제거
            signYN.addEventListener("click", signIn); // 신규 이벤트 등록
            if ( !liAdmin.classList.contains("hidden")) {
                liAdmin.classList.add("hidden");
            }
            if ( !liStore.classList.contains("hidden")) {
                liStore.classList.add("hidden");
            }
            if ( !liStoreAdmin.classList.contains("hidden")) {
                liStoreAdmin.classList.add("hidden");
            }
            if ( !liDeliveryAdmin.classList.contains("hidden")) {
                liDeliveryAdmin.classList.add("hidden");
            }
            loadOrderMain();
            alert(data.msg);
        } else {
            alert("❌ 예상치 못한 BIZ_SUCCESS : " + data.BIZ_SUCCESS) ;
        }
    })
    .catch(error => {
        alert("❌ 설마 이거? " + error.message) ;
    })
    ;
}
function loadStoreAdminMain() {
    safeLoadContent("/store-service/html/storeAdminMain");
}
function loadDeliveryAdminMain() {
    safeLoadContent("/delivery-service/html/deliveryAdminMain");
}
function loadStoreMain() {
    safeLoadContent("/store-service/html/storeMain");
}
function loadOrderMain() {
    // 이벤트 중복 등록 방지
    window.menuEventAdded = false ;
//    window.btnDeleteMenuEventAdded = false ;
    window.btnOrder = false ;
    safeLoadContent("/order-service/html/orderMain");
}

function loadMyPageMain() {
    // 기존 달력 제거
    safeLoadContent("/mypage-service/html/myPageMain", function () {
        if (typeof initMyPageMain === 'function') {
            initMyPageMain();
        }
    });
}

function loadRoleMain() {
    safeLoadContent("/common-service/html/roleMain");
}

function safeLoadContent(url, callback) {
    cleanupAllBeforeLoad();
    $("#content").load(url, callback);
}

function cleanupAllBeforeLoad() {
    // [myPageMain] flatpickr 달력 인스턴스가 있으면 정리
    if (window.calendarInstance) {
        try {
            window.calendarInstance.destroy();
        } catch (e) {
            console.warn("flatpickr destroy 실패", e);
        }
        window.calendarInstance = null;
    }

    // 혹시 DOM에 남은 달력 DOM도 제거 (확실하게)
    document.querySelectorAll(".flatpickr-calendar").forEach(el => el.remove());

    // [storeMain 등] 모달 백드롭 제거
    document.body.classList.remove("modal-open");
    document.querySelectorAll(".modal-backdrop").forEach(el => el.remove());
}

// myPageMain 로딩 시 호출되는 함수 내부
function initMyPageMain() {
//    const today = new Date();
//    flatpickr.localize(flatpickr.l10ns.ko);
//
//    // 달력 제거
//    if (window.calendarInstance && typeof window.calendarInstance.destroy === "function") {
//        window.calendarInstance.destroy();
//        document.querySelectorAll(".flatpickr-calendar").forEach(el => el.remove());
//        window.calendarInstance = null;
//    }
//
//    // DOM에 요소가 있는 경우에만 초기화
//    const startInput = document.getElementById("startDate");
//    if (startInput) {
//        window.calendarInstance = flatpickr("#startDate", {
//            mode: "range",
//            dateFormat: "Y-m-d",
//            defaultDate: [today, today],
//            locale: "ko",
//            plugins: [new rangePlugin({ input: "#endDate" })]
//        });
//    }
//
//    loadMyOrder();
}

/*
    top 우측상단의 "로그인"/"로그아웃" 결정하기 위한 자동호출
*/
function checkSignIn() {
    let initDisplay = "" ;
    const url = '/common-service/api/auth/checkSignIn' ;
    const params = {
      method: 'GET',
      credentials: 'include'
    };
    const signYN  = document.getElementById("signYN") ;
    const liAdmin = document.getElementById("liAdmin") ;
    const liStore = document.getElementById("liStore") ;

    fetch(url, params)
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
    .then(data => {
        // 로그인 된 사용자인 경우
        if ( data.BIZ_SUCCESS == "0" ) {
            signYN.innerText = '로그아웃' ;
            signYN.setAttribute("data-value", "Y") ;
            signYN.removeEventListener("click", signIn); // 기존 이벤트 제거
            signYN.addEventListener("click", logout); // 신규 이벤트 등록

            const authorities = data.data ;
            console.log("# authorities : " + authorities) ;

            // ROLE_CUSTOMER 권한이 있을 경우
            if ( authorities.includes(Roles.ROLE_CUSTOMER) ) {
                liOrder.classList.remove("hidden");
                liMyPage.classList.remove("hidden");
                initDisplay = "loadOrderMain" ;    // Spring Boot 호출
            }
            // ROLE_ADMIN 권한이 있을 경우
            if ( authorities.includes(Roles.ROLE_ADMIN) ) {
                liStore.classList.remove("hidden");
                liAdmin.classList.remove("hidden");
                initDisplay = "loadOrderMain" ;    // Spring Boot 호출
            }
            // ROLE_STORE_ADMIN 권한이 있을 경우
            if ( authorities.includes(Roles.ROLE_STORE_ADMIN) ) {
                liStoreAdmin.classList.remove("hidden");
                initDisplay = "loadStoreAdminMain" ;    // Spring Boot 호출
            }
            // ROLE_STORE_ADMIN 권한이 있을 경우
            if ( authorities.includes(Roles.ROLE_DELIVERY_ADMIN) ) {
                liDeliveryAdmin.classList.remove("hidden");
                initDisplay = "loadDeliveryAdminMain" ;    // Spring Boot 호출
            }
        // 비로그인 사용자 인 경우
        } else if ( data.BIZ_SUCCESS == "1" ) {
            signYN.innerText = '로그인' ;
            signYN.setAttribute("data-value", "N") ;
            signYN.removeEventListener("click", logout); // 기존 이벤트 제거
            signYN.addEventListener("click", signIn); // 신규 이벤트 등록
            liOrder.classList.remove("hidden");
            liMyPage.classList.remove("hidden");
            initDisplay = "loadOrderMain" ;    // Spring Boot 호출
        } else {
            alert("예상치 못한 BIZ_SUCCESS : " + data.BIZ_SUCCESS) ;
            return ;
        }

        // ROLE에 따른 초기 화면 호출
        if ( initDisplay != null ) {
            if (typeof window[initDisplay] === "function") {
                window[initDisplay]();
            } else {
                alert("❌ 해당 함수가 존재하지 않습니다. ["+initDisplay+"()]");
            }
        }

    })
    .catch(error => {
        alert("❌ 설마 이거? " + error.message) ;
    })
    ;
}
