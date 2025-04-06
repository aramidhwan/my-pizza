$( function() {
    const signInBtn = document.getElementById("signIn");
    const signUpBtn = document.getElementById("signUp");
    const formSignUp = document.getElementById("formSignUp");
    const formSignIn = document.getElementById("formSignIn");
    const container = document.querySelector(".container");
    let   jwtAccessToken ;

    // 화면 load 시 로그인 부터 표시
    container.classList.remove("right-panel-active");

    signInBtn.addEventListener("click", () => {
      container.classList.remove("right-panel-active");
    });

    signUpBtn.addEventListener("click", () => {
      container.classList.add("right-panel-active");
    });

    // -------------------------------
    // 회원가입 처리
    // -------------------------------
    formSignUp.addEventListener("submit", function (event) {
        event.preventDefault();
        const emailSignUp    = document.getElementById("emailSignUp");
        const passwordSignUp = document.getElementById("passwordSignUp");
        const customerIdSignUp = document.getElementById("customerIdSignUp");

        fetch('/common-service/api/auth/sign-up', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json' // 데이터 형식 설정
            },
            body: JSON.stringify({
                'customerId': customerIdSignUp.value,
                'email': emailSignUp.value,
                'password': passwordSignUp.value
            })
        })
        .then(response => response.json()) // 서버 응답을 JSON 형식으로 변환합니다.
        .then(data => {
            if ( data.BIZ_SUCCESS == "0" ) {
                alert(data.msg) ;
                // 화면 load 시 로그인 부터 표시
                container.classList.remove("right-panel-active");
                document.getElementById("emailSignIn").value = emailSignUp.value ;
                document.getElementById("passwordSignIn").value = "" ;

            } else if ( data.BIZ_SUCCESS == "2" ) {
                alert(data.msg) ;
            } else {
                alert(data.msg);
            }
        });
    });

    // -------------------------------
    // 로그인액션 처리
    // -------------------------------
    formSignIn.addEventListener("submit", function (event) {
        event.preventDefault();
        const emailSignIn    = document.getElementById("emailSignIn");
        const passwordSignIn = document.getElementById("passwordSignIn");
        const redirectUrl = document.getElementById("redirectUrl");

        const url = '/common-service/api/auth/sign-in' ;
        const params = {
            method: 'POST',
            credentials: 'include',
            redirect: "follow", // follow, error, or manual
            headers: {
                'Content-Type': 'application/json' // 데이터 형식 설정
            },
            body: JSON.stringify({
                'email': emailSignIn.value,
                'password': passwordSignIn.value,
                'redirectUrl': redirectUrl.value
            })
        };

        callFetchApi(url, null, params, "loadMainPage") ;
    });
} );

// -------------------------------
// 로그인 성공 시 메인페이지 호출
// -------------------------------
function loadMainPage(data) {
    window.location.href = '/common-service/html/index';
}

// 회원가입 form에서 ID 변경 시 email도 실시간 변경
document.addEventListener("DOMContentLoaded", function () {
    const customerIdInput = document.getElementById("customerIdSignUp");
    const emailInput = document.getElementById("emailSignUp");

    customerIdInput.addEventListener("input", function () {
        const idValue = customerIdInput.value.trim();
        emailInput.value = idValue ? `${idValue}@naver.com` : "";
    });
});
