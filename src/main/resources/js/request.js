var g_requestCid = regenerateCid();

function getQueryURL(server, endpoint, querySelectedType, queryValue)
{
    var endpointURL = getEndpointURL(server, endpoint);
    if(querySelectedType == "byId")
    {
        return queryValue == "" ? endpointURL : endpointURL +"/" +queryValue;
    }
    else if(querySelectedType == "other")
    {
        return queryValue == "" ? endpointURL : endpointURL +"?" +queryValue;
    }

    return queryValue == "" ? endpointURL : endpointURL +"?" +querySelectedType +"=" +queryValue;
}

function processRequest(e) {
    e.preventDefault();

    // reset view
    $("#response").val("");
    $("#progressTable").html("");

    if(!validateGeneralInputs())
        return;

    toggleSpinner(true);

    regenerateCid();

    var targetServerIndex = $("#target_server").val();
    var targetEndpointIndex = $("#target_endpoint").val();
    var server = serverInfo[targetServerIndex];
    var endpoint = endpoints[targetEndpointIndex];
    var username = $("#mpx_username").val();
    var password = $("#mpx_password").val();

    performAuthorizeRequest(server.endpointIDMURL, username, password, g_requestCid,
            function(){
                // get the active tab
                var activeTab = getActiveTab();
                if(activeTab != null)
                    window["process" + activeTab.id + "Request"](server, endpoint);
                // TODO: alert on no tab
            },
            function(error){
                console.log(error);
                toggleSpinner(false);
            });
}

function processGETRequest(server, endpoint){
    var queryElement = document.getElementById("get_query_type");
    var querySelectedType = queryElement.options[queryElement.selectedIndex].value;

    performRequest(
        "GET",
        getQueryURL(server, endpoint, querySelectedType, getQueryValue("get_query_value")),
        null,
        function(response) {
            if (endpoint.name === 'Agenda Progress' && $("#showProgressTable").is(":checked"))
            {
                writeAgendaProgressTable(response);
            }
        }
    );

}

function processPOSTRequest(server, endpoint){
    var postData = $("#data_to_post").val();
    if(!verifyJson(postData)){
        toggleSpinner(false);
        return;
    }

    performRequest("POST", getEndpointURL(server, endpoint), postData, null);
}

function processPUTRequest(server, endpoint){
    var putData = $("#data_to_put").val();
    if(!verifyJson(putData)){
        toggleSpinner(false);
        return;
    }
    performRequest("PUT", getEndpointURL(server, endpoint), putData, null);
}

function processDELETERequest(server, endpoint){
    performRequest("DELETE", getQueryURL(server, endpoint, "byId", getQueryValue("delete_query_value")), null, null);
}

function performRequest(httpVerb, url, data, successFunction){
    $.ajax({
        type: httpVerb,
        url: url,
        crossDomain: true,
        jsonp: true,
        contentType: "application/json",
        data: data,
        headers: {
            'Authorization': "Basic " + btoa(getAccountId() + ":" + g_idmToken),
            'Content-Type': "application/json",
            'X-thePlatform-cid': g_requestCid
        },
        success: function (response) {
            toggleSpinner(false);
            $("#response").val(JSON.stringify(response, null, 2));
            $('#json-renderer').jsonViewer(response);
            if(successFunction != null)
                successFunction(response);
        },
        error: function (response) {
            toggleSpinner(false);
            // show an error message
            $('#json-renderer').jsonViewer(response);
            alert("Unsuccessful CID '" + g_requestCid +"'");
        }
    });
}

function getAccountId(){
    return accountId = $("#mpx_account").val();
}

function regenerateCid(){
    g_requestCid = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
    return g_requestCid;
}

function getQueryValue(fieldName){
    var queryValue = $("#"+fieldName).val();

    return encodeURIComponent(queryValue);
}

function verifyJson(json)
{
    try {
        JSON.parse(json);
        return true;
    }
    catch(err) {
        $("#response").val(err);
        return false;
    }
}

function validateGeneralInputs(){
    if ($("#mpx_username").val()=="") {
        alert ("Please enter your user id");
        return false;
    }
    if ($("#mpx_password").val()=="") {
        alert ("Please enter your password");
        return false;
    }
    return true;
}

function toggleSpinner(enable){
    var spinner = $("#fission_spinner");

    if(0 == Math.floor(Math.random() * 3)){
        spinner.attr("src", "images/babs_head.png")
    }
    else{
        spinner.attr("src", "images/fission.png")
    }

    if(enable){
        spinner.css("display", "block");
    }
    else{
        spinner.css("display", "none");
    }
}

function writeAgendaProgressTable(response)
{
    var tableText = "";
    if(response.errorResponse != null) return;
    tableText += "<table>";
    tableText += "<tr>";
    response["all"].forEach(function (item, index) {
        tableText += "<td valign=\"top\"><table border='1' style='border-collapse:collapse'>";
        tableText += "<tr><td>Operation</td><td>ProcessingState</td><td>ProcessingStateMessage</td></tr>";
        item.operationProgress.forEach(function(opProgress, progressIndex){
            tableText += "<tr><td>" + opProgress.operation + "</td><td>" + opProgress.processingState + "</td><td>" + opProgress.processingStateMessage + "</td></tr>";
        });
        tableText += "</table></td>";
    });
    tableText += "</tr>";
    tableText += "</table>";
    document.getElementById("progressTable").innerHTML = tableText;
}
