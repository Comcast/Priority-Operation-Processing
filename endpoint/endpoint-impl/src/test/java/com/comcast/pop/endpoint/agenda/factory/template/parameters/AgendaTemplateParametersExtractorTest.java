package com.comcast.pop.endpoint.agenda.factory.template.parameters;

import com.comcast.pop.modules.jsonhelper.JsonHelper;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class AgendaTemplateParametersExtractorTest
{
    private final String MISSING_PARAM = "missing";
    private final String VALID_PARAM = "valid";

    private JsonHelper jsonHelper = new JsonHelper();
    private AgendaTemplateParametersExtractor extractor;

    @BeforeMethod
    public void setup()
    {
        extractor = new AgendaTemplateParametersExtractor();
    }

    @Test(expectedExceptions = MissingTemplateParameterException.class)
    public void testMissingRequiredParameter()
    {
        Map<String, Object> requiredParams = new HashMap<>();
        requiredParams.put(MISSING_PARAM, null);
        requiredParams.put(VALID_PARAM, null);

        extractor.setRequiredParameters(jsonHelper.getObjectMapper().valueToTree(requiredParams));

        Map<String, Object> inputParams = new HashMap<>();
        inputParams.put(VALID_PARAM, null);

        extractor.updateParameterMap(new HashMap<>(), jsonHelper.getObjectMapper().valueToTree(inputParams));
    }
}
