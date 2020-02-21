package com.theplatform.dfh.cp.handler.puller.impl.executor.kubernetes;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.base.payload.PayloadWriter;
import com.theplatform.dfh.cp.handler.kubernetes.support.payload.EnvironmentPayloadWriter;
import com.theplatform.dfh.cp.handler.puller.impl.CaptureLogger;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ExecutionAgendaConfiguratorTest
{
    private CaptureLogger captureLogger = new CaptureLogger();
    private ExecutionAgendaConfigurator executionConfigurator;
    private PayloadWriter payloadWriter;
    private ExecutionConfig executionConfig;
    private JsonHelper jsonHelper;
    private Agenda agenda;
    private AgendaProgress agendaProgress = new AgendaProgress();
    private String payload = "testPayload";
    private String progressPayload = "testProgressPayload";
    private String agendaId = "testAgendaId";
    private String cid = "testCID";
    private String customerId = "testCustomerId";
    private String progressId = "testProgressId";

    @BeforeMethod
    public void init()
    {
        jsonHelper = mock(JsonHelper.class);
        executionConfig = new ExecutionConfig();
        payloadWriter = new EnvironmentPayloadWriter();
        executionConfigurator = new ExecutionAgendaConfigurator(executionConfig, jsonHelper, payloadWriter);
        executionConfigurator.setLogger(captureLogger);
    }

    @DataProvider
    public Object[][] envVarProvider()
    {
        return new Object[][] {{true}, {false}};
    }

    @Test(dataProvider = "envVarProvider")
    public void testSetEnvVars(boolean setAgendaProgress)
    {
        agenda = makeAgenda();
        setupJsonHelperResponses(setAgendaProgress);
        executionConfigurator.setEnvVars(
            agenda,
            setAgendaProgress ? agendaProgress : null);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.CID.name())).isEqualTo(cid);
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.PAYLOAD.name())).isEqualTo(payload);
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.AGENDA_ID.name())).isEqualTo(agendaId);
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.CUSTOMER_ID.name())).isEqualTo(customerId);
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.PROGRESS_ID.name())).isEqualTo(progressId);
            if(setAgendaProgress)
                softly.assertThat(executionConfig.getEnvVars().get(HandlerField.LAST_PROGRESS.name())).isEqualTo(progressPayload);
            else
                softly.assertThat(executionConfig.getEnvVars().get(HandlerField.LAST_PROGRESS.name())).isNull();
        });
    }

    @Test
    public void testLogWarning()
    {
        agenda = makeAgenda();
        agenda.setCustomerId(null);
        setupJsonHelperResponses(true);
        executionConfigurator.setEnvVars(agenda, null);

        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(captureLogger.getWarn()).isEqualTo("No value for key - CUSTOMER_ID - was set on the Agenda: testAgendaId");
            // expect everything else is still good.
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.CID.name())).isEqualTo(cid);
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.PAYLOAD.name())).isEqualTo(payload);
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.AGENDA_ID.name())).isEqualTo(agendaId);
            softly.assertThat(executionConfig.getEnvVars().get(HandlerField.PROGRESS_ID.name())).isEqualTo(progressId);
        });
    }

    private void setupJsonHelperResponses(boolean setAgendaProgress)
    {
        doReturn(payload).when(jsonHelper).getJSONString(agenda);
        if(setAgendaProgress)
            doReturn(progressPayload).when(jsonHelper).getJSONString(agendaProgress);
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