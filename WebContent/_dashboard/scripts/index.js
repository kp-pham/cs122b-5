function handleResult(resultData) {
    const schemas = $("#schemas");

    resultData.forEach(table => {
        let tableHTML = `
            <div class="table">
                <h3>${table["name"]}</h3>
                <table id="${table["name"]}-table" class="table table-striped" style="table-layout: fixed; width: 100%;">
                    <colgroup>
                        <col style="width=50%;">
                        <col style="width=50%;">
                    </colgroup>
                    <thead>
                    <tr>
                        <th>Attribute</th>
                        <th>Type</th>
                    </tr>
                    </thead>
                    <tbody id="${table["name"]}-table-body">
                        ${table["columns"].map(column => {
                            return `
                                <tr>
                                    <td>${column["name"]}</td>
                                    <td>${column["type"].toUpperCase()}</td> 
                                </tr>      
                            `;
                        }).join("")}
                    </tbody>
                </table>
            </div>
        `;

        schemas.append(tableHTML);
    });
}

jQuery.ajax({
    dataType: "json",
    method: "GET",
    url: baseURL + "/api/employees/schema",
    success: (resultData) => handleResult(resultData)
});