document.addEventListener("DOMContentLoaded", function() {
    // Добавляем обработчик события отправки формы
    document.getElementById("informationForm").addEventListener("submit", async function(event) {
        event.preventDefault(); // Предотвращаем отправку формы по умолчанию

        // Получаем файл из input[type=file]
        const fileInput = document.getElementById("formFileMultiple");
        const file = fileInput.files[0];

        // Проверяем размер файла
        if (file && file.size > 5 * 1024 * 1024) { // Если размер файла больше 5 МБ
            // Получаем сообщение об ошибке
            const errorMessage = "Файл слишком большой. Максимальный размер файла: 5 МБ.";

            // Добавляем параметр error к текущему URL
            const url = new URL(window.location.href);
            url.searchParams.set('error', errorMessage);
            window.location.href = url.toString();
            return; // Прекращаем выполнение функции
        }

        try {
            const formData = new FormData(this);

            const response = await fetch(this.action, { // Используем атрибут action формы как путь запроса
                method: "PATCH",
                body: formData,
                headers: {
                    'X-CSRF-TOKEN': document.querySelector('input[name="_csrf"]').value
                },
            });

            if (response.ok) {
                // Если запрос успешен, перенаправляем пользователя на профиль с указанным именем
                const name = document.querySelector('input[name="PathUserName"]').value;
                window.location.href = `/api/resume_v1/profile/${name}`;
            } else {
                const errorMessage = await response.text();
                const url = new URL(window.location.href);
                url.searchParams.set('error', errorMessage);
                window.location.href = url.toString();
            }
        } catch (error) {
            console.error("Ошибка отправки запроса:", error);
        }
    });
});

var skills = [
    "HTML",
    "CSS",
    "JavaScript",
    "Python",
    "Java",
    "C++",
    "Ruby",
    "React",
    "Angular",
    "Vue.js",
    "Node.js",
    "SQL",
    "Git",
    "UI/UX Design",
    "Graphic Design",
    "Data Analysis",
    "Machine Learning",
    "DevOps",
    "Agile Methodologies"
    // Добавьте другие навыки по вашему выбору
];

var input = document.getElementById("skillsInput");
var autocompleteItems = document.querySelector(".autocomplete-items");

input.addEventListener("input", function() {
    var value = this.value;
    if (!value) {
        closeAutocomplete();
        return false;
    }
    var matches = skills.filter(function(skill) {
        return skill.toLowerCase().indexOf(value.toLowerCase()) > -1;
    });
    if (matches.length === 0) {
        closeAutocomplete();
        return false;
    }
    displayAutocomplete(matches);
});

function displayAutocomplete(matches) {
    closeAutocomplete();
    matches.forEach(function(match) {
        var item = document.createElement("div");
        item.classList.add("dropdown-item");
        item.textContent = match;
        item.addEventListener("click", function() {
            input.value = match + ", ";
            closeAutocomplete();
            input.focus();
        });
        autocompleteItems.appendChild(item);
    });
}

function closeAutocomplete() {
    autocompleteItems.innerHTML = "";
}

document.addEventListener("click", function(e) {
    if (!e.target.matches("#skillsInput")) {
        closeAutocomplete();
    }
});

$(document).ready(function() {
    var jobs = [
        "Backend разработчик",
        "Бэкенд разработчик",
        "Frontend разработчик",
        "Фронтенд разработчик",
        "Full-stack разработчик",
        "Фулл-стэк разработчик",
        "Тестировщик",
        "DevOps инженер",
        "Системный администратор",
        "UI/UX дизайнер",
        "Product owner",
        "Архитектор программного обеспечения",
        "Аналитик",
        "Менеджер проекта",
        "Технический писатель",
        "Специалист по маркетингу",
        "QA инженер",
        "Инженер-разработчик",
        "Бизнес-аналитик",
        "Сетевой инженер",
        "Технический менеджер"
    ];

    $('#jobInput').autocomplete({
        source: jobs
    });
});