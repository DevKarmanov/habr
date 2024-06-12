document.getElementById('downgradeUserForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    const csrfToken = document.getElementById('csrfToken').value;
    const userName = document.getElementById('userName').value;
    const url = `/api/resume_v1/profile/${userName}/status-change`;

    try {
        const response = await fetch(url, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({ downgrade: true })
        });

        const result = await response.text();
        if (response.ok) {
            location.reload();
            alert('Успешно понижен');
        } else {
            alert('Ошибка: ' + result);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Ошибка: ' + error.message);
    }
});