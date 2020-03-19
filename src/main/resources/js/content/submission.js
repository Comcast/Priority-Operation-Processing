// this is a by name object/map
var g_VisibleAgendaTemplates = {};

function getAgendaTemplates(event){
    var endpoint = endpointsByName["Agenda Template"];
    var server = getServer();

    showPopupWithHtml(
            event,
            "<img id=\"fission_spinner_temp\" src=\"images/fission.png\" width=\"40px\" height=\"40px\" class=\"rotate\"><br>Loading Agenda Templates...",
            false);

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
