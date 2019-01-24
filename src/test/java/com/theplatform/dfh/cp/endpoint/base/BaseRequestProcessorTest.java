package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ObjectNotFoundException;
import com.theplatform.dfh.endpoint.api.data.DataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.endpoint.api.data.query.ByTitle;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import com.theplatform.dfh.persistence.api.query.Query;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.List;

public class BaseRequestProcessorTest
{
    ObjectPersister<SimpleObject> objectPersister = Mockito.mock(ObjectPersister.class);

    @Test
    public void testGetByQueryBad() throws BadRequestException, PersistenceException
    {
        TestBaseProcessor processor = new TestBaseProcessor(objectPersister);
        Mockito.when(objectPersister.retrieve(Mockito.anyList())).thenThrow(PersistenceException.class);
        DefaultDataObjectRequest<SimpleObject> request = new DefaultDataObjectRequest<>();
        request.setQueries(Collections.singletonList(new ByTitle("xyz")));
        DataObjectResponse<SimpleObject> response = processor.handleGET(request);
        Assert.assertTrue(response.isError());
        Assert.assertEquals(response.getErrorResponse().getTitle(), "ObjectNotFoundException");
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
        @Override
        public String getId()
        {
            return "98839-02394029-";
        }

        @Override
        public void setId(String s)
        {

        }

        @Override
        public String getCustomerId()
        {
            return null;
        }

        @Override
        public void setCustomerId(String customerId)
        {

        }
    }
}