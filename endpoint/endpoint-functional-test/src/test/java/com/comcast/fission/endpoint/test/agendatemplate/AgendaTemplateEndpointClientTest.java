package com.comcast.fission.endpoint.test.agendatemplate;

import com.comcast.fission.endpoint.api.data.DataObjectResponse;
import com.comcast.fission.endpoint.api.data.query.ByTitle;
import com.comcast.fission.endpoint.test.BaseEndpointObjectClientTest;
import com.comcast.fission.endpoint.test.factory.DataGenerator;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.Collections;

public class AgendaTemplateEndpointClientTest extends BaseEndpointObjectClientTest<AgendaTemplate>
{
    private final String UPDATED_TITLE = "theUpdate";

    private static final Logger logger = LoggerFactory.getLogger(AgendaTemplateEndpointClientTest.class);

    public AgendaTemplateEndpointClientTest()
    {
        super(AgendaTemplate.class);
    }

    @Override
    public String getEndpointUrl()
    {
        return agendaTemplateUrl;
    }

    @Override
    protected AgendaTemplate getTestObject()
    {
        return DataGenerator.generateAgendaTemplate(testCustomerId);
    }

    @Override
    protected AgendaTemplate updateTestObject(AgendaTemplate object)
    {
        object.setTitle(UPDATED_TITLE);
        return object;
    }

    @Override
    protected void verifyCreatedTestObject(AgendaTemplate createdObject, AgendaTemplate testObject)
    {
        final String EXPECTED_CID = testObject.getCid();

        Assert.assertEquals(createdObject.getCid(), EXPECTED_CID);
    }

    @Test
    public void testByTitle()
    {
        ByTitle byTitle = new ByTitle(UPDATED_TITLE);
        AgendaTemplate agendaTemplate = DataGenerator.generateAgendaTemplate(testCustomerId);
        agendaTemplate.setTitle(UPDATED_TITLE);

        DataObjectResponse<AgendaTemplate> response = agendaTemplateClient.persistObject(agendaTemplate);
        Assert.assertFalse(response.isError());

        response = agendaTemplateClient.getObjects(Collections.singletonList(byTitle));
        Assert.assertFalse(response.isError());
        Assert.assertEquals(response.getFirst().getTitle(), UPDATED_TITLE);
    }

    @Override
    protected void verifyUpdatedTestObject(AgendaTemplate object)
    {
        Assert.assertEquals(object.getTitle(), UPDATED_TITLE);
    }
}
