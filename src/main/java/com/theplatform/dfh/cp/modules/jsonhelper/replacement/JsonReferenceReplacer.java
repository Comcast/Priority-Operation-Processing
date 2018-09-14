package com.theplatform.dfh.cp.modules.jsonhelper.replacement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for traversing a Json structure and replacing references
 *
 */
public class JsonReferenceReplacer
{
    private static final String DEFAULT_REFERENCE_PREFIX = "@@";
    private static final String DEFAULT_REFERENCE_SEPARATOR = "::";
    private static final String JACKSON_PATH_SEPARATOR = "/";
    public static final String REFERENCE_GROUP = "reference";
    public static final String PATH_GROUP = "path";

    private final String CONTEXT_REFERENCE_PREFIX;
    private final String CONTEXT_REFERENCE_SEPARATOR;
    private final Pattern referencePattern;

    private ArrayNodeReplacer arrayNodeReplacer = new ArrayNodeReplacer();
    private ObjectNodeReplacer objectNodeReplacer = new ObjectNodeReplacer();

    public JsonReferenceReplacer()
    {
        this(DEFAULT_REFERENCE_PREFIX, DEFAULT_REFERENCE_SEPARATOR);
    }

    public JsonReferenceReplacer(String referencePrefix, String referenceSeparator)
    {
        CONTEXT_REFERENCE_PREFIX = referencePrefix;
        CONTEXT_REFERENCE_SEPARATOR = referenceSeparator;
        // the quote replacements escape any special characters (like $)
        String pattern = String.format("%1$s(?<%2$s>.+?)(%3$s(?<%4$s>.+))?",
            Matcher.quoteReplacement(CONTEXT_REFERENCE_PREFIX),
            REFERENCE_GROUP,
            Matcher.quoteReplacement(CONTEXT_REFERENCE_SEPARATOR),
            PATH_GROUP);
        referencePattern = Pattern.compile(pattern);
    }

    /**
     * This is primarily a test method for pattern verification
     * @param input The input to match
     * @return A matcher using the internal pattern
     */
    protected Matcher getMatcher(String input)
    {
        return referencePattern.matcher(input);
    }

    /**
     * Generates a reference
     * @param referenceName The name of reference
     * @param jsonPtrExpr (optional) json pointer within the reference
     * @return The reference string for use with a default JsonReferenceReplacer
     */
    public String generateReference(String referenceName, String jsonPtrExpr)
    {
        return CONTEXT_REFERENCE_PREFIX + referenceName +
            (jsonPtrExpr == null
               ? ""
               : CONTEXT_REFERENCE_SEPARATOR + jsonPtrExpr);
    }
    /**
     * Replaces the references in the specified json structure
     * @param rootNode The root JsonNode to traverse for references
     * @param referenceMap The reference map to use for replacement.
     * @return
     */
    public ReferenceReplacementResult replaceReferences(JsonNode rootNode, Map<String, JsonNode> referenceMap)
    {
        ReferenceReplacementResult referenceReplacementResult = new ReferenceReplacementResult();
        StringSubstitutor strSubstitutor = new StringSubstitutor(referenceMap);
        traverseNodeForTokenReplacement(rootNode, referenceMap, strSubstitutor, referenceReplacementResult);
        referenceReplacementResult.setResult(rootNode.toString());
        return referenceReplacementResult;
    }


    /**
     * Processes the specified node (and all child nodes for replacement). This is a non-recursive implementation
     * @param inputNode The node to evaluate
     * @param parameterMap The map of parameters for replacement (used for whole object replacement)
     * @param strSubstitutor The string substitutor for replacement (used for string token replacement)
     */
    private void traverseNodeForTokenReplacement(JsonNode inputNode, Map<String, JsonNode> parameterMap, StringSubstitutor strSubstitutor,
        ReferenceReplacementResult report)
    {
        // instead of recursion use a stack that is added to
        Stack<JsonNodeWrapper> nodeEvaluationStack = new Stack<>();
        nodeEvaluationStack.push(new JsonNodeWrapper(inputNode, null, null));

        while(!nodeEvaluationStack.empty())
        {
            JsonNodeWrapper jsonNodeWrapper = nodeEvaluationStack.pop();
            JsonNode node = jsonNodeWrapper.getNode();
            JsonNode parentNode = jsonNodeWrapper.getParent();
            String nodeId = jsonNodeWrapper.getNodeId();

            // evaluate the node (adding any child nodes to the stack for evaluation)
            if (node.isArray())
            {
                ArrayNode arrayNode = (ArrayNode) node;
                List<JsonNode> nodes = new ArrayList<>();
                for (final JsonNode objNode : node)
                {
                    nodes.add(objNode);
                }
                // have to avoid using an iterator because the array itself may be modified (index will always be replaced)
                for (int idx = 0; idx < nodes.size(); idx++)
                {
                    JsonNode objNode = nodes.get(idx);
                    if (objNode.isTextual())
                    {
                        performTokenReplacement(
                            objNode,
                            nodeId,
                            parentNode,
                            arrayNodeReplacer.configureArrayIndex(arrayNode, idx),
                            parameterMap,
                            strSubstitutor,
                            report,
                            nodeEvaluationStack);
                    }
                    // this else section is completely unsafe and likely to cause bugs (no type check and the null nodeId!)
                    else
                    {
                        // no replacement will take place on this object reference (but possibly its children) because it is not text
                        nodeEvaluationStack.push(new JsonNodeWrapper(objNode, null, null));
                    }
                }
            }
            else if (node.isTextual())
            {
                performTokenReplacement(
                    node,
                    nodeId,
                    parentNode,
                    objectNodeReplacer.configureField(parentNode, nodeId),
                    parameterMap,
                    strSubstitutor,
                    report,
                    nodeEvaluationStack);
            }
            else if (node.isObject())
            {
                Iterator<Map.Entry<String, JsonNode>> fieldIterator = node.fields();
                while (fieldIterator.hasNext())
                {
                    Map.Entry<String, JsonNode> entry = fieldIterator.next();
                    nodeEvaluationStack.push(new JsonNodeWrapper(entry.getValue(), entry.getKey(), node));
                }
            }
        }
    }

    /**
     * Performs token replacement based on the context of the node and the parameter replacing within it.
     * @param node The node to possibly update
     * @param jsonNodeReplacer The replacer to use to perform the update
     * @param parameterMap The map of parameters for replacement (used for whole object replacement)
     * @param strSubstitutor The string substitutor for replacement (used for string token replacement)
     * @param report The result object to add issues related to the replacement processing
     * @param nodeEvaluationStack The stack of nodes to push any additional nodes for evaluation
     */
    protected void performTokenReplacement(JsonNode node, String nodeId, JsonNode parentNode, JsonNodeReplacer jsonNodeReplacer,
        Map<String, JsonNode> parameterMap, StringSubstitutor strSubstitutor, ReferenceReplacementResult report, Stack<JsonNodeWrapper> nodeEvaluationStack)
    {
        final String nodeValue = node.asText();
        if (nodeValue != null)
        {
            boolean replaced = false;
            Matcher matcher = getMatcher(nodeValue);
            if (matcher.matches())
            {
                String referenceName = matcher.group(REFERENCE_GROUP);
                String referencePath = matcher.group(PATH_GROUP);

                //logger.debug("Checking for token [{}] in node [{}]", translatedValue, nodeId);
                JsonNode parameterValue = getParameterValue(parameterMap, referenceName, referencePath, report);
                if (parameterValue != null)
                {
                    if(parameterValue.isObject() || parameterValue.isArray())
                    {
                        replaced = true;
                        //logger.debug("Object Replace: {} => {}", nodeValue, parameterValue);
                        jsonNodeReplacer.updateValue(parameterValue);
                        // TODO: consider supporting nested references (unlikely)
                        // TODO: this would allow self-referencing and is quite risky
                        //nodeEvaluationStack.push(new JsonNodeWrapper(parameterValue, nodeId, parentNode));
                    }
                    else if(parameterValue.isTextual() || parameterValue.isNumber())
                    {
                        replaced = true;
                        //logger.debug("JSON Pointer String Replace: {} => {}", nodeValue, parameterValue);
                        jsonNodeReplacer.updateValue(parameterValue);
                    }
                }
            }
            // ${parameter} => string replacement
            if (!replaced)
            {
                String result = strSubstitutor.replace(nodeValue);
                if (!StringUtils.equals(result, nodeValue))
                {
                    //logger.debug("String Replace: {} => {}", nodeValue, result);
                    jsonNodeReplacer.updateValue(result);
                }
            }
        }
    }

    /**
     * Performs a lookup for the parameter, checking for the presence of a JSON pointer string and evaluating it
     * @param parameterMap The map of all parameters by the field name
     * @param parameter The parameter name to look up
     * @param jsonPtrExpr The json pointer expression
     * @return The JsonNode value or null if not found
     */
    protected JsonNode getParameterValue(Map<String, JsonNode> parameterMap, String parameter, String jsonPtrExpr,
        ReferenceReplacementResult report)
    {
        if(!parameterMap.containsKey(parameter))
        {
            // this is a missing reference
            report.addMissingReference(generateReference(parameter, jsonPtrExpr));
            return null;
        }

        JsonNode parameterValue = parameterMap.get(parameter);
        // at method does not appear to support the json pointer path '/'
        if(StringUtils.isBlank(jsonPtrExpr) || StringUtils.equals(jsonPtrExpr, JACKSON_PATH_SEPARATOR))
        {
            return parameterValue;
        }

        JsonNode atNode = parameterValue.at(StringUtils.prependIfMissing(jsonPtrExpr, JACKSON_PATH_SEPARATOR));
        if(atNode.isMissingNode())
        {
            // this is an invalid reference
            report.addInvalidReference(generateReference(parameter, jsonPtrExpr));
            return null;
        }
        return atNode;
    }

    public ArrayNodeReplacer getArrayNodeReplacer()
    {
        return arrayNodeReplacer;
    }

    public void setArrayNodeReplacer(ArrayNodeReplacer arrayNodeReplacer)
    {
        this.arrayNodeReplacer = arrayNodeReplacer;
    }

    public ObjectNodeReplacer getObjectNodeReplacer()
    {
        return objectNodeReplacer;
    }

    public void setObjectNodeReplacer(ObjectNodeReplacer objectNodeReplacer)
    {
        this.objectNodeReplacer = objectNodeReplacer;
    }

    /**
     * Wrapper for JsonNode providing parent and id information. JsonNode by default has neither.
     */
    private static class JsonNodeWrapper
    {
        private final JsonNode node;
        private final JsonNode parent;
        private final String nodeId;

        public JsonNodeWrapper(JsonNode node, String nodeId, JsonNode parent)
        {
            this.node = node;
            this.nodeId = nodeId;
            this.parent = parent;
        }

        public JsonNode getNode()
        {
            return node;
        }

        public JsonNode getParent()
        {
            return parent;
        }

        public String getNodeId()
        {
            return nodeId;
        }
    }
}
