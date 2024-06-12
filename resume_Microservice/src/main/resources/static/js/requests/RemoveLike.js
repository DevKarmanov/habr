document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('removeLikeForm').addEventListener('submit', function(event) {
        event.preventDefault(); // Prevent the default form submission

        var form = event.target;
        var formData = new FormData(form);
        var csrfToken = form.querySelector("input[name='_csrf']").value;

        fetch(form.action, {
            method: 'DELETE',
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
                    throw new Error('Ошибка при удалении из избранного');
                }
            })
            .catch(error => {
                console.error('There was a problem with the fetch operation:', error);
                alert('An error occurred while unliking the post.');
            });
    });
});