function handleResult(resultData) {
    $("title").text(resultData["title"]);
    $("h3").text(resultData["title"] + " (" + resultData["year"] + ")");

    let movieTable = jQuery("#movie-table-body");

    let row = `
        <tr>
            <td>${resultData['director']}</td>
            <td>${resultData['genres'].join(', ')}</td>
            <td>
                ${resultData['stars'].map(({id, name}) => {
                    return `<a href="single-star.html?id=${id}">${name}</a>`
                }).join(', ')}
            </td>
            <td>${(resultData['rating'] ?? "N/A")}</td>
        </tr>
    `;

    movieTable.append(row);
}

let movieId = getParameterByName('id')

// 404 Page for failure?
jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/customers/movie?id=" + movieId,
    success: (resultData) => handleResult(resultData)
})