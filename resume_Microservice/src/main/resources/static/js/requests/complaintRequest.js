document.addEventListener('DOMContentLoaded', function() {
    var complaintForm = document.getElementById('complaintForm');

    complaintForm.addEventListener('submit', function(event) {
        event.preventDefault(); // Предотвращаем обычную отправку формы

        var formData = new FormData(complaintForm); // Получаем данные формы
        var csrfToken = complaintForm.querySelector("input[name='_csrf']").value; // Получаем CSRF токен


        var files = formData.getAll('images'); // Предполагается, что input type="file" имеет name="images"

        // Проверка количества файлов
        if (files.length > 5) {
            alert('Вы не можете загрузить больше 5 фотографий.');
            return;
        }

        // Проверка размера каждого файла
        for (var i = 0; i < files.length; i++) {
            if (files[i].size > 5 * 1024 * 1024) { // 5 мегабайт
                alert('Каждая фотография не должна превышать 5 мегабайт.');
                return;
            }
        }

        // Отправляем запрос на сервер
        fetch(complaintForm.action, {
            method: 'POST',
            body: formData,
            headers: {
                'X-Requested-With': 'XMLHttpRequest',
                'X-CSRF-TOKEN': csrfToken // Добавляем CSRF токен в заголовки
            }
        })
            .then(response => {
                if (response.ok) {
                    // Если запрос успешен, закрываем модальное окно и выводим сообщение об успешной отправке
                    $('#complaintModal').modal('hide');
                    alert('Жалоба успешно отправлена!');
                } else {
                    // Если возникла ошибка, выводим сообщение об ошибке
                    alert('Ошибка при отправке жалобы. Скорее всего, на этого пользователя уже пожаловались');
                }
            })
            .catch(error => {
                console.error('Проблема с выполнением запроса:', error);
                alert('Произошла ошибка при отправке жалобы.');
            });
    });
});