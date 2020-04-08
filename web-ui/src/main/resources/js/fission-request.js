var g_requestCid = regenerateCid();

var g_ignoreUserPass = false;

function regenerateCid(){
    g_requestCid = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
    return g_requestCid;
}

function validateCredentialInputs(){
    if(g_ignoreUserPass)
        return true;

    if ($("#modlgn-username").val()==="") {
        alert ("Please enter your user id");
        return false;
    }
    if ($("#modlgn-passwd").val()==="") {
        alert ("Please enter your password");
        return false;
    }
    return true;
}

// This method is a wrapper around the idm request (only making the call to identity if necessary)
// The success callback should be a function to make the underlying request once the token is known to be good.
// Highly recommend using makeServiceRequest as part of the successCallback
function makeAuthorizedRequest(e, server, successCallback, failCallback) {
    e.preventDefault();

    if(!validateCredentialInputs())
        return;

    regenerateCid();

    var username = $("#modlgn-username").val();
    var password = $("#modlgn-passwd").val();

    performAuthorizeRequest(server.endpointIDMURL, username, password, g_requestCid,
            function(idmResponse){
                if(successCallback != null)
                    successCallback(idmResponse);
            },
            function(error){
                console.log(error);
                if(failCallback != null)
                    failCallback(error);
            });
}

// NOTE: This is intended for use as part of a successCallback to processAuthorizedRequest
function makeServiceRequest(httpVerb, url, data, successCallback, failCallback){
    $.ajax({
        type: httpVerb,
        url: url,
        crossDomain: true,
        jsonp: true,
        contentType: "application/json",
        data: data,
        headers: {
            'Authorization': "Basic " + btoa(getAccountId() + ":" + getAuthToken()),
            'Content-Type': "application/json",
            'X-pop-cid': g_requestCid
        },
        success: function (response) {
            if(successCallback != null)
                successCallback(response);
        },
        error: function (response) {
            if(failCallback != null)
                failCallback(response)
            resetTokenInfo();
            alert("Unsuccessful CID '" + g_requestCid +"' -- Identity token has been reset. Please try the request again.");
        }
    });
}

function getAccountId(){
    return accountId = $("#mpx_account").val();
}

function getQueryValue(fieldName){
    var queryValue = $("#"+fieldName).val();

    return queryValue;
}

function getQueryURL(server, endpoint, querySelectedType, queryValue, limitValue, fieldsValue)
{
    var endpointURL = getEndpointURL(server, endpoint);
    var queryParams = new Array();
    appendQueryParamToArray(queryParams, "byfields=", fieldsValue);
    appendQueryParamToArray(queryParams, "bylimit=", limitValue);

    if(querySelectedType == "byId")
    {
        // Note: byId is an endpoint, not a query param
        endpointURL = queryValue == "" ? endpointURL : endpointURL + "/" + encodeURIComponent(queryValue);
    }
    else if(querySelectedType == "other")
    {
        appendQueryParamToArray(queryParams, "", queryValue);
    }
    else
    {
        appendQueryParamToArray(queryParams, querySelectedType + "=", encodeURIComponent(queryValue));
    }

    if(queryParams.length > 0)
        endpointURL = endpointURL + "?" + queryParams.join("&")

    return endpointURL;
}

// basic util for appending non-empty valued queries (DO NOT include the & or ?)
function appendQueryParamToArray(array, queryPrefix, queryValue) {
    if(queryValue != "")
        array.push(queryPrefix + queryValue);
}
