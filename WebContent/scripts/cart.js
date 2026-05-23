function handleResult(resultData) {
    let itemsList = $("#items-table-body");
    let rows = "";

    resultData["items"].forEach(item => {
        rows += `
            <tr id="${item['id']}">
                <td>
                    <a href="single-movie.html?id=${item['id']}">${item['title']}</a>
                </td>
                <td class="d-flex gap-2">
                    <form class="subtract-form" method="POST" action="#">
                        <input type="hidden" name="id" value="${item['id']}">
                        <button type="submit" class="rounded text-white bg-dark" ${item["quantity"] <= 1 ? "disabled" : ""}>-</button>
                    </form>
                    <span class="quantity">${item['quantity']}</span>
                    <form class="add-form" method="POST" action="#">
                        <input type="hidden" name="id" value="${item['id']}">
                        <button type="submit" class="rounded text-white bg-dark">+</button>
                    </form>
                </td>
                <td>
                    <form class="remove-form" method="POST" action="#">
                        <input type="hidden" name="id" value="${item['id']}">
                        <button class="rounded text-white" style="background-color: #fe0000; border-color: #fe0000;">Remove</button>
                    </form>
                </td>
                <td>$${item['price'].toFixed(2)}</td>
                <td>$${item['subtotal'].toFixed(2)}</td>
            </tr>
        `;
    });

    itemsList.html(rows);

    let itemsTable = $("#items-table");

    let footer = `
        <tfoot id="items-table-footer">
            <tr>
                <td colspan="3"></td>
                <td class="text-end text-dark fw-bold">Total:</td>
                <td>$${resultData["total"]}</td>
            </tr>
        </tfoot>
    `;

    $("#items-table-footer").remove();
    itemsTable.append(footer);

    const items = $("#items-table tbody tr").length;
    $("#proceed-button").prop("disabled", items === 0);
}

function submitAddForm(submitFormEvent) {
    submitFormEvent.preventDefault();

    let id = $(this).find("input[name='id']").val();

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: `api/customers/cart?action=add&id=${encodeURIComponent(id)}`,
        success: (resultData) => handleResult(resultData)
    });
}

function submitSubtractForm(submitFormEvent) {
    submitFormEvent.preventDefault();

    let id = $(this).find("input[name='id']").val();

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: `api/customers/cart?action=subtract&id=${encodeURIComponent(id)}`,
        success: (resultData) => handleResult(resultData)
    });
}

function submitRemoveForm(submitFormEvent) {
    submitFormEvent.preventDefault();

    let id = $(this).find("input[name='id']").val();

    jQuery.ajax({
        dataType: "json",
        method: "POST",
        url: `api/customers/cart?action=remove&id=${encodeURIComponent(id)}`,
        success: (resultData) => handleResult(resultData)
    });
}

function submitProceedForm(submitFormEvent) {
    submitFormEvent.preventDefault();
    window.location.href = "payment.html";
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: "api/customers/cart",
    success: (resultData) => handleResult(resultData)
});

$(document).on("submit", ".add-form", submitAddForm);
$(document).on("submit", ".subtract-form", submitSubtractForm);
$(document).on("submit", ".remove-form", submitRemoveForm);

$("#proceed-form").submit(submitProceedForm);