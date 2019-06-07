package com.theplatform.dfh.cp.handler.puller.impl.executor.kubernetes;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.handler.puller.impl.CaptureLogger;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.KubeConfig;
import com.theplatform.dfh.cp.modules.kube.client.config.PodConfig;
import org.assertj.core.api.SoftAssertions;
import org.slf4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.testng.Assert.*;

public class KubernetesLauncherTest
{

    TestLogKuberentesLauncher kubernetesLauncher;

    @BeforeTest
    public void init()
    {
        kubernetesLauncher = new TestLogKuberentesLauncher(new KubeConfig(), new PodConfig(), new ExecutionConfig());
    }

    @Test
    public void testLog()
    {
        Logger captureLogger = new CaptureLogger("testLogger");
        kubernetesLauncher.setLogger(captureLogger);
        Agenda agenda = new Agenda();
        String customerId = "testCustomerId";
        agenda.setCustomerId(customerId);
        String agendaId = "testAgendaId";
        agenda.setId(agendaId);
        kubernetesLauncher.logAgendaMetadata(agenda);
        String capturedLog = ((CaptureLogger) captureLogger).getInfo();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(capturedLog).contains(customerId);
            softly.assertThat(capturedLog).contains(agendaId);
        });
    }

    @Test
    public void testLogNullAgenda()
    {
        Logger captureLogger = new CaptureLogger("testLogger");
        kubernetesLauncher.setLogger(captureLogger);
        kubernetesLauncher.logAgendaMetadata(null);
        String capturedLog = ((CaptureLogger) captureLogger).getInfo();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(capturedLog).contains("agenda owner not visible");
            softly.assertThat(capturedLog).contains("agendaId not visible");
        });
    }

    @Test
    public void testLogNullAgendaFields()
    {
        Logger captureLogger = new CaptureLogger("testLogger");
        kubernetesLauncher.setLogger(captureLogger);
        kubernetesLauncher.logAgendaMetadata(new Agenda());
        String capturedLog = ((CaptureLogger) captureLogger).getInfo();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(capturedLog).contains("agenda owner not visible");
            softly.assertThat(capturedLog).contains("agendaId not visible");
        });
    }
}