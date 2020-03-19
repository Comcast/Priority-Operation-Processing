function getAgendaTemplates(event){
    var endpoint = endpointsByName["Agenda Template"];
    var server = getServer();

    toggleSpinner(true);

    processAuthorizeRequest(event,
            server,
            function(event){
                processServiceRequest(
                        "GET",
                        getQueryURL(server, endpoint, "", "", "", ""),
                        null,
                        function(response){
                            //console.log(JSON.stringify(response, null, 2));
                            //$("#response").val(JSON.stringify(response, null, 2));
                            addAgendaTemplates(response['all']);
                            toggleSpinner(false);
                        },
                        function(error){
                            toggleSpinner(false);
                        });
            },
            function(error){
                toggleSpinner(false);
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

// this is a by name object/map
var g_VisibleAgendaTemplates = {};
