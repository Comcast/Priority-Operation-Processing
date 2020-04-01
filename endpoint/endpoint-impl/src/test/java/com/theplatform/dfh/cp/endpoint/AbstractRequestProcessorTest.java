package com.theplatform.dfh.cp.endpoint;

import com.theplatform.dfh.cp.endpoint.base.DataObjectRequestProcessor;
import com.comcast.fission.endpoint.api.auth.AuthorizationResponseBuilder;
import com.comcast.fission.endpoint.api.data.DataObjectResponse;
import com.comcast.fission.endpoint.api.data.DefaultDataObjectRequest;
import com.theplatform.dfh.object.api.IdentifiedObject;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.mockito.Mockito;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public abstract class AbstractRequestProcessorTest<T extends IdentifiedObject>
{
    private ObjectPersister<T> persister = Mockito.mock(ObjectPersister.class);
    public abstract T getDataObject();
    public abstract DataObjectRequestProcessor getRequestProcessor(ObjectPersister<T> persister);

    @BeforeMethod
    public void setUp()
    {
        persister = mock(ObjectPersister.class);
    }

    @Test
    public void testHandlePostCustomerNotVisibile()
    {
        T dataObject = getDataObject();
        dataObject.setCustomerId("other customer");

        DefaultDataObjectRequest<T> request = new DefaultDataObjectRequest<>();
        request.setDataObject(dataObject);
        request.setAuthorizationResponse(new AuthorizationResponseBuilder().withAccounts("my customer").build());
        DataObjectRequestProcessor processor = getRequestProcessor(getPersister());
        DataObjectResponse<T> response = processor.handlePOST(request);
        Assert.assertTrue(response.isError());
        Assert.assertTrue(response.getErrorResponse().getTitle().contains("Unauthorized"));
    }

    @Test
    public void testHandlePostCustomerIsOwnerVisibility() throws PersistenceException
    {
        T dataObject = getDataObject();
        dataObject.setCustomerId("my customer");
        when(getPersister().persist(any())).thenReturn(dataObject);

        DefaultDataObjectRequest<T> request = new DefaultDataObjectRequest<>();
        request.setDataObject(dataObject);
        request.setAuthorizationResponse(new AuthorizationResponseBuilder().withAccounts("my customer").build());
        DataObjectResponse<T> response = getRequestProcessor(getPersister()).handlePOST(request);
        Assert.assertFalse(response.isError());
    }


    public ObjectPersister<T> getPersister()
    {
        return persister;
    }
}
