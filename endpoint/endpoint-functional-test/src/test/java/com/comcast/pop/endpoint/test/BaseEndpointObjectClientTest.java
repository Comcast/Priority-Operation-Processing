package com.comcast.pop.endpoint.test;

import com.comcast.pop.endpoint.api.ValidationException;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.test.base.EndpointTestBase;
import com.comcast.pop.api.DefaultEndpointDataObject;
import com.theplatform.dfh.cp.test.cleanup.endpoint.TrackedHttpObjectClient;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.endpoint.client.HttpObjectClient;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.apache.commons.beanutils.BeanMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public abstract class BaseEndpointObjectClientTest<T extends DefaultEndpointDataObject> extends EndpointTestBase
{
    private static final Logger logger = LoggerFactory.getLogger(BaseEndpointObjectClientTest.class);
    private final JsonHelper jsonHelper = new JsonHelper();
    private final Class<T> testObjectClass;
    private static final String CLASS_FIELD = "class";

    public BaseEndpointObjectClientTest(Class<T> testObjectClass)
    {
        this.testObjectClass = testObjectClass;
    }

    protected abstract T getTestObject();
    protected abstract T updateTestObject(T object);
    protected abstract void verifyUpdatedTestObject(T object);
    protected abstract void verifyCreatedTestObject(T createdObject, T testObject);

    protected boolean canPerformPUT() { return true; }

    @Test
    public void testCRUD()
    {
        HttpObjectClient<T> objectClient = getObjectClient(getDefaultHttpURLConnectionFactory());

        T testObject = getTestObject();
        DataObjectResponse<T> createdObjectResponse = objectClient.persistObject(testObject);
        Assert.assertFalse(createdObjectResponse.isError(), String.format("Unexpected error: %1$s", createdObjectResponse.getErrorResponse()));
        T createdObject = createdObjectResponse.getFirst();
        onCreate(createdObject);
        logger.info("New object id: {}", createdObject.getId());
        DataObjectResponse<T> retrieveObjectResponse = objectClient.getObject(createdObject.getId());
        Assert.assertFalse(retrieveObjectResponse.isError());
        T retrievedObject = retrieveObjectResponse.getFirst();
        Assert.assertNotNull(retrievedObject);
        verifyCreatedTestObject(retrievedObject, testObject);

        T updatedTestObject = updateTestObject(retrievedObject);
        DataObjectResponse<T> objectUpdateResponse = objectClient.updateObject(updatedTestObject, retrievedObject.getId());
        if(canPerformPUT())
        {
            Assert.assertFalse(objectUpdateResponse.isError());
            retrieveObjectResponse = objectClient.getObject(createdObject.getId());
            Assert.assertFalse(retrieveObjectResponse.isError());
            retrievedObject = retrieveObjectResponse.getFirst();
            verifyUpdatedTestObject(retrievedObject);
        }
        else
        {
            Assert.assertTrue(objectUpdateResponse.isError());
            Assert.assertTrue(StringUtils.containsIgnoreCase(objectUpdateResponse.getErrorResponse().getDescription(), "PUT is not implemented"));
        }

        logger.info("Retrieved object: {}", jsonHelper.getPrettyJSONString(retrievedObject));
        objectClient.deleteObject(createdObject.getId());
        logger.info("Deleted object id: {}", createdObject.getId());

        DataObjectResponse<T> dataObjectResponse = objectClient.getObject(createdObject.getId());
        // TODO: the result object should have nothing, not a list of 1 that is null
        Assert.assertNull(dataObjectResponse.getFirst());
        Assert.assertFalse(dataObjectResponse.isError());
    }

    // if you have a real authorizer this test will apply
    @Test(enabled = false)
    public void testInvalidAuthPOST()
    {
        HttpObjectClient<T> objectClient = getObjectClient(httpUrlConnectionFactory);
        DataObjectResponse<T> response = objectClient.persistObject(getTestObject());
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "ObjectClientException");
        Assert.assertEquals(response.getErrorResponse().getResponseCode(), (Integer) 401);
    }

    // if you have a real authorizer this test will apply
    @Test(enabled = false)
    public void testInvalidAuthGET()
    {
        HttpObjectClient<T> objectClient = getObjectClient(httpUrlConnectionFactory);
        DataObjectResponse<T> response = objectClient.getObject(UUID.randomUUID().toString());
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "ObjectClientException");
        Assert.assertEquals(response.getErrorResponse().getResponseCode(), (Integer) 401);
    }

    // if you have a real authorizer this test will apply
    @Test(enabled = false)
    public void testPutInvalidId()
    {
        if(!canPerformPUT()) return;

        // PUT with ID that does not match existing object

        HttpObjectClient<T> objectClient = getObjectClient(getDefaultHttpURLConnectionFactory());

        T testObject = getTestObject();
        testObject.setId(UUID.randomUUID().toString());

        DataObjectResponse<T> response = objectClient.updateObject(testObject, testObject.getId());
        verifyError(response, 404, "ObjectNotFoundException");
    }

    // DFH-3914
    @Test
    public void testPutWithoutIdInBody()
    {
        if(!canPerformPUT()) return;

        HttpObjectClient<T> objectClient = getObjectClient(getDefaultHttpURLConnectionFactory());

        T testObject = getTestObject();
        DataObjectResponse<T> createdObjectResponse = objectClient.persistObject(testObject);
        onCreate(createdObjectResponse.getFirst());
        verifyNoError(createdObjectResponse);
        T createdObject = createdObjectResponse.getFirst();
        final String createdObjectId = createdObject.getId();

        T updatedTestObject = updateTestObject(createdObject);
        updatedTestObject.setId(null);
        DataObjectResponse<T> updateResponse = objectClient.updateObject(updatedTestObject, createdObjectId);
        verifyNoError(updateResponse);
    }

    @Test
    public void testMissingCustomerId()
    {
        T testObject = getTestObject();
        testObject.setCustomerId(null);
        DataObjectResponse<T> response = getObjectClient(getDefaultHttpURLConnectionFactory()).persistObject(testObject);
        Assert.assertTrue(response.isError());
        Assert.assertTrue(StringUtils.containsIgnoreCase(response.getErrorResponse().getDescription(), "The customerId field must be specified"));
    }

    protected void verifyValidationExceptionOnPersist(T dataObject, String expectedDescriptionFragment)
    {
        DataObjectResponse<T> dataObjectResponse =
            getObjectClient(getDefaultHttpURLConnectionFactory()).persistObject(dataObject);
        verifyError(dataObjectResponse, 422, ValidationException.class.getSimpleName(), expectedDescriptionFragment);
    }

    public void onCreate(T createdObject){}

    public abstract String getEndpointUrl();

    protected void assertEqual(Object createdObject, Object testObject)
    {
        assertEqual(createdObject, testObject, Arrays.asList("id", "updatedTime", "addedTime", "isGlobal"));
    }

    protected void assertEqual(Object createdObject, Object testObject, List<String> fieldsToIgnore)
    {
        Assert.assertNotNull(createdObject);
        BeanMap beanMapOrig = new BeanMap(testObject);
        BeanMap beanMapDest = new BeanMap(createdObject);
        Iterator<Map.Entry<Object, Object>> propertyIterator = beanMapOrig.entryIterator();
        while (propertyIterator.hasNext())
        {
            Map.Entry<Object, Object> entry = propertyIterator.next();
            if(CLASS_FIELD.equals(entry.getKey()) || fieldsToIgnore.contains(entry.getKey().toString())) continue;
            Assert.assertEquals(beanMapDest.get(entry.getKey()), (entry.getValue()), String.format("Field value mismatch: %1$s", entry.getKey()));
        }
    }

    public HttpObjectClient<T> getObjectClient(HttpURLConnectionFactory urlConnectionFactory)
    {
        return new TrackedHttpObjectClient<>(getEndpointUrl(), urlConnectionFactory, testObjectClass, identifiedObjectCleanupManager.getIdentifiedObjectCreateTracker());
    }
}
