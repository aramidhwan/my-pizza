(() => {
    loadStoreAdmin() ;
})()

function loadStoreAdmin() {
    const url = '/store-service/api/getStoreAdmin' ;
    const params = {
      method: 'GET',
      credentials: 'include',
      redirect: "follow" // follow, error, or manual
    };

    callFetchApi(url, null, params, "displayStoreAdmin") ;
}

function displayStoreAdmin(json) {
    let storeDto = null ;
    let storeOrderDto = null ;
    let storeOrderDetailDto = null ;
    let cellIndex = 0 ;
    let prevOrderId = null ;
    let tbody = null; // 🔹 tbody 변수를 for 루프 밖에서 선언

    // [강남1호점]
    for (let inx = 0; inx < json.data.length; inx++) {
        storeDto = json.data[inx] ;

        // 테이블 동적 생성 함수 실행
        createStoreAdminTable(storeDto.storeId, storeDto.storeNm);
        const tblStore = document.getElementById("tblStoreAdmin"+storeDto.storeId);
        tbody = tblStore.querySelector("tbody"); // tbody 요소를 선택
        //tbody.innerHTML = ""; // tbody 내 모든 내용을 삭제

        // 오늘 주문 접수 내역이 없을 경우
        if ( storeDto.storeOrderDtos.length == 0 ) {
            var newRow   = tbody.insertRow(tbody.rows.length);
            var newCell1 = newRow.insertCell(0);    // 주문번호
            newCell1.classList.add("cssAlignCenter") ;
            newCell1.colSpan = tblStore.querySelectorAll("th").length; ; ;
            newCell1.innerText = "오늘 주문 접수 내역이 없습니다." ;
            continue ; // 주문이 없는 경우 다음 반복문 실행
        }

        storeDto.storeOrderDtos.forEach((storeOrderDto) => {
            storeOrderDto.storeOrderDetailDtos.forEach((storeOrderDetailDto) => {
                var newRow = tbody.insertRow(tbody.rows.length);
                cellIndex = 0 ;
                let displayCheckBox = "" ;

                if ( storeOrderDetailDto.orderId != prevOrderId ) {
                    var newCell1 = newRow.insertCell(cellIndex++);    // 주문번호
                    newCell1.classList.add("cssAlignCenter") ;
                    newCell1.rowSpan = storeOrderDto.storeOrderDetailDtos.length ;
                    newCell1.innerText = storeOrderDetailDto.orderId ;
                }

                var newCell2 = newRow.insertCell(cellIndex++);    // 주문메뉴
                newCell2.classList.add("cssAlignCenter") ;
                newCell2.innerText = storeOrderDetailDto.itemNm ;

                var newCell3 = newRow.insertCell(cellIndex++);    // 주문메뉴
                newCell3.classList.add("cssAlignCenter") ;
                newCell3.innerText = storeOrderDetailDto.qty ;

                if ( storeOrderDetailDto.orderId != prevOrderId ) {
                    if ( storeOrderDto.status == "ORDER_ACCEPTED" && OrderStatus.hasOwnProperty(storeOrderDto.status) ) {
                        displayCheckBox = "" ;
                    } else {
                        displayCheckBox = " checked" ;
                    }

                    var newCell4 = newRow.insertCell(cellIndex++);    // 주문메뉴
                    newCell4.classList.add("cssAlignCenter") ;
                    newCell4.rowSpan = storeOrderDto.storeOrderDetailDtos.length ;
                    newCell4.innerHTML = '<div class="form-check form-switch center-checkbox"><input name="cooked" value="Y" data-order-id="'+storeOrderDto.orderId+'" class="form-check-input" type="checkbox" role="switch"'+displayCheckBox+'></div>';
                }

                prevOrderId = storeOrderDetailDto.orderId ;
            }) ;
        }) ;
    }

    addEventToCheckBox() ;
}

function addEventToCheckBox() {
    // 모든 체크박스 선택
    const checkboxes = document.querySelectorAll("input[name='cooked']");

    checkboxes.forEach(checkbox => {
        checkbox.addEventListener("change", function (event) {
            if (!this.checked) {
                alert("이미 조리완료된 건입니다.\n상태를 되돌릴 수 없습니다.") ;
                this.checked = true ;
                return false ;
            }
            const orderId = this.dataset.orderId; // 주문 ID 가져오기 (예: data-order-id)
            if ( !confirm("주문번호["+orderId+"] : 조리완료 처리하시겠습니까?\n조리완료 처리 시 취소할 수 없습니다.") ) {
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
                    'status': "COOKED"
                })
            } ;

            // 서버로 데이터 전송 (fetch API)
            callFetchApi(url, null, params, "successfullyUpdated") ;
        });
    });
}

function successfullyUpdated(json) {
    const selectedCheckBox = document.querySelector('input[name="cooked"][data-order-id="'+json.data.orderId+'"]');
    selectedCheckBox.checked = true ;
}

function createStoreAdminTable(storeId, storeNm) {
    // 테이블 컨테이너 (기존 테이블이 있을 경우 제거)
    let container = document.getElementById("tableContainer");
//    container.innerHTML = ""; // 기존 테이블 제거 후 새로 생성

    // 기존 span이 있다면 삭제 (중복 방지)
    const existingSpan = document.getElementById("divTblStoreAdminTitle" + storeId);
    if (existingSpan) {
        existingSpan.remove();
    }

    // <span> 요소 생성
    let span = document.createElement("span");
    span.id = "divTblStoreAdminTitle" + storeId; // 동적 ID 설정
    span.innerText = `[${storeNm}]`; // 텍스트 설정

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
        { text: "주문번호", width: "70px" },
        { text: "주문메뉴", width: "180px" },
        { text: "수량", width: "70px" },
        { text: "조리완료", width: "70px" }
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
