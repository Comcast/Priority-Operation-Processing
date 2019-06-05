package com.theplatform.dfh.cp.handler.puller.impl.executor.kubernetes;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.handler.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.puller.impl.CaptureLogger;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ExecutionAgendaConfiguratorTest
{
    private CaptureLogger captureLogger = new CaptureLogger();
    private ExecutionAgendaConfigurator executionConfigurator;
    private ExecutionConfig executionConfig;
    private String payload = "testPayload";
    private String agendaId = "testAgendaId";
    private String cid = "testCID";
    private String customerId = "testCustomerId";
    private String progressId = "testProgressId";

    @BeforeTest
    public void init()
    {
        executionConfig = new ExecutionConfig();
        JsonHelper jsonHelper = mock(JsonHelper.class);
        when(jsonHelper.getJSONString(anyObject())).thenReturn(payload);
        executionConfigurator = new ExecutionAgendaConfigurator(executionConfig, jsonHelper);
        executionConfigurator.setLogger(captureLogger);
    }

    @Test
    public void testSetEnvVars()
    {
        Agenda agenda = makeAgenda();
        executionConfigurator.setEnvVars(agenda);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.CID.name())).isEqualTo(cid);
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.PAYLOAD.name())).isEqualTo(payload);
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.AGENDA_ID.name())).isEqualTo(agendaId);
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.CUSTOMER_ID.name())).isEqualTo(customerId);
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.PROGRESS_ID.name())).isEqualTo(progressId);
        });
    }

    @Test
    public void testLogWarning()
    {
        Agenda agenda = makeAgenda();
        agenda.setCustomerId(null);
        executionConfigurator.setEnvVars(agenda);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(captureLogger.getWarn()).isEqualTo("No value for key - CUSTOMER_ID - was set on the Agenda: testAgendaId");
            // expect everything else is still good.
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.CID.name())).isEqualTo(cid);
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.PAYLOAD.name())).isEqualTo(payload);
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.AGENDA_ID.name())).isEqualTo(agendaId);
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.PROGRESS_ID.name())).isEqualTo(progressId);
        });
    }

    private Agenda makeAgenda()
    {
        Agenda agenda = new Agenda();
        agenda.setId(agendaId);
        agenda.setCid(cid);
        agenda.setCustomerId(customerId);
        agenda.setProgressId(progressId);
        return agenda;
    }
}