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
    if ($("#id_value").val()=="") {
        alert ("Please enter your agenda progress ID");
        return;
    }
    var username = $("#mpx_username").val();
    var  password = $("#mpx_password").val();
    var  id_value = $("#id_value").val();

    $.ajax({
        type: "POST",
        url: "https://identity.auth.test.corp.theplatform.com/idm/web/Authentication/signIn?form=json&schema=1.0",
        dataType: "json",
        crossDomain: "true",

        headers: {
            "Authorization": "Basic " + btoa(username + ":" + password),
            "Accept": "*/*",
            "Content-Type": "application/json; charset=utf-8"
        },
        success: function (data) {
            $.ajax({
                type: "GET",
                url: "https://3io93ms3z8.execute-api.us-west-2.amazonaws.com/dev/dfh/idm/progress/agenda/" +id_value,
                dataType: "json",
                crossDomain: "true",
                contentType: "application/json",

                headers: {
                    "Authorization": "Basic " + btoa(data),
                    "Access-Control-Allow-Origin": "*",
                    "Content-Type": "application/json"
                },
                data: JSON.stringify(data),

                success: function () {
                    // clear form and show a success message
                    alert("Successfull");
                    document.getElementById("contact-form").reset();
                    location.reload();
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