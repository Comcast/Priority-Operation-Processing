// this is indexed by the pulldown
var g_VisibleAgendaTemplates = [];

function getAgendaTemplates(event){
    var endpoint = endpointsByName["Agenda Template"];
    var server = getServer();

    showPopupWithHtml(
            event,
            "<img id=\"fission_spinner_temp\" src=\"images/fission.png\" width=\"40px\" height=\"40px\" class=\"rotate\"><br>Loading Agenda Templates...",
            false);

    makeAuthorizedRequest(event,
            server,
            function(event){
                makeServiceRequest(
                        "GET",
                        getQueryURL(server, endpoint, "", "", "", ""),
                        null,
                        function(response){
                            //console.log(JSON.stringify(response, null, 2));
                            //$("#response").val(JSON.stringify(response, null, 2));
                            addAgendaTemplates(response['all']);
                            showPopup(new Event("success"), false);
                        },
                        function(error){
                            showPopup(new Event("error"), false);
                        });
            },
            function(error){
                showPopup(new Event("error"), false);
            }
    );
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
    makeAuthorizedRequest(event,
        server,
        function(response){
            makeServiceRequest(
                "POST",
                getEndpointURL(server, serviceEndpoint),
                JSON.stringify(igniteRequest),
                function(response){
                    $("#response").val(JSON.stringify(response, null, 2));
                },
                function(error){
                    $("#response").val(JSON.stringify(error, null, 2));
                }
            );
        },
        function(error){
            $("#response").val(JSON.stringify(error, null, 2));
        }
    );
}

function processReigniteCall(event){
    var server = getServer();
    var serviceEndpoint = serviceEndpointsByName["Reignite"];
    var params = [];

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
                        $("#response").val(JSON.stringify(response, null, 2));
                    },
                    function(error){
                        $("#response").val(JSON.stringify(error, null, 2));
                    }
            );
        },
        function(error){
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
