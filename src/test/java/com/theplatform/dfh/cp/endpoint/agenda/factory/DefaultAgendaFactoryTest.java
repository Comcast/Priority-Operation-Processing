package com.theplatform.dfh.cp.endpoint.agenda.factory;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.endpoint.agenda.factory.template.AgendaTemplateMapper;
import com.theplatform.dfh.endpoint.api.data.DefaultDataObjectResponse;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class DefaultAgendaFactoryTest
{
    private final String TEMPLATE_ID = "theId";
    private final String TEMPLATE_NAME = "theName";
    private final String PROGRESS_ID = "theProgressId";
    private final String CID = "theCid";
    private DefaultDataObjectResponse<AgendaTemplate> response;

    private DefaultAgendaFactory agendaFactory;
    private PrepOpsGenerator mockPrepOpsGenerator;
    private Agenda templateGeneratedAgenda = new Agenda();
    private AgendaTemplateMapper mockAgendaTemplateMapper;
    private ObjectClient<AgendaTemplate> mockAgendaTemplateClient;

    @BeforeMethod
    public void setup()
    {
        mockPrepOpsGenerator = mock(PrepOpsGenerator.class);
        mockAgendaTemplateMapper = mock(AgendaTemplateMapper.class);
        doReturn(templateGeneratedAgenda).when(mockAgendaTemplateMapper).map(any(), any());
        mockAgendaTemplateClient = mock(ObjectClient.class);
        agendaFactory = new DefaultAgendaFactory(mockAgendaTemplateClient)
            .setAgendaTemplateMapper(mockAgendaTemplateMapper)
            .setPrepOpsGenerator(mockPrepOpsGenerator);
    }

    @Test
    public void testPrepOpsFallback()
    {
        doReturn(new Agenda()).when(mockPrepOpsGenerator).generateAgenda(any());
        Agenda agenda = agendaFactory.createAgenda(new TransformRequest(), PROGRESS_ID, CID);
        Assert.assertEquals(agenda.getProgressId(), PROGRESS_ID);
        Assert.assertEquals(agenda.getCid(), CID);
        verify(mockPrepOpsGenerator, times(1)).generateAgenda(any());
    }

    @DataProvider
    public Object[][] transformRequestProvider()
    {
        return new Object[][]
            {
                { createTransformRequest(TEMPLATE_ID, null) },
                { createTransformRequest(null, TEMPLATE_NAME) },
            };
    }

    @Test(dataProvider = "transformRequestProvider")
    public void testWithTemplate(TransformRequest transformRequest)
    {
        response = new DefaultDataObjectResponse<>();
        response.add(new AgendaTemplate());
        doReturn(response).when(mockAgendaTemplateClient).getObject(TEMPLATE_ID);
        doReturn(response).when(mockAgendaTemplateClient).getObjects(anyList());
        Assert.assertEquals(agendaFactory.generateTemplatedAgenda(transformRequest), templateGeneratedAgenda);
    }


    @Test
    public void testNoTemplateFound()
    {
        doReturn(new DefaultDataObjectResponse<>()).when(mockAgendaTemplateClient).getObject(TEMPLATE_ID);
        Assert.assertNull(agendaFactory.generateTemplatedAgenda(createTransformRequest(TEMPLATE_ID, null)));
    }

    @Test
    public void testNoTemplateSpecified()
    {
        Assert.assertNull(agendaFactory.generateTemplatedAgenda(createTransformRequest(null, null)));
        verify(mockAgendaTemplateClient, times(0)).getObject(any());
        verify(mockAgendaTemplateClient, times(0)).getObjects(anyList());
    }

    public TransformRequest createTransformRequest(String templateId, String templateTitle)
    {
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setAgendaTemplateId(templateId);
        transformRequest.setAgendaTemplateTitle(templateTitle);
        return transformRequest;
    }
}
