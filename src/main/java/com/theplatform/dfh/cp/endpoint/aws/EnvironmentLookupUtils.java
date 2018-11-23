package com.theplatform.dfh.cp.endpoint.aws;

import org.apache.commons.lang3.StringUtils;

/**
 * Utility class for performing lookups against the given environment (AWS in this case)
 */
public class EnvironmentLookupUtils
{
    public static final String DB_TABLE_NAME_ENV_VAR = "DB_TABLE_NAME";

    public String getTableName(LambdaRequest lambdaRequest)
    {
        return getTableName(lambdaRequest, DB_TABLE_NAME_ENV_VAR);
    }

    public String getTableName(LambdaRequest lambdaRequest, String tableEnvironmentVariableName)
    {
        String tableName = System.getenv(tableEnvironmentVariableName);
        if(tableName == null)
        {
            return null;
        }
        return tableName + "-" + lambdaRequest.getStageName();
    }


    public String getAPIEndpointURL(LambdaRequest lambdaRequest, String pathStageVar)
    {
        String domainName = lambdaRequest.getDomainName();
        String stageName = lambdaRequest.getStageName();
        String path = lambdaRequest.getStageVariable(pathStageVar);
        if(StringUtils.isBlank(domainName) ||
            StringUtils.isBlank(stageName) ||
            StringUtils.isBlank(path))
        {
            return null;
        }

        return "https://" + domainName + "/" + stageName + path;
    }

}
