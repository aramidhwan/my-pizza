(() => {
    loadDeliveryAdmin() ;
})()

function loadDeliveryAdmin() {
    const url = '/delivery-service/api/getDeliveryAdmin' ;
    const params = {
      method: 'GET',
      credentials: 'include',
      redirect: "follow" // follow, error, or manual
    };

    callFetchApi(url, null, params, "displayDeliveryAdmin") ;
}

function displayDeliveryAdmin(json) {
    let deliveryDto = null ;
    let cellIndex = 0 ;
    let prevStoreId = null ;
    let tbody = null; // 🔹 tbody 변수를 for 루프 밖에서 선언

    // 테이블 동적 생성 함수 실행
    createDeliveryAdminTable();

    // tbody 동적 생성
    const tableDelivery = document.getElementById("tblDeliveryAdmin");
    tbody = tableDelivery.querySelector("tbody"); // tbody 요소를 선택
    tbody.innerHTML = ""; // tbody 내 모든 내용을 삭제

    // 오늘 주문 접수 내역이 없을 경우
    if ( json.data.length == 0 ) {
        var newRow   = tbody.insertRow(tbody.rows.length);
        var newCell1 = newRow.insertCell(0);    // 주문번호
        newCell1.classList.add("cssAlignCenter") ;
        newCell1.colSpan = 6 ;
        newCell1.innerText = "오늘 배달 접수 내역이 없습니다." ;
        return ;
    }

    // col rowSpan을 위한 선 loop 처리
    const storeIdMap = new Map();
    for (let inx = 0; inx < json.data.length; inx++) {
        deliveryDto = json.data[inx] ;
        if ( storeIdMap.has(deliveryDto.storeDto.storeId) ) {
            storeIdMap.set(deliveryDto.storeDto.storeId, storeIdMap.get(deliveryDto.storeDto.storeId)+1) ;
        } else {
            storeIdMap.set(deliveryDto.storeDto.storeId, 1) ;
        }
    }

    for (let inx = 0; inx < json.data.length; inx++) {
        deliveryDto = json.data[inx] ;
        cellIndex = 0 ;
        let chkboxForDeliveryStarted = "" ;
        let chkboxForDelivered = "" ;

        // 상태가 "배달접수" 일때
        if ( deliveryDto.status == "DELIVERY_ACCEPTED" && OrderStatus.hasOwnProperty(deliveryDto.status) ) {
            chkboxForDeliveryStarted = "" ;
            chkboxForDelivered = " disabled" ;
        // 상태가 "배달시작" 일때
        } else if ( deliveryDto.status == "DELIVERY_STARTED" && OrderStatus.hasOwnProperty(deliveryDto.status) ) {
            chkboxForDeliveryStarted = " checked" ;
            chkboxForDelivered = "" ;
        } else {
            chkboxForDeliveryStarted = " checked" ;
            chkboxForDelivered = " checked" ;
        }

        // 신규 Row 생성
        var newRow = tbody.insertRow();

        if ( deliveryDto.storeDto.storeId != prevStoreId ) {
            var newCell1 = newRow.insertCell(cellIndex++);    // 상점명
            newCell1.classList.add("cssAlignCenter") ;
            newCell1.rowSpan = storeIdMap.get(deliveryDto.storeDto.storeId) ;
            newCell1.innerText = deliveryDto.storeDto.storeNm ;
        }
        var newCell2 = newRow.insertCell(cellIndex++);    // 주문번호
        newCell2.classList.add("cssAlignCenter") ;
        newCell2.innerText = deliveryDto.orderId ;

        var newCell3 = newRow.insertCell(cellIndex++);    // 상태 STATUS
        newCell3.classList.add("cssAlignCenter") ;
        newCell3.id = "status" ;
        newCell3.innerText = OrderStatus[deliveryDto.status] || "알 수 없음" ;

        var newCell4 = newRow.insertCell(cellIndex++);    // 접수시간
        newCell4.classList.add("cssAlignCenter") ;
        newCell4.innerText = dateTimeFormatter(deliveryDto.createDt) ;

        var newCell5 = newRow.insertCell(cellIndex++);    // 체크박스
        newCell5.classList.add("cssAlignCenter") ;
        newCell5.innerHTML = '<div class="form-check form-switch center-checkbox"><input name="deliveryStarted" data-order-id="'+deliveryDto.orderId+'" class="form-check-input" type="checkbox" role="switch"'+chkboxForDeliveryStarted+'></div>';

        var newCell6 = newRow.insertCell(cellIndex++);    // 체크박스
        newCell6.classList.add("cssAlignCenter") ;
        newCell6.innerHTML = '<div class="form-check form-switch center-checkbox"><input name="delivered" data-order-id="'+deliveryDto.orderId+'" class="form-check-input" type="checkbox" role="switch"'+chkboxForDelivered+'></div>';

        prevStoreId = deliveryDto.storeDto.storeId ;
    }

    addEventCheckBox(document.querySelectorAll("input[name='deliveryStarted']")) ;
    addEventCheckBox(document.querySelectorAll("input[name='delivered']")) ;

}

function addEventCheckBox(checkboxes) {
    let displayStatus = null ;
    let status = null ;
    checkboxes.forEach(checkbox => {
        checkbox.addEventListener("change", function (event) {
            // this는 INPUT 객체임
            if ( this.name == "deliveryStarted" ) {
                displayStatus = OrderStatus["DELIVERY_STARTED"] ;
                status = "DELIVERY_STARTED" ;
            } else if ( this.name == "delivered" ) {
                displayStatus = OrderStatus["DELIVERED"]
                status = "DELIVERED" ;
            }
            if (!this.checked) {
                alert("이미 "+displayStatus+"된 건입니다.\n상태를 되돌릴 수 없습니다.") ;
                this.checked = true ;
                return false ;
            }
            const orderId = this.dataset.orderId; // 주문 ID 가져오기 (예: data-order-id)
            if ( !confirm("주문번호["+orderId+"] : "+displayStatus+" 처리하시겠습니까?\n"+displayStatus+" 처리 시 취소할 수 없습니다.") ) {
                this.checked = false ;
                return false ;
            }

            // fetch 응답 성공 시 까지는 checked를 false로 둔다.
            this.checked = false ;

            // 서버로 전송할 데이터
            const url = "/delivery-service/api/updateOrderStatus" ;
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

function successfullyUpdated(json) {
    const orderId = json.data.orderId ;
    const status = json.data.status ;
    const displayStatus = OrderStatus[status];
    let selectedCheckBox = null ;

    if ( status == "DELIVERY_STARTED" ) {
        selectedCheckBox = document.querySelector('input[name="deliveryStarted"][data-order-id="'+orderId+'"]');
        document.querySelector('input[name="delivered"][data-order-id="'+orderId+'"]').disabled = false ;
    } else if ( status == "DELIVERED" ) {
        selectedCheckBox = document.querySelector('input[name="delivered"][data-order-id="'+orderId+'"]');
    }

    selectedCheckBox.checked = true ;
    selectedCheckBox.closest("tr").querySelector("td#status").innerText = displayStatus ; // 상태(STATUS) td 객체
//    selectedCheckBox.closest("div").innerHTML = "<span class='cssAlignCenter'>배달시작</span>"; // 가장 가까운 div 찾기
//    selectedCheckBox.closest("div").innerHTML = "<span class='cssAlignCenter' style='display: flex; justify-content: center; align-items: center; width: 100%; height: 100%;'>배달시작</span>";
}

function createDeliveryAdminTable() {
    // 테이블 컨테이너 (기존 테이블이 있을 경우 제거)
    let container = document.getElementById("tableContainer");
//    container.innerHTML = ""; // 기존 테이블 제거 후 새로 생성

    // 기존 span이 없다면 삭제 (중복 방지)
    let span = document.getElementById("divTblDeliveryAdminTitle");
    if (!span) {
        // Title 요소 생성
        span = document.createElement("span");
        span.id = "divTblDeliveryAdminTitle" ; // 동적 ID 설정
        span.innerText = '[배달점]'// `[${deliveryNm}]`; // 텍스트 설정
    }

    // <div>에 <span> 추가
    container.appendChild(span);

    // <table> 요소 생성
    let table = document.createElement("table");
    table.id = "tblDeliveryAdmin" ;
    table.classList.add("table", "table-striped", "table-bordered");
    table.style.maxHeight = "150px";
    table.style.overflowY = "scroll";

    // <thead> 생성
    let thead = document.createElement("thead");
    let headerRow = document.createElement("tr");
    headerRow.classList.add("table-warning");

    // 테이블 헤더 컬럼 데이터
    const headers = [
        { text: "상점명", width: "60px" },
        { text: "주문번호", width: "50px" },
        { text: "상태", width: "90px" },
        { text: "접수시간", width: "100px" },
        { text: "배달시작", width: "30px" },
        { text: "배달완료", width: "30px" }
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
