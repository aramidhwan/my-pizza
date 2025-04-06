(() => {
    loadStores() ;

    // -------------------------------
    // 가맹점 신규 등록 Modal 팝업 처리
    // -------------------------------
    const registModal = document.getElementById('registModal') ;
    if (registModal) {
        registModal.addEventListener('show.bs.modal', fnShowRegistModal) ;
    }

    // -------------------------------
    // 주문 접수 내역 Modal 팝업 처리
    // -------------------------------
    const orderCntModal = document.getElementById('orderCntModal') ;
    let   orderCntModalOpen = false ;
    if (orderCntModal) {
//        orderCntModal.removeEventListener('show.bs.modal', fnOrderCnt);
        orderCntModal.addEventListener('show.bs.modal', fnOrderCnt);

        // -------------------------------
        // Modal hidden 이벤트 처리
        // -------------------------------
        orderCntModal.addEventListener('hidden.bs.modal', event => {
            // table element 찾기
            const table = document.getElementById('tblStoreOrder');
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
    // storeMain.html 화면 우측상단 "신규가맹점 등록" 버튼 이벤트 등록
    // -------------------------------
    document.getElementById("buttonCreateStore").addEventListener("click", createStoreClicked) ;

    // -------------------------------
    // 드랍다운[지역명] 처리
    // -------------------------------
    $('#dropdownRegion .dropdown-menu li > a').bind('click', function (e) {
        var html = $(this).html();
        $('#regionNm').html(html+' ');
        $('#regionNm').val(html);
    });

})()

// 전역 변수 선언 (중복 방지)
if (typeof gStoreId === "undefined") {
    var gStoreId = "";  // `let`이 아니라 `var` 사용
} else {
    gStoreId = "" ;
}

// -------------------------------
// 가맹점 신규 등록 Modal 팝업 처리
// -------------------------------
function fnShowRegistModal(event) {
    event.stopPropagation();
    console.log("# 가맹점 등록/수정 Modal");

    // Button that triggered the modal
    const clickedObj = event.relatedTarget
    // Extract info from data-bs-* attributes
    gStoreId = clickedObj.getAttribute('data-bs-storeId') ;
    // If necessary, you could initiate an Ajax request here
    // and then do the updating in a callback.

    // Update the modal's content.
    const modalTitle = registModal.querySelector('.modal-title') ;
    if ( gStoreId === "" ) {
        modalTitle.textContent = `신규 가맹점 등록` ;
        updateTips("상점 정보를 입력하십시요.") ;
        document.getElementById("buttonCreateStore").innerText = "신규등록" ;
    } else {
        modalTitle.textContent = `가맹점 정보 수정` ;
        updateTips("상점 정보를 수정하십시요.") ;

        const storeNm = clickedObj.getAttribute('data-bs-storeNm') ;
        registModal.querySelector('#storeNm').value = storeNm ;

        const regionNm = clickedObj.getAttribute('data-bs-regionNm') ;
        registModal.querySelector('#regionNm').value = regionNm ;
        registModal.querySelector('#regionNm').innerHTML = regionNm+' ' ;

        const addr = clickedObj.getAttribute('data-bs-addr') ;
        registModal.querySelector('#addr').value = addr ;

        const openYN = clickedObj.getAttribute('data-bs-openYN') ;
        if ( openYN == "true" ) {
            registModal.querySelector('#openY').checked = true ;
        } else {
            registModal.querySelector('#openN').checked = true ;
        }
        document.getElementById("buttonCreateStore").innerText = "수정" ;
    }
}

// -------------------------------
// Modal hidden 이벤트 처리
// -------------------------------
registModal.addEventListener('hidden.bs.modal', event => {
    gStoreId = "" ;
    registModal.querySelector('#storeNm').value = "" ;
    registModal.querySelector('#regionNm').innerText = "지역을 선택하세요..." ;
    registModal.querySelector('#regionNm').value = "" ;
    registModal.querySelector('#addr').value = "" ;
    registModal.querySelector('#openY').checked = true ;
})

// "주문 접수 내역" --> 현재 안쓰는 기능임
function fnOrderCnt(event) {
//    if (orderCntModalOpen) {
//        return false;
//    }
//    orderCntModalOpen = true ;
    event.stopPropagation();
    console.log("# 주문 접수 내역 Modal 팝업");

    // Button that triggered the modal
    const clickedObj = event.relatedTarget
    // Extract info from data-bs-* attributes
    const storeId = clickedObj.getAttribute('data-bs-storeId') ;
    const storeNm = clickedObj.getAttribute('data-bs-storeNm') ;
    const modalTitle = orderCntModal.querySelector('.modal-title') ;
    modalTitle.textContent = "["+storeNm+"] 주문 접수 내역" ;

    // If necessary, you could initiate an Ajax request here
    // and then do the updating in a callback.
    const url = '/store-service/api/storeOrder' ;   // 안쓰이는 기능임
    const params = {
      method: 'GET',
      credentials: 'include',
      redirect: "follow" // follow, error, or manual
    };
    const requestParams = {
        storeId: storeId
    };
    // URLSearchParams를 사용해 파라미터를 쿼리 스트링 형식으로 변환
    const queryString = new URLSearchParams(requestParams).toString();

    callFetchApi(url, queryString, params, "displayStoreOrder") ;
}

function displayStoreOrder(data) {
    let cellIndex = 0 ;
    const storeOrderDtos = data.data ;
    storeOrderDtos.forEach(storeOrderDto => {
        // table element 찾기
        const table = document.getElementById('tblStoreOrder');

        // 새 행(Row) 추가
        const newRow = table.insertRow();
        // 새 행(Row)에 Cell 추가
        const newCell1 = newRow.insertCell(cellIndex++);    // 주문번호
        const newCell2 = newRow.insertCell(cellIndex++);    // 주문상태(status)
        const newCell3 = newRow.insertCell(cellIndex++);    // 접수시간
        // css 적용
        newCell1.style.textAlign = "center" ;
        // Cell에 텍스트 추가
        newCell1.innerText = storeOrderDto.orderId;
        newCell2.innerText = storeOrderDto.status;
        newCell3.innerText = dateTimeFormatter(storeOrderDto.createDt);
    }) ;
}

function loadStores() {
    const url = '/store-service/api/getStores' ;
    const params = {
      method: 'GET',
      credentials: 'include',
      redirect: "follow" // follow, error, or manual
    };

    callFetchApi(url, null, params, "displayStores") ;
}

function createStoreClicked(event) {
    const storeNm = document.getElementById("storeNm") ;
    const regionNm = document.getElementById("regionNm") ;
    const addr = document.getElementById("addr") ;

    let valid = true;
    storeNm.classList.remove("ui-state-error");
    regionNm.classList.remove("ui-state-error");
    addr.classList.remove("ui-state-error");

    valid = valid && checkLength( storeNm, "상점명", 2, 20 );
    valid = valid && checkSelect( regionNm, "지역" ) ;
    valid = valid && checkLength( addr, "주소", 2, 50 );

    if ( !valid ) {
        return ;
    }

    const url = '/store-service/api/createModifyStore' ;
    const params = {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json', // 데이터 형식 설정
        },
        redirect: "follow", // follow, error, or manual
        body: JSON.stringify({
            'storeId': gStoreId,
            'storeNm': storeNm.value,
            'regionNm': regionNm.value,
            'addr': addr.value,
            'openYN': $("input:radio[name='openYN']:checked").val()
        })
    } ;

    callFetchApi(url, null, params, "loadStoreMain") ;

    // 수동으로 모달 닫기
    const registModal = new bootstrap.Modal(document.getElementById("registModal"));
    registModal.hide();
}
// 신규가맹점 등록 버튼 처리 END

function displayStores(data) {
    let cellIndex = 0 ;
    const tableStore = document.getElementById("tableStore");

    const tbody = tableStore.querySelector("tbody"); // tbody 요소를 선택
    tbody.innerHTML = ""; // tbody 내 모든 내용을 삭제

    for (let inx = 0; inx < data.data.length; inx++) {
        cellIndex = 0 ;
        const storeDto = data.data[inx];

        // 선택된 메뉴 내용 표시
        var newRow = tbody.insertRow(tbody.rows.length);

        var newCell1 = newRow.insertCell(cellIndex++);    // #
        var newCell2 = newRow.insertCell(cellIndex++);    // 상점명
        var newCell3 = newRow.insertCell(cellIndex++);    // 지역명
        var newCell4 = newRow.insertCell(cellIndex++);    // 영업여부
//        var newCell5 = newRow.insertCell(cellIndex++);    // 주문건수
        var newCell6 = newRow.insertCell(cellIndex++);    // 주소
        newCell1.classList.add("cssAlignCenter") ;
        newCell2.classList.add("cssAlignCenter") ;
        newCell3.classList.add("cssAlignCenter") ;
        newCell4.classList.add("cssAlignCenter") ;
//        newCell5.classList.add("cssAlignCenter") ;
        newCell6.classList.add("cssAlignCenter") ;
        newCell1.innerText = storeDto.storeId ;
        newCell2.innerHTML = '<a class="storeNm-css1" data-bs-toggle="modal" data-bs-target="#registModal" data-bs-storeId="'+storeDto.storeId+'" data-bs-storeNm="'+storeDto.storeNm+'" data-bs-regionNm="'+storeDto.regionNm+'" data-bs-addr="'+storeDto.addr+'" data-bs-openYN="'+storeDto.openYN+'" href="#">'+storeDto.storeNm+'</a>' ;
        newCell3.innerText = storeDto.regionNm ;
        newCell4.innerText = storeDto.openYN? '영업중':'휴업' ;
//        newCell5.innerHTML = (storeDto.orderCnt===0)? '0건':'<a class="storeNm-css1" data-bs-toggle="modal" data-bs-target="#orderCntModal" data-bs-storeId="'+storeDto.storeId+'" data-bs-storeNm="'+storeDto.storeNm+'" href="#">'+storeDto.orderCnt+'건</a>' ;
        newCell6.innerText = storeDto.addr ;
    }
}
