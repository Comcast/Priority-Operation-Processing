package com.theplatform.dfh.cp.endpoint.aws;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Utility class for performing lookups against the given environment (AWS in this case)
 */
public class EnvironmentLookupUtils
{
    private final String DB_TABLE_NAME_ENV_VAR = "DB_TABLE_NAME";

    private final String STAGE_FIELD_PATH = "/stage";

    public String getTableName(JsonNode rootRequestNode)
    {
        String tableName = System.getenv(DB_TABLE_NAME_ENV_VAR);
        if(tableName == null)
        {
            return null;
        }
        return tableName + "-" + getStageName(rootRequestNode);
    }

    public String getStageName(JsonNode rootRequestNode)
    {
        JsonNode stageName = rootRequestNode.at(STAGE_FIELD_PATH);
        if(stageName.isMissingNode())
        {
            return null;
        }
        return stageName.asText();
    }
}
