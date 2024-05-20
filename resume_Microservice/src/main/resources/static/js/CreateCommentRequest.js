document.addEventListener('DOMContentLoaded', (event) => {
    const form = document.getElementById('createCommentForm');
    const submitButton = document.getElementById('submitCommentButton');

    submitButton.addEventListener('click', async () => {
        // Собираем данные формы
        const formData = new FormData(form);
        const data = {
            text: formData.get('text'),
            cardId: formData.get('cardId')
        };

        // Получаем CSRF токен
        const csrfToken = formData.get('_csrf');

        try {
            // Отправляем запрос
            const response = await fetch('/api/resume_v1/create-comment', {
                method: 'POST',
                headers: {
                    'X-CSRF-TOKEN': csrfToken
                },
                body: formData
            });

            // Обрабатываем ответ
            const result = await response.text();
            if (response.ok) {
                alert('Комментарий успешно опубликован!');
                form.reset(); // Очищаем форму после успешной отправки
                location.reload();
            } else {
                alert('Ошибка: ' + result);
            }
        } catch (error) {
            console.error('Ошибка при отправке формы:', error);
            alert('Произошла ошибка при отправке комментария. Пожалуйста, попробуйте снова.');
        }
    });
});
