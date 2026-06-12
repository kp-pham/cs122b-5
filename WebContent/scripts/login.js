const loginForm = $("#login_form");
const errorMessage = $("#login_error_message")

function handleLoginSuccess(resultData) {
    window.location.replace("index.html");
}

function handleLoginFailure(jqXHR) {
    errorMessage.removeClass("d-none");
    errorMessage.text(jqXHR.responseJSON.message ?? "Unexpected error occurred");
}

function submitLoginForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    $.ajax(
        "api/customers/login", {
            method: "POST",
            data: loginForm.serialize(),
            success: handleLoginSuccess,
            error: (jqXHR) => handleLoginFailure(jqXHR)
        }
    );
}

loginForm.submit(submitLoginForm);