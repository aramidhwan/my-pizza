(() => {
    // 오늘 날짜 계산
    const today = new Date();
    const oneMonthAgo = new Date();
    oneMonthAgo.setMonth(today.getMonth() - 1);

    // 한국어 로케일을 flatpickr에 등록
    flatpickr.localize(flatpickr.l10ns.ko);
    flatpickr("#startDate", {
        mode: "range",
        dateFormat: "Y-m-d",
        defaultDate: [today, today],  // ✅ 기본 선택값 설정
        locale: "ko", // 한국어 설정
        plugins: [new rangePlugin({ input: "#endDate" })]
    });

    // -------------------------------
    // Modal 드랍다운[KAFKA 이벤트 발생기의 STATUS 선택] 처리
    // -------------------------------
    $('#dropdownKafkaStatus .dropdown-menu li > a').bind('click', function (e) {
        var html = $(this).html();
        $('#kafkaStatus').html(html+' ');
        $('#kafkaStatus').val(html);
    });

    // -------------------------------
    // Modal KAFKA Sumbit 버튼 이벤트 등록
    // -------------------------------
    document.getElementById("buttonPublishKafka").addEventListener("click", publishKafkaClicked) ;

    // 👇 모달이 닫힐 때 포커스 해제 (접근성 경고 해결)
    const kafkaModalEl = document.getElementById('kafkaTestModal');
    if (kafkaModalEl) {
        kafkaModalEl.addEventListener('hide.bs.modal', function () {
            // 취소 버튼 등에 남아있는 포커스를 강제로 해제
            if (document.activeElement) {
                document.activeElement.blur();
            }
        });
    }

    loadStoreAdmin() ;
})()

function loadStoreAdmin() {
    const url = '/store-service/api/getStoreAdmin' ;
    const params = {
      method: 'GET',
      credentials: 'include',
      redirect: "follow" // follow, error, or manual
    };
    const requestParams = {
        startDate: document.getElementById("startDate").value,
        endDate: document.getElementById("endDate").value
    };
    // URLSearchParams를 사용해 파라미터를 쿼리 스트링 형식으로 변환
    const queryString = new URLSearchParams(requestParams).toString();

    callFetchApi(url, queryString, params, "displayStoreAdmin") ;
}

function displayStoreAdmin(json) {
    let storeDto = null ;
    let storeOrderDto = null ;
    let storeOrderDetailDto = null ;
    let cellIndex = 0 ;
    let prevOrderId = null ;
    let tbody = null; // 🔹 tbody 변수를 for 루프 밖에서 선언

    // 기존 (테이블 컨테이너) 내용 삭제
    clearTableContainer() ;
    // [강남1호점], [강남2호점]...
    for (let inx = 0; inx < json.data.length; inx++) {
        storeDto = json.data[inx] ;

        // 테이블 동적 생성 함수 실행
        createStoreAdminTable(storeDto.storeId, storeDto.storeNm, storeDto.openYN);
        const tblStore = document.getElementById("tblStoreAdmin"+storeDto.storeId);
        tbody = tblStore.querySelector("tbody"); // tbody 요소를 선택
        //tbody.innerHTML = ""; // tbody 내 모든 내용을 삭제

        // [강남1호점]의 주문 접수 내역이 없을 경우 다음 상점으로 Continue.
        if ( storeDto.storeOrderDtoList == null || storeDto.storeOrderDtoList.length == 0 ) {
            var newRow   = tbody.insertRow(tbody.rows.length);
            var newCell1 = newRow.insertCell(0);    // 주문번호
            newCell1.classList.add("cssAlignCenter") ;
            newCell1.colSpan = tblStore.querySelectorAll("th").length;
            newCell1.innerText = "주문 접수 내역이 없습니다." ;
            continue ; // 주문이 없는 경우 다음 반복문 실행
        }

        storeDto.storeOrderDtoList.forEach((storeOrderDto) => {
            storeOrderDto.storeOrderDetailDtoList.forEach((storeOrderDetailDto) => {
                var newRow = tbody.insertRow(tbody.rows.length);
                cellIndex = 0 ;
                let displayCheckBox = "" ;
                if ( storeOrderDto.orderId != prevOrderId ) {
                    var newCell1 = newRow.insertCell(cellIndex++);    // 주문번호
                    newCell1.classList.add("cssAlignCenter") ;
                    newCell1.rowSpan = storeOrderDto.storeOrderDetailDtoList.length ;
                    newCell1.innerText = storeOrderDto.orderId ;
                }

                var newCell2 = newRow.insertCell(cellIndex++);    // 주문메뉴
                newCell2.classList.add("cssAlignCenter") ;
                newCell2.innerText = storeOrderDetailDto.itemNm ;

                var newCell3 = newRow.insertCell(cellIndex++);    // 주문수량
                newCell3.classList.add("cssAlignCenter") ;
                newCell3.innerText = storeOrderDetailDto.qty ;

                if ( storeOrderDto.orderId != prevOrderId ) {
                    var newCell4 = newRow.insertCell(cellIndex++);       // 접수시간
                    newCell4.classList.add("cssAlignCenter") ;
                    newCell4.rowSpan = storeOrderDto.storeOrderDetailDtoList.length ;
                    newCell4.innerText = dateTimeFormatter(storeOrderDto.createDt) ;

                    var newCell5 = newRow.insertCell(cellIndex++);       // 조리완료시간
                    newCell5.id = "cookedDt_" + storeOrderDto.storeOrderId ; // 👈 조리완료시간 셀에 고유 ID 부여!
                    newCell5.classList.add("cssAlignCenter") ;
                    newCell5.rowSpan = storeOrderDto.storeOrderDetailDtoList.length ;
                    // STORE MSA의 t_store_order에는 STATUS가 2가지 밖에 없다. (ORDER_ACCEPTED, COOKED)
                    if ( storeOrderDto.status == "COOKED" ) {
                        newCell5.innerText = "(완료) " + dateTimeFormatter(storeOrderDto.updateDt) ;
                    } else if ( storeOrderDto.status == "ORDER_ACCEPTED" ) {
                        newCell5.innerText = "(접수) " + dateTimeFormatter(storeOrderDto.updateDt) ;
                    } else if ( storeOrderDto.status == "ORDER_REJECTED" ) {
                        newCell5.innerText = "(거절) " + dateTimeFormatter(storeOrderDto.updateDt) ;
                    } else if ( storeOrderDto.status == "ORDER_CANCELLED" ) {
                        newCell5.innerText = "(주문취소) " + dateTimeFormatter(storeOrderDto.updateDt) ;
                    } else {
                        newCell5.innerText = "" ;
                    }

                    // 주문접수 체크박스 처리
                    if ( storeOrderDto.status == "ORDERED" && OrderStatus.hasOwnProperty(storeOrderDto.status) ) {
                        displayCheckBox = "" ;
                    } else if ( storeOrderDto.status == "ORDER_ACCEPTED" && OrderStatus.hasOwnProperty(storeOrderDto.status) ) {
                        displayCheckBox = " checked" ;
                    } else if ( storeOrderDto.status == "ORDER_REJECTED" && OrderStatus.hasOwnProperty(storeOrderDto.status) ) {
                        displayCheckBox = " disabled" ;
                    } else if ( storeOrderDto.status == "COOKED" && OrderStatus.hasOwnProperty(storeOrderDto.status) ) {
                        displayCheckBox = " disabled" ;
                    } else {
                        displayCheckBox = " disabled" ;
                    }
                    newCell5 = newRow.insertCell(cellIndex++);       // 주문접수 체크박스
                    newCell5.classList.add("cssAlignCenter") ;
                    newCell5.rowSpan = storeOrderDto.storeOrderDetailDtoList.length ;
                    newCell5.innerHTML = '<div class="form-check form-switch center-checkbox"><input name="OrderAccepted" value="Y" data-order-id="'+storeOrderDto.orderId+'" class="form-check-input" type="checkbox" role="switch"'+displayCheckBox+'></div>';

                    // 주문거절 체크박스 처리
                    if ( storeOrderDto.status == "ORDERED" && OrderStatus.hasOwnProperty(storeOrderDto.status) ) {
                        displayCheckBox = "" ;
                    } else if ( storeOrderDto.status == "ORDER_ACCEPTED" && OrderStatus.hasOwnProperty(storeOrderDto.status) ) {
                        displayCheckBox = " disabled" ;
                    } else if ( storeOrderDto.status == "ORDER_REJECTED" && OrderStatus.hasOwnProperty(storeOrderDto.status) ) {
                        displayCheckBox = " checked" ;
                    } else if ( storeOrderDto.status == "COOKED" && OrderStatus.hasOwnProperty(storeOrderDto.status) ) {
                        displayCheckBox = " disabled" ;
                    } else {
                        displayCheckBox = " disabled" ;
                    }
                    var newCell6 = newRow.insertCell(cellIndex++);    // 주문거절 체크박스
                    newCell6.classList.add("cssAlignCenter") ;
                    newCell6.rowSpan = storeOrderDto.storeOrderDetailDtoList.length ;
                    newCell6.innerHTML = '<div class="form-check form-switch center-checkbox"><input name="OrderRejected" value="Y" data-order-id="'+storeOrderDto.orderId+'" class="form-check-input" type="checkbox" role="switch"'+displayCheckBox+'></div>';

                    // 조리완료 체크박스 처리
                    if ( storeOrderDto.status == "ORDERED" && OrderStatus.hasOwnProperty(storeOrderDto.status) ) {
                        displayCheckBox = " disabled" ;
                    } else if ( storeOrderDto.status == "ORDER_ACCEPTED" && OrderStatus.hasOwnProperty(storeOrderDto.status) ) {
                        displayCheckBox = "" ;
                    } else if ( storeOrderDto.status == "ORDER_REJECTED" && OrderStatus.hasOwnProperty(storeOrderDto.status) ) {
                        displayCheckBox = " disabled" ;
                    } else if ( (storeOrderDto.status == "COOKED" || storeOrderDto.status == "DELIVERY_ACCEPTED") && OrderStatus.hasOwnProperty(storeOrderDto.status) ) {
                        displayCheckBox = " checked" ;
                        // disabled 처리하니깐 모양이 안예쁠
//                        displayCheckBox = " checked disabled" ;
                    // 다른 상태는 없을껄??
                    } else {
                        displayCheckBox = " disabled" ;
                    }
                    var newCell7 = newRow.insertCell(cellIndex++);    // 조리완료 체크박스
                    newCell7.classList.add("cssAlignCenter") ;
                    newCell7.rowSpan = storeOrderDto.storeOrderDetailDtoList.length ;
                    newCell7.innerHTML = '<div class="form-check form-switch center-checkbox"><input name="cooked" value="Y" data-order-id="'+storeOrderDto.orderId+'" class="form-check-input" type="checkbox" role="switch"'+displayCheckBox+'></div>';
                }

                prevOrderId = storeOrderDto.orderId ;
            }) ;
        }) ;
    }

    addEventToCheckBox(document.querySelectorAll("input[name='OrderAccepted']")) ;
    addEventToCheckBox(document.querySelectorAll("input[name='OrderRejected']")) ;
    addEventToCheckBox(document.querySelectorAll("input[name='cooked']")) ;
}

function addEventToCheckBox(checkboxes) {
    let displayStatus = null ;
    let status = null ;

    checkboxes.forEach(checkbox => {
        checkbox.addEventListener("change", function (event) {
            // this는 INPUT 객체임
            if ( this.name == "OrderAccepted" ) {
                displayStatus = OrderStatus["ORDER_ACCEPTED"] ;
                status = "ORDER_ACCEPTED" ;
            } else if ( this.name == "cooked" ) {
                displayStatus = OrderStatus["COOKED"] ;
                status = "COOKED" ;
            } else if ( this.name == "OrderRejected" ) {
                displayStatus = OrderStatus["ORDER_REJECTED"]
                status = "ORDER_REJECTED" ;
            }
            if (!this.checked) {
                showToast("⚠️ 이미 "+displayStatus+"된 건입니다.") ;
                this.checked = true ;
                return false ;
            }
            const orderId = this.dataset.orderId; // 주문 ID 가져오기 (예: data-order-id)
            if ( !confirm("주문번호["+orderId+"] : "+displayStatus+" 처리하시겠습니까?\n"+displayStatus+" 처리 시 취소할 수 없습니다.") ) {
                // confirm 취소 시 checked를 false로 둔다.
                this.checked = false ;
                return false ;
            }

            // fetch 응답 성공 시 까지는 checked를 false로 둔다.
            this.checked = false ;

            // 서버로 전송할 데이터
            const url = "/store-service/api/updateOrderStatus" ;
            const params = {
                method: 'POST',
                credentials: 'include',
                headers: {
                    'Content-Type': 'application/json', // 데이터 형식 설정
                },
                redirect: "follow", // follow, error, or manual
                body: JSON.stringify({
                    "orderId": orderId,
                    'status': status
                })
            } ;

            // 서버로 데이터 전송 (fetch API)
            callFetchApi(url, null, params, "successfullyUpdated") ;
        });
    });
}

// 가맹점주가 "Cooked", "ORDER_REJECTED" 처리했을 시 응답처리
function successfullyUpdated(json) {
    const orderId = json.data.orderId ;
    const status = json.data.status ;
    const displayStatus = OrderStatus[status];
    let selectedCheckBox = null ;

    if ( status == "ORDER_ACCEPTED" ) {
        document.querySelector('input[name="OrderAccepted"][data-order-id="'+orderId+'"]').checked = true ;
        document.querySelector('input[name="OrderRejected"][data-order-id="'+orderId+'"]').disabled = true ;
        document.querySelector('input[name="cooked"][data-order-id="'+orderId+'"]').disabled = false ;
        // 접수시간 셀 찾아서 innerText 업데이트
        const cookedDtCell = document.getElementById("cookedDt_" + json.data.storeOrderId);
        if (cookedDtCell) {
            // 서버에서 받아온 updateDt를 포맷팅하여 입력
            cookedDtCell.innerText = "(접수) "+dateTimeFormatter(json.data.updateDt);
        }
    } else if ( status == "ORDER_REJECTED" ) {
        selectedCheckBox = document.querySelector('input[name="OrderRejected"][data-order-id="'+orderId+'"]');
        selectedCheckBox.checked = true ;
        document.querySelector('input[name="OrderAccepted"][data-order-id="'+orderId+'"]').checked = false ;
        document.querySelector('input[name="OrderAccepted"][data-order-id="'+orderId+'"]').disabled = true ;
        document.querySelector('input[name="cooked"][data-order-id="'+orderId+'"]').disabled = true ;
        // 주문거절시간 셀 찾아서 innerText 업데이트
        const cookedDtCell = document.getElementById("cookedDt_" + json.data.storeOrderId);
        if (cookedDtCell) {
            // 서버에서 받아온 updateDt를 포맷팅하여 입력
            cookedDtCell.innerText = "(거절) "+dateTimeFormatter(json.data.updateDt);
        }
    } else if ( status == "COOKED" ) {
        selectedCheckBox = document.querySelector('input[name="cooked"][data-order-id="'+orderId+'"]');
        selectedCheckBox.checked = true ;
        document.querySelector('input[name="OrderRejected"][data-order-id="'+orderId+'"]').disabled = true ;
        // 조리완료시간 셀 찾아서 innerText 업데이트
        const cookedDtCell = document.getElementById("cookedDt_" + json.data.storeOrderId);
        if (cookedDtCell) {
            // 서버에서 받아온 updateDt를 포맷팅하여 입력
            cookedDtCell.innerText = "(완료) "+dateTimeFormatter(json.data.updateDt);
        }
    }

    // 👇 함수 맨 마지막에 아래 코드를 추가하세요.
    // 서버 처리가 정상적으로 끝난 후, 이벤트 발생기 모달이 "열려있다면" 닫아줍니다.
    const modalEl = document.getElementById("kafkaTestModal");
    if (modalEl && modalEl.classList.contains('show')) {
        // new 키워드 대신 기존 인스턴스를 가져와서 안전하게 닫기 (오작동 방지)
        const kafkaTestModal = bootstrap.Modal.getInstance(modalEl);
        if (kafkaTestModal) {
            kafkaTestModal.hide();
        }
    }
}

function publishKafkaClicked(event) {
    const kafkaOrderId = document.getElementById("kafkaOrderId") ;
    const kafkaStatus = document.getElementById("kafkaStatus") ;

    let valid = true;
    kafkaOrderId.classList.remove("ui-state-error");
    kafkaStatus.classList.remove("ui-state-error");

    valid = valid && checkLength( kafkaOrderId, "주문번호", 1, 5 );
    valid = valid && checkSelect( kafkaStatus, "상태(STATUS)" ) ;

    if ( !valid ) {
        return ;
    }

    // 서버로 전송할 데이터
    const url = "/store-service/api/updateOrderStatus" ;
    const params = {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json', // 데이터 형식 설정
        },
        redirect: "follow", // follow, error, or manual
        body: JSON.stringify({
            "orderId": kafkaOrderId.value,
            'status': kafkaStatus.value,
            'forTest': 'SKIP_STATUS_CHECK'
        })
    } ;

    // 서버로 데이터 전송 (fetch API)
    callFetchApi(url, null, params, "successfullyUpdated") ;
}

// 가맹점주 화면 그리기
// 테이블 컨테이너 (tableContainer) 초기화
function clearTableContainer() {
    // 테이블 컨테이너 (기존 테이블이 있을 경우 제거)
    let container = document.getElementById("tableContainer");
    container.innerHTML = ""; // 기존 테이블 내용 제거 후 새로 생성
}

// 가맹점주 화면 ("Cooked" 처리) 그리기
function createStoreAdminTable(storeId, storeNm, openYN) {
    // 테이블 컨테이너 (기존 테이블이 있을 경우 제거)
    let container = document.getElementById("tableContainer");

    // 기존 span이 있다면 삭제 (중복 방지)
    const existingSpan = document.getElementById("divTblStoreAdminTitle" + storeId);
    if (existingSpan) {
        existingSpan.remove();
    }

    // <span> 요소 생성
    let span = document.createElement("span");
    span.id = "divTblStoreAdminTitle" + storeId; // 동적 ID 설정
    span.innerText = `[${storeNm} - ${openYN == 1 ? '영업중' : '휴업중'}]`;   // 텍스트 설정

    // <div>에 <span> 추가
    container.appendChild(span);

    // <table> 요소 생성
    let table = document.createElement("table");
    table.id = "tblStoreAdmin" + storeId;
    table.classList.add("table", "table-striped", "table-bordered");
//    table.style.maxHeight = "150px";
    table.style.overflowY = "scroll";

    // <thead> 생성
    let thead = document.createElement("thead");
    let headerRow = document.createElement("tr");
    headerRow.classList.add("table-warning");

    // 테이블 헤더 컬럼 데이터
    const headers = [
        { text: "주문번호", width: "20px" },
        { text: "주문메뉴", width: "70px" },
        { text: "수량", width: "20px" },
        { text: "접수시간", width: "80px" },
        { text: "조리완료시간", width: "80px" },
        { text: "주문접수", width: "30px" },
        { text: "주문거절", width: "30px" },
        { text: "조리완료", width: "30px" }
    ];

    // <th> 요소 동적 생성
    headers.forEach(header => {
        let th = document.createElement("th");
        th.classList.add("cssAlignCenter");
        th.style.width = header.width;
        th.innerText = header.text;
        headerRow.appendChild(th);
    });

    // <thead>에 <tr> 추가
    thead.appendChild(headerRow);
    table.appendChild(thead);

    // <tbody> 추가 (데이터가 추가될 영역)
    let tbody = document.createElement("tbody");
    table.appendChild(tbody);

    // 컨테이너에 테이블 추가
    container.appendChild(table);
}
