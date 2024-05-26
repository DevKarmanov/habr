document.getElementById('logoutButton').addEventListener('click', function(event) {
    event.preventDefault();

    const confirmation = confirm('Вы действительно хотите выйти из аккаунта?');
    if (confirmation) {
        const csrfToken = document.getElementById('csrfTokenForLogout').value;

        fetch('/api/resume_v1/logout', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            }
        })
            .then(response => {
                if (response.ok) {
                    window.location.href = '/api/resume_v1/login'; // Перенаправление на страницу логина после выхода
                } else {
                    console.error('Logout failed');
                }
            })
            .catch(error => console.error('Error:', error));
    }
});
