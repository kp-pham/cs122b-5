const fullTextSearch = $("#full-text");

function handleLookup(query, done) {
    console.log("Autocomplete search initiated: ", query);

    const cached = sessionStorage.getItem(query);

    if (cached) {
        console.log("Using cached search results...");

        const suggestions = JSON.parse(cached);

        console.log("Cached suggestion list: ", suggestions);

        done({ suggestions: suggestions });

        return;
    }

    console.log("Sending AJAX request to server...");

    $.ajax({
        "dataType": "json",
        "method": "GET",
        "url": "api/customers/autocomplete?q=" + encodeURIComponent(query),
        "success": (data) => handleLookupAjaxSuccess(data, query, done),
    });
}

function handleLookupAjaxSuccess(data, query, done) {
    console.log("Suggestions list from server: ", data);
    sessionStorage.setItem(query, JSON.stringify(data));

    done({ suggestions: data });
}

function handleSelectSuggestion(suggestion) {
    window.location.href = `single-movie.html?id=${suggestion["data"]["id"]}`;
}

$("#autocomplete").autocomplete({
    lookup: (query, done) => handleLookup(query, done),
    onSelect: (suggestion) => handleSelectSuggestion(suggestion),
    deferRequestBy: 300,
    minChars: 3,
    triggerSelectOnValidInput: false,
    noCache: true,
});

function handleFullTextSearch(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    let query = $.trim($("input[name=q]").val());

    if (query.length === 0) {
        return;
    }

    window.location.href = "list.html?q=" + query;
}

fullTextSearch.submit(handleFullTextSearch);