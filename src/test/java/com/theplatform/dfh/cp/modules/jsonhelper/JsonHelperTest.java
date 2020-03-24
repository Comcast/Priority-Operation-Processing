package com.theplatform.dfh.cp.modules.jsonhelper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

public class JsonHelperTest
{
    private static final String OBJ_ID = "100";
    private static final String SUB_ID = "50";

    private JsonHelper jsonHelper = new JsonHelper();
    private ObjectMapper objectMapper = new ObjectMapper();

    @Test
    public void testGetObjectFromRef()
    {
        SampleObject sampleObject = new SampleObject().setId(OBJ_ID).setSubObject(new SampleObject().setId(SUB_ID));
        SampleObject subObject = jsonHelper.getObjectFromRef(objectMapper.valueToTree(sampleObject), "/subObject", SampleObject.class);
        Assert.assertEquals(subObject.getId(), SUB_ID);
    }

    @Test
    public void testGetMapFromObject()
    {
        SampleObject sampleObject = new SampleObject().setId(OBJ_ID).setSubObject(new SampleObject().setId(SUB_ID));
        Map<String, Object> objectMap = jsonHelper.getMapFromObject(sampleObject);
        Assert.assertEquals(objectMap.get("id"), OBJ_ID);
        Map<String, Object> subObjectMap = (Map<String, Object>) objectMap.get("subObject");
        Assert.assertEquals(subObjectMap.get("id"), SUB_ID);
    }

    @Test
    public void testGetObjectFromMap()
    {
        Map<String, Object> objectMap = new HashMap<>();
        objectMap.put("id", OBJ_ID);
        Map<String, Object> subObjectMap = new HashMap<>();
        subObjectMap.put("id", SUB_ID);
        objectMap.put("subObject", subObjectMap);
        SampleObject sampleObject = jsonHelper.getObjectFromMap(objectMap, SampleObject.class);

        Assert.assertNotNull(sampleObject);
        Assert.assertEquals(sampleObject.getId(), OBJ_ID);
        Assert.assertNotNull(sampleObject.getSubObject());
        Assert.assertEquals(sampleObject.getSubObject().getId(), SUB_ID);
    }

    @DataProvider
    public Object[][] invalidSetNodeJsonPointers()
    {
        return new Object[][]
            {
                {null},
                {""},
                {"/"},
                {"/this/is/bad/"}
            };
    }

    @Test (dataProvider = "invalidSetNodeJsonPointers", expectedExceptions = JsonHelperException.class, expectedExceptionsMessageRegExp = "jsonPtrExpr must be a subnode.+")
    public void testSetNodeValueInvalidInput(String jsonPtrExpr)
    {
        jsonHelper.setNodeValue(null, jsonPtrExpr, SUB_ID);
    }

    @Test
    public void testSetNodeValue() throws Exception
    {
        SampleObject sampleObject = new SampleObject().setId(OBJ_ID);
        JsonNode jsonNode = objectMapper.valueToTree(sampleObject);
        jsonHelper.setNodeValue(jsonNode, "/id", SUB_ID);
        SampleObject adjustedObject = objectMapper.treeToValue(jsonNode, SampleObject.class);
        Assert.assertEquals(adjustedObject.getId(), SUB_ID);
    }
}
