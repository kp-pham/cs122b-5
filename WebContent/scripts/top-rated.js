const alertContainer = $("#alert-container")[0];

function handleResult(resultData) {
    let movieTable = jQuery("#movie-table-body");

    resultData.forEach(movie => {
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

jQuery.ajax({
   dataType: "json",
   method: "GET",
   url: "api/",
   success: (resultData) => handleResult(resultData)
});

$
$(document).on("submit", ".cart-form", submitCartForm);