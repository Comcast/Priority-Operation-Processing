const operationReferenceRegex = new RegExp(/@<(.+?)(::(.+?))?>/g);
const operationStatusColors = {
    WAITING: "#AAAAAA",
    EXECUTING: "#f28ef2",
    COMPLETE: "#22CC22",
    UNKNOWN: "#FFFFFF"
};

const operationFailedColor = "#CC2222";

function processAgendaStatusRequest(e) {
    var targetServerIndex = $("#target_server").val();
    var server = serverInfo[targetServerIndex];
    var endpoint = endpointsByName["Agenda Progress"];
    var fieldsValue = "id,agendaId,percentComplete,attemptsCompleted,maximumAttempts,processingState,processingStateMessage,cid";
    performRequest(
            "GET",
            //getQueryURL(server, endpoint, "byid", "6feff642-93db-404b-952e-21f1161e1f29", 20, fieldsValue),
            getQueryURL(server, endpoint, "byid", "", 20, fieldsValue),
            null,
            function(response) {
                $("#agendaStatusArea").val(JSON.stringify(response, null, 2));
                $("#agendaStatusArea").val($("#agendaStatusArea").val() + "more stuff");
                buildAgendaStatusTable(response);
            }
    );
}

function buildAgendaStatusTable(response) {
    var tableText = "";
    if(response.errorResponse != null)
    {
        $("#agendaStatusTable").innerHTML = tableText;
        return;
    }
    tableText += "<table>";
    tableText += "<tr>";
    tableText += "<td valign=\"top\"><table border='1' style='border-collapse:collapse'>";
    tableText += "<tr><td>Agenda</td><td>State</td><td>Message</td><td>Attempts Completed</td><td>CID</td></tr>";
    response["all"].forEach(function (agendaProgress, index) {
        tableText += "<tr>"
            //+ addTd(agendaProgress.agendaId)
            + addTd("<a onClick=\"requestAgendaNodeViewUpdate(\'" + agendaProgress.agendaId + "\', \'" + agendaProgress.id + "\');\" style=\"cursor: pointer; cursor:"
                        + " hand;\">" + agendaProgress.agendaId + "</a>")
            + addTd(agendaProgress.processingState)
            + addTd(agendaProgress.processingStateMessage)
            + addTd(defined(agendaProgress.attemptsCompleted) ? agendaProgress.attemptsCompleted : "0")
            + addTd(defined(agendaProgress.cid) ? agendaProgress.cid : "unset")
            + "</tr>";
    });
    tableText += "</table></td>";
    //$("agendaStatusTable").innerHTML = tableText;
    document.getElementById("agendaStatusTable").innerHTML = tableText;
}

function addTd(text) {
    return "<td>" + text + "</td>";
}

function requestAgendaNodeViewUpdate(agendaId, agendaProgressId) {
    var targetServerIndex = $("#target_server").val();
    var server = serverInfo[targetServerIndex];
    performRequest(
            "GET",
            getQueryURL(server, endpointsByName["Agenda"], "byid", agendaId, 1, ""),
            null,
            function(response) {
                var agenda = response["all"][0];
                // TODO: would like to do this request at the same time...
                performRequest(
                        "GET",
                        getQueryURL(server, endpointsByName["Agenda Progress"], "byid", agendaProgressId, 1, ""),
                        null,
                        function(response) {
                            var agendaProgress = response["all"][0];
                            setupAgendaNetwork(agenda, agendaProgress);
                        }
                );
            }
    );
}

function setupAgendaNetwork(agenda, agendaProgress) {
    var operationNodes = new Array();
    var operationEdges = new Array();

    var progressMap = {};
    agendaProgress.operationProgress.forEach(function(opProgress, index){
        progressMap[opProgress.operation] = opProgress;
    });

    agenda["operations"].forEach(function (operation, index) {
        buildOperationNode(operationNodes, operation, progressMap[operation.name]);
        buildOperationDependencyEdges(operationEdges, operation);
    });
    // create an array with nodes
    var nodes = new vis.DataSet(operationNodes);
    // create an array with edges
    var edges = new vis.DataSet(operationEdges);
    // create a network
    var container = document.getElementById('mynetwork');
    var data = {
        nodes: nodes,
        edges: edges
    };
    var options ={
        layout: {
            hierarchical: {
                direction: "LR",
                sortMethod: "directed"
            }
        },
        physics: {
            hierarchicalRepulsion: {
                avoidOverlap: 1
            }
        }};
    var network = new vis.Network(container, data, options);
}

function buildOperationNode(operationNodes, operation, operationProgress) {
    var operationNode = {
        id: operation.name,
        label: operation.name
    };
    operationNode.color = operationStatusColors.UNKNOWN;
    if(defined(operationProgress.processingState)
            && operationProgress.processingState != null
            && defined(operationStatusColors[operationProgress.processingState])) {
        if(operationProgress.processingState === "COMPLETE"
                && defined(operationProgress.processingStateMessage) &&
                operationProgress.processingStateMessage === "failed")
            operationNode.color = operationFailedColor;
        else
            operationNode.color = operationStatusColors[operationProgress.processingState];
    }

    operationNodes.push(operationNode);
}

function buildOperationDependencyEdges(operationEdges, operation) {
    if(!defined(operation))
        return;
    var dependencies = {};

    seekDependencies(dependencies, operation);

    if(defined(operation.params))
    {
        // get any dependsOn from the paramsMap
        if(defined(operation.params.dependsOn))
        {
            operation.params.dependsOn.split(",").forEach(function(dependencyName, index){
               dependencies[dependencyName] = true;
            });
        }

        // get the operation generator parent
        if(defined(operation.params.generatedOperationParent))
        {
            dependencies[operation.params.generatedOperationParent] = true;
        }
    }

    for(const dependencyName of Object.keys(dependencies)) {
        operationEdges.push({
            from: dependencyName, to: operation.name, arrows: {to: { enabled: true, type: "arrow" }}
        })
    };
}

// regex learning...
/**
function doStuff()
{
    //let operationReferenceRegex = new RegExp("(@<(.+?)(::(.+?))?>)")
    var sample = "@<this::/path>";
    var result1 = sample.matchAll(operationReferenceRegex);
    for(const match of result1){
       console.log(match);
    };
    var result2 = "@<this>".matchAll(operationReferenceRegex);
    for(const match of result2){
        console.log(match);
    };
    var result3 = "@<this> @<that::/path2>".matchAll(operationReferenceRegex);
    for(const match of result3){
        console.log(match);
    };
}
doStuff();
 */

function seekDependencies(dependencies, obj) {
    Object.keys(obj).forEach(function(field, index){
        if(typeof obj[field] === 'string') {
            var mathces = obj[field].matchAll(operationReferenceRegex);
            for(const match of mathces){
                if(match.length > 1){
                    var reference = match[1];
                    if(reference !== "fission.agendaId"
                            && reference !== "fission.operationName"){
                        dependencies[reference.replace(".out", "")] = true;
                    }
                }
            }
        }
        else if(typeof obj[field] === 'object') {
            seekDependencies(dependencies, obj[field]);
        }
    });
}

function defined(variable) {
    return (typeof variable !== 'undefined');
}