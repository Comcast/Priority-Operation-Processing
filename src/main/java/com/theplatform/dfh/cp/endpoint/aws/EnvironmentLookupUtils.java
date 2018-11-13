package com.theplatform.dfh.cp.endpoint.aws;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for performing lookups against the given environment (AWS in this case)
 */
public class EnvironmentLookupUtils
{
    private final String DB_TABLE_NAME_ENV_VAR = "DB_TABLE_NAME";

    private final String STAGE_FIELD_PATH = "/requestContext/stage";
    private final String DOMAIN_NAME_FIELD_PATH = "/requestContext/domainName";
    private final String STAGE_VARIABLES_PATH = "/stageVariables/";

    public String getTableName(JsonNode rootRequestNode)
    {
        String tableName = System.getenv(DB_TABLE_NAME_ENV_VAR);
        if(tableName == null)
        {
            return null;
        }
        return tableName + "-" + getStageName(rootRequestNode);
    }

    public String getAPIEndpointURL(JsonNode rootRequestNode, String pathStageVar)
    {
        String domainName = getDomainName(rootRequestNode);
        String stageName = getStageName(rootRequestNode);
        String path = getStageVariable(rootRequestNode, pathStageVar);
        if(StringUtils.isBlank(domainName) ||
            StringUtils.isBlank(stageName) ||
            StringUtils.isBlank(path))
        {
            return null;
        }

        return "https://" + domainName + "/" + stageName + path;
    }

    public String getStageVariable(JsonNode rootRequestNode, String stageVariableName)
    {
        return getRequestValue(rootRequestNode, STAGE_VARIABLES_PATH + stageVariableName);
    }

    public String getDomainName(JsonNode rootRequestNode)
    {
        return getRequestValue(rootRequestNode, DOMAIN_NAME_FIELD_PATH);
    }

    public String getStageName(JsonNode rootRequestNode)
    {
        return getRequestValue(rootRequestNode, STAGE_FIELD_PATH);
    }

    public String getRequestValue(JsonNode rootRequestNode, String path)
    {
        JsonNode requestValueNode = rootRequestNode.at(path);
        if(requestValueNode.isMissingNode())
        {
            return null;
        }
        return requestValueNode.asText();
    }
}
