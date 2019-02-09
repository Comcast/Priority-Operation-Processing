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
    var accountId = $("#mpx_account").val();

    var CID = Math.random().toString(36).substring(2, 15) + Math.random().toString(36).substring(2, 15);
    var authUserPasswordHeader = "Basic " +  btoa(username + ":" + password);

    $.ajax({
        type: "POST",
        url: $("#mpx_idm_url").val(),
        crossDomain: "true",
        cache:false,
        dataType:"json",
        jsonp: false,

        headers: {
            "Authorization": authUserPasswordHeader,
            "Accept": "*/*",
            "Content-Type": "application/json"
        },
        data: "{\"signIn\": {\"duration\": 86400000, \"idleTimeout\": 14400000}}",


//    {"signInResponse":{"token":"XYZ","userId":"http://identity.auth.test.corp.theplatform.com/idm/data/User/mpx/ZZZ","userName":"me@me.com","duration":86400000,"idleTimeout":14400000}}

        success: function (idmResponse) {
            console.log(idmResponse);
            if(idmResponse.isException == true)
            {
                alert(idmResponse.description);
                return;
            }
            $.ajax({
                type: "GET",
                url: getEndpointURL(),
                crossDomain: true,
                jsonp: true,
                contentType: "application/json",
                headers: {
                    'Authorization': "Basic " +btoa(accountId +":" +idmResponse.signInResponse.token),
                    'Content-Type': "application/json",
                    'X-thePlatform-cid': CID
                },
                success: function (response) {
                    document.getElementById("response").value = JSON.stringify(response, null, 2);
                    $('#json-renderer').jsonViewer(response);
                },
                error: function (response) {
                    // show an error message
                    $('#json-renderer').jsonViewer(response);
                    alert("UnSuccessfull CID '" +CID +"'");
                }
            });
        },
        error: function () {
            // show an error message
            alert("UnSuccessfull");
        }
    });


    function getEndpointURL()
    {
        var endpointURL = $("#endpointURL").val();
        var queryElement = document.getElementById("query");
        var querySelectedType = queryElement.options[queryElement.selectedIndex].value;
        var queryValue = document.getElementById("query_value").value;
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
}