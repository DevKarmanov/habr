// Функция для отправки PATCH запроса
async function sendPatchRequest(form) {
    const formData = new FormData(form);
    const actionUrl = form.getAttribute('action');

    // Получаем 'name' и 'cardId' из URL действия формы
    let urlParts = actionUrl.split('/');
    let name = urlParts[4];
    let cardId = urlParts[6];

    let redirectUrl = `/api/resume_v1/profile/${name}/edit/${cardId}`;

    // Проверяем размер каждого файла в FormData
    for (let pair of formData.entries()) {
        const [key, value] = pair;
        if (value instanceof File && value.size > 5 * 1024 * 1024) {
            // Если размер файла больше 5 МБ, перенаправляем пользователя на страницу с ошибкой
            window.location.href = `${redirectUrl}?error=Размер одного или нескольких файлов превышает 5 МБ`;
            return; // Прерываем выполнение функции
        }
    }

    try {
        const response = await fetch(actionUrl, {
            method: "PATCH",
            body: formData,
            headers: {
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            },
        });

        // Проверяем статус ответа и перенаправляем пользователя в зависимости от результата
        if (response.status === 202) {
            window.location.href = redirectUrl;
        } else if (response.status === 400) {
            const errorMessage = await response.text();
            window.location.href = `${redirectUrl}?error=${encodeURIComponent(errorMessage)}`;
        }
    } catch (error) {
        // Если произошла ошибка, перенаправляем пользователя на страницу с сообщением об ошибке
        window.location.href = `${redirectUrl}?error=Что-то пошло не так. Убедитесь, что вы соблюдаете все правила публикации`;
    }
}

let patchForm = document.getElementById('patchCardForm');


// Добавляем обработчик события 'submit' для формы PATCH
patchForm.addEventListener('submit', function(event) {
    event.preventDefault();
    sendPatchRequest(patchForm);
});

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