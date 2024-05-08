window.onload = function() {
    document.getElementById('registerForm').addEventListener('submit', function(event) {
        event.preventDefault();
        const formData = new FormData(this);
        const csrfToken = document.querySelector('input[name="_csrf"]').value;
        formData.append('_csrf', csrfToken); // Добавляем CSRF-токен в FormData
        fetch('/api/resume_v1/register', {
            method: 'POST',
            body: formData // Отправляем FormData с данными формы
        }).then(response => {
            if (response.ok) {
                window.location.href = '/api/resume_v1/login';
            } else {
                response.text().then(errorMessage => {
                    window.location.href = '/api/resume_v1/registerForm?err=' + encodeURIComponent(errorMessage);
                });
            }
        }).catch(error => {
            console.error('Error:', error);
            window.location.href = '/api/resume_v1/registerForm?err=Изображение должно весить меньше 5 мб';
        });
    });
};