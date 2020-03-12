package com.theplatform.dfh.cp.modules.jsonhelper.replacement;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;

// TODO: more error case tests

public class JsonReferenceReplacerTest extends JsonReplacementTestBase
{
    private JsonHelper jsonHelper = new JsonHelper();
    final JsonNode SAMPLE_JSON_NODE;

    public JsonReferenceReplacerTest() throws Exception
    {
        SAMPLE_JSON_NODE = objectMapper.readTree(
        "[ { \"test\":\"a\" }, { \"test\":\"b\" } ]"
        );
    }

    private JsonReferenceReplacer jsonReferenceReplacer = new JsonReferenceReplacer();

    @DataProvider
    public Object[][] matcherTestProvider()
    {
        return new Object[][]
            {
                {"@<mediainfo.1::/>", true, "mediainfo.1", "/"},
                {"@<mediainfo.1::/test/sub/path>", true, "mediainfo.1", "/test/sub/path"},
                {"@<mediainfo.1>", true, "mediainfo.1", null}
            };
    }

    @Test(dataProvider = "matcherTestProvider")
    public void testGetMatcher(String input, boolean matchExpected, String expectedReference, String expectedPath)
    {
        Matcher matcher = jsonReferenceReplacer.getMatcher(input);
        Assert.assertEquals(matcher.matches(), matchExpected);
        if(matchExpected)
        {
            Assert.assertEquals(matcher.group(JsonReferenceReplacer.REFERENCE_GROUP_NAME), expectedReference);
            Assert.assertEquals(matcher.group(JsonReferenceReplacer.PATH_GROUP_NAME), expectedPath);
        }
    }

    @Test
    public void testReplaceReferences() throws Exception
    {
        final String AGENDA_ID = "456-789";
        JsonNode paramsNode = getJsonNodeFromFile("/testParams.json");
        JsonNode referencesNode = getJsonNodeFromFile("/testReferences.json");
        JsonNode expectedNode = getJsonNodeFromFile("/testExpectedResult.json");

        JsonContext jsonContext = new JsonContext();
        jsonContext.setContextMap(getParameterMap(paramsNode));
        jsonContext.addData("agendaId", TextNode.valueOf(AGENDA_ID).toString());

        ReferenceReplacementResult result = jsonReferenceReplacer.replaceReferences
            (
                referencesNode,
                jsonContext.getContextMap()
            );

        //System.out.println(objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(resultNode));
        Assert.assertEquals(objectMapper.readTree(result.getResult()), expectedNode);
    }

    @DataProvider
    public Object[][] getParameterTestValueProvider() throws Exception
    {
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
        ReferenceReplacementResult referenceReplacementResult = new ReferenceReplacementResult();
        Assert.assertEquals(jsonReferenceReplacer.getParameterValue(parameterMap, parameter, jsonPtrExpr, referenceReplacementResult), expectedResult);
    }

    @Test
    public void testMissingReference()
    {
        final String REFERENCE_NAME = "badSample";
        ReferenceReplacementResult referenceReplacementResult = new ReferenceReplacementResult();
        jsonReferenceReplacer.getParameterValue(getSingleTestParamMap("sample", SAMPLE_JSON_NODE), REFERENCE_NAME, null, referenceReplacementResult);
        Assert.assertTrue(referenceReplacementResult.getMissingReferences().contains(jsonReferenceReplacer.generateReference(REFERENCE_NAME, null)));
        Assert.assertEquals(referenceReplacementResult.getInvalidReferences().size(), 0);
    }

    @Test
    public void testInvalidReference()
    {
        final String REFERENCE_NAME = "sample";
        final String INVALID_REFERENCE = "/unknown";
        ReferenceReplacementResult referenceReplacementResult = new ReferenceReplacementResult();
        jsonReferenceReplacer.getParameterValue(getSingleTestParamMap("sample", SAMPLE_JSON_NODE), REFERENCE_NAME, INVALID_REFERENCE, referenceReplacementResult);
        Assert.assertTrue(referenceReplacementResult.getInvalidReferences().contains(jsonReferenceReplacer.generateReference(REFERENCE_NAME, INVALID_REFERENCE)));
        Assert.assertEquals(referenceReplacementResult.getMissingReferences().size(), 0);
    }

    @DataProvider
    public Object[][] compositeReferenceStringProvider()
    {
        return new Object[][]
            {
                {"85", "85"},
                {"@<sampleValue>", "85"},
                {"@<sampleValue><xmltag>45</xmltag>", "85<xmltag>45</xmltag>"},
                {"@<sampleValueX><xmltag>45</xmltag>", "@<sampleValueX><xmltag>45</xmltag>"},
                {"@<sampleValueX>", "@<sampleValueX>"},
                {"--@<sampleValueX>", "--@<sampleValueX>"},
                {"--@<sampleValueX>--", "--@<sampleValueX>--"},
                {"--@<sampleValueZ>----@<sampleValueB>--", "--@<sampleValueZ>----@<sampleValueB>--"},
                {"--@<sampleValue>--@<sampleValue2>", "--85--95"},
                {"@<sampleValue3>--@<sampleValue2>--", "@<sampleValue3>--95--"}
            };
    }

    @Test(dataProvider = "compositeReferenceStringProvider")
    public void testCompositeReferenceStrings(String inputString, String expectedResult) throws IOException
    {
        final String FIELD_NAME = "testField";
        JsonNode rootNode = objectMapper.readTree(String.format("{\"%1$s\":\"%2$s\"}", FIELD_NAME, inputString));
        Map<String, JsonNode> referenceMap = new HashMap<>();
        referenceMap.put("sampleValue", new TextNode("85"));
        referenceMap.put("sampleValue2", new TextNode("95"));
        ObjectNodeReplacer objectNodeReplacer = new ObjectNodeReplacer()
            .configureField(rootNode, FIELD_NAME);
        JsonNode fieldNode = rootNode.get(FIELD_NAME);
        jsonReferenceReplacer.performTokenReplacement(fieldNode, objectNodeReplacer, referenceMap, new ReferenceReplacementResult());
        //System.out.println(jsonHelper.getPrettyJSONString(rootNode));
        Assert.assertEquals(rootNode.get(FIELD_NAME).textValue(), expectedResult);
    }

    @Test
    public void testReferenceIsObjectInCompositeString() throws IOException
    {
        final String FIELD_NAME = "testField";
        JsonNode rootNode = objectMapper.readTree(String.format("{\"%1$s\":\"-@<sampleValue::/testField>-\"}", FIELD_NAME));
        Map<String, JsonNode> referenceMap = new HashMap<>();
        referenceMap.put("sampleValue", objectMapper.readTree(String.format("{\"%1$s\":{ \"theField\":\"theValue\"}}", FIELD_NAME)));
        ObjectNodeReplacer objectNodeReplacer = new ObjectNodeReplacer()
            .configureField(rootNode, FIELD_NAME);
        JsonNode fieldNode = rootNode.get(FIELD_NAME);
        ReferenceReplacementResult replacementResult = new ReferenceReplacementResult();
        jsonReferenceReplacer.performTokenReplacement(fieldNode, objectNodeReplacer, referenceMap, replacementResult);
        //System.out.println(jsonHelper.getPrettyJSONString(rootNode));
        Assert.assertTrue(replacementResult.getInvalidReferences().contains("sampleValue cannot be used in composite string"));
    }

    @DataProvider
    public Object[][] compositeSubReferenceStringProvider()
    {
        return new Object[][]
            {
                {"@<sampleValue::subItem/item>-", "55-"},
                {"@<sampleValue::subItem/item2/leaf>-", "node-"},
                {"-@<sampleValue::subItem/item2/leaf>-", "-node-"},
                {"@<sampleValue::subItem/item3>", "45"},
                {"@<sampleValue::subItem/item3/nothere?95>", "95"},
                {"-@<sampleValue::subItem/item3/nothere?95>", "-95"},
                {"-@<sampleValue::/wrongitem?95>", "-95"},
                {"-@<sampleValue::/wrongitem?>", "-null"},
                {"@<sampleValue::/wrongitem?>", null},
            };
    }

    @Test(dataProvider = "compositeSubReferenceStringProvider")
    public void testCompositeSubReferenceStrings(String inputString, String expectedResult) throws IOException
    {
        final String FIELD_NAME = "testField";
        JsonNode rootNode = objectMapper.readTree(String.format("{\"%1$s\":\"%2$s\"}", FIELD_NAME, inputString));
        Map<String, JsonNode> referenceMap = new HashMap<>();
        referenceMap.put("sampleValue", objectMapper.readTree("{\"subItem\":{ \"item\":\"55\", \"item2\":{\"leaf\":\"node\"}, \"item3\":\"45\"}}"));
        ObjectNodeReplacer objectNodeReplacer = new ObjectNodeReplacer()
            .configureField(rootNode, FIELD_NAME);
        JsonNode fieldNode = rootNode.get(FIELD_NAME);
        jsonReferenceReplacer.performTokenReplacement(fieldNode, objectNodeReplacer, referenceMap, new ReferenceReplacementResult());
        //System.out.println(jsonHelper.getPrettyJSONString(rootNode));
        Assert.assertEquals(rootNode.get(FIELD_NAME).textValue(), expectedResult);
    }
}
