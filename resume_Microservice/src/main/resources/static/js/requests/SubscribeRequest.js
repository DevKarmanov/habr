document.addEventListener('DOMContentLoaded', function() {
    // Обработка формы подписки
    document.getElementById('subscribeForm').addEventListener('submit', function(event) {
        event.preventDefault(); // Предотвращаем обычную отправку формы

        var form = event.target;
        var formData = new FormData(form);
        var csrfToken = form.querySelector("input[name='_csrf']").value;

        fetch(form.action, {
            method: 'POST',
            body: formData,
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-TOKEN': csrfToken // Добавляем CSRF токен в заголовки
            }
        })
            .then(response => {
                if (response.ok) {
                    // Перезагружаем страницу
                    location.reload();
                } else {
                    throw new Error('Ошибка при подписке на пользователя');
                }
            })
            .catch(error => {
                console.error('Проблема с выполнением запроса:', error);
                alert('Произошла ошибка при подписке на пользователя.');
            });
    });
});