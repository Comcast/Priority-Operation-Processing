package com.theplatform.dfh.cp.modules.jsonhelper.replacement;

import com.fasterxml.jackson.databind.JsonNode;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.regex.Matcher;

// TODO: more error case tests

public class JsonReferenceReplacerTest extends JsonReplacementTestBase
{
    private JsonReferenceReplacer jsonReferenceReplacer = new JsonReferenceReplacer();

    @DataProvider
    public Object[][] matcherTestProvider()
    {
        return new Object[][]
            {
                {"$$mediainfo.1::/", true, "mediainfo.1", "/"},
                {"$$mediainfo.1::/test/sub/path", true, "mediainfo.1", "/test/sub/path"},
                {"$$mediainfo.1", true, "mediainfo.1", null}
            };
    }

    @Test(dataProvider = "matcherTestProvider")
    public void testGetMatcher(String input, boolean matchExpected, String expectedReference, String expectedPath)
    {
        Matcher matcher = jsonReferenceReplacer.getMatcher(input);
        Assert.assertEquals(matcher.matches(), matchExpected);
        if(matchExpected)
        {
            Assert.assertEquals(matcher.group(JsonReferenceReplacer.REFERENCE_GROUP), expectedReference);
            Assert.assertEquals(matcher.group(JsonReferenceReplacer.PATH_GROUP), expectedPath);
        }
    }

    @Test
    public void testReplaceReferences() throws Exception
    {
        JsonNode paramsNode = getJsonNodeFromFile("/testParams.json");
        JsonNode referencesNode = getJsonNodeFromFile("/testReferences.json");
        JsonNode expectedNode = getJsonNodeFromFile("/testExpectedResult.json");

        JsonNode resultNode = jsonReferenceReplacer.replaceReferences
            (
                referencesNode,
                getParameterMap(paramsNode)
            );

        //System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultNode));
        Assert.assertEquals(resultNode, expectedNode);
    }

    @DataProvider
    public Object[][] getParameterTestValueProvider() throws Exception
    {
        final JsonNode SAMPLE_JSON_NODE = objectMapper.readTree(
            "[ { \"test\":\"a\" }, { \"test\":\"b\" } ]"
        );

        return new Object[][]
            {
                // missing param
                { getSingleTestParamMap(null, null), "sample", null, null },

                // valid nested path
                { getSingleTestParamMap("sample", SAMPLE_JSON_NODE),
                    "sample",
                    "/1/test",
                    objectMapper.readTree("\"b\"") },

                // invalid nested path
                { getSingleTestParamMap("sample", SAMPLE_JSON_NODE),
                    "sample",
                    "/2/test",
                    null },

                // valid root path
                { getSingleTestParamMap("sample", SAMPLE_JSON_NODE),
                    "sample",
                    "/",
                    SAMPLE_JSON_NODE },

                // valid root no path
                { getSingleTestParamMap("sample", SAMPLE_JSON_NODE),
                    "sample",
                    null,
                    SAMPLE_JSON_NODE }
            };
    }

    @Test(dataProvider = "getParameterTestValueProvider")
    public void testGetParameterValue(Map<String, JsonNode> parameterMap, String parameter, String jsonPtrExpr, JsonNode expectedResult)
    {
        Assert.assertEquals(jsonReferenceReplacer.getParameterValue(parameterMap, parameter, jsonPtrExpr), expectedResult);
    }

}
