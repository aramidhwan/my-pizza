(() => {
    // "OrderReject" 상태일 시 팝업 설명
    const popoverTriggerList = document.querySelectorAll('[data-bs-toggle="popover"]')
    const popoverList = [...popoverTriggerList].map(popoverTriggerEl => new bootstrap.Popover(popoverTriggerEl))

    loadMyOrder(/*[[${viewType}]]*/ "onlyToday") ;
})()

// 함수 노출: 전역에서 호출 가능하도록 window 객체에 할당
function loadMyOrder(viewType) {
    const url = '/mypage-service/api/getMyOrders' ;
    const params = {
      method: 'GET',
      credentials: 'include',
      redirect: "follow" // follow, error, or manual
    };
    const requestParams = {
        viewType: viewType
    };
    // URLSearchParams를 사용해 파라미터를 쿼리 스트링 형식으로 변환
    const queryString = new URLSearchParams(requestParams).toString();

    callFetchApi(url, queryString, params, "displayMyOrder") ;
}

function displayMyOrder(json) {
    let cellIndex = 0 ;
    let prevStoreId = "" ;
    const imgViewType   = document.getElementById("imgViewType");
    const aViewType     = document.getElementById("aViewType");

    if (json.data.viewType == "onlyToday") {
        imgViewType.src = '/mypage-service/images/myPageMain2.jpg';
        aViewType.setAttribute('onClick', 'loadMyOrder("all");') ;
    } else if (json.data.viewType == "all") {
        imgViewType.src = '/mypage-service/images/myPageMain1.jpg';
        aViewType.setAttribute('onClick', 'loadMyOrder("onlyToday");') ;
    } else {
        alert("도데체 뭐야? json.data.viewType : "+json.data.viewType) ;
    }

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
        tbody.innerHTML = ""; // tbody 내 모든 내용을 삭제

        myPageDto.myPageOrderDetailDtos.forEach((myPageOrderDetailDto, inx) => {
            // 선택된 메뉴 내용 표시
            var newRow = tbody.insertRow();
            cellIndex = 0 ;

            if ( inx == 0 ) {
                var newCell1 = newRow.insertCell(cellIndex++);    // 주문번호
                newCell1.classList.add("cssAlignCenter") ;
                newCell1.rowSpan = rowSpan ;
                newCell1.innerText = myPageDto.orderId ;

//                var newCell2 = newRow.insertCell(cellIndex++);    // 배정상점
//                newCell2.classList.add("cssAlignCenter") ;
//                newCell2.rowSpan = rowSpan ;
//                newCell2.innerText = myPageDto.storeNm ;
            }

            var newCell3 = newRow.insertCell(cellIndex++);    // 주문메뉴
            newCell3.classList.add("cssAlignCenter") ;
            newCell3.innerText = myPageOrderDetailDto.itemNm ;

            var newCell4 = newRow.insertCell(cellIndex++);    // 수량
            newCell4.classList.add("cssAlignCenter") ;
            newCell4.innerText = myPageOrderDetailDto.qty ;

            if ( inx == 0 ) {
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
    span.innerText = (storeNm!="")? `[${storeNm}]`:(status=="ORDER_REJECTED")? '['+OrderStatus[status]+']':''; // 텍스트 설정

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
