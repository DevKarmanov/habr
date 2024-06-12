document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('addLikeForm').addEventListener('submit', function(event) {
        event.preventDefault(); // Prevent the default form submission

        var form = event.target;
        var formData = new FormData(form);
        var csrfToken = document.getElementById('csrfToken').value;

        fetch('/api/resume_v1/add-like', {
            method: 'POST',
            body: formData,
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-TOKEN': csrfToken // Add CSRF token to the headers
            }
        })
            .then(response => {
                if (response.ok) {
                    location.reload();
                } else {
                    throw new Error('Ошибка при добавлении в избранное');
                }
            })
            .catch(error => {
                console.error('There was a problem with the fetch operation:', error);
                alert('An error occurred while liking the post.');
            });
    });
});
