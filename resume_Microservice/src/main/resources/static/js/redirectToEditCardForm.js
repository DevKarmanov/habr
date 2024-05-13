document.addEventListener("DOMContentLoaded", function() {
    // Найти все ссылки с классом "edit-button"
    const editButtons = document.querySelectorAll('.edit-button');

    // Для каждой найденной ссылки добавить обработчик события клика
    editButtons.forEach(function(button) {
        button.addEventListener('click', function(event) {
            // Получить значение атрибута data-card-id из ссылки
            const cardId = button.getAttribute('data-card-id');

            // Получить имя пользователя из адресной строки
            const pathUserName = window.location.pathname.split('/')[4];

            // Сформировать URL для перенаправления
            const redirectURL = `/api/resume_v1/profile/${pathUserName}/edit/${cardId}`;

            // Перенаправить на сформированный URL
            window.location.href = redirectURL;
        });
    });
});