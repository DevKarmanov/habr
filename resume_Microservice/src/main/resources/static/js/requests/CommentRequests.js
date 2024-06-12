//Запрос на создание комментария
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

//Запрос на удаления комментария
document.addEventListener("DOMContentLoaded", function() {
    const deleteForms = document.querySelectorAll("[id^='deleteCommentForm']");

    deleteForms.forEach(function(form) {
        form.addEventListener("submit", async function(event) {
            event.preventDefault();

            const csrfToken = document.querySelector("input[name='_csrf']").value;
            const formData = new FormData(form);
            const headers = new Headers();
            headers.append("X-CSRF-TOKEN", csrfToken);

            const options = {
                method: "DELETE",
                headers: headers,
                body: formData
            };

            var modalId = '#successModal' + formData.get('commentId');
            var modalElement = document.querySelector(modalId);
            var bootstrapModal = bootstrap.Modal.getInstance(modalElement);

            try {
                const response = await fetch(form.action, options);
                bootstrapModal.hide();
                if (response.ok) {
                    alert("После обновления страницы больше никто не увидит ваш комментарий");
                    location.reload();
                } else {
                    alert("Произошла ошибка")
                }
            } catch (error) {
                console.error("Ошибка при выполнении запроса:", error);
            }
        });
    });
});

//Запрос на редактирование комментария
document.addEventListener("DOMContentLoaded", function() {
    // Найти все формы редактирования комментариев
    const editCommentForms = document.querySelectorAll('[id^="editCommentForm"]');

    editCommentForms.forEach(form => {
        form.addEventListener("submit", function(event) {
            event.preventDefault(); // Предотвратить отправку формы по умолчанию

            const commentId = form.querySelector('input[name="commentId"]').value;
            submitEditForm(commentId);
        });
    });
});

function submitEditForm(commentId) {
    const form = document.getElementById(`editCommentForm${commentId}`);
    const formData = new FormData(form);
    const csrfToken = document.querySelector("input[name='_csrf']").value;

    fetch('/api/resume_v1/patch-comment', {
        method: 'PATCH',
        headers: {
            'X-CSRF-TOKEN': csrfToken
        },
        body: formData
    })
        .then(response => {
            if (response.ok) {
                return response.text(); // assuming response is text
            } else {
                return response.text().then(errorMessage => {
                    throw new Error(errorMessage);
                });
            }
        })
        .then(data => {
            alert("Комментарий успешно обновлен");
            location.reload();
            // Обновите комментарий на странице или выполните другие действия
        })
        .catch(error => {
            console.error('Ошибка:', error);
            alert("Ошибка: " + error.message);
        });
}

//Запрос на создание ответного комментария
document.addEventListener('DOMContentLoaded', function() {
    // Находим кнопку, которая открывает модальное окно и имеет класс create-comment
    var modalButton = document.querySelector('.create-comment');
    // Находим поле replyToCommentId
    var replyToCommentIdInput = document.getElementById('replyToCommentId');

    // Добавляем обработчик события при клике на кнопку, которая открывает модальное окно
    modalButton.addEventListener('click', function() {
        // Очищаем значение поля replyToCommentId
        replyToCommentIdInput.value = '';
    });
});

// JavaScript для заполнения скрытого поля при ответе на комментарий
document.querySelectorAll('.btn-reply').forEach(button => {
    button.addEventListener('click', function() {
        const commentId = this.getAttribute('data-comment-id');
        document.getElementById('replyToCommentId').value = commentId;
    });
});

//Подсветить комментарий
document.addEventListener('DOMContentLoaded', function() {
    // Обработчик события для всех ссылок, к которым вы хотите добавить подсветку комментария
    document.querySelectorAll('a.comment-link').forEach(link => {
        link.addEventListener('click', function(event) {
            event.preventDefault(); // Предотвращаем стандартное поведение перехода по ссылке

            // Удаляем подсветку со всех комментариев
            document.querySelectorAll('.highlighted-comment').forEach(comment => {
                comment.classList.remove('highlighted-comment');
            });

            // Получаем идентификатор комментария из атрибута href ссылки
            const commentId = this.getAttribute('href').substring(1); // Убираем решетку (#) из начала строки

            // Находим комментарий с соответствующим идентификатором и добавляем ему класс для подсветки
            const comment = document.getElementById(commentId);
            if (comment) {
                comment.classList.add('highlighted-comment');
            }

            // Переходим по ссылке
            window.location.href = this.href;
        });
    });
});