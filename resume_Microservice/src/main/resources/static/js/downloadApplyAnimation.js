// Функция для отображения спиннера и изменения текста
function showSpinnerAndMessage() {
    // Находим элементы спиннера и заголовка
    const spinner = document.querySelector('.spinner-border');
    const h3 = document.querySelector('.contact-form h3');
    const logo = document.querySelector('.contact-image img');

    // Показываем спиннер и скрываем логотип
    spinner.classList.remove('d-none');
    logo.classList.add('d-none');

    // Изменяем текст заголовка
    h3.textContent = 'Изменения применяются';

    // Блокируем все кнопки на странице, кроме нужной
    const buttons = document.querySelectorAll('button:not(.allowed-button)');
    buttons.forEach(button => {
        button.disabled = true;
        button.classList.add('disabled');
    });
}

// Находим все формы на странице
const forms = document.querySelectorAll('form');

// Для каждой формы добавляем обработчик события 'submit'
forms.forEach(form => {
    form.addEventListener('submit', event => {
        // Предотвращаем отправку формы по умолчанию
        event.preventDefault();

        // Вызываем функцию для отображения спиннера и изменения текста
        showSpinnerAndMessage();

        // Здесь можно добавить код для отправки формы на сервер
        // Например, использовать fetch() или другой метод для отправки данных на сервер
    });
});