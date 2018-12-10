function authorizeWithLambda(e) {
    e.preventDefault();

    if ($("#mpx_username").val()=="") {
        alert ("Please enter your user id");
        return;
    }
    if ($("#mpx_password").val()=="") {
        alert ("Please enter your password");
        return;
    }
    var username = $("#mpx_username").val();
    var password = $("#mpx_password").val();
    var id_value = $("#id_value").val();
    var endpointURL = $("#endpointURL").val();
    var CID = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);

    $.ajax({
        type: "POST",
        url: "https://identity.auth.test.corp.theplatform.com/idm/web/Authentication?form=json&schema=1.1",
        crossDomain: "true",
        cache:false,
        dataType:"json",
        jsonp: false,

        headers: {
            "Authorization": "Basic " + btoa(username + ":" + password),
            "Accept": "*/*",
            "Content-Type": "application/json"
        },
        data: "{\"signIn\": {\"duration\": 86400000, \"idleTimeout\": 14400000}}",


//    {"signInResponse":{"token":"XYZ","userId":"http://identity.auth.test.corp.theplatform.com/idm/data/User/mpx/ZZZ","userName":"me@me.com","duration":86400000,"idleTimeout":14400000}}

        success: function (idmResponse) {
            var url = id_value == "" ? endpointURL : endpointURL +"/" +id_value
            $.ajax({
                type: "GET",
                url: url,
                crossDomain: true,
                jsonp: true,
                contentType: "application/json",
                headers: {
                    'Authorization': "Basic " +  btoa(idmResponse.signInResponse.token),
                    'Content-Type': "application/json",
                    'X-thePlatform-cid': CID
                },
                success: function (response) {
                    document.getElementById("response").value = JSON.stringify(response, null, 2);
                    $('#json-renderer').jsonViewer(response);
                },
                error: function () {
                    // show an error message
                    alert("UnSuccessfull CID '" +CID +"'");
                }
            });
        },
        error: function () {
            // show an error message
            alert("UnSuccessfull");
        }
    });
}