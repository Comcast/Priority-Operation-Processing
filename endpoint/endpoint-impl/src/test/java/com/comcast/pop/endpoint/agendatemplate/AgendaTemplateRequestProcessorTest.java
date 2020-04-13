package com.comcast.pop.endpoint.agendatemplate;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.ParamsMap;
import com.comcast.pop.endpoint.AbstractRequestProcessorTest;
import com.comcast.pop.endpoint.base.DataObjectRequestProcessor;
import com.comcast.pop.endpoint.api.auth.AuthorizationResponseBuilder;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.api.data.DefaultDataObjectRequest;
import com.comcast.pop.persistence.api.ObjectPersister;
import com.comcast.pop.persistence.api.PersistenceException;
import org.apache.commons.lang3.RandomStringUtils;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

public class AgendaTemplateRequestProcessorTest extends AbstractRequestProcessorTest<AgendaTemplate>
{
    private static final String INSIGHT_ID = "theInsightId";

    @Test
    void testHandlePost() throws PersistenceException
    {
        int numOps = 2;
        AgendaTemplate dataObject = getDataObject(numOps);
        doReturn(new AgendaTemplate()).when(getPersister()).persist(any());

        DefaultDataObjectRequest<AgendaTemplate> request = new DefaultDataObjectRequest<>();
        request.setDataObject(dataObject);
        request.setAuthorizationResponse(new AuthorizationResponseBuilder().withSuperUser(true).build());
        DataObjectResponse<AgendaTemplate> response = getRequestProcessor(getPersister()).handlePOST(request);
        Assert.assertFalse(response.isError());

        Assert.assertNotNull(dataObject.getUpdatedTime());
    }
    @Test
    void testHandlePostIsGlobalVisibility() throws PersistenceException
    {
        AgendaTemplate dataObject = getDataObject();
        dataObject.setCustomerId("other customer");
        dataObject.setIsGlobal(true);
        when(getPersister().persist(any())).thenReturn(dataObject);

        DefaultDataObjectRequest<AgendaTemplate> request = new DefaultDataObjectRequest<>();
        request.setDataObject(dataObject);
        request.setAuthorizationResponse(new AuthorizationResponseBuilder().withAccounts("my customer").build());
        DataObjectResponse<AgendaTemplate> response = getRequestProcessor(getPersister()).handlePOST(request);
        Assert.assertTrue(response.isError());
        Assert.assertTrue(response.getErrorResponse().getTitle().contains("Unauthorized"));
    }
    @Test
    void testHandleGetIsGlobalVisibility() throws PersistenceException
    {
        AgendaTemplate dataObject = getDataObject();
        dataObject.setCustomerId("other customer");
        dataObject.setIsGlobal(true);
        when(getPersister().persist(any())).thenReturn(dataObject);

        DefaultDataObjectRequest<AgendaTemplate> request = new DefaultDataObjectRequest<>();
        request.setDataObject(dataObject);
        request.setAuthorizationResponse(new AuthorizationResponseBuilder().withAccounts("my customer").build());
        DataObjectResponse<AgendaTemplate> response = getRequestProcessor(getPersister()).handleGET(request);
        Assert.assertFalse(response.isError());
    }

    @Test
    void testHandleGetAllowedAccountVisibility() throws PersistenceException
    {
        AgendaTemplate dataObject = getDataObject();
        dataObject.setCustomerId("other customer");
        dataObject.setAllowedCustomerIds(Collections.singleton("my customer"));
        when(getPersister().persist(any())).thenReturn(dataObject);

        DefaultDataObjectRequest<AgendaTemplate> request = new DefaultDataObjectRequest<>();
        request.setDataObject(dataObject);
        request.setAuthorizationResponse(new AuthorizationResponseBuilder().withAccounts("my customer").build());
        DataObjectResponse<AgendaTemplate> response = getRequestProcessor(getPersister()).handleGET(request);
        Assert.assertFalse(response.isError());
    }
    @Override
    public AgendaTemplate getDataObject()
    {
        return getDataObject(1);
    }

    @Override
    public DataObjectRequestProcessor getRequestProcessor(ObjectPersister<AgendaTemplate> objectPersister)
    {
        return new AgendaTemplateRequestProcessor(objectPersister);
    }

    private AgendaTemplate getDataObject(int numOps)
    {
        List<Operation> ops = new ArrayList<>();
        for (int i = 1; i <= numOps; i++)
        {
            Operation operation = new Operation();
            operation.setName(RandomStringUtils.randomAlphabetic(10));
            operation.setPayload(RandomStringUtils.randomAlphanumeric(10));

            ParamsMap params = new ParamsMap();
            params.put("foo", "bar");
            operation.setParams(params);
            ops.add(operation);
        }

        AgendaTemplate agendaTemp = new AgendaTemplate();
        agendaTemp.setCustomerId("theCustomer");
        Agenda agenda = new Agenda();
        agenda.setOperations(ops);
        agendaTemp.setAgenda(agenda);
        return agendaTemp;
    }


}
