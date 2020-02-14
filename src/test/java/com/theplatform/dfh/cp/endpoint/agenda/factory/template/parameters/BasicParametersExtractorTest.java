package com.theplatform.dfh.cp.endpoint.agenda.factory.template.parameters;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class BasicParametersExtractorTest
{
    private final String MISSING_PARAM = "missing";
    private final String VALID_PARAM = "valid";

    private JsonHelper jsonHelper = new JsonHelper();
    private BasicParametersExtractor extractor;

    @BeforeMethod
    public void setup()
    {
        extractor = new BasicParametersExtractor();
    }


    @Test(expectedExceptions = DuplicateParameterException.class)
    public void testDuplicateParameter()
    {
        Map<String, Object> inputParams = new HashMap<>();
        inputParams.put(VALID_PARAM, null);

        Map<String, JsonNode> parameterMap = new HashMap<>();

        extractor.updateParameterMap(parameterMap, jsonHelper.getObjectMapper().valueToTree(inputParams));
        extractor.updateParameterMap(parameterMap, jsonHelper.getObjectMapper().valueToTree(inputParams));
    }
}
