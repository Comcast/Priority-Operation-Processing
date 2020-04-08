let consoleJs = new ConsoleJS();

function attemptLoomGetContext() {
    consoleJs
            .getContext()
            .then(e => loomContextSuccess(e))
            .catch(e => loomContextFail(e));
}

function loomContextSuccess(response){
    $("#fission_main_logo").attr("src", "images/fission_logo_2.png");
    // docs say the tokens are 30 minutes (the response doesn't say)
    updateTokenInfo({
        signInResponse :{
            token: response.token,
            duration: 30 * 60 * 1000
        }
    });
    // this line does not work :(
    $("#target_server").val(1).change();
    g_ignoreUserPass = true;
    $("#form-login-username").toggle(false);
    $("#form-login-password").toggle(false);
}

function loomContextFail(error){
    if(defined(error.error) && error.error.indexOf('Window must be in a console iframe')){
        console.log("You're not in Loom. You'll have to login manually.");
    }
    else{
        console.log("getContextError: " + JSON.stringify(
                data,
                null,
                2
        ));
    }
}


function handleGetContext() {
    log("");
    consoleJs
            .getContext()
            .then(e => log(e))
.catch(e => logError(e));
}

function handleKeepAlive() {
    log("");
    consoleJs
            .keepAlive()
            .then(e => log(e))
.catch(e => logError(e));
}

function log(data) {
    console.log(JSON.stringify(
            data,
            null,
            2
    ));
}

function logError(data) {
    console.log("error" + JSON.stringify(
            data,
            null,
            2
    ));
}