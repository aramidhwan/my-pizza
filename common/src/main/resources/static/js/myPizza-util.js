/******************************************************************************
 * Function명 :  CF_toStringByFormatting
 * 설명       : 자바의 Date 타입 변수를 String으로 변환
 * Params     : source - Date타입의 변수
 * Return     : YYYY-MM-DD 형식의 String
 * 사용법    :
 *      var dateStr = CF_toStringByFormatting(item.USER_DATE);
*******************************************************************************/
function CF_toStringByFormatting(source){
	var date = new Date(source);
	const year = date.getFullYear();
	const month = CF_leftPad(date.getMonth() + 1);
	const day = CF_leftPad(date.getDate());
	return [year, month, day].join('-');
}

/******************************************************************************
 * Function명 :  CF_leftPad
 * 설명       : 한자리 숫자를 두자리로 변환
 * Params     : value 숫자 String
 * Return     : MM 형식의 월 String
 * 사용법    :
 *      CF_leftPad(date.getMonth() + 1);
*******************************************************************************/
function CF_leftPad(value){
	if (Number(value) >= 10) {
		return value;
	}
	return "0" + value;
}

function getFormatTime(date) {
    const yyyy = date.getFullYear() ;
    const month = date.getMonth()+1 ;
    const day = date.getDay() ;
    const hh = date.getHours() ;
    const mm = date.getMinutes() ;
    const ss = date.getSeconds() ;

    return yyyy + '년 ' + month + '월 ' + day + '일 ' + hh + '시 ' + mm + '분 ' + ss + '초' ;
}

function checkLength( obj, n, min, max ) {
    if ( obj.value.length > max || obj.value.length < min ) {
        obj.classList.add( "ui-state-error" );
        updateTips( n + " 길이를 맞추어 주세요 \n(최소:" + min + "글자, 최대:" + max + "글자)" );
        setTimeout(function() {
            obj.classList.remove("ui-state-error");
        }, 1000);


        return false;
    } else {
        return true;
    }
}

function checkSelect(obj, n) {
    if (obj.value === "") { // jQuery의 obj.val() 대신 obj.value 사용
        obj.classList.add("ui-state-error"); // 클래스 추가
        updateTips(n + " 선택이 필요합니다."); // 메시지 표시

        // 2초 후 클래스 제거 (애니메이션 효과 포함)
        setTimeout(function() {
            obj.classList.remove("ui-state-error");
        }, 1000);

        return false;
    } else {
        return true;
    }
}


function dateTimeFormatter(localDateTimeStr) {
    // JavaScript의 Date 객체로 변환
    const dateObj = new Date(localDateTimeStr);

    // 연, 월, 일, 시, 분, 초 추출
    const year = dateObj.getFullYear();
    const month = String(dateObj.getMonth() + 1).padStart(2, '0'); // 월은 0부터 시작하므로 +1 필요
    const day = String(dateObj.getDate()).padStart(2, '0');
    const hours = String(dateObj.getHours()).padStart(2, '0');
    const minutes = String(dateObj.getMinutes()).padStart(2, '0');
    const seconds = String(dateObj.getSeconds()).padStart(2, '0');

    // 원하는 형식으로 출력
    const formattedDate = `${year}년 ${month}월 ${day}일 ${hours}:${minutes}:${seconds}`;

//    console.log(formattedDate); // 출력: "2025-03-06 14:30:45"
    return formattedDate ;
}

// 입력필드 오류 검사 후 툴팁
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