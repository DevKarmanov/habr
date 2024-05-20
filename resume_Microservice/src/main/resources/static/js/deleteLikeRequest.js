document.addEventListener("DOMContentLoaded", function() {
    const deleteForms = document.querySelectorAll("[id^='deleteLike']");

    deleteForms.forEach(function(form) {
        form.addEventListener("submit", async function(event) {
            event.preventDefault();

            // Confirm deletion
            const confirmDelete = confirm("Вы действительно хотите удалить?");
            if (!confirmDelete) {
                return; // If user clicks cancel, do not proceed
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
                const userName = form.querySelector("input[name='userName']").value;
                if (response.ok) {
                    window.location.href = `/api/resume_v1/profile/${userName}`;
                } else {
                    const errorMessage = await response.text();
                    window.location.href = `/api/resume_v1/profile/${userName}?error=${encodeURIComponent(errorMessage)}`;
                }
            } catch (error) {
                console.error("Ошибка при выполнении запроса:", error);
                const errorMessage = error.message;
                const userName = form.querySelector("input[name='userName']").value;
                window.location.href = `/api/resume_v1/profile/${userName}?error=${encodeURIComponent(errorMessage)}`;
            }
        });
    });
});