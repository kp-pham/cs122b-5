function handleResult(resultData) {
    let genresList = $("#genres-list");

    resultData.forEach((genre) => {
        let link = $(`<a href="list.html?genre=${encodeURIComponent(genre)}" class="text-center">${genre}</a>`);
        genresList.append(link);
    });
}

function showPrefixes() {
    let prefixList = $("#prefix-list");
    let prefixes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789*";

    for (const prefix of prefixes) {
        let link = $(`<a href="list.html?prefix=${encodeURIComponent(prefix)}" >${prefix}</a>`);
        prefixList.append(link);
    }
}

function submitSearchForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    let title = $.trim($("input[name=title]").val());
    let year = $.trim($("input[name=year]").val());
    let director = $.trim($("input[name=director]").val());
    let star = $.trim($("input[name=star]").val());

    if (title.length === 0 && year.length === 0 && director.length === 0 && star.length === 0) {
        return;
    }

    let params = [];

    if (title.length !== 0) {
        params.push(`title=${encodeURIComponent(title)}`);
    }

    if (year.length !== 0) {
        params.push(`year=${encodeURIComponent(year)}`);
    }

    if (director.length !== 0) {
        params.push(`director=${encodeURIComponent(director)}`);
    }

    if (star.length !== 0) {
        params.push(`star=${encodeURIComponent(star)}`);
    }

    window.location.href = "list.html?" + params.join("&");
}

showPrefixes();

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/customers/genres",
    success: (resultData) => handleResult(resultData)
});

$("#search-form").submit(submitSearchForm);