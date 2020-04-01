package com.theplatform.dfh.cp.endpoint.agenda.factory;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.endpoint.agenda.factory.template.AgendaTemplateMapper;
import com.comcast.fission.endpoint.api.data.DefaultDataObjectResponse;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class DefaultAgendaFactoryTest
{
    private final String TEMPLATE_ID = "theId";
    private final String TEMPLATE_NAME = "theName";
    private final String PROGRESS_ID = "theProgressId";
    private final String CID = "theCid";
    private DefaultDataObjectResponse<AgendaTemplate> response;

    private DefaultAgendaFactory agendaFactory;
    private AgendaTemplate agendaTemplate = new AgendaTemplate();
    private Agenda templateGeneratedAgenda = new Agenda();
    private AgendaTemplateMapper mockAgendaTemplateMapper;

    @BeforeMethod
    public void setup()
    {
        mockAgendaTemplateMapper = mock(AgendaTemplateMapper.class);
        doReturn(templateGeneratedAgenda).when(mockAgendaTemplateMapper).map(any(), any());
        agendaFactory = new DefaultAgendaFactory()
            .setAgendaTemplateMapper(mockAgendaTemplateMapper);
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
        Assert.assertEquals(agendaFactory.generateTemplatedAgenda(agendaTemplate, transformRequest), templateGeneratedAgenda);
    }

    public TransformRequest createTransformRequest(String templateId, String templateTitle)
    {
        TransformRequest transformRequest = new TransformRequest();
        transformRequest.setAgendaTemplateId(templateId);
        transformRequest.setAgendaTemplateTitle(templateTitle);
        return transformRequest;
    }
}
