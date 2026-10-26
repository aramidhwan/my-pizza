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

    loadMyOrder() ;
})()
// layout.html의 일부 영역에 동적으로 삽입되기 때문에, DOMContentLoaded 이벤트는 이미 발생한 후입니다.
// 따라서 document.addEventListener("DOMContentLoaded", ...)는 절대 호출되지 않아요.
// document.addEventListener("DOMContentLoaded", function () {
// });

// 함수 노출: 전역에서 호출 가능하도록 window 객체에 할당
function loadMyOrder() {
    const url = '/mypage-service/api/getMyOrders' ;
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

    callFetchApi(url, queryString, params, "displayMyOrder") ;
}

function displayMyOrder(json) {
    let cellIndex = 0 ;
    let prevStoreId = "" ;

    // 기존 (테이블 컨테이너) 내용 삭제
    clearTableContainer() ;

    // 주문 내역이 없을 경우
    if ( json.data.myPageDtos.length == 0 ) {
        createMyOrderTable("0", "" ,"") ;
        var tblMyOrders = document.getElementById("tblMyOrders" + "0");
        var tbody = tblMyOrders.querySelector("tbody"); // tbody 요소를 선택
        tbody.innerHTML = ""; // tbody 내 모든 내용을 삭제

        var newRow = tbody.insertRow();
        var newCell1 = newRow.insertCell(0);    //
        newCell1.classList.add("cssAlignCenter") ;
        newCell1.colSpan = tblMyOrders.querySelectorAll("th").length; ;
        newCell1.innerText = "주문하신 내역이 없습니다." ;
        return ;
    }

    // 주문 내역이 있을 경우
    for (let inx = 0; inx < json.data.myPageDtos.length; inx++) {
        const myPageDto = json.data.myPageDtos[inx];
        const rowSpan = myPageDto.myPageOrderDetailDtoList.length ;

        if ( myPageDto.storeId != prevStoreId ) {
            createMyOrderTable(myPageDto.storeId, myPageDto.storeNm, myPageDto.status) ;
        }

        var tblMyOrders = document.getElementById("tblMyOrders" + myPageDto.storeId);
        var tbody = tblMyOrders.querySelector("tbody"); // tbody 요소를 선택
//        tbody.innerHTML = ""; // tbody 내 모든 내용을 삭제

        myPageDto.myPageOrderDetailDtoList.forEach((myPageOrderDetailDto, iny) => {
            // 선택된 메뉴 내용 표시
            var newRow = tbody.insertRow();
            var newCell = null ;
            cellIndex = 0 ;

            if ( iny == 0 ) {
                newCell = newRow.insertCell(cellIndex++);    // 주문번호
                newCell.classList.add("cssAlignCenter") ;
                newCell.rowSpan = rowSpan ;
                newCell.innerText = myPageDto.orderId ;
            }

            newCell = newRow.insertCell(cellIndex++);    // 주문메뉴
            newCell.classList.add("cssAlignCenter") ;
            newCell.innerText = myPageOrderDetailDto.itemNm ;

            newCell = newRow.insertCell(cellIndex++);    // 수량
            newCell.classList.add("cssAlignCenter") ;
            newCell.innerText = myPageOrderDetailDto.qty ;

            if ( iny == 0 ) {
                newCell = newRow.insertCell(cellIndex++);    // 주문자
                newCell.classList.add("cssAlignCenter") ;
                newCell.rowSpan = rowSpan ;
                newCell.innerText = myPageDto.customerName ;

                newCell = newRow.insertCell(cellIndex++);    // 처리상태
                newCell.classList.add("cssAlignCenter") ;
                newCell.rowSpan = rowSpan ;

                // 동적으로 생성되는 처리상태 문자열을 팝오버 대상 요소 안에 표시 xxx
                const popoverSpan = document.createElement("span");         // 처리상태 팝오버
                // 👉 식별을 위한 속성 추가 (예: orderId 활용, dataset.orderId → data-order-id 로 찾을 수 있음)
                // 또는 id를 동적으로 부여: popoverSpan.id = "popover-status-" + myPageDto.orderId;
                popoverSpan.dataset.orderId = myPageDto.orderId;
                popoverSpan.innerText = OrderStatus[myPageDto.status] || "알 수 없음";
                popoverSpan.setAttribute("data-bs-toggle", "popover");
                popoverSpan.setAttribute("data-bs-trigger", "hover focus");
                popoverSpan.setAttribute("data-bs-placement", "top");
                popoverSpan.setAttribute("data-bs-custom-class", "custom-popover");
                popoverSpan.setAttribute("data-bs-title", "상태 이력");
                popoverSpan.setAttribute("data-bs-content", myPageDto.statusInfo || "추가 정보가 없습니다.");
                popoverSpan.style.cursor = "help";
                popoverSpan.style.textDecoration = "underline dotted";
                popoverSpan.style.textUnderlineOffset = "4px";
                popoverSpan.tabIndex = 0;
                newCell.appendChild(popoverSpan);
                // 동적 생성 요소이므로 DOM에 추가한 직후 개별 초기화
                new bootstrap.Popover(popoverSpan);

                newCell = newRow.insertCell(cellIndex++);    // 총주문가격
                newCell.classList.add("cssAlignCenter") ;
                newCell.rowSpan = rowSpan ;
                newCell.innerText =
                    myPageDto.totalPrice != null
                        ? Number(myPageDto.totalPrice).toLocaleString("ko-KR") + " 원"
                        : "-";

                newCell = newRow.insertCell(cellIndex++);    // 주문시간
                newCell.classList.add("cssAlignCenter") ;
                newCell.rowSpan = rowSpan ;
                newCell.innerText = dateTimeFormatter(myPageDto.createDt) ;

                newCell = newRow.insertCell(cellIndex++);    // 현상태시간
                newCell.id = "updateDt_" + myPageDto.orderId ; // 👈 현상태시간 셀에 고유 ID 부여!
                newCell.classList.add("cssAlignCenter") ;
                newCell.rowSpan = rowSpan ;
                if ( myPageDto.status == "ORDER_CANCELLED" ) {
                    newCell.innerText = "(취소) " + dateTimeFormatter(myPageDto.updateDt) ;
                } else {
                    newCell.innerText = dateTimeFormatter(myPageDto.updateDt) ;
                }

                newCell = newRow.insertCell(cellIndex++);    // 주문취소 체크박스
                newCell.classList.add("cssAlignCenter") ;
                newCell.rowSpan = rowSpan ;
                // 주문취소 체크박스 처리
                if ( myPageDto.status == "ORDERED" && OrderStatus.hasOwnProperty(myPageDto.status) ) {
                    displayCheckBox = "" ;
                } else if ( myPageDto.status == "ORDER_CANCELLED" && OrderStatus.hasOwnProperty(myPageDto.status) ) {
                    displayCheckBox = " checked" ;
                } else {
                    displayCheckBox = " disabled" ;
                }
                newCell.innerHTML = '<div class="form-check form-switch center-checkbox"><input name="OrderCancelled" value="Y" data-order-id="'+myPageDto.orderId+'" class="form-check-input" type="checkbox" role="switch"'+displayCheckBox+'></div>';
            }
        }) ;

        prevStoreId = myPageDto.storeId ;
    }

    addEventToCheckBox(document.querySelectorAll("input[name='OrderCancelled']")) ;
}

function addEventToCheckBox(checkboxes) {
    let displayStatus = null ;
    let status = null ;

    checkboxes.forEach(checkbox => {
        checkbox.addEventListener("change", function (event) {
            // this는 INPUT 객체임
            if ( this.name == "OrderCancelled" ) {
                displayStatus = OrderStatus["ORDER_CANCELLED"] ;
                status = "ORDER_CANCELLED" ;
            } else {
                alert("⚠️ 알수 없는 상태! " + this.name) ;
                return false ;
            }
            if (!this.checked) {
                showToast("⚠️ 이미 "+displayStatus+"된 건입니다.") ;
                this.checked = true ;
                return false ;
            }
            const orderId = this.dataset.orderId; // 주문 ID 가져오기 (예: dataset.orderId → data-order-id)
            if ( !confirm("주문번호["+orderId+"] : "+displayStatus+" 처리하시겠습니까?\n"+displayStatus+" 처리 시 취소할 수 없습니다.") ) {
                // confirm 취소 시 checked를 false로 둔다.
                this.checked = false ;
                return false ;
            }

            // fetch 응답 성공 시 까지는 checked를 false로 둔다.
            this.checked = false ;

            // 서버로 전송할 데이터
            const url = "/mypage-service/api/updateOrderStatus" ;
//            const params = {
//                method: 'POST',
//                credentials: 'include',
//                headers: {
//                    'Content-Type': 'application/json', // 데이터 형식 설정
//                },
//                redirect: "follow", // follow, error, or manual
//                body: JSON.stringify({
//                    "orderId": orderId,
//                    'status': status
//                })
//            } ;
            const queryString = new URLSearchParams({
                orderId: orderId,
                status: status
            }).toString();

            const params = {
                method: "POST",
                credentials: "include",
                redirect: "follow"
            };

            // 서버로 데이터 전송 (fetch API)
            callFetchApi(url, queryString, params, "successfullyUpdated") ;
        });
    });
}

// "ORDER_CANCELLED" 처리했을 시 응답처리
function successfullyUpdated(json) {
    const orderId = json.data.orderId ;
    const status = json.data.status ;
    const statusInfo = json.data.statusInfo ;
    const displayStatus = OrderStatus[status];
    let selectedCheckBox = null ;

    if ( status == "ORDER_CANCELLED" ) {
        document.querySelector('input[name="OrderCancelled"][data-order-id="'+orderId+'"]').checked = true ;
        // 처리상태 셀 찾아서 innerText 업데이트
        // 1. orderId를 이용해 해당 popoverSpan 객체 찾기
        var targetSpan = document.querySelector(`span[data-order-id="${orderId}"]`);

        if (!targetSpan) {
            alert("⚠️ 해당 주문의 처리상태 Cell을 찾을 수 없습니다. orderId:", orderId);
            return;
        }

        // 2. innerText 변경
        targetSpan.innerText = OrderStatus[status] || "알 수 없음";
        targetSpan.setAttribute("data-bs-content", statusInfo);

        // 3. Bootstrap Popover 인스턴스 갱신
        var popoverInstance = bootstrap.Popover.getInstance(targetSpan);
        if (popoverInstance) {
            popoverInstance.setContent({
                '.popover-body': statusInfo
            });
        }

        // 4. 현상태시간 업데이트
        const updateDtCell = document.getElementById("updateDt_" + orderId);
        if (updateDtCell) {
            // 서버에서 받아온 updateDt를 포맷팅하여 입력
            updateDtCell.innerText = "(취소) "+dateTimeFormatter(json.data.updateDt);
        }
    } else {
        document.querySelector('input[name="OrderCancelled"][data-order-id="${orderId}"]').disabled = true ;
    }
}

// mypage 화면 그리기
// 테이블 컨테이너 (tableContainer) 초기화
function clearTableContainer() {
    // 테이블 컨테이너 (기존 테이블이 있을 경우 제거)
    let container = document.getElementById("tableContainer");
    container.innerHTML = ""; // 기존 테이블 제거 후 새로 생성
}

function createMyOrderTable(storeId, storeNm, status) {
    // 테이블 컨테이너 (기존 테이블이 있을 경우 제거)
    let container = document.getElementById("tableContainer");

    // 기존 span이 있다면 삭제 (중복 방지)
    const existingSpan = document.getElementById("divTblMyOrderTitle" + storeId);
    if (existingSpan) {
        existingSpan.remove();
    }

    // <span> 요소 생성 (매장 제목)
    let span = document.createElement("span");
    span.id = "divTblMyOrderTitle" + storeId; // 동적 ID 설정
    const storeNmStr = (storeNm!="")? `[${storeNm}]`:(status=="ORDER_REJECTED")? '['+OrderStatus[status]+']':'' ;
    span.innerHTML = `<strong>${storeNmStr}</strong>` ;
    // <div>에 <span> 추가
    container.appendChild(span);

    // <table> 요소 생성
    let table = document.createElement("table");
    table.id = "tblMyOrders" + storeId;
    table.classList.add("table", "table-striped", "table-bordered");
//    table.style.maxHeight = "150px";
//    table.style.overflowY = "scroll";

    // <thead> 생성
    let thead = document.createElement("thead");
    let headerRow = document.createElement("tr");
    headerRow.classList.add("table-warning");

    // 테이블 헤더 컬럼 데이터
    const headers = [
        { text: "주문번호", width: "80px" },
//        { text: "배정상점", width: "180px" },
        { text: "주문메뉴", width: "200px" },
        { text: "수량", width: "70px" },
        { text: "주문자", width: "100px" },
        { text: "처리상태", width: "180px" },
        { text: "총주문가격", width: "120px" },
        { text: "주문시간", width: "180px" },
        { text: "현상태시간", width: "180px" },
        { text: "주문취소", width: "100px" }
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
