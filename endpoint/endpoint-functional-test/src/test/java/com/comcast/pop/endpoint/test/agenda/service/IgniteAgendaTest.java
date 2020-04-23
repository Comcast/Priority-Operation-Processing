package com.comcast.pop.endpoint.test.agenda.service;

import com.comcast.pop.endpoint.api.agenda.RunAgendaRequest;
import com.comcast.pop.endpoint.api.agenda.RunAgendaResponse;
import com.comcast.pop.endpoint.api.data.DataObjectResponse;
import com.comcast.pop.endpoint.test.base.EndpointTestBase;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.AgendaTemplate;
import com.comcast.pop.api.operation.Operation;
import com.comcast.pop.api.params.GeneralParamKey;
import com.comcast.pop.api.params.ParamsMap;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Test the creation of Agendas with a template+payload
 */
public class IgniteAgendaTest extends EndpointTestBase
{
    private static final Logger logger = LoggerFactory.getLogger(IgniteAgendaTest.class);

    @Test(enabled = false)
    public void simpleIgniteAgenda()
    {
        RunAgendaRequest generateAgendaRequest = createRequest(
            "899f2241-3877-4301-b3d2-aa7e4274e499",
            "{\"logMessage\":\"This is the message from the input payload.\"}"
        );

        RunAgendaResponse response = agendaServiceClient.runAgenda(generateAgendaRequest);
        logger.info(jsonHelper.getPrettyJSONString(response));
    }

    @DataProvider
    public Object[][] validTemplateParams()
    {
        return new Object[][]
            {
                {"Hi!", "{}"}, // no params
                {"@<pop.payload::/value>", "{\"value\":\"1234\"}"} // a param
            };
    }

    @Test(dataProvider = "validTemplateParams")
    public void testValidTemplateParams(String logMessage, String payload)
    {
        String agendaTemplateId = createTestAgendaTemplate(logMessage);
        RunAgendaResponse response = agendaServiceClient.runAgenda(createRequest(agendaTemplateId, payload));
        verifyNoError(response);
        Assert.assertNotNull(response.getAgendas());
        Assert.assertEquals(response.getAgendas().size(), 1);
        Agenda agenda = new LinkedList<>(response.getAgendas()).get(0);
        identifiedObjectCleanupManager.getIdentifiedObjectCreateTracker().registerForCleanup(Agenda.class, agenda.getId());
        registerProgressObjectsForCleanup(agenda.getProgressId(), agenda.getOperations());
        Assert.assertEquals(agenda.getOperations().size(), 1);
        Assert.assertEquals(agenda.getOperations().get(0).getType(), testInsightOperation);
    }

    @DataProvider
    public Object[][] invalidParametersProvider()
    {
        return new Object[][]
            {
                { null, "must be specified"},
                { createRequest(null, null), "must be specified" },
                { createRequest("", null), "must be specified" },
                { createRequest("A", "{"), "Unable to parse input payload as JSON" }
            };
    }

    @Test(dataProvider = "invalidParametersProvider")
    public void testInvalidParameters(RunAgendaRequest igniteAgendaRequest, String expectedMessageFragment)
    {
        RunAgendaResponse response = agendaServiceClient.runAgenda(igniteAgendaRequest);
        Assert.assertTrue(response.isError());
        Assert.assertTrue(StringUtils.containsIgnoreCase(response.getErrorResponse().getDescription(), expectedMessageFragment));
    }

    private RunAgendaRequest createRequest(String agendaTemplateId, String payload)
    {
        RunAgendaRequest request = new RunAgendaRequest();
        request.setAgendaTemplateId(agendaTemplateId);
        request.setPayload(payload);
        return request;
    }

    private String createTestAgendaTemplate(String messageString)
    {
        AgendaTemplate agendaTemplate = new AgendaTemplate();
        agendaTemplate.setCustomerId(testCustomerId);
        Agenda agenda = new Agenda();
        agenda.setCustomerId(testCustomerId);
        Map<String, List<String>> logHandlerInput = new HashMap<>();
        logHandlerInput.put(testInsightOperation, Collections.singletonList(messageString));
        Operation operation = new Operation();
        operation.setType(testInsightOperation);
        operation.setName("log.1");
        operation.setPayload(logHandlerInput);
        agenda.setOperations(Collections.singletonList(operation));
        ParamsMap paramsMap = new ParamsMap();
        // These agendas are not intended to execute!
        paramsMap.put(GeneralParamKey.doNotRun, null);
        agenda.setParams(paramsMap);
        agendaTemplate.setAgenda(agenda);

        agendaTemplate.setTitle(this.getClass().getSimpleName() + "-" + UUID.randomUUID().toString());

        DataObjectResponse<AgendaTemplate> response = agendaTemplateClient.persistObject(agendaTemplate);
        verifyNoError(response);
        Assert.assertNotNull(response.getFirst(), "No AgendaTemplate created!");
        return response.getFirst().getId();
    }
}
