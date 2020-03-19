function verifyJson(json)
{
    try {
        JSON.parse(json);
        return true;
    }
    catch(err) {
        // TODO: need to provide a callback function not directly reference #response
        $("#response").val(err);
        return false;
    }
}

function defined(variable) {
    return (typeof variable !== 'undefined');
}