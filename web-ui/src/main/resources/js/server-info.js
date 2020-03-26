var labIDMURL = "https://identity.auth.test.corp.theplatform.com/idm/web/Authentication?form=json&schema=1.1";
var prodIDMURL = "https://identity.auth.theplatform.com/idm/web/Authentication?form=json&schema=1.1";
var serverInfo =
        [
            {
                name:"Twinkle (Lab)",
                endpointServerURL:"https://g9solclg15.execute-api.us-west-2.amazonaws.com",
                endpointStage:"dev",
                endpointIDMURL:labIDMURL
            },
            {
                name:"Rage (Prod)",
                endpointServerURL:"https://fhm5nnh1n3.execute-api.us-west-2.amazonaws.com",
                endpointStage:"SEA1",
                endpointIDMURL:prodIDMURL
            }
        ];

var endpointPrefix = "/dfh/idm/";

var endpoints =
        [
            {
                name:"Agenda",
                path:"agenda"
            },
            {
                name:"Agenda Progress",
                path:"progress/agenda"
            },
            {
                name:"Customer",
                path:"customer"
            },
            {
                name:"Insight",
                path:"insight"
            },
            {
                name:"Operation Progress",
                path:"progress/operation"
            },
            {
                name:"Resource Pool",
                path:"resourcepool"
            },
            {
                name:"Transform",
                path:"transform"
            },
            {
                name:"Agenda Template",
                path:"agendatemplate"
            }
        ];

var serviceEndpopints =
        [
            {
                name:"Ignite",
                path:"agenda/service/ignite"
            },
            {
                name:"Reignite",
                path:"agenda/service/reignite"
            }
        ];

function getEndpointURL(server, endpoint) {
    return server.endpointServerURL + "/" + server.endpointStage + endpointPrefix + endpoint.path;
}

var serviceEndpointsByName = [];
var endpointsByName = [];

// build the reverse lookup maps
serviceEndpopints.forEach(function (item, index) {
    serviceEndpointsByName[item.name] = item;
});

endpoints.forEach(function (item, index) {
    endpointsByName[item.name] = item;
});