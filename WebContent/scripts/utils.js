const baseURL = window.location.origin + '/' + window.location.pathname.split('/')[1];

const navPlaceholder = $("#nav-placeholder");
navPlaceholder.load("nav.html", () => {
    const logoutForm = $("#logout-form");

    function handleSuccess() {
        window.location.replace("login.html");
    }

    function submitLogoutForm(formSubmitEvent) {
        formSubmitEvent.preventDefault();

        $.ajax({
            url: baseURL + "/api/customers/logout",
            method: "POST",
            success: handleSuccess
        });
    }

    logoutForm.submit(submitLogoutForm);
});

function getParameterByName(target) {
    // Retrieve URL from browser window
    let url = window.location.href;

    //Escape special characters
    target = target.replace(/[\[\]]/g, "\\$&");

    // Build and execute regular expression
    let regex = new RegExp("[?&]" + target + "(=([^&#]*)|&|#|$)");
    let results = regex.exec(url);

    // Handle no matches and empty values
    if (!results) return null;
    if (!results[2]) return '';

    return decodeURIComponent(results[2]);
}