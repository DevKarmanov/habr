document.getElementById('upgradeUserForm').addEventListener('submit', async function(event) {
    event.preventDefault();

    const csrfToken = document.getElementById('csrfToken').value;
    const userName = document.getElementById('userNameUp').value;
    const url = `/api/resume_v1/profile/${userName}/status-change`;

    try {
        const response = await fetch(url, {
            method: 'PATCH',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': csrfToken
            },
            body: JSON.stringify({ downgrade: false })
        });

        const result = await response.text();
        if (response.ok) {
            alert('Успешно повышен');
            location.reload();
        } else {
            alert('Ошибка: ' + result);
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Ошибка: ' + error.message);
    }
});