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