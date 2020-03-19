function performLogin(event){
    makeAuthorizedRequest(event, getServer(), null, null);
}

function authorizationChange(idmResponse){
    if(idmResponse != null && !idmResponse.isException)
    {
        var expirationDate = new Date(new Date().getTime() + idmResponse.signInResponse.duration);
        $("#authInfo").html("Authorized<br>Expiration: " + expirationDate.toString());
    }
    else
    {
        $("#authInfo").html("Not authorized");
    }
}

registerOnAuthorizationCallback(authorizationChange);