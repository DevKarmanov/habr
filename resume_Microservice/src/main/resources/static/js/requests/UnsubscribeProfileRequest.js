document.addEventListener('DOMContentLoaded', function() {
    // Находим все формы для отписки и добавляем обработчик события submit
    var unsubscribeForms = document.querySelectorAll('form[id^="unsubscribeForm_"]');
    unsubscribeForms.forEach(function(form) {
        form.addEventListener('submit', function(event) {
            event.preventDefault(); // Предотвращаем обычную отправку формы

            var formData = new FormData(form); // Получаем данные формы
            var csrfToken = form.querySelector("input[name='_csrf']").value; // Получаем CSRF токен

            // Отправляем запрос на сервер
            fetch(form.action, {
                method: 'DELETE',
                body: formData,
                headers: {
                    'X-Requested-With': 'XMLHttpRequest',
                    'X-CSRF-TOKEN': csrfToken // Добавляем CSRF токен в заголовки
                }
            })
                .then(response => {
                    if (response.ok) {
                        // Если запрос успешен, меняем стиль и текст кнопки
                        var unsubscribeButton = form.querySelector('.unsubscribe-button');
                        unsubscribeButton.disabled = true;
                        unsubscribeButton.classList.remove('btn-danger');
                        unsubscribeButton.classList.add('btn-secondary');
                        unsubscribeButton.innerHTML = 'Вы отписались';
                    } else {
                        throw new Error('Ошибка при отписке от пользователя');
                    }
                })
                .catch(error => {
                    console.error('Проблема с выполнением запроса:', error);
                    alert('Произошла ошибка при отписке от пользователя.');
                });
        });
    });
});