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
