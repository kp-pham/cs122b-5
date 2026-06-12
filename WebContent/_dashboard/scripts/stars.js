const starsForm = $("#stars-form");
const alertSuccess  = $("#alert-success");
const alertFailure = $("#alert-failure");

function handleSuccess(resultData) {
    alertFailure.addClass("d-none");

    alertSuccess.text(resultData["message"]);
    alertSuccess.removeClass("d-none");
}

function handleFailure(jqXHR) {
    alertSuccess.addClass("d-none");

    alertFailure.text(jqXHR.responseJSON.message ?? "Something went wrong. Please try again.");
    alertFailure.removeClass("d-none");
}

function handleFormSubmit(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    $.ajax({
        url: baseURL + "/api/employees/star",
        method: "POST",
        data: starsForm.serialize(),
        success: (resultData) => handleSuccess(resultData),
        error: (jqXHR) => handleFailure(jqXHR)
    });
}

starsForm.submit(handleFormSubmit);