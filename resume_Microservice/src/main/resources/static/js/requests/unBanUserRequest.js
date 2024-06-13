document.querySelectorAll('.unban-form').forEach(form => {
    form.addEventListener('submit', async (event) => {
        event.preventDefault();

        const formData = new FormData(form);
        const csrfToken = document.getElementById('csrfToken').value;

        try {
            const response = await fetch(form.action, {
                method: 'PATCH',
                headers: {
                    'X-CSRF-TOKEN': csrfToken,
                    'Content-Type': 'application/x-www-form-urlencoded'
                },
                body: new URLSearchParams(formData)
            });

            if (response.ok) {
                alert("Вы успешно разблокировали пользователя");
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