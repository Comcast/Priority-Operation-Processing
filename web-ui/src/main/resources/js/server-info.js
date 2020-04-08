var testAuthURL = null;
var serverInfo =
        [
            {
                name:"POP(Sample)",
                // This should be the url to your API Gateway (do not include the stage!)
                endpointServerURL:"https://[your url here WITHOUT the stage]",
                endpointStage:"dev",
                endpointIDMURL:testAuthURL
            }
        ];

var endpointPrefix = "/fission/";

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
                name:"rerun",
                path:"agenda/service/rerun"
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
