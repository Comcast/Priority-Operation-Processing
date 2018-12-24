package com.theplatform.dfh.cp.endpoint.base;

import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.query.ByTitle;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.mockito.Mockito;
import org.testng.annotations.Test;

import java.util.Collections;

public class BaseRequestProcessorTest
{
    ObjectPersister<SimpleObject> objectPersister = Mockito.mock(ObjectPersister.class);

    @Test(expectedExceptions = BadRequestException.class)
    public void testGetByQueryBad() throws BadRequestException, PersistenceException
    {
        TestBaseProcessor processor = new TestBaseProcessor(objectPersister);
        Mockito.when(objectPersister.retrieve(Mockito.anyList())).thenThrow(PersistenceException.class);
        processor.handleGET(Collections.singletonList(new ByTitle("xyz")));
    }

    private class TestBaseProcessor extends BaseRequestProcessor<SimpleObject>
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
    }
}