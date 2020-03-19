const operationReferenceRegex = new RegExp(/@<(.+?)(::(.+?))?>/g);
const operationStatusColors = {
    WAITING: "#AAAAAA",
    EXECUTING: "#f28ef2",
    COMPLETE: "#22CC22",
    UNKNOWN: "#FFFFFF"
};

const operationFailedColor = "#CC2222";

var currentAgendaProgressObjects = [];

function showAgendaStatusList(event){
    var endpoint = endpointsByName["Agenda Progress"];
    var server = getServer();

    toggleSpinner(true);

    processAuthorizeRequest(event,
            server,
            function(event){
                var queryElement = document.getElementById("get_query_type");
                var querySelectedType = queryElement.options[queryElement.selectedIndex].value;
                var queryValue = getQueryValue("get_query_value");
                var limitValue = getQueryValue("get_limit_value");

                processServiceRequest(
                        "GET",
                        getQueryURL(server, endpoint, querySelectedType, queryValue, limitValue, ""),
                        null,
                        function(response){
                            $("#response").val(JSON.stringify(response, null, 2));
                            buildAgendaStatusTable(response);
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

function buildAgendaStatusTable(response) {
    var tableText = "";
    if(response.errorResponse != null)
    {
        $("#agendaStatusTable").innerHTML = tableText;
        return;
    }
    tableText += "<table class=\"table-bordered\">";
    tableText += "<thead><tr><th>Agenda</th><th>State</th><th>Message</th><th>Attempts Completed</th><th>CID</th></tr></thead>";
    tableText += "<tbody>";
    response["all"].forEach(function (agendaProgress, index) {
        tableText += "<tr>"
                //+ addTd(agendaProgress.agendaId)
                + addTd("<a onClick=\"requestAgendaNodeViewUpdate(event, \'" + agendaProgress.agendaId + "\', \'" + agendaProgress.id + "\');\" style=\"cursor: pointer; cursor:"
                        + " hand;\">" + agendaProgress.agendaId + "</a>")
                + addTd(agendaProgress.processingState)
                + addTd(agendaProgress.processingStateMessage)
                + addTd(defined(agendaProgress.attemptsCompleted) ? agendaProgress.attemptsCompleted : "0")
                + addTd(defined(agendaProgress.cid) ? agendaProgress.cid : "unset")
                + "</tr>";
    });
    tableText += "</tbody></table>";
    $("#agendaStatusTable").html(tableText);
}

function requestAgendaNodeViewUpdate(event, agendaId, agendaProgressId) {
    var server = getServer();
    toggleSpinner(true);
    processAuthorizeRequest(event,
        getServer(),
        processServiceRequest(
                "GET",
                getQueryURL(server, endpointsByName["Agenda"], "byid", agendaId, 1, ""),
                null,
                function(response) {
                    var agenda = response["all"][0];
                    // TODO: would like to do this request at the same time...
                    processServiceRequest(
                            "GET",
                            getQueryURL(server, endpointsByName["Agenda Progress"], "byid", agendaProgressId, 1, ""),
                            null,
                            function(response) {
                                var agendaProgress = response["all"][0];
                                setupAgendaNetwork(agenda, agendaProgress);
                                writeSingleAgendaProgressTable(response);
                                switchToSingleAgendaView(agendaId);
                                toggleSpinner(false);
                            }
                    );
                },
                function (error) {
                    toggleSpinner(false);
                }
        ),
        function (error) {
            toggleSpinner(false);
        }
    );
}

function switchToSingleAgendaView(agendaId){
    $("#singleAgendaTitle").html("<p class=\"topic-header\">Agenda</p><p>" + agendaId +"</p>");
    updateViewState(true);
}

function updateViewState(showSingleAgenda){
    $("#singleAgendaView").toggle(showSingleAgenda);
    $("#mynetwork").toggle(showSingleAgenda);
    $("#agendaStatusView").toggle(!showSingleAgenda);
}

function writeSingleAgendaProgressTable(response)
{
    var tableText = "";
    if(response.errorResponse != null) return;
    currentAgendaProgressObjects = [];
    response["all"].forEach(function (item, agendaProgressIndex) {
        currentAgendaProgressObjects.push(item);
        tableText += "<table class=\"table-bordered\">";
        tableText += "<thead><tr><th>Operation</th><th>ProcessingState</th><th>ProcessingStateMessage</th><th>Error</th></tr></thead>";
        tableText += "<tbody>";
        item.operationProgress.forEach(function(opProgress, operationProgressIndex){
            var errorMessage = "";
            var popupButton = "";
            if(opProgress.diagnosticEvents != null)
            {
                errorMessage = "<div class=\"expandable\"><input id=\"toggle\" type=\"checkbox\" hidden>"
                        +"<label for=\"toggle\" class=\"expand-label\">SHOW ERROR</label>"
                        +"<div id=\"expand\">"
                        + "<textarea id=\"response\" name=\"response\" style=\"width:100%;height:100%;\">" +opProgress.diagnosticEvents[0].stackTrace +"</textarea>"
                        + "</div></div>";
                popupButton = "<input type=\"submit\" value=\"Show Error\" name=\"show_error_" + opProgress.operation + "\"" +
                        "onclick=\"showOpProgressError(event," + agendaProgressIndex + "," + operationProgressIndex + ", true);\" class=\"btn btn-primary submit-button\"/>"

            }
            tableText += "<tr><td>" + opProgress.operation + "</td><td>" + opProgress.processingState + "</td><td>" + opProgress.processingStateMessage + "</td>";
            tableText +=  "<td>" + popupButton + errorMessage +"</td></tr>";
        });
        tableText += "<tr><td>Overall Status</td><td>" + item.processingState + "</td><td>" + item.processingStateMessage + "(" + item.percentComplete + ")</td></tr>";
        tableText += "</tbody></table>";
    });
    document.getElementById("progressTable").innerHTML = tableText;
}

function showOpProgressError(event, agendaProgressIndex, operationProgressIndex){
    var opProgress = currentAgendaProgressObjects[agendaProgressIndex].operationProgress[operationProgressIndex];
    var textAreaHTML = "<textarea id='response' name='response' style='width:80%;height:100%;'>" +opProgress.diagnosticEvents[0].stackTrace +"</textarea>";
    showPopupWithHtml(event, textAreaHTML, true);
}

function setupAgendaNetwork(agenda, agendaProgress) {
    var operationNodes = new Array();
    var operationEdges = new Array();

    var progressMap = {};
    agendaProgress.operationProgress.forEach(function (opProgress, index) {
        progressMap[opProgress.operation] = opProgress;
    });

    if (agenda && agenda["operations"])
    {
        agenda["operations"].forEach(function (operation, index) {
            buildOperationNode(operationNodes, operation, progressMap[operation.name]);
            buildOperationDependencyEdges(operationEdges, operation);
        });
    } else {
        $("#mynetwork").empty();
        alert( "No agenda found.");
        return;
    }

    var dependencyMap = new Map();

    // build a map of direct dependencies
    operationEdges.forEach(function (edge, index) {
        var existing = dependencyMap.get(edge.to);
        if(!existing) {
            existing = new Set();
            dependencyMap.set(edge.to, existing);
        }
        existing.add(edge.from);
    });

    //hack: add all parents edges, poorly
    for (let step = 0; step < operationEdges.length; step++) {
        operationEdges.forEach(function (edge, index) {
            var existing = dependencyMap.get(edge.to);
            var parent = dependencyMap.get(edge.from);
            if(parent)
            {
                parent.forEach(existing.add, existing);
            }
        });
    }

    //now all contain full dependencies to top, so if we remove parents deps, we end up with only unique
    dependencyMap.forEach(function (value, key, map)
    {
        var allParents = new Set();
        value.forEach(function (value1, value2, set) {
            var parent = dependencyMap.get(value1);
            if (parent)
            {
                parent.forEach(allParents.add, allParents);
            }
        });

        allParents.forEach(value.delete, value);
    });

    //finally remove edges not in it.
    var minimizedEdges = new Array();
    operationEdges.forEach(function (edge, index) {
        if(dependencyMap.get(edge.to).has(edge.from)) {
            minimizedEdges.push(edge);
        }
    });

    // now we're going to find each nodes max distance to root.
    var depthMap = new Map();
    var nodeMap = new Map();
    var nodeDepthMap = new Map();

    operationNodes.forEach(function (node, index) {
        var depth = getDepth(node.id, dependencyMap, depthMap, 0);
        var existing = depthMap.get(depth);
        if (!existing)
        {
            existing = new Set();
            depthMap.set(depth, existing);
        }
        existing.add(node.id);
        nodeDepthMap.set(node.id, depth);
        nodeMap.set(node.id, node);
    });

    //now we build a tree of depths
    var treeData = new Array();
    var nodes = depthMap.get(0);
    if (!nodes) {
        nodes = new Set();
    }
    var shrinkingNodeMap = new Map(nodeMap);
    nodes.forEach(function (value, value2, set) {
        var treeNode = {
            name: value,
            parent: null,
            color: nodeMap.get(value).color,
            children: getChildren(value, shrinkingNodeMap, depthMap, dependencyMap, 1)
        };
        treeData.push(treeNode);
    });

    var width = 954;

    let tree = data => {
        const root = d3.hierarchy(data);
        root.dx = 50;
        root.dy = width / (root.height + 1);
        return d3.tree().nodeSize([root.dx, root.dy])(root);
    };

    //draw the tree
    let x0 = Infinity;
    let x1 = -x0;
    //todo - if multiple roots, need to handle. or add a 'total' with overall progress.
    const root = tree(treeData[0]);
    root.each(d => {
        if (d.x > x1) x1 = d.x;
    if (d.x < x0) x0 = d.x;
});

    // wipte the existing render
    $("#mynetwork").empty();
    // instead of append, need to clear, else multiclicks means multipics
    const svg = d3.select("#mynetwork").append("svg")
            .attr("viewBox", [0, 0, width, (x1 - x0 + root.dx) * 2]);

    const g = svg.append("g")
            .attr("font-family", "sans-serif")
            .attr("font-size", 10)
            .attr("transform", `translate(${root.dy / 3},${(root.dx - x0)})`);

    var links = root.links();

    //todo - should really link from the dependencies, not from tree links.
    const link = g.append("g")
            .attr("fill", "none")
            .attr("stroke", "#555")
            .attr("stroke-opacity", 0.4)
            .attr("stroke-width", 1.5)
            .selectAll("path")
            .data(root.links())
            .join("path")
            .attr("d", d3.linkHorizontal()
                    .x(d => d.y)
            .y(d => d.x));

    const node = g.append("g")
            .attr("stroke-linejoin", "round")
            .attr("stroke-width", 3)
            .selectAll("g")
            .data(root.descendants())
            .join("g")
            .attr("transform", d => `translate(${d.y},${d.x})`);

    node.append("g")
            .attr("class", "nodes")
            .append(d => loadLiquidFillGaugeSVG(nodeMap.get(d.data.name)).node())
.attr("transform", `translate(-25,-25)`);

    var text = node.append("text")
            .attr("dy", "25")
            // .attr("dx", "5")
            // .attr("x", d => d.children ? -6 : 6)
            .attr("text-anchor", "middle")
            .text(d => d.data.name)
.clone(true).lower()
            .attr("stroke", "white");
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
        {
            operationNode.color = operationFailedColor;
        }
        else
        {
            operationNode.color = operationStatusColors[operationProgress.processingState];
        }
    }
    if(defined(operationProgress.percentComplete)) {
        operationNode.percentComplete = operationProgress.percentComplete;
    } else if (defined(operationProgress.processingState) && operationProgress.processingState === "COMPLETE") {
        operationNode.percentComplete = 100;
    } else {
        operationNode.percentComplete = 0;
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
            from: dependencyName,
            to: operation.name,
            arrows: {to: { enabled: true, type: "arrow" }},
            source:dependencyName,
            target: operation.name
        })
    }
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
            var matches = obj[field].matchAll(operationReferenceRegex);
            for(const match of matches){
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

function getChildren(parentName, nodeMap, depthMap, dependencies, depth) {
    var tree = new Array();
    var potentialNames = depthMap.get(depth);

    if(!potentialNames) {
        return null;
    }
    potentialNames.forEach(function (value, value2, set) {
        var deps = dependencies.get(value);
        //if its a dep, and not already accounted for, we add it
        if(deps.has(parentName) && nodeMap.has(value)) {
            var treeNode = {
                name: value,
                parent: parentName,
                color: nodeMap.get(value).color,
                children: getChildren(value, nodeMap, depthMap, dependencies, depth+1)
            };
            nodeMap.delete(value);
            tree.push(treeNode);
        }
    });
    return tree;
}

function getDepth(nodeId, dependencyMap, depthMap, depth) {
    var dependencies = dependencyMap.get(nodeId);
    if (!dependencies)
    {
        return depth;
    }
    var maxDepth = 0;
    dependencies.forEach(function (value, value2, set) {
        var depth = depthMap.get(value);
        if (!depth)
        {
            depth = getDepth(value, dependencyMap, depthMap, depth);
        }
        if (depth > maxDepth)
        {
            maxDepth = depth;
        }
    });
    return maxDepth + 1;
}