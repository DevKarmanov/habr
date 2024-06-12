document.getElementById("resumeForm").addEventListener("submit", async function (event) {
    event.preventDefault();

    const form = event.target;
    const formData = new FormData(form);

    // Делаем кнопку отправки неактивной и меняем её стиль
    const submitButton = form.querySelector('input[name="btnSubmit"]');
    submitButton.disabled = true;
    submitButton.classList.add('processing');

    try {
        // Показываем загрузку перед отправкой формы
        const contactImage = document.querySelector('.contact-image');
        contactImage.querySelector('.spinner-border').classList.remove('d-none');
        contactImage.querySelector('img').classList.add('d-none');

        const response = await fetch("/api/resume_v1/create", {
            method: "POST",
            body: formData,
            headers: {
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            },
        });

        // Проверяем статус ответа и перенаправляем пользователя в зависимости от результата
        if (response.status === 202) {
            window.location.href = "/api/resume_v1/user";
        } else if (response.status === 400) {
            const errorMessage = await response.text();
            window.location.href = `/api/resume_v1/resumeForm?error=${encodeURIComponent(errorMessage)}`;
        }
    } catch (error) {
        console.error("Error sending request:", error);
    }
});