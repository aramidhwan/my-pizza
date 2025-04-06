document.addEventListener("DOMContentLoaded", function() {
    fetchStores(); // 페이지가 로드될 때 Store 목록을 가져옵니다.

    document.getElementById("storeForm").addEventListener("submit", function(event) {
        event.preventDefault(); // 기본 폼 제출 동작을 막습니다.
        addStore(); // 새로운 할 일을 추가합니다.
    });
});

function fetchStores() {
    fetch('/stores') // 서버에서 TODO 목록을 가져옵니다.
        .then(response => response.json()) // 서버 응답을 JSON 형식으로 변환합니다.
        .then(data => {
            const storeList = document.getElementById('storeList');
            storeList.innerHTML = ''; // 목록을 초기화합니다.
            data.forEach(store => {
                const li = document.createElement('li'); // 새로운 리스트 아이템을 만듭니다.
                li.className = "list-group-item d-flex justify-content-between align-items-center"; // 부트스트랩 스타일을 적용
                li.textContent = store.storeNm + ' ' + store.regionNm; // 할 일을 표시합니다.
                storeList.appendChild(li); // 목록에 추가합니다.
            });
        });
}

function fetchTodos2() {
    fetch('/todos') // 서버에서 TODO 목록을 가져옵니다.
        .then(response => response.json()) // 서버 응답을 JSON 형식으로 변환합니다.
        .then(data => {
            const storeList = document.getElementById('storeList');
            storeList.innerHTML = ''; // 목록을 초기화합니다.
            data.forEach(todo => {
                const li = document.createElement('li'); // 새로운 리스트 아이템을 만듭니다.
                li.textContent = todo.task + (todo.completed ? ' (완료)' : ' (미완료)'); // 할 일을 표시합니다.
                li.className = "list-group-item"; // 부트스트랩 스타일을 적용합니다.
                storeList.appendChild(li); // 목록에 추가합니다.
            });
        });
}

function addStore() {
    const taskInput = document.getElementById("task"); // 입력 필드를 가져옵니다.
    const task = taskInput.value; // 입력된 값을 가져옵니다.

    fetch('/add', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded', // 데이터 형식 설정
        },
        body: new URLSearchParams({ task }) // 입력된 값을 URL 형식으로 변환합니다.
    })
    .then(response => response.json()) // 서버 응답을 JSON 형식으로 변환합니다.
    .then(todo => {
        fetchStores(); // TODO 목록을 새로 고칩니다.
        taskInput.value = ''; // 입력 필드를 초기화합니다.
    });
}

// 할 일의 완료 상태를 토글하는 함수
function toggleTodoCompletion(todo) {
    fetch(`/update/${todo.id}`, {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/x-www-form-urlencoded',
        },
        body: new URLSearchParams({ completed: !todo.completed }) // 현재 상태의 반대로 전송
    })
    .then(() => {
        fetchStores(); // 수정 후 TODO 목록을 새로 고칩니다.
    });
}

function deleteTodo(id) {
    if (confirm("정말로 삭제하시겠습니까?")) { // 삭제 확인 메시지
        fetch(`/delete/${id}`, {
            method: 'DELETE'
        })
        .then(() => {
            fetchStores(); // 삭제 후 TODO 목록을 새로 고칩니다.
        });
    }
}