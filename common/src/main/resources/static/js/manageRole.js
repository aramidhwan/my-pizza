(() => {
    loadCustomers() ;
    
    // -------------------------------
    // 주문 접수 내역 Modal 팝업 처리
    // -------------------------------
    // Modal 'Varying modal content' example in docs and StackBlitz
    // js-docs-start varying-modal-content
    const roleModal = document.getElementById('roleModal') ;
    let   roleModalOpen = false ;
    if (roleModal) {
//        orderCntModal.removeEventListener('show.bs.modal', fnOrderCnt);
        roleModal.addEventListener('show.bs.modal', fnAddRole);

        function fnAddRole(event) {
            if (roleModalOpen) {
                return false;
            }
            roleModalOpen = true ;
            event.stopPropagation();
            console.log("# 권한 Modal 팝업");

            // Button that triggered the modal
            const clickedObj = event.relatedTarget
            // Extract info from data-bs-* attributes
            const customerNm = clickedObj.getAttribute('data-bs-customerNm') ;
            const email      = clickedObj.getAttribute('data-bs-email') ;
            const modalTitle = roleModal.querySelector('.modal-title') ;
            modalTitle.textContent = "[권한] " + customerNm + " " + email ;

            // If necessary, you could initiate an Ajax request here
            // and then do the updating in a callback.
            const url = '/common-service/api/getRoles' ;
            const params = {
              method: 'GET',
              credentials: 'include',
              redirect: "follow" // follow, error, or manual
            };
            const queryString = new URLSearchParams();
            queryString.append('email', email);

            fetch(url + '?' + queryString.toString(), params)
            .then(response => {
                if ( !response.ok ) {
                    if ( response.status == 401 ) {
                        // Do Nothing Here!
                    } else {
                        throw new Error("❌ " + response.status + ' 에러가 발생했습니다.') ;
                    }
                }
                return response.json() ;
            }) // 서버 응답을 JSON 형식으로 변환합니다.
            .then(data => {
                if ( data.BIZ_SUCCESS == "0" ) {
                    const customerDto = data.data.customerDto ;
                    const authorityDtos = data.data.authorityDtos ;
                    // table element 찾기
                    document.getElementById('email').value = customerDto.email ;
                    const table = document.getElementById('tblRole');

                    authorityDtos.forEach(authorityDto => {
                        // 새 행(Row) 추가
                        const newRow = table.insertRow();
                        // 새 행(Row)에 Cell 추가
                        const newCell1 = newRow.insertCell(0);    // 권한명
//                        const newCell2 = newRow.insertCell(1);    // 권한여부
                        // css 적용
                        newCell1.classList.add("cssAlignCenter") ;
//                        newCell2.classList.add("cssAlignCenter") ;

                        let checked = "" ;
                        customerDto.authorities.forEach(data => {
                            if ( authorityDto.role === data.role) {
                                checked = " checked" ;
                            }
                        })
                        // Cell에 텍스트 추가
                        newCell1.innerHTML = '<div class="form-check form-switch"><input name="givenRole" value="'+authorityDto.role+'" class="form-check-input" type="checkbox" role="switch"'+checked+'>'+authorityDto.role+'</div>';
//                        newCell2.innerHTML = '<div class="form-check form-switch"><input name="givenRole" class="form-check-input" type="checkbox" role="switch"'+checked+'></div>';
                    })

                } else if ( data.BIZ_SUCCESS == "1" ) {
                    if ( data.jwtAuth == JwtAuth.JWT_REGENERATED) {
                        // RefreshToken 처리
                        fnAddRole(event) ;
                    } else if (data.jwtAuth == JwtAuth.NO_JWT
                            || data.jwtAuth == JwtAuth.WRONG_JWT
                            || data.jwtAuth == JwtAuth.REFRESH_EXPIRED
                            ) {
                        if (confirm("⚠️ 로그인이 필요합니다. 로그인 페이지로 이동합니다.\n로그인 후 자동으로 본 페이지로 다시 이동합니다.") ) {
                            window.location.href = global_loginPageUrl;
                        } else {
                            // Do Nothing Here!
                        }
                    } else {
                        alert("❌ 예상치 못한 상태, jwtAuth : " + data.jwtAuth + ", " + data.msg);
                    }
                }
            })
            .catch(error => {
                alert(error.message) ;
            })
            ;

            // Update the modal's content.
//            const modalTitle = registModal.querySelector('.modal-title') ;
//            const regionNm = clickedObj.getAttribute('data-bs-regionNm') ;
            roleModalOpen = false ;
        }

        // -------------------------------
        // Modal hidden 이벤트 처리
        // -------------------------------
        roleModal.addEventListener('hidden.bs.modal', event => {
            // table element 찾기
            const table = document.getElementById('tblRole');
            const rowCnt = table.rows.length ;

            if ( rowCnt > 1 ) {
                for (let inx=0 ; inx < rowCnt-1 ; inx++ ) {
                    table.deleteRow(-1) ;
                }
            }
        });
    }
    // js-docs-end varying-modal-content END

    // -------------------------------
    // 등록 버튼 처리
    // -------------------------------
    const btnUpdateRole = document.getElementById("btnUpdateRole");
    btnUpdateRole.addEventListener("click", updateRole) ;

    function updateRole(event) {
        // 선택된 Role 값 가져오기
        const selectedRoles = $("input:checkbox[name='givenRole']:checked")
            .map(function() {
                return this.value;  // 체크된 체크박스의 value 값 반환
            })
            .get();  // 배열로 변환

        const url = '/common-service/api/addModifyRole' ;
        const params = {
            method: 'POST',
            credentials: 'include',
            redirect: "follow",
            headers: {
                'Content-Type': 'application/json', // 데이터 형식 설정
            },
            body: JSON.stringify({
                'email': document.getElementById('email').value,
                'authorities': selectedRoles
            })
        };
        fetch(url, params)
        .then(response => {
            if ( !response.ok ) {
                if ( response.status == 401 ) {
                    // Do Nothing Here!
                } else {
                    throw new Error(response.status + ' 에러가 발생했습니다.') ;
                }
            }
            return response.json() ;
        }) // 서버 응답을 JSON 형식으로 변환합니다.
        .then(data => {
            if ( data.BIZ_SUCCESS == "0" ) {
                document.getElementById("btnCancel").click();
                alert(data.msg);
            } else if ( data.BIZ_SUCCESS == "1" ) {
                if ( data.jwtAuth == JwtAuth.JWT_REGENERATED) {
                    // RefreshToken 처리
                    updateRole(event) ;
                } else if (data.jwtAuth == JwtAuth.NO_JWT
                        || data.jwtAuth == JwtAuth.WRONG_JWT
                        || data.jwtAuth == JwtAuth.REFRESH_EXPIRED
                        ) {
                    if (confirm("로그인이 필요합니다. 로그인 페이지로 이동합니다.\n로그인 후 자동으로 본 페이지로 다시 이동합니다.") ) {
                        window.location.href = global_loginPageUrl;
                    } else {
                        // Do Nothing Here!
                    }
                } else {
                    alert("❌ 예상치 못한 상태, jwtAuth : " + data.jwtAuth + ", " + data.msg);
                }
            }
        })
        .catch(error => {
            alert(error.message) ;
        })
        ;
    }
    // 등록 버튼 처리 END

})()

// 함수 노출: 전역에서 호출 가능하도록 window 객체에 할당
function loadCustomers() {
    const url = '/common-service/api/getCustomers' ;
    const params = {
      method: 'GET',
      credentials: 'include',
      redirect: "follow" // follow, error, or manual
    };

    callFetchApi(url, null, params, "displayCustomers")
}

function displayCustomers(data) {
    const tblCustomer = document.getElementById("tblCustomer");

    const tbody = tblCustomer.querySelector("tbody"); // tbody 요소를 선택
    tbody.innerHTML = ""; // tbody 내 모든 내용을 삭제

    for (let inx = 0; inx < data.data.length; inx++) {
        const customerDto = data.data[inx];
        <!-- 선택된 메뉴 내용 표시 -->
        var newRow = tbody.insertRow(tbody.rows.length);

        var newCell1 = newRow.insertCell(0);    // #
        var newCell2 = newRow.insertCell(1);    // Customer ID
        var newCell3 = newRow.insertCell(2);    // Customer Name
        var newCell4 = newRow.insertCell(3);    // Email
        var newCell5 = newRow.insertCell(4);    // Activated
        newCell1.classList.add("cssAlignCenter") ;
        newCell2.classList.add("cssAlignCenter") ;
        newCell3.classList.add("cssAlignCenter") ;
        newCell4.classList.add("cssAlignCenter") ;
        newCell5.classList.add("cssAlignCenter") ;
        newCell1.innerText = customerDto.customerNo ;
        newCell2.innerHTML = '<a class="no-line" data-bs-toggle="modal" data-bs-target="#roleModal" data-bs-email="'+customerDto.email+'" data-bs-customerNm="'+customerDto.customerName+'" href="#">'+customerDto.customerId+'</a>' ;
        newCell3.innerText = customerDto.customerName ;
        newCell4.innerHTML = '<a class="no-line" data-bs-toggle="modal" data-bs-target="#roleModal" data-bs-email="'+customerDto.email+'" data-bs-customerNm="'+customerDto.customerName+'" href="#">'+customerDto.email+'</a>' ;
        newCell5.innerText = customerDto.activated ;
    }

}
