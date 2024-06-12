document.getElementById('copyButton').addEventListener('click', function() {
    var adminKeyInput = document.getElementById('adminKey');
    adminKeyInput.select();
    adminKeyInput.setSelectionRange(0, 99999); // For mobile devices
    document.execCommand('copy');

    // Optionally, show a tooltip or a temporary message
    alert('Ключ скопирован');
});

// Form submission functionality
document.getElementById('generateNewKeyButton').addEventListener('click', function() {
    var csrfToken = document.getElementById('csrfTokenForLogout').value;

    fetch('/api/resume_v1/generate-new-key', {
        method: 'PATCH',
        headers: {
            'Content-Type': 'application/json',
            'X-CSRF-TOKEN': csrfToken
        },
        body: JSON.stringify({}) // Add any necessary request body data here
    })
        .then(response => {
            if (response.ok) {
                return response.text();
            }
            throw new Error('Network response was not ok.');
        })
        .then(data => {
            alert('Вы успешно изменили ключ.\nПерезагрузите страницу\nКлюч, который вы сейчас видите, больше недействителен');
            // Optionally, update the adminKey input with the new key if provided
            // document.getElementById('adminKey').value = data.newKey;
        })
        .catch(error => {
            alert('Ошибка при изменении ключа: ' + error.message);
        });
});