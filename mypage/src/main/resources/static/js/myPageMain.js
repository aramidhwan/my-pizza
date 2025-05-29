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

    // "OrderReject" 상태일 시 팝업 설명
    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]')
    const popoverList = [...popoverTriggerList].map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl))

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

    // 기존 내역 삭제
    clearTableContainer() ;

    // 내역이 없을 경우
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

    for (let inx = 0; inx < json.data.myPageDtos.length; inx++) {
        const myPageDto = json.data.myPageDtos[inx];
        const rowSpan = myPageDto.myPageOrderDetailDtos.length ;

        if ( myPageDto.storeId != prevStoreId ) {
            createMyOrderTable(myPageDto.storeId, myPageDto.storeNm, myPageDto.status) ;
        }

        var tblMyOrders = document.getElementById("tblMyOrders" + myPageDto.storeId);
        var tbody = tblMyOrders.querySelector("tbody"); // tbody 요소를 선택
//        tbody.innerHTML = ""; // tbody 내 모든 내용을 삭제

        myPageDto.myPageOrderDetailDtos.forEach((myPageOrderDetailDto, iny) => {
            // 선택된 메뉴 내용 표시
            var newRow = tbody.insertRow();
            cellIndex = 0 ;

            if ( iny == 0 ) {
                var newCell1 = newRow.insertCell(cellIndex++);    // 주문번호
                newCell1.classList.add("cssAlignCenter") ;
                newCell1.rowSpan = rowSpan ;
                newCell1.innerText = myPageDto.orderId ;
            }

            var newCell3 = newRow.insertCell(cellIndex++);    // 주문메뉴
            newCell3.classList.add("cssAlignCenter") ;
            newCell3.innerText = myPageOrderDetailDto.itemNm ;

            var newCell4 = newRow.insertCell(cellIndex++);    // 수량
            newCell4.classList.add("cssAlignCenter") ;
            newCell4.innerText = myPageOrderDetailDto.qty ;

            if ( iny == 0 ) {
                var newCell5 = newRow.insertCell(cellIndex++);    // 처리상태
                newCell5.classList.add("cssAlignCenter") ;
                newCell5.rowSpan = rowSpan ;
                newCell5.innerText = OrderStatus[myPageDto.status] || "알 수 없음" ;

                var newCell6 = newRow.insertCell(cellIndex++);    // 주문시간
                newCell6.classList.add("cssAlignCenter") ;
                newCell6.rowSpan = rowSpan ;
                newCell6.innerText = dateTimeFormatter(myPageDto.createDt) ;
            }
        }) ;

        prevStoreId = myPageDto.storeId ;
    }
}

function clearTableContainer() {
    // 테이블 컨테이너 (기존 테이블이 있을 경우 제거)
    let container = document.getElementById("tableContainer");
    container.innerHTML = ""; // 기존 테이블 제거 후 새로 생성
}

function createMyOrderTable(storeId, storeNm, status) {
    // 테이블 컨테이너 (기존 테이블이 있을 경우 제거)
    let container = document.getElementById("tableContainer");
//    container.innerHTML = ""; // 기존 테이블 제거 후 새로 생성

    // 기존 span이 있다면 삭제 (중복 방지)
    const existingSpan = document.getElementById("divTblMyOrderTitle" + storeId);
    if (existingSpan) {
        existingSpan.remove();
    }

    // <span> 요소 생성
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
    table.style.overflowY = "scroll";

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
        { text: "처리상태", width: "300px" },
        { text: "주문시간" }
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
