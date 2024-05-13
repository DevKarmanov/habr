document.addEventListener("DOMContentLoaded", function() {
    const deleteForms = document.querySelectorAll("[id^='deleteCard']");

    deleteForms.forEach(function(form) {
        form.addEventListener("submit", async function(event) {
            event.preventDefault();

            // Добавьте это
            const confirmDelete = confirm("Вы действительно хотите удалить?");
            if (!confirmDelete) {
                return; // Если пользователь нажал Отмена, просто вернитесь и не продолжайте удаление
            }

            const csrfToken = document.querySelector("input[name='_csrf']").value;
            const formData = new FormData(form);
            const headers = new Headers();
            headers.append("X-CSRF-TOKEN", csrfToken);

            const options = {
                method: "DELETE",
                headers: headers,
                body: formData
            };

            try {
                const response = await fetch(form.action, options);
                if (response.ok) {
                    const name = form.getAttribute('action').split('/')[4];
                    window.location.href = `/api/resume_v1/profile/${name}`;
                } else {
                    const errorMessage = await response.text();
                    const name = form.getAttribute('action').split('/')[4];
                    window.location.href = `/api/resume_v1/profile/${name}?error=${encodeURIComponent(errorMessage)}`;
                }
            } catch (error) {
                console.error("Ошибка при выполнении запроса:", error);
                const errorMessage = error.message;
                const name = form.getAttribute('action').split('/')[4];
                window.location.href = `/api/resume_v1/profile/${name}?error=${encodeURIComponent(errorMessage)}`;
            }
        });
    });
});