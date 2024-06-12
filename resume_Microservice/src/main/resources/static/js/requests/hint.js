// Функция для показа сообщения через модальное окно
function showMessage() {
    // Проверяем, есть ли атрибут "error" в URL-адресе
    const urlParams = new URLSearchParams(window.location.search);
    if (!urlParams.has('error')) {
        // Если атрибут "error" отсутствует, то создаем экземпляр модального окна и показываем его
        const rulesModal = new bootstrap.Modal(document.getElementById('hintModal'));
        rulesModal.show();
    }
}

// Вызываем функцию для показа модального окна при загрузке страницы
showMessage();

$(document).ready(function(){
    $('#hintModal .btn-danger').click(function(e){
        e.preventDefault();
        let isChecked = $('#disableNotificationCheckbox').is(':checked');
        if (!isChecked) {
            // Если чекбокс не отмечен, прекращаем выполнение функции
            return;
        }
        let pathName = $('input[name="pathName"]').val();
        let csrfToken = $('input[name="_csrf"]').val();
        $.ajax({
            url: '/api/resume_v1/profile/' + pathName + '/set-hint-show',
            type: 'PATCH',
            headers: {
                'X-CSRF-TOKEN': csrfToken
            },
            data: { hintValueSetting: isChecked },
            success: function() {
                // Всплывающее окно для успешного ответа
                alert("Договорились, больше не покажем👌");
            },
            error: function() {
                // Всплывающее окно для неуспешного ответа
                alert("Какая-то ошибка⚠️. Просим вас потерпеть подсказку немного. Приносим свои извинения");
            }
        });
    });
});
