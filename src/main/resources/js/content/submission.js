// this is indexed by the pulldown
var g_VisibleAgendaTemplates = [];

function getAgendaTemplates(event){
    var endpoint = endpointsByName["Agenda Template"];
    var server = getServer();

    toggleLoading(true);

    makeAuthorizedRequest(event,
            server,
            function(event){
                makeServiceRequest(
                        "GET",
                        getQueryURL(server, endpoint, "", "", "", ""),
                        null,
                        function(response){
                            toggleLoading(false);
                            addAgendaTemplates(response['all']);
                        },
                        function(error){
                            toggleLoading(false);
                            showPopupWithHtml(
                                    new Event("error_get_templates"),
                                    "Error loading AgendaTemplates",
                                    JSON.stringify(error, null, 2));
                        });
            },
            function(error){
                toggleLoading(false);
                showPopupWithHtml(
                        new Event("error_get_templates"),
                        "Error loading AgendaTemplates",
                        JSON.stringify(error, null, 2));
            }
    );
}

function toggleLoading(loading){
    $("#loading_templates").toggle(loading);
    $("#submission_content").toggle(!loading);
}

function addAgendaTemplates(agendaTemplates){
    g_VisibleAgendaTemplates = [];
    if(agendaTemplates != null)
    {
        var agendaTemplateDropDown = $('#ignite_agenda_template_name');
        agendaTemplates.forEach(function (template, index) {
            g_VisibleAgendaTemplates.push(template);
            agendaTemplateDropDown.append("<option value=\""+ index +"\">" + template.title + "</option>")
        });
    }
}

function processIgniteCall(event){

    var server = getServer();
    var serviceEndpoint = serviceEndpointsByName["Ignite"];
    var payloadJson = $("#ignite_payload").val();
    var agendaTemplateName = $("#ignite_agenda_template_name").val();
    var agendaTemplateId = g_VisibleAgendaTemplates[agendaTemplateName].id;
    if(!verifyJson(payloadJson)){
        return;
    }
    var igniteRequest = {
        payload: payloadJson,
        agendaTemplateId: agendaTemplateId
    };

    var spinner = $("#fission_spinner_ignite");
    toggleSpinnerObject(spinner, true);

    makeAuthorizedRequest(event,
        server,
        function(response){
            makeServiceRequest(
                "POST",
                getEndpointURL(server, serviceEndpoint),
                JSON.stringify(igniteRequest),
                function(response){
                    toggleSpinnerObject(spinner, false);
                    $("#response").val(JSON.stringify(response, null, 2));
                },
                function(error){
                    toggleSpinnerObject(spinner, false);
                    $("#response").val(JSON.stringify(error, null, 2));
                }
            );
        },
        function(error){
            toggleSpinnerObject(spinner, false);
            $("#response").val(JSON.stringify(error, null, 2));
        }
    );
}

function processReigniteCall(event){
    var server = getServer();
    var serviceEndpoint = serviceEndpointsByName["Reignite"];
    var params = [];

    var spinner = $("#fission_spinner_reignite");
    toggleSpinnerObject(spinner, true);

    pushParamValueIfSpecified(params, $("#reignite_reset_operations"), "operationsToReset");
    pushParamIfChecked(params, $("#reignite_reset_all"), "resetAll");
    pushParamIfChecked(params, $("#reignite_continue"), "continue");
    pushParamIfChecked(params, $("#reignite_skip_execution"), "skipExecution");

    var reigniteRequest = {
        agendaId: $("#reignite_agenda_id").val(),
        params: params
    };
    makeAuthorizedRequest(event,
        server,
        function(response){
            makeServiceRequest(
                    "POST",
                    getEndpointURL(server, serviceEndpoint),
                    JSON.stringify(reigniteRequest),
                    function(response){
                        toggleSpinnerObject(spinner, false);
                        $("#response").val(JSON.stringify(response, null, 2));
                    },
                    function(error){
                        toggleSpinnerObject(spinner, false);
                        $("#response").val(JSON.stringify(error, null, 2));
                    }
            );
        },
        function(error){
            toggleSpinnerObject(spinner, false);
            $("#response").val(JSON.stringify(error, null, 2));
        }
    );
}

function pushParamValueIfSpecified(params, textbox, paramName){
    if(textbox.val() !== ""){
        params.push(paramName + "=" + textbox.val());
    }
}

function pushParamIfChecked(params, checkbox, paramName){
    if(checkbox.prop("checked")){
        params.push(paramName);
    }
}
