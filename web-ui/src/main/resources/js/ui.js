function toggleSpinner(enable){
    toggleSpinnerObject($("#fission_spinner"), enable);
}

function toggleSpinnerObject(spinner, enable){
    if(!defined(spinner)){
        console.log("fission_spinner not found on page. Unable to toggle.");
        return;
    }

    if(0 == Math.floor(Math.random() * 3)){
        spinner.attr("src", "images/babs_head.png")
    }
    else{
        spinner.attr("src", "images/fission.png")
    }

    if(enable){
        spinner.css("display", "block");
    }
    else{
        spinner.css("display", "none");
    }
}

function addTd(text) {
    return "<td>" + text + "</td>";
}

function getServer(){
    var targetServerIndex = $("#target_server").val();
    return serverInfo[targetServerIndex];
}