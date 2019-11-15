package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityFilter;
import com.theplatform.dfh.cp.endpoint.base.visibility.VisibilityFilterMap;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.query.ByTitle;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.DataObjectFeed;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DataObjectRequestProcessorTest
{
    private static final String URL_ID = "theURLId";

    private ObjectPersister<SimpleObject> mockObjectPersister;
    private TestBaseProcessor processor;
    private DefaultDataObjectRequest<SimpleObject> request;
    private VisibilityFilter<SimpleObject, DataObjectRequest<SimpleObject>> mockVisibilityFilter;

    @BeforeMethod
    public void setup()
    {
        mockObjectPersister = mock(ObjectPersister.class);
        mockVisibilityFilter = mock(VisibilityFilter.class);
        processor = new TestBaseProcessor(mockObjectPersister);
        VisibilityFilterMap map = new VisibilityFilterMap();
        map.putForWrite(mockVisibilityFilter);
        map.putForRead(mockVisibilityFilter);
        processor.setVisibilityFilterMap(map);
        request = new DefaultDataObjectRequest<>();
    }

    @Test
    public void testGETSingleIdObjectMissing() throws BadRequestException, PersistenceException
    {
        doReturn(null).when(mockObjectPersister).retrieve(anyList());
        request.setId(URL_ID);
        DataObjectResponse<SimpleObject> response = processor.handleGET(request);
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "ObjectNotFoundException");
    }

    @Test
    public void testGETSingleIdInvalidVisibility() throws BadRequestException, PersistenceException
    {
        doReturn(new SimpleObject()).when(mockObjectPersister).retrieve(anyString());
        request.setId(URL_ID);
        doReturn(false).when(mockVisibilityFilter).isVisible(any(), any());
        DataObjectResponse<SimpleObject> response = processor.handleGET(request);
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "UnauthorizedException");
    }

    @Test
    public void testGETSingleIdSuccess() throws BadRequestException, PersistenceException
    {
        doReturn(new SimpleObject()).when(mockObjectPersister).retrieve(anyString());
        request.setId(URL_ID);
        doReturn(true).when(mockVisibilityFilter).isVisible(any(), any());
        DataObjectResponse<SimpleObject> response = processor.handleGET(request);
        Assert.assertFalse(response.isError());
        Assert.assertNotNull(response.getFirst());
    }

    @Test
    public void testGETQueryAllFiltered() throws BadRequestException, PersistenceException
    {
        doReturn(new DataObjectFeed<>()).when(mockObjectPersister).retrieve(anyList());
        request.setQueries(Collections.singletonList(new ByTitle("xyz")));
        doReturn(new ArrayList<SimpleObject>()).when(mockVisibilityFilter).filterByVisible(any(), any());
        DataObjectResponse<SimpleObject> response = processor.handleGET(request);
        Assert.assertFalse(response.isError());
        Assert.assertEquals(response.getAll().size(), 0);
    }

    @Test
    public void testGETQuerySuccess() throws BadRequestException, PersistenceException
    {
        final List<SimpleObject> filteredResult = Arrays.asList(new SimpleObject(), new SimpleObject());
        doReturn(new DataObjectFeed<>()).when(mockObjectPersister).retrieve(anyList());
        request.setQueries(Collections.singletonList(new ByTitle("xyz")));
        doReturn(filteredResult).when(mockVisibilityFilter).filterByVisible(any(), any());
        DataObjectResponse<SimpleObject> response = processor.handleGET(request);
        Assert.assertFalse(response.isError());
        Assert.assertEquals(response.getAll().size(), filteredResult.size());
    }

    @Test
    public void testGETQueryPersistenceException() throws BadRequestException, PersistenceException
    {
        doThrow(new PersistenceException("")).when(mockObjectPersister).retrieve(anyList());
        request.setQueries(Collections.singletonList(new ByTitle("xyz")));
        DataObjectResponse<SimpleObject> response = processor.handleGET(request);
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "ObjectNotFoundException");
    }

    @Test
    public void testPOSTInvalidVisibility()
    {
        request.setDataObject(new SimpleObject());
        doReturn(false).when(mockVisibilityFilter).isVisible(any(), any());
        DataObjectResponse<SimpleObject> response = processor.handlePOST(request);
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "UnauthorizedException");
    }

    @Test
    public void testPOSTPersistNullResult() throws PersistenceException
    {
        request.setDataObject(new SimpleObject());
        doReturn(true).when(mockVisibilityFilter).isVisible(any(), any());
        doReturn(null).when(mockObjectPersister).persist(any());
        DataObjectResponse<SimpleObject> response = processor.handlePOST(request);
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "RuntimeException");
    }

    @Test
    public void testPOSTPersistenceException() throws PersistenceException
    {
        request.setDataObject(new SimpleObject());
        doReturn(true).when(mockVisibilityFilter).isVisible(any(), any());
        doThrow(new PersistenceException("")).when(mockObjectPersister).persist(any());
        DataObjectResponse<SimpleObject> response = processor.handlePOST(request);
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "BadRequestException");
    }

    @Test
    public void testPUTMissingObject() throws BadRequestException, PersistenceException
    {
        doReturn(null).when(mockObjectPersister).retrieve(anyString());
        request.setDataObject(new SimpleObject());
        request.setId("theId");
        DataObjectResponse<SimpleObject> response = processor.handlePUT(request);
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "ObjectNotFoundException");
        verify(mockVisibilityFilter, times(0)).isVisible(any(), any());
    }

    @Test
    public void testPUTWithIdInURL() throws PersistenceException
    {
        request.setId(URL_ID);
        SimpleObject simpleObject = new SimpleObject();
        request.setDataObject(simpleObject);
        doReturn(true).when(mockVisibilityFilter).isVisible(any(), any());
        doReturn(simpleObject).when(mockObjectPersister).retrieve(anyString());
        DataObjectResponse<SimpleObject> response = processor.handlePUT(request);
        // verify the id from the url is applied to the sample object
        Assert.assertEquals(simpleObject.getId(), URL_ID);
        Assert.assertFalse(response.isError());
    }

    @Test
    public void testPUTWithInvalidVisibility() throws PersistenceException
    {
        request.setId(URL_ID);
        SimpleObject simpleObject = new SimpleObject();
        request.setDataObject(simpleObject);
        doReturn(false).when(mockVisibilityFilter).isVisible(any(), any());
        doReturn(simpleObject).when(mockObjectPersister).retrieve(anyString());
        DataObjectResponse<SimpleObject> response = processor.handlePUT(request);
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "UnauthorizedException");
    }

    @Test
    public void testPUTWithCustomerIdValidVisibility() throws PersistenceException
    {
        final String PERSISTED_CUSTOMER = "customerA";
        final String UPDATED_CUSTOMER = "customerB";
        request.setId(URL_ID);
        SimpleObject persistedSimpleObject = new SimpleObject();
        persistedSimpleObject.setCustomerId(PERSISTED_CUSTOMER);
        SimpleObject updatedSimpleObject = new SimpleObject();
        updatedSimpleObject.setCustomerId(UPDATED_CUSTOMER);
        request.setDataObject(updatedSimpleObject);
        doReturn(persistedSimpleObject).when(mockObjectPersister).retrieve(anyString());
        doReturn(true).when(mockVisibilityFilter).isVisible(any(), any());
        DataObjectResponse<SimpleObject> response = processor.handlePUT(request);
        Assert.assertFalse(response.isError());
        verify(mockVisibilityFilter, times(2)).isVisible(any(), any());
    }

    @Test
    public void testPUTWithCustomerIdInvalidVisibility() throws PersistenceException
    {
        final String PERSISTED_CUSTOMER = "customerA";
        final String UPDATED_CUSTOMER = "customerB";
        request.setId(URL_ID);
        SimpleObject persistedSimpleObject = new SimpleObject();
        persistedSimpleObject.setCustomerId(PERSISTED_CUSTOMER);
        SimpleObject updatedSimpleObject = new SimpleObject();
        updatedSimpleObject.setCustomerId(UPDATED_CUSTOMER);
        request.setDataObject(updatedSimpleObject);
        doReturn(persistedSimpleObject).when(mockObjectPersister).retrieve(anyString());
        doAnswer(new Answer()
        {
            private int visibilityCallCount = 0;

            @Override
            public Object answer(InvocationOnMock invocationOnMock) throws Throwable
            {
                boolean returnValue = visibilityCallCount == 0;
                visibilityCallCount++;
                return returnValue;
            }
        }).when(mockVisibilityFilter).isVisible(any(), any());
        DataObjectResponse<SimpleObject> response = processor.handlePUT(request);
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "UnauthorizedException");
        verify(mockVisibilityFilter, times(2)).isVisible(any(), any());
    }

    @Test
    public void testPUTPersistenceException() throws PersistenceException
    {
        request.setId(URL_ID);
        SimpleObject simpleObject = new SimpleObject();
        request.setDataObject(simpleObject);
        doReturn(simpleObject).when(mockObjectPersister).retrieve(anyString());
        doReturn(true).when(mockVisibilityFilter).isVisible(any(), any());
        doThrow(new PersistenceException("")).when(mockObjectPersister).update(any());
        DataObjectResponse<SimpleObject> response = processor.handlePUT(request);
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "BadRequestException");
    }

    @Test
    public void testDELETEMissingObject() throws PersistenceException
    {
        request.setId(URL_ID);
        doReturn(null).when(mockObjectPersister).retrieve(anyString());
        DataObjectResponse<SimpleObject> response = processor.handleDELETE(request);
        Assert.assertFalse(response.isError());
    }

    @Test
    public void testDELETEInvalidVisibility() throws PersistenceException
    {
        request.setId(URL_ID);
        doReturn(new SimpleObject()).when(mockObjectPersister).retrieve(anyString());
        doReturn(false).when(mockVisibilityFilter).isVisible(any(), any());
        DataObjectResponse<SimpleObject> response = processor.handleDELETE(request);
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "UnauthorizedException");
    }

    @Test
    public void testDELETEValidVisibility() throws PersistenceException
    {
        request.setId(URL_ID);
        doReturn(new SimpleObject()).when(mockObjectPersister).retrieve(anyString());
        doReturn(true).when(mockVisibilityFilter).isVisible(any(), any());
        DataObjectResponse<SimpleObject> response = processor.handleDELETE(request);
        Assert.assertFalse(response.isError());
        verify(mockObjectPersister, times(1)).delete(anyString());
    }

    @Test
    public void testDELETEPersistenceException() throws PersistenceException
    {
        request.setId(URL_ID);
        doReturn(new SimpleObject()).when(mockObjectPersister).retrieve(anyString());
        doReturn(true).when(mockVisibilityFilter).isVisible(any(), any());
        doThrow(new PersistenceException("")).when(mockObjectPersister).delete(anyString());
        DataObjectResponse<SimpleObject> response = processor.handleDELETE(request);
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "BadRequestException");
    }

    private class TestBaseProcessor extends DataObjectRequestProcessor<SimpleObject>
    {
        public TestBaseProcessor(ObjectPersister<SimpleObject> objectPersister)
        {
            super(objectPersister);
        }
    }

    private class SimpleObject implements IdentifiedObject
    {
        private String id;
        private String customerId;

        @Override
        public String getId()
        {
            return id;
        }

        @Override
        public void setId(String id)
        {
            this.id = id;
        }

        @Override
        public String getCustomerId()
        {
            return customerId;
        }

        @Override
        public void setCustomerId(String customerId)
        {
            this.customerId = customerId;
        }
    }
}