window.onload = function() {
    document.getElementById('registerForm').addEventListener('submit', function(event) {
        event.preventDefault();
        const formData = new FormData(this);
        const data = Object.fromEntries(formData.entries());
        fetch('/test/register', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
            },
            body: JSON.stringify(data)
        }).then(response => {
            if (response.status === 201) {
                window.location.href = '/test/login';
            } else if (response.status === 409) {
                window.location.href = '/test/registerForm?err=true';
            }
        });
    });
};