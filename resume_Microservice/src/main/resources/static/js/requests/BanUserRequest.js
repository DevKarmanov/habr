document.querySelectorAll('.success-ban-form').forEach(form => {
    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        const formData = new FormData(form);
        const csrfToken = document.getElementById('csrfTokenForLogout').value;

        try {
            const response = await fetch(form.action, {
                method: 'POST',
                headers: {
                    'X-CSRF-TOKEN': csrfToken,
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams(formData)
            });

            if (response.ok) {
                alert("Вы успешно заблокировали пользователя");
                location.reload();
            } else {
                const errorText = await response.text();
                alert(`Ошибка: ${errorText}`);
            }
        } catch (error) {
            console.error('Ошибка:', error);
            alert('Произошла ошибка. Попробуйте позже.');
        }
    });
});