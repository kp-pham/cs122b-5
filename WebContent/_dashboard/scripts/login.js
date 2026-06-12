const loginForm = $("#login_form");
const errorMessage = $("#login_error_message");

const baseURL = window.location.origin + '/' + window.location.pathname.split('/')[1];

function handleLoginSuccess() {
    window.location.replace("index.html");
}

function handleLoginFailure(jqXHR) {
    errorMessage.removeClass("d-none");
    errorMessage.text(jqXHR.responseJSON.message ?? "Unexpected error occurred");
}

function submitLoginForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    $.ajax(
        baseURL + "/api/employees/login", {
            method: "POST",
            data: loginForm.serialize(),
            success: handleLoginSuccess,
            error: (jqXHR) => handleLoginFailure(jqXHR)
        }
    );
}

loginForm.submit(submitLoginForm);