// TODO: these need to reset!
var g_idmToken = "";
var g_idmTokenExpireUTC = 0;

function isAuthorizationRequired(){
    return new Date().getTime() > g_idmTokenExpireUTC;
}

function resetTokenInfo(){
    g_idmToken = "";
    g_idmTokenExpireUTC = 0;
}

function updateTokenInfo(idmResponse){
    g_idmToken = idmResponse.signInResponse.token;
    // make the expiration a little fuzzy, 5 minutes before actual expiration
    g_idmTokenExpireUTC = (new Date().getTime() + idmResponse.signInResponse.duration) - (5 * 60 * 1000);
}

function performAuthorizeRequest(idmURL, username, password, cid, successCallback, errorCallback){

    if(!isAuthorizationRequired())
        successCallback();
    else{
        $.ajax({
            type: "POST",
            url: idmURL,
            crossDomain: "true",
            cache:false,
            dataType:"json",
            jsonp: false,

            headers: {
                "Authorization": buildIDMAuthHeader(username, password),
                "Accept": "*/*",
                "Content-Type": "application/json"
                // TODO: if CID is included CORS fails -- 'X-thePlatform-cid': cid
            },
            data: "{\"signIn\": {\"duration\": 86400000, \"idleTimeout\": 14400000}}",

            success: function (idmResponse) {
                //console.log(idmResponse);
                if(idmResponse.isException === true){
                    alert(idmResponse.description);
                }
                else{
                    updateTokenInfo(idmResponse);
                    successCallback();
                }
            },
            error: function (error) {
                errorCallback(error);
            }
        });
    }
}

function buildIDMAuthHeader(username, password){
    return authUserPasswordHeader = "Basic " +  btoa(username + ":" + password);
}