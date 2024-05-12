// Функция для отправки DELETE запроса
async function sendDeleteRequest(form) {
    const formData = new FormData(form);
    const actionUrl = form.getAttribute('action');

    // Получаем 'name' и 'cardId' из URL действия формы
    let urlParts = actionUrl.split('/');
    let name = urlParts[4];
    let cardId = urlParts[6];

    try {
        const response = await fetch(actionUrl, {
            method: "DELETE",
            body: formData,
            headers: {
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            },
        });

        // Проверяем статус ответа и перенаправляем пользователя в зависимости от результата
        if (response.status === 202) {
            window.location.href = `/api/resume_v1/profile/${name}/edit/${cardId}`;
        } else if (response.status === 400) {
            const errorMessage = await response.text();
            window.location.href = `/api/resume_v1/profile/${name}/edit/${cardId}?error=${encodeURIComponent(errorMessage)}`;
        }
    } catch (error) {
        console.error("Error sending DELETE request:", error);
    }
}

// Получаем форму deleteForm1 по ее ID
let deleteForm1 = document.getElementById('deleteFirstImageInForm');

// Получаем все формы с классом carousel-item
let deleteForms = document.querySelectorAll('.carousel-item form');

// Добавляем обработчик события 'submit' для каждой формы DELETE
deleteForms.forEach(form => {
    form.addEventListener('submit', function(event) {
        event.preventDefault();
        sendDeleteRequest(form);
    });
});
