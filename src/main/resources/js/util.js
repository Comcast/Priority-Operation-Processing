function verifyJson(json)
{
    try {
        JSON.parse(json);
        return true;
    }
    catch(err) {
        $("#response").val(err);
        return false;
    }
}

function defined(variable) {
    return (typeof variable !== 'undefined');
}