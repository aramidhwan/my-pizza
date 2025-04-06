$( function() {
    // 메뉴 클릭시 이벤트 작성
    if (!window.menuEventAdded) {  // 이미 실행되었는지 체크
        console.log("# menu.addEventListener is called. (SHOULD BE ONCE ONLY!)") ;
        document.getElementById("menu").addEventListener("click", function(event) {
            // 클릭된 요소(event.target)가 <li>인지 확인
            if (event.target.tagName == "LI" || event.target.closest("li")) {
                const liElement = event.target.closest("li"); // 클릭된 <li> 요소 가져오기
                menuClicked(liElement);
            }
        }) ;
        window.menuEventAdded = true;  // 중복 실행 방지 플래그 설정
    }

    // 주문 버튼 클릭시 이벤트
    if (!window.btnOrder) {  // 이미 실행되었는지 체크
        document.getElementById("btnOrder").addEventListener("click", orderClicked) ;
        window.btnOrder = true;  // 중복 실행 방지 플래그 설정
    }

    // 메뉴 그룹(itemGroup) 부분 비활성화
    $( "#menu" ).menu({
        items: "> :not(.ui-widget-header)"
    });

} );

// "삭제" 버튼 클릭 시 호출되는 함수
function deleteSelectedMenu(event) {
    event.stopPropagation();

    // this : 삭제 BUTTON 객체
    const tdCells = this.parentElement.parentElement.children ;     // TD Cells
    let selectedItemPricePerOne = 0 ;
    let selectedItemQty   = 0 ;

    for (let tdCell of tdCells) {       // Cell 개수 만큼만 loop
        if ( tdCell.getAttribute('id') == 'selectedItemPricePerOne' ) {
            selectedItemPricePerOne = tdCell.getAttribute('value') ;
        } else if ( tdCell.getAttribute('id') == 'selectedItemQty' ) {
            selectedItemQty = tdCell.getAttribute('value') ;
        }
    }

    // 총 주문금액 재계산
    calcTotalPrice(selectedItemPricePerOne, selectedItemQty, "MENU_DELETED") ;

    // 선택 메뉴 tr 삭제
    $(this).parent().parent().remove();
}

// "주문" 버튼 클릭시 호출되는 함수
function orderClicked() {
    if ( document.getElementById("signYN").getAttribute("data-value") !== "Y" ) {
        if (confirm("[orderMain.js]\n주문하시려면 로그인이 필요합니다.\n로그인 페이지로 이동하시겠습니까?") ) {
            window.location.href = global_loginPageUrl + "?redirectUrl=/order-service/orderMain" ;
            return false;
        } else {
            // Confirm 취소 : Do Nothing Here!
            return false;
        }
    }
    // 선택된 메뉴들
    const arrayItemIds  = $("tr[id=selectedItemId]").get() ;
    const arrayItemQtys  = $("td[id=selectedItemQty]").get() ;

    if ( arrayItemIds.length == 0 ) {
        alert("⚠️ 주문하실 메뉴를 선택해 주세요.");
        return false;
    }
    if ( !confirm("주문하시겠습니까?") ) {
        return false;
    }

    const arrayOrderedItems = [];

    for ( let inx = 0 ; inx < arrayItemIds.length ; inx++ ) {
        const objOrderedItems = {};
        objOrderedItems.itemId = arrayItemIds[inx].getAttribute('value');
        objOrderedItems.qty    = arrayItemQtys[inx].getAttribute('value');
        arrayOrderedItems.push(objOrderedItems);
    }

    const url = '/order-service/api/createOrder' ;
    const params = {
        method: 'POST',
        credentials: 'include',
        headers: {
            'Content-Type': 'application/json', // 데이터 형식 설정
        },
        redirect: "follow", // follow, error, or manual
        body: JSON.stringify({
            'customerNo': $("#customerNo").attr('value'),
            'regionNm': $("#regionNm").attr('value'),
            'totalPrice': $("#tdTotalPrice").attr('value'),
            'items': arrayOrderedItems
        })
    } ;

    callFetchApi(url, null, params, "loadMyPageMain") ;
}

// "메뉴 아이템" 클릭시 호출되는 함수
function menuClicked(liElement) {
//    console.log("# [menuClicked] is called.") ;
    // "메뉴 그룹" 클릭은 무시!
    if (liElement.getAttribute("id")!='menu_li') {
        return;
    }

    const tableOrdered = document.getElementById("tableOrdered");

    // liElement 는 li 임
    const itemIdString      = liElement.getAttribute("value").split('^')[0];
    const itemNmString      = liElement.getAttribute("value").split('^')[1];
    const pricePerOneString = liElement.getAttribute("value").split('^')[2];

    // 클릭된 메뉴가 기존 주문 목록에 있으면 수량만 증가
    for (let trSelectedMenuInfo of tableOrdered.rows) {
        if ( itemIdString == trSelectedMenuInfo.getAttribute('value') ) {
            // 수량 = 기존 수량 + 1
            trSelectedMenuInfo.cells[2].innerHTML = (parseInt(trSelectedMenuInfo.cells[2].innerHTML)+1) + '개';
            trSelectedMenuInfo.cells[2].setAttribute('value', parseInt(trSelectedMenuInfo.cells[2].innerHTML));

            // 총 주문금액 계산
            calcTotalPrice(pricePerOneString, 0, "MENU_ADDED") ;

            return ;
        }
    }

    // 최대 5개 품목까지만 주문 가능
    if (tableOrdered.rows.length < 7) {
        // 선택된 메뉴 삭제 버튼
        const delButton = document.createElement('button'); // 새로운 button 아이템을 만듭니다.
        delButton.name = "btnDeleteMenu" ;
        delButton.value = itemIdString ;
        delButton.classList.add("btn", "btn-outline-danger"); // Bootstrap 스타일 적용 예시
        delButton.textContent = '삭제' ;

        // 선택된 메뉴 내용 표시
        var newRow = tableOrdered.insertRow(tableOrdered.rows.length-1);
        newRow.setAttribute('id', "selectedItemId") ;
        newRow.setAttribute('value', itemIdString) ;

        var newCell1 = newRow.insertCell(0);    // #
        var newCell2 = newRow.insertCell(1);    // 메뉴명
        var newCell3 = newRow.insertCell(2);    // 수량
        var newCell4 = newRow.insertCell(3);    // 단가(개당)
        var newCell5 = newRow.insertCell(4);    // 삭제버튼
        newCell3.setAttribute('id', "selectedItemQty") ;
        newCell3.setAttribute('value', "1") ;
        newCell4.setAttribute('id', "selectedItemPricePerOne") ;
        newCell4.setAttribute('value', pricePerOneString) ;

        newCell1.innerText = tableOrdered.rows.length-2;    // #
        newCell2.innerText = itemNmString;                  // 메뉴명
        newCell3.innerText = '1개';                          // 주문수량
        newCell4.innerText = parseInt(pricePerOneString).toLocaleString()+'원';         // 단가(개당)
        newCell5.appendChild(delButton);                    // 삭제버튼

        // 삭제버튼 이벤트 등록
        delButton.addEventListener("click", deleteSelectedMenu);

        // 총 주문금액 계산
        calcTotalPrice(pricePerOneString, 0, "MENU_ADDED") ;

    } else {
        alert("⚠️ 최대 5개 품목까지만 주문 가능합니다.");
        return false;
    }
}

// 총 주문금액 계산
function calcTotalPrice(selectedItemPricePerOne, selectedItemQty, operator) {
    const objTotalPrice = document.getElementById('tdTotalPrice') ;
    const prevTotalPrice = objTotalPrice.getAttribute('value') ;
    objTotalPrice.style.textAlign = "right";
    let totalPrice = 0 ;

    if ( operator == "MENU_ADDED" ) {
        totalPrice = parseInt(prevTotalPrice) + parseInt(selectedItemPricePerOne) ;
    } else if ( operator == "MENU_DELETED" ) {
        totalPrice = parseInt(prevTotalPrice) - (parseInt(selectedItemPricePerOne) * parseInt(selectedItemQty)) ;
    }

    objTotalPrice.setAttribute('value', totalPrice) ;
    objTotalPrice.innerText = "Total Price : " + totalPrice + "원" ;

    if ( totalPrice == 0 ) {
        objTotalPrice.style.textAlign = "left";
        objTotalPrice.innerText = '주문하실 메뉴를 선택해 주세요.' ;
    }
}
