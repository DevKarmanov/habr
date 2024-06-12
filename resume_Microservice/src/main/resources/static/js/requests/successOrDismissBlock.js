document.addEventListener('DOMContentLoaded', () => {
    const csrfToken = document.getElementById('csrfTokenForLogout').value;

    // Обработка формы dismissMessageForm
    document.querySelectorAll('.dismiss-message-form').forEach(form => {
        form.addEventListener('submit', async (event) => {
            event.preventDefault();

            const formData = new FormData(form);
            const complaintId = form.querySelector('[name="complaintId"]').value;

            try {
                const response = await fetch(form.action, {
                    method: 'DELETE',
                    headers: {
                        'X-CSRF-TOKEN': csrfToken,
                        'Content-Type': 'application/x-www-form-urlencoded'
                    },
                    body: new URLSearchParams(formData)
                });

                if (response.ok) {
                    const card = document.querySelector(`.complaint-card[data-complaint-id="${complaintId}"]`);
                    if (card) {
                        card.classList.add('disabled-card');
                    }

                    // Close the modal
                    const modal = bootstrap.Modal.getInstance(form.closest('.modal'));
                    modal.hide();
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

    // Обработка формы successBanModal
    document.querySelectorAll('.success-ban-form').forEach(form => {
        form.addEventListener('submit', async (event) => {
            event.preventDefault();

            const formData = new FormData(form);
            const complaintId = form.querySelector('[name="complaintId"]').value;

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
                    const card = document.querySelector(`.complaint-card[data-complaint-id="${complaintId}"]`);
                    if (card) {
                        card.classList.add('disabled-card');
                    }

                    // Close the modal
                    const modal = bootstrap.Modal.getInstance(form.closest('.modal'));
                    modal.hide();
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
});