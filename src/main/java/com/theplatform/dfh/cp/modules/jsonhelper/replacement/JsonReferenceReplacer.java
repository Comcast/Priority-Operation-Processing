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
 */
public class JsonReferenceReplacer
{
    private static final String DEFAULT_REFERENCE_PREFIX = "\\$\\$";
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
        String pattern = String.format("%1$s(?<%2$s>.+?)(%3$s(?<%4$s>.+))?",
            CONTEXT_REFERENCE_PREFIX,
            REFERENCE_GROUP,
            CONTEXT_REFERENCE_SEPARATOR,
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
    public JsonNode replaceReferences(JsonNode rootNode, Map<String, JsonNode> referenceMap)
    {
        StringSubstitutor strSubstitutor = new StringSubstitutor(referenceMap);
        // Note: there is no parent to start with. (this should immediately traverse)
        traverseNodeForTokenReplacement(rootNode, "/", null, referenceMap, strSubstitutor);
        return rootNode;
    }


    /**
     * Recursive evaluation of the specified node for token replacement or traversal based on type.
     * @param node The node to evaluate
     * @param nodeId The field name of the node
     * @param parentNode The parent of the node
     * @param parameterMap The map of parameters for replacement (used for whole object replacement)
     * @param strSubstitutor The string substitutor for replacement (used for string token replacement)
     */
    private void traverseNodeForTokenReplacement(JsonNode node, String nodeId, JsonNode parentNode,
        Map<String, JsonNode> parameterMap, StringSubstitutor strSubstitutor)
    {
        if(node.isArray())
        {
            ArrayNode arrayNode = (ArrayNode)node;
            List<JsonNode> nodes = new ArrayList<>();
            for (final JsonNode objNode : node)
            {
                nodes.add(objNode);
            }
            // have to avoid using an iterator because the array itself may be modified (index will always be replaced)
            for(int idx = 0; idx < nodes.size(); idx++)
            {
                JsonNode objNode = nodes.get(idx);
                if(objNode.isTextual())
                {
                    performTokenReplacement(
                        objNode,
                        nodeId,
                        parentNode,
                        arrayNodeReplacer.configureArrayIndex(arrayNode, idx),
                        parameterMap,
                        strSubstitutor);
                }
                // this else section is completely unsafe and likely to cause bugs (no type check and the null nodeId!)
                else
                {
                    traverseNodeForTokenReplacement(objNode, null, node, parameterMap, strSubstitutor);
                }
            }
        }
        else if(node.isTextual())
        {
            performTokenReplacement(
                node,
                nodeId,
                parentNode,
                objectNodeReplacer.configureField(parentNode, nodeId),
                parameterMap,
                strSubstitutor);
        }
        else if(node.isObject())
        {
            Iterator<Map.Entry<String, JsonNode>> fieldIterator = node.fields();
            while(fieldIterator.hasNext())
            {
                Map.Entry<String,JsonNode> entry = fieldIterator.next();
                traverseNodeForTokenReplacement(entry.getValue(), entry.getKey(), node, parameterMap, strSubstitutor);
            }
        }
    }

    /**
     * Performs token replacement based on the context of the node and the parameter replacing within it.
     * @param node The node to possibly update
     * @param jsonNodeReplacer The replacer to use to perform the update
     * @param parameterMap The map of parameters for replacement (used for whole object replacement)
     * @param strSubstitutor The string substitutor for replacement (used for string token replacement)
     */
    protected void performTokenReplacement(JsonNode node, String nodeId, JsonNode parentNode, JsonNodeReplacer jsonNodeReplacer,
        Map<String, JsonNode> parameterMap, StringSubstitutor strSubstitutor)
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
                JsonNode parameterValue = getParameterValue(parameterMap, referenceName, referencePath);
                if (parameterValue != null)
                {
                    if(parameterValue.isObject() || parameterValue.isArray())
                    {
                        replaced = true;
                        //logger.debug("Object Replace: {} => {}", nodeValue, parameterValue);
                        jsonNodeReplacer.updateValue(parameterValue);
                        traverseNodeForTokenReplacement(
                            parameterValue, nodeId, parentNode, parameterMap, strSubstitutor);
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
    protected JsonNode getParameterValue(Map<String, JsonNode> parameterMap, String parameter, String jsonPtrExpr)
    {
        JsonNode parameterValue = parameterMap.get(parameter);
        // at method does not appear to support the json pointer path '/'
        if(StringUtils.isBlank(jsonPtrExpr) || StringUtils.equals(jsonPtrExpr, JACKSON_PATH_SEPARATOR))
        {
            return parameterValue;
        }

        JsonNode atNode = parameterValue.at(StringUtils.prependIfMissing(jsonPtrExpr, JACKSON_PATH_SEPARATOR));
        return atNode.isMissingNode() ? null : atNode;
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
}
