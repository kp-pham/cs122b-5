const moviesForm = $("#movies-form");
const alertSuccess  = $("#alert-success");
const alertFailure = $("#alert-failure");

function handleSuccess(resultData) {
    alertFailure.addClass("d-none");

    alertSuccess.text(resultData["message"]);
    alertSuccess.removeClass("d-none");
}

function handleFailure(jqXHR) {
    alertSuccess.addClass("d-none");

    const message = JSON.parse(jqXHR.responseText).message || "Something went wrong. Please try again.";
    alertFailure.text(message);
    alertFailure.removeClass("d-none");
}

function handleFormSubmit(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    $.ajax({
        dataType: "json",
        method: "POST",
        data: moviesForm.serialize(),
        url: baseURL + "/api/employees/movie",
        success: (resultData) => handleSuccess(resultData),
        error: (jqXHR) => handleFailure(jqXHR)
    });
}

moviesForm.submit(handleFormSubmit);