document.addEventListener("DOMContentLoaded", function() {
    // Добавляем обработчик события отправки формы
    document.getElementById("informationForm").addEventListener("submit", async function(event) {
        event.preventDefault(); // Предотвращаем отправку формы по умолчанию

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
                // Если статус ответа не 200, получаем текст ошибки из тела ответа
                const errorMessage = await response.text();
                // Перенаправляем пользователя на текущую страницу, добавляя атрибут error с текстом ошибки
                window.location.href = `${window.location.href}?error=${encodeURIComponent(errorMessage)}`;
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