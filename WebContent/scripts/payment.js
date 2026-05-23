const paymentForm = $("#payment-form");
const errorMessage = $("#payment_error_message");

function handleResult(resultData) {
    $("#total").text(`Order Total: $${resultData["total"].toFixed(2)}`);
}

function handleSuccess(resultData) {
    sessionStorage.setItem("sales", JSON.stringify(resultData));
    window.location.href = "confirmation.html";
}

function handleFailure(jqXHR) {
    errorMessage.removeClass("d-none");
    errorMessage.text(jqXHR.responseJSON.message ?? "Unexpected error occurred");
}

function submitPaymentForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        data: paymentForm.serialize(),
        url: "api/customers/transactions",
        success: (resultData) => handleSuccess(resultData),
        error: (jqXHR) => handleFailure(jqXHR)
    });
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/customers/transactions",
    success: (resultData) => handleResult(resultData)
});

paymentForm.submit(submitPaymentForm);