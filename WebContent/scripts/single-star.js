function handleResult(resultData) {
    $('title').text(resultData["name"]);
    $('h3').text(resultData["name"] + ' (' + (resultData["birthYear"] ?? "N/A") + ')');

    let movieTable = jQuery("#movie_table_body");

    resultData["movies"].forEach(movie => {
        let row = `
            <tr>
                <td>
                    <a href="single-movie.html?id=${movie['id']}">${movie["title"]}</a>
                </td>
                <td>${movie["year"]}</td>
                <td>${movie["director"]}</td>
            </tr>
        `;

        movieTable.append(row);
    });
}

let starId = getParameterByName('id');

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/customers/star?id=" + starId,
    success: (resultData) => handleResult(resultData)
});