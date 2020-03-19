function processCRUDRequest(e) {
    // reset view
    $("#response").val("");
    $("#progressTable").html("");

    toggleSpinner(true);

    var server = getServer();
    var targetEndpointIndex = $("#target_endpoint").val();
    var endpoint = endpoints[targetEndpointIndex];

    makeAuthorizedRequest(
            e,
            server,
            function(){
                // get the active tab
                var activeTab = getActiveTab();
                // This calls the processXRequest methods below based on the active tab in the view
                if(activeTab != null) {
                    window["process" + activeTab.id + "Request"](server, endpoint);
                }
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
    var queryValue = getQueryValue("get_query_value");
    var limitValue = getQueryValue("get_limit_value");
    var fieldsValue = getQueryValue("get_fields_value");
    performRequest(
            "GET",
            getQueryURL(server, endpoint, querySelectedType, queryValue, limitValue, fieldsValue),
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
    performRequest("DELETE", getQueryURL(server, endpoint, "byId", getQueryValue("delete_query_value"), "", ""), null, null);
}

function performRequest(httpVerb, url, data, successFunction){
    makeServiceRequest(
        httpVerb,
        url,
        data,
        function (response) {
            toggleSpinner(false);
            $("#response").val(JSON.stringify(response, null, 2));
            $('#json-renderer').jsonViewer(response);
            if(successFunction != null)
                successFunction(response);
        },
        function (response) {
            toggleSpinner(false);
            // show an error message
            $('#json-renderer').jsonViewer(response);
        }
    );
}