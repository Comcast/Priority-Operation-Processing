package com.theplatform.dfh.cp.modules.jsonhelper.replacement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.TextNode;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for traversing a Json structure and replacing references
 *
 */
public class JsonReferenceReplacer
{
    public static final String DEFAULT_REFERENCE_PREFIX = "@<";
    public static final String DEFAULT_REFERENCE_SUFFIX = ">";
    public static final String DEFAULT_REFERENCE_SEPARATOR = "::";
    public static final String DEFAULT_FALLBACK_VALUE_SEPARATOR = "?";
    public static final String JACKSON_PATH_SEPARATOR = "/";
    public static final String REFERENCE_GROUP_NAME = "reference";
    public static final String PATH_GROUP_NAME = "path";

    private final String CONTEXT_REFERENCE_PREFIX;
    private final String CONTEXT_REFERENCE_SUFFIX;
    private final String CONTEXT_REFERENCE_SEPARATOR;
    private final String CONTEXT_FALLBACK_VALUE_SEPARATOR;
    private final Pattern referencePattern;

    private ArrayNodeReplacer arrayNodeReplacer = new ArrayNodeReplacer();
    private ObjectNodeReplacer objectNodeReplacer = new ObjectNodeReplacer();

    public JsonReferenceReplacer()
    {
        this(DEFAULT_REFERENCE_PREFIX, DEFAULT_REFERENCE_SEPARATOR, DEFAULT_REFERENCE_SUFFIX, DEFAULT_FALLBACK_VALUE_SEPARATOR);
    }

    public JsonReferenceReplacer(String referencePrefix, String referenceSeparator, String referenceSuffix, String fallbackValueSeparator)
    {
        CONTEXT_REFERENCE_PREFIX = referencePrefix;
        CONTEXT_REFERENCE_SEPARATOR = referenceSeparator;
        CONTEXT_REFERENCE_SUFFIX = referenceSuffix;
        CONTEXT_FALLBACK_VALUE_SEPARATOR = fallbackValueSeparator;
        // the quote replacements escape any special characters (like $)
        String pattern = String.format("(%1$s(?<%2$s>.+?)(%3$s(?<%4$s>.+?))?%5$s)",
            Matcher.quoteReplacement(CONTEXT_REFERENCE_PREFIX),
            REFERENCE_GROUP_NAME,
            Matcher.quoteReplacement(CONTEXT_REFERENCE_SEPARATOR),
            PATH_GROUP_NAME,
            Matcher.quoteReplacement(CONTEXT_REFERENCE_SUFFIX));
        referencePattern = Pattern.compile(pattern);
    }

    public String getPrefix()
    {
        return CONTEXT_REFERENCE_PREFIX;
    }

    public String getSuffix()
    {
        return CONTEXT_REFERENCE_SUFFIX;
    }

    public String getSeparator()
    {
        return CONTEXT_REFERENCE_SEPARATOR;
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
        return CONTEXT_REFERENCE_PREFIX
            + referenceName
            + (jsonPtrExpr == null
               ? ""
               : CONTEXT_REFERENCE_SEPARATOR + jsonPtrExpr)
            + CONTEXT_REFERENCE_SUFFIX;
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

        traverseNodeForTokenReplacement(rootNode, referenceMap, referenceReplacementResult);
        referenceReplacementResult.setResult(rootNode.toString());
        return referenceReplacementResult;
    }


    /**
     * Processes the specified node (and all child nodes for replacement). This is a non-recursive implementation
     * @param inputNode The node to evaluate
     * @param parameterMap The map of parameters for replacement (used for whole object replacement)
     */
    private void traverseNodeForTokenReplacement(JsonNode inputNode, Map<String, JsonNode> parameterMap, ReferenceReplacementResult report)
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

            if(node == null) continue;

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
                            arrayNodeReplacer.configureArrayIndex(arrayNode, idx),
                            parameterMap,
                            report);
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
                    objectNodeReplacer.configureField(parentNode, nodeId),
                    parameterMap,
                    report);
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
     * @param report The result object to add issues related to the replacement processing
     */
    protected void performTokenReplacement(JsonNode node, JsonNodeReplacer jsonNodeReplacer, Map<String, JsonNode> parameterMap, ReferenceReplacementResult report)
    {
        final String nodeValue = node.asText();
        if (nodeValue != null)
        {
            boolean fullNodeReplace = false;
            Matcher matcher = getMatcher(nodeValue);
            // exact match (not building a composite string)
            if (matcher.matches())
            {
                String referenceName = matcher.group(REFERENCE_GROUP_NAME);
                String referencePath = matcher.group(PATH_GROUP_NAME);

                //logger.debug("Checking for token [{}] in node [{}]", translatedValue, nodeId);
                JsonNode parameterValue = getParameterValue(parameterMap, referenceName, referencePath, report);

                if (parameterValue != null)
                {
                    fullNodeReplace = true;
                    if(parameterValue.isObject()
                        || parameterValue.isArray()
                        || parameterValue.isTextual()
                        || parameterValue.isNumber()
                        || parameterValue == NullNode.getInstance())
                    {
                        jsonNodeReplacer.updateValue(parameterValue);
                    }
                }
            }
            if(!fullNodeReplace)
            {
                // look for any internal matches (composite string)
                // reset the matcher (necessary because of the .matches call above)
                matcher.reset();
                Map<String, String> replacementValues = new HashMap<>();
                while(matcher.find())
                {
                    String referenceName = matcher.group(REFERENCE_GROUP_NAME);
                    String referencePath = matcher.group(PATH_GROUP_NAME);

                    //logger.debug("Checking for token [{}] in node [{}]", translatedValue, nodeId);
                    JsonNode parameterValue = getParameterValue(parameterMap, referenceName, referencePath, report);
                    if (parameterValue != null)
                    {
                        if(!parameterValue.isValueNode())
                        {
                            report.addInvalidReference(String.format("%1$s cannot be used in composite string", referenceName));
                        }
                        else if(parameterValue.isValueNode())
                        {
                            // get the entire match so it can be replaced as necessary
                            // NOTE: if the same item is added again it is harmless
                            replacementValues.put(matcher.group(0), parameterValue.asText());
                        }
                    }
                }
                if(replacementValues.size() > 0)
                {
                    String newValue = nodeValue;
                    for(Map.Entry<String,String> kvp : replacementValues.entrySet())
                    {
                        newValue = StringUtils.replace(newValue, kvp.getKey(), kvp.getValue());
                    }
                    jsonNodeReplacer.updateValue(newValue);
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

        // NOTE / TODO: If we opt to create more special types I recommend individual processors and multiple characters (don't overload '?' for example)
        String[] splitPtr = StringUtils.split(jsonPtrExpr, CONTEXT_FALLBACK_VALUE_SEPARATOR, 2);
        String jsonPointer = splitPtr[0];

        JsonNode atNode = parameterValue.at(StringUtils.prependIfMissing(jsonPointer, JACKSON_PATH_SEPARATOR));
        if(atNode.isMissingNode())
        {
            if(splitPtr.length == 1
                && jsonPtrExpr.endsWith(CONTEXT_FALLBACK_VALUE_SEPARATOR))
            {
                return NullNode.getInstance();
            }
            else if(splitPtr.length > 1)
            {
                return new TextNode(splitPtr[1]);
            }
            else
            {
                // this is an invalid reference
                report.addInvalidReference(generateReference(parameter, jsonPointer));
                return null;
            }
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
     * Returns the name of the reference by matching a full reference specification
     * @param reference The reference to attempt to parse
     * @return The name of the reference or null if unable to parse
     */
    public String getReferenceName(String reference)
    {
        Matcher matcher = getMatcher(reference);
        if (matcher.matches())
        {
            return matcher.group(REFERENCE_GROUP_NAME);
        }
        return null;
    }

    /**
     * Wrapper for JsonNode providing parent and id information. JsonNode by default has neither.
     */
    protected static class JsonNodeWrapper
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
