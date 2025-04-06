(() => {
    let storeId = "" ;

    // -------------------------------
    // Modal 팝업 처리
    // -------------------------------
    // Modal 'Varying modal content' example in docs and StackBlitz
    // js-docs-start varying-modal-content
    const exampleModal = document.getElementById('exampleModal') ;
    if (exampleModal) {
        exampleModal.addEventListener('show.bs.modal', event => {
            event.stopPropagation();
            console.log("#1 : " + this.tagName);

            // Button that triggered the modal
            const clickedObj = event.relatedTarget
            // Extract info from data-bs-* attributes
            storeId = clickedObj.getAttribute('data-bs-storeId') ;
            // If necessary, you could initiate an Ajax request here
            // and then do the updating in a callback.

            // Update the modal's content.
            const modalTitle = exampleModal.querySelector('.modal-title') ;
            if ( storeId == "" ) {
                modalTitle.textContent = `신규 가맹점 등록` ;
                updateTips("상점 정보를 입력하십시요.")
            } else {
                modalTitle.textContent = `가맹점 정보 수정` ;
                updateTips("상점 정보를 수정하십시요.")

                const storeNm = clickedObj.getAttribute('data-bs-storeNm') ;
                exampleModal.querySelector('#storeNm').value = storeNm ;

                const regionNm = clickedObj.getAttribute('data-bs-regionNm') ;
                $('#regionNm').html(regionNm+' ');
                $('#regionNm').val(regionNm);

                const addr = clickedObj.getAttribute('data-bs-addr') ;
                exampleModal.querySelector('#addr').value = addr ;

                const openYN = clickedObj.getAttribute('data-bs-openYN') ;
                if ( openYN == "true" ) {
                    exampleModal.querySelector('#openY').checked = true ;
                } else {
                    exampleModal.querySelector('#openN').checked = true ;
                }
            }
<!--            modalTitle.textContent = `New message to ${recipient}`-->

        });

    // -------------------------------
    // Modal hidden 이벤트 처리
    // -------------------------------
        exampleModal.addEventListener('hidden.bs.modal', event => {
            storeId = "" ;
            exampleModal.querySelector('#storeNm').value = "" ;
            exampleModal.querySelector('#regionNm').innerText = "지역을 선택하세요..." ;
            exampleModal.querySelector('#regionNm').value = "" ;
            exampleModal.querySelector('#addr').value = "" ;
            exampleModal.querySelector('#openY').checked = true ;
        })
    }
    // js-docs-end varying-modal-content END

    // -------------------------------
    // 신규가맹점 등록 버튼 처리
    // -------------------------------
    $( "#buttonCreateStore" ).button().on( "click", function() {
        const storeNm = $( "#storeNm" ),
            regionNm = $( "#regionNm" ),
            addr = $( "#addr" ),
            allFields = $( [] ).add( storeNm ).add( regionNm ).add( addr );

        var valid = true;
        allFields.removeClass( "ui-state-error" );

        valid = valid && checkLength( storeNm, "상점명", 2, 20 );
        valid = valid && checkSelect( regionNm, "지역" ) ;
        valid = valid && checkLength( addr, "주소", 2, 50 );

        if ( valid ) {
            fetch('/store-service/createStore', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json', // 데이터 형식 설정
                },
                body: JSON.stringify({
                    'storeId': storeId,
                    'storeNm': storeNm.val(),
                    'regionNm': regionNm.val(),
                    'addr': addr.val(),
                    'openYN': $("input:radio[name='openYN']:checked").val()
                })
            })
            .then(response => response.json()) // 서버 응답을 JSON 형식으로 변환합니다.
            .then(data => {
                if ( data.rtnCode == "0" ) {
                    document.getElementById("buttonCancel").click();
//                    $( "#target" ).trigger( "click" );
                    alert(data.msg);
                    loadStoreMain();
                } else {
                    alert(data.msg);
                }
            });
        }
    });
    // 신규가맹점 등록 버튼 처리 END

    // -------------------------------
    // 드랍다운[지역명] 처리
    // -------------------------------
    $('#dropdownRegion .dropdown-menu li > a').bind('click', function (e) {
        var html = $(this).html();
        $('#regionNm').html(html+' ');
        $('#regionNm').val(html);
    });

})()

function updateTips( msg ) {
//    const tips = $( ".validateTips" ) ;
//
//    tips.text( msg ) ;
//    tips.removeClass('animateTips') ;
//    tips.addClass('animateTips') ;

    const tips = document.querySelector('.validateTips') ;
    tips.innerText = msg ;
    tips.classList.remove("animateTips");

    // -> triggering reflow /* The actual magic */
    // without this it wouldn't work. Try uncommenting the line and the transition won't be retriggered.
    // Oops! This won't work in strict mode. Thanks Felis Phasma!
    // element.offsetWidth = element.offsetWidth;
    // Do this instead:
    void tips.offsetWidth;

    // -> and re-adding the class
    tips.classList.add("animateTips");

//    tips.addClass( "ui-state-highlight" );
//    setTimeout(function() {
//            tips.removeClass( "ui-state-highlight", 1500 );
//            }, 500
//        );
}