const alertContainer = $("#alert-container")[0];

function encodeParams(params) {
    return params.toString().replace(/\+/g, "%20");
}

function handleResult(resultData) {
    let state = JSON.parse(sessionStorage.getItem("movieListState"));

    const sort = state["sort"];
    const page = Number(state["page"]);
    const pageSize = Number(state["pageSize"]);

    const sortDropdown = jQuery("#sort");
    sortDropdown.val(sort);

    let pageSizeDropdown = jQuery("#pageSize");
    pageSizeDropdown.val(pageSize);

    let pageLookup = jQuery("#page-lookup");
    pageLookup.append(`
        <form id="previous-page" method="GET" action="#" class="page-form">
            <input type="hidden" id="page" value="${page - 1}">
            <button type="submit" class="rounded text-white bg-dark" 
                    ${(page - 1 < 1 || resultData["outOfBounds"]) ? "disabled" : ""}>\<</button>
        </form>
        <input type="text" pattern="[0-9]+" id="page" 
               value="${!resultData["outOfBounds"] ? page : ""}" disabled>
        <form id="next-page" method="GET" action="#" class="page-form">
            <input type="hidden" id="page" value="${page + 1}">
            <button type="submit" class="rounded text-white bg-dark"
                    ${resultData["lastPage"] ? "disabled" : ""}>\></button>
        </form>
    `);

    let movieTable = jQuery("#movie-table-body");

    resultData["results"].forEach(movie => {
        let row = `
            <tr>
                <td>
                    <a href="single-movie.html?id=${movie['id']}">${movie['title']}</a>
                </td>
                <td>${movie['year']}</td>
                <td>${movie['director']}</td>
                <td>${movie['genres'].slice(0, 3).join(', ')}</td>
                <td>
                    ${movie['stars'].slice(0, 3).map(({id, name}) => {
                        return `<a href="single-star.html?id=${id}">${name}</a>`
                    }).join(', ')}
                </td>
                <td>${(movie['rating'] ?? "N/A")}</td>
                <td>
                    <form class="cart-form" method="POST" action="#">
                        <input type="hidden" name="id" value="${movie['id']}">
                        <button type="submit" class="rounded text-white bg-dark">Add</button>
                    </form>
                </td>
            </tr>
        `;

        movieTable.append(row);
    });
}

function hasSearchParams() {
    return window.location.search.length > 0;
}

function isValid(state) {
    return state && (state.type === "browse" || state.type === "search");
}

function showResults() {
    let state = null;

    let storedState = JSON.parse(sessionStorage.getItem("movieListState"));

    if (hasSearchParams()) {
        const genre = getParameterByName("genre");
        const prefix = getParameterByName("prefix");
        const title = getParameterByName("title");
        const year = getParameterByName("year");
        const director = getParameterByName("director");
        const query = getParameterByName("q");
        const star = getParameterByName("star");
        const sort = getParameterByName("sort") || "title-asc-rating-desc";
        const page = getParameterByName("page") || 1;
        const pageSize = getParameterByName("pageSize") || 25;

        if (genre != null) {
            state = {
                type: "browse",
                genre: genre,
                sort: sort,
                page: page,
                pageSize: pageSize
            }
        } else if (prefix != null) {
            state = {
                type: "browse",
                prefix: prefix,
                sort: sort,
                page: page,
                pageSize: pageSize
            }
        } else if (query != null) {
            state = {
                type: "full-text",
                q: query,
                sort: sort,
                page: page,
                pageSize: pageSize
            }
        } else {
            state = {
                type: "search",
                title: title,
                year: year,
                director: director,
                star: star,
                sort: sort,
                page: page,
                pageSize: pageSize
            }
        }

        sessionStorage.setItem("movieListState", JSON.stringify(state));

    } else if (isValid(storedState)) {
        state = storedState;

    } else {
        return;
    }

    const params = new URLSearchParams();
    Object.entries(state).forEach(([key, value]) => {
        if (key !== "type" && value != null) {
            params.append(key, value);
        }
    });

    jQuery.ajax({
        dataType: "json",
        method: "GET",
        url: `api/customers/${state["type"]}?${encodeParams(params)}`,
        success: (resultData) => handleResult(resultData)
    });
}

function showSuccess() {
    alertContainer.insertAdjacentHTML("afterbegin", `
        <div class="alert alert-success alert-dismissible fade show position-absolute top-0 w-100 m-0 rounded-0" role="alert">
            Successfully added movie to cart!
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `);
}

function showFailure() {
    alertContainer.insertAdjacentHTML("afterbegin", `
        <div class="alert alert-warning alert-dismissible fade show position-absolute top-0 w-100 m-0 rounded-0" role="alert">
            Something went wrong, please try again.
            <button type="button" class="btn-close" data-bs-dismiss="alert" aria-label="Close"></button>
        </div>
    `);
}

function submitCartForm(submitFormEvent) {
    submitFormEvent.preventDefault();

    let id = $(this).find("input[name='id']").val();

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: `api/customers/cart?action=add&id=${encodeURIComponent(id)}`,
        success: showSuccess,
        failure: showFailure
    });
}

function submitOptionsForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    const state = JSON.parse(sessionStorage.getItem("movieListState"));
    const params = new URLSearchParams();

    Object.entries(state).forEach(([key, value]) => {
        if (key !== "type" && value != null) {
            params.append(key, value);
        }
    });

    const sort = $("select[name=sort]").val();
    params.set("sort", sort);

    const pageSize = $("select[name=pageSize]").val();
    params.set("pageSize", pageSize);

    window.location.href = `list.html?${encodeParams(params)}`;
}

function submitPageForm(formSubmitEvent) {
    formSubmitEvent.preventDefault();

    const state = JSON.parse(sessionStorage.getItem("movieListState"));
    const params = new URLSearchParams();

    Object.entries(state).forEach(([key, value]) => {
        if (key !== "type" && value != null) {
            params.append(key, value);
        }
    });

    const page = $(this).find("input[id='page']").val();
    params.set("page", page);

    window.location.href = `list.html?${encodeParams(params)}`;
}

showResults();

$("#options-form").submit(submitOptionsForm);
$(document).on("submit", ".page-form", submitPageForm);
$(document).on("submit", ".cart-form", submitCartForm);