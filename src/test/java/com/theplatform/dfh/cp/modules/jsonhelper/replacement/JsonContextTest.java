package com.theplatform.dfh.cp.modules.jsonhelper.replacement;

import com.fasterxml.jackson.databind.JsonNode;
import org.testng.Assert;
import org.testng.annotations.Test;

// TODO: more error case tests

public class JsonContextTest extends JsonReplacementTestBase
{
    private JsonContext jsonContext = new JsonContext();

    @Test
    public void testProcessReferences() throws Exception
    {
        final String ENCODE = "encode.1";
        final String ANALYSIS = "analysis.1";

        String encodeOutput = getStringFromResourceFile("/context/encode.context.json");
        String analysisOutput = getStringFromResourceFile("/context/analysis.context.json");
        JsonNode referenceNode = getJsonNodeFromFile("/context/reference.json");
        JsonNode expectedNode = getJsonNodeFromFile("/context/expectedResult.json");
        jsonContext.addData(ENCODE, encodeOutput);
        jsonContext.addData(ANALYSIS, analysisOutput);
        ReferenceReplacementResult result = jsonContext.processReferences(referenceNode);
        Assert.assertEquals(objectMapper.readTree(result.getResult()), expectedNode);
    }

    @Test
    public void testNoData() throws Exception
    {
        jsonContext.addData("foo", null);
    }
}
