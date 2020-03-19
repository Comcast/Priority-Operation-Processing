// this is a by name object/map
var g_VisibleAgendaTemplates = {};

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
    g_VisibleAgendaTemplates = {};
    if(agendaTemplates != null)
    {
        var agendaTemplateDropDown = $('#ignite_agenda_template_name');
        agendaTemplates.forEach(function (template, index) {
            g_VisibleAgendaTemplates[template.title] = template;
            agendaTemplateDropDown.append("<option value=\""+ index +"\">" + template.title + "</option>")
        });
    }
}

function processIgniteCall(event){

    var server = getServer();
    var serviceEndpoint = serviceEndpointsByName["Ignite"];
    var payloadJson = $("#ignite_payload").val();
    if(!verifyJson(payloadJson)){
        return;
    }
    var igniteRequest = {
        payload: payloadJson,
        agendaTemplateId: null
    };
    makeAuthorizedRequest(event,
        server,
        function(response){
            makeServiceRequest(
                "POST",
                getEndpointURL(server, serviceEndpoint),
                JSON.stringify(igniteRequest),
                function(response){
                    console.log(JSON.stringify(response));
                },
                function(error){
                    console.log(JSON.stringify(error));
                }
            );
        },
        function(error){
            console.log(JSON.stringify(error));
        }
    );
}

function processReigniteCall(event){
    var server = getServer();
    var serviceEndpoint = serviceEndpointsByName["Reignite"];
    var reigniteRequest = {
        agendaId: $("#reignite_agenda_id").val(),
        params: []
    };
    makeAuthorizedRequest(event,
        server,
        function(response){
            makeServiceRequest(
                    "POST",
                    getEndpointURL(server, serviceEndpoint),
                    JSON.stringify(reigniteRequest),
                    function(response){
                        console.log(JSON.stringify(response));
                    },
                    function(error){
                        console.log(JSON.stringify(error));
                    }
            );
        },
        function(error){
            console.log(JSON.stringify(error));
        }
    );
}
