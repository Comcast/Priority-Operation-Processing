package com.comcast.pop.cp.endpoint.agenda.factory.template;

import com.fasterxml.jackson.databind.JsonNode;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.modules.jsonhelper.JsonHelper;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.IOException;

public class AgendaTemplateMapperTest
{
    private static Logger logger = LoggerFactory.getLogger(AgendaTemplateMapperTest.class);

    private JsonHelper jsonHelper = new JsonHelper();

    @Test
    public void testMapToAgenda() throws Exception
    {
        // These resulting expressed node graph is an actual NodeGraph pojo, the other tests are purely json based
        final String expectedExpressedNodeGraph = getStringFromResourceFile("/nodegraph/AgendaExpressed.json");
        final String nodeGraphTemplate = getStringFromResourceFile("/nodegraph/AgendaTemplate.json");
        final AgendaTemplate agendaTemplate = jsonHelper.getObjectFromString(nodeGraphTemplate, AgendaTemplate.class);
        final String nodeGraphParameters = getStringFromResourceFile("/nodegraph/AgendaTemplateParameters.json");
        final JsonNode paramsNode = jsonHelper.getObjectMapper().readTree(nodeGraphParameters);
        final Agenda agenda = new AgendaTemplateMapper().map(agendaTemplate, paramsNode);

        Assert.assertNotNull(agenda);

        final JsonNode expectedExpressed = jsonHelper.getObjectMapper().readTree(expectedExpressedNodeGraph);
        final JsonNode actualExpressed = jsonHelper.getObjectMapper().valueToTree(agenda);

        logger.info(jsonHelper.getPrettyJSONString(actualExpressed));
        logger.info(jsonHelper.getPrettyJSONString(expectedExpressed));

        // NOTE: This is converting to json for comparing (not a raw string compare as that will likely never match)
        Assert.assertEquals(actualExpressed, expectedExpressed);
    }

    protected String getStringFromResourceFile(String file) throws IOException
    {
        return IOUtils.toString(
                this.getClass().getResource(file),
                "UTF-8"
        );
    }
}
