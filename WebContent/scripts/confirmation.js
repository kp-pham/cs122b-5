function showOrderDetails() {
    let recordsList = $("#sales-table-body");

    const resultData = JSON.parse(sessionStorage.getItem("sales"));

    resultData["sales"].forEach(record => {
        let row = `
            <tr>
                <td>${record["saleId"]}</td>
                <td>
                    <a href="single-movie.html?id=${record['movieId']}">${record["title"]}</a>
                </td>
                <td>${record["quantity"]}</td>
                <td>$${record["price"]}</td>
                <td>$${record["subtotal"]}</td>
            </tr>
        `;

        recordsList.append(row);
    });

    let salesTable = $("#sales-table");

    let footer = `
        <tfoot id="sales-table-footer">
            <tr>
                <td colspan="3"></td>
                <td class="text-end text-dark fw-bold">Total:</td>
                <td>$${resultData["total"]}</td>
            </tr>
        </tfoot>
    `;

    salesTable.append(footer);

    sessionStorage.removeItem("sales");
}

showOrderDetails();