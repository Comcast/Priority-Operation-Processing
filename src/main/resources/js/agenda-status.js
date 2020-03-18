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
            getQueryURL(server, endpoint, "byid", "6feff642-93db-404b-952e-21f1161e1f29", 20, fieldsValue),
            // getQueryURL(server, endpoint, "byid", "", 20, fieldsValue),
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
    agendaProgress.operationProgress.forEach(function (opProgress, index) {
        progressMap[opProgress.operation] = opProgress;
    });

    if (agenda["operations"])
    {
        agenda["operations"].forEach(function (operation, index) {
            buildOperationNode(operationNodes, operation, progressMap[operation.name]);
            buildOperationDependencyEdges(operationEdges, operation);
        });
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

    // using visjs
    ////////
    // // create an array with nodes
    // var nodes = new vis.DataSet(operationNodes);
    // // create an array with edges
    // var edges = new vis.DataSet(minimizedEdges);
    // create a network
    // var container = document.getElementById('mynetwork');

    // var data = {
    //     nodes: nodes,
    //     edges: edges
    // };
    // var options ={
    //     layout: {
    //         hierarchical: {
    //             direction: "LR",
    //             sortMethod: "directed"
    //         }
    //     },
    //     physics: {
    //         hierarchicalRepulsion: {
    //             avoidOverlap: 1
    //         }
    //     }};
    // var network = new vis.Network(container, data, options);

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

    // console.log(JSON.stringify(tree));

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

    const svg = d3.select("#mynetwork").append("svg")
            .attr("viewBox", [0, 0, width, (x1 - x0 + root.dx) * 2]);

    const g = svg.append("g")
            .attr("font-family", "sans-serif")
            .attr("font-size", 10)
            .attr("transform", `translate(${root.dy / 3},${(root.dx - x0)})`);

    var links = root.links();

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

    // node.append("circle")
    //         .attr("fill", d => nodeMap.get(d.data.name).color)
    //         .attr("r", 2.5);
    node.append("g")
            .attr("class", "nodes")
            .append(d => loadLiquidFillGaugeSVG(nodeMap.get(d.data.name)).node())
            .attr("transform", `translate(-25,-25)`);


    node.append("text")
            .attr("dy", "25")
            .attr("dx", "5")
            .attr("x", d => d.children ? -6 : 6)
            .attr("text-anchor", "middle")
            .text(d => d.data.name)
            .clone(true).lower()
            .attr("stroke", "white");

    // var width = 800;
    // var height = 800;
    // var margin = {
    //     top: 30,
    //     right: 80,
    //     bottom: 30,
    //     left: 30
    // };
    //
    // var container = d3.select("#mynetwork");
    // const svg = container
    //         .append("svg")
    //         .attr("width", width + margin.left + margin.right)
    //         .attr("height", height + margin.top + margin.bottom)
    //         .append("g")
    //         .attr("transform", `translate(${margin.left},${margin.top})`);
    //
    //
    //
    //
    // const dataset =  {
    //     nodes: operationNodes,
    //     links: minimizedEdges
    // };
    //
    // console.log(JSON.stringify(dataset));
    //
    // var simulation = d3.forceSimulation()
    //         .force('charge', d3.forceManyBody().strength(-900))
    //         .force('center', d3.forceCenter(width / 2, height / 2));
    //
    // // Initialize the links
    // const link = svg.append("g")
    //         .attr("class", "links")
    //         .selectAll("line")
    //         .data(dataset.links)
    //         .enter().append("line")
    //         .attr("stroke-width", 2)
    //         .attr("stroke", "black");
    //
    // // Initialize the nodes
    // const node = svg.append("g")
    //         .attr("class", "nodes")
    //         .selectAll("circle")
    //         .data(dataset.nodes)
    //         .enter()
    //         .append(d => loadLiquidFillGaugeSVG(d.id, d.color, d.percentComplete).node())
    //         .call(d3.drag()
    //                 .on("start", dragstarted)
    //                 .on("drag", dragged)
    //                 .on("end", dragended)
    //         );
    //
    // // Text to nodes
    // const text = svg.append("g")
    //         .attr("class", "text")
    //         .selectAll("text")
    //         .data(dataset.nodes)
    //         .enter().append("text")
    //         .text(d => d.id);
    //
    // //Listen for tick events to render the nodes as they update in your Canvas or SVG.
    // simulation
    //         .nodes(dataset.nodes)//sets the simulation’s nodes to the specified array of objects, initializing their positions and velocities, and then re-initializes any bound forces;
    //         .on("tick", ticked);//use simulation.on to listen for tick events as the simulation runs.
    //
    // simulation.force("link", d3.forceLink()
    //         .id(function(d) { return d.id; })
    //         .links(dataset.links).distance(80));
    //
    // // This function is run at each iteration of the force algorithm, updating the nodes position (the nodes data array is directly manipulated).
    // function ticked() {
    //     link.attr("x1", d => d.source.x)
    //             .attr("y1", d => d.source.y)
    //             .attr("x2", d => d.target.x)
    //             .attr("y2", d => d.target.y);
    //
    //     node.attr("transform", d => 'translate(' + (d.x-25) + ',' + (d.y-35) + ')');
    //
    //     text.attr("x", d => d.x - 5) //position of the lower left point of the text
    //             .attr("y", d => d.y + 5); //position of the lower left point of the text
    // }
    //
    // //When the drag gesture starts, the targeted node is fixed to the pointer
    // //The simulation is temporarily “heated” during interaction by setting the target alpha to a non-zero value.
    // function dragstarted(d) {
    //     if (!d3.event.active) simulation.alphaTarget(0.3).restart();//sets the current target alpha to the specified number in the range [0,1].
    //     d.fy = d.y; //fx - the node’s fixed x-position. Original is null.
    //     d.fx = d.x; //fy - the node’s fixed y-position. Original is null.
    // }
    //
    // //When the drag gesture starts, the targeted node is fixed to the pointer
    // function dragged(d) {
    //     d.fx = d3.event.x;
    //     d.fy = d3.event.y;
    // }
    //
    // //the targeted node is released when the gesture ends
    // function dragended(d) {
    //     if (!d3.event.active) simulation.alphaTarget(0);
    //     d.fx = null;
    //     d.fy = null;
    //
    //     console.log("dataset after dragged is ...",dataset);
    // }
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

function defined(variable) {
    return (typeof variable !== 'undefined');
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