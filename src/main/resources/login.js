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

    var data = {
        username : username,
        password : password
    };

    $.ajax({
        type: "POST",
        url : "https://atcfap5cvk.execute-api.us-west-2.amazonaws.com/dev/dfh/idm/progress",
        dataType: "json",
        crossDomain: "true",
        contentType: "application/json; charset=utf-8",

        headers: {
            "Authorization": "Basic " + btoa(username + ":" + password),
            "Access-Control-Allow-Origin": "*",
            "Content-Type": "application/json"
        },
        //"Access-Control-Allow-Credentials":"true",
       // "Access-Control-Allow-Methods": "GET, POST, PATCH, PUT, DELETE, OPTIONS",
       // "Access-Control-Allow-Headers": "Access-Control-Allow-Headers, Origin,Accept, X-Requested-With, Content-Type, Access-Control-Request-Method,
        // Access-Control-Request-Headers"

       // data: JSON.stringify(data),

        success: function () {
            // clear form and show a success message
            alert("Successfull");
            document.getElementById("contact-form").reset();
            location.reload();
        },
        error: function () {
            // show an error message
            alert("UnSuccessfull");
        }});
}