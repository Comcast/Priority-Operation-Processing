var g_authToken = "";
var g_authTokenExpireUTC = 0;
var g_onAuthorizationCallback = null;

function getAuthToken(){
    return g_authToken;
}

function registerOnAuthorizationCallback(callback){
    g_onAuthorizationCallback = callback;
}

function isAuthorizationRequired(){
    return new Date().getTime() > g_authTokenExpireUTC;
}

function resetTokenInfo(){
    g_authToken = "";
    g_authTokenExpireUTC = 0;
}

function updateTokenInfo(idmResponse){
    g_authToken = idmResponse.signInResponse.token;
    // make the expiration a little fuzzy, 5 minutes before actual expiration
    g_authTokenExpireUTC = (new Date().getTime() + idmResponse.signInResponse.duration) - (5 * 60 * 1000);
}

function performAuthorizeRequest(authURL, username, password, cid, successCallback, errorCallback){
    // TEMP If there is no authorization url just apply a dummy token and call the callback
    if(authURL == null){
        var idmResponse = {
            signInResponse:{
                token: "dummytoken",
                duration: 1000 * 60 * 24 * 7
            }
        };
        updateTokenInfo(idmResponse);
        if(g_onAuthorizationCallback != null)
            g_onAuthorizationCallback(idmResponse);
        successCallback();
        return;
    }

    if(!isAuthorizationRequired())
        successCallback();
    else{
        $.ajax({
            type: "POST",
            url: authURL,
            crossDomain: "true",
            cache:false,
            dataType:"json",
            jsonp: false,

            headers: {
                "Authorization": buildIDMAuthHeader(username, password),
                "Accept": "*/*",
                "Content-Type": "application/json"
                // TODO: if CID is included CORS fails -- 'X-pop-cid': cid
            },
            data: "{\"signIn\": {\"duration\": 86400000, \"idleTimeout\": 14400000}}",

            success: function (idmResponse) {
                //console.log(idmResponse);
                if(g_onAuthorizationCallback != null)
                    g_onAuthorizationCallback(idmResponse);
                if(idmResponse.isException === true){
                    alert(idmResponse.description);
                    if(errorCallback != null)
                        errorCallback(idmResponse);
                }
                else{
                    updateTokenInfo(idmResponse);
                    successCallback(idmResponse);
                }
            },
            error: function (error) {
                if(g_onAuthorizationCallback != null)
                    g_onAuthorizationCallback(null);
                errorCallback(error);
            }
        });
    }
}

function buildIDMAuthHeader(username, password){
    return authUserPasswordHeader = "Basic " +  btoa(username + ":" + password);
}