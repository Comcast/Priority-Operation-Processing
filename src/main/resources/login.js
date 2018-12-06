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
    var  password = $("#mpx_password").val();
    var  id_value = $("#id_value").val();

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


//    {"signInResponse":{"token":"-4mfRXUCyoskRI-Q5taOYdBQAPBOkHBE","userId":"http://identity.auth.test.corp.theplatform.com/idm/data/User/mpx/6111539","userName":"admin@theplatform.com","duration":86400000,"idleTimeout":14400000}}

        success: function (idmResponse) {
            $.ajax({
                type: "GET",
                url: "https://fission.aort.theplatform.com/dev/dfh/idm/progress/agenda/" +id_value,
                crossDomain: true,
                jsonp: true,
                contentType: "application/json",
                headers: {
                    'Authorization': "Basic " +  btoa(idmResponse.signInResponse.token),
                    "Content-Type": "application/json"
                },
                success: function (response) {
                    document.getElementById("response").value = JSON.stringify(response, null, 2);
                    $('#json-renderer').jsonViewer(response);
                },
                error: function () {
                    // show an error message
                    alert("UnSuccessfull");
                }
            });
        },
        error: function () {
            // show an error message
            alert("UnSuccessfull");
        }
    });
}