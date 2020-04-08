package com.comcast.pop.handler.puller.impl.executor.kubernetes;

import com.comcast.pop.handler.puller.impl.CaptureLogger;
import com.comcast.pop.api.Agenda;
import com.comcast.pop.modules.kube.client.config.ExecutionConfig;
import com.comcast.pop.modules.kube.client.config.KubeConfig;
import com.comcast.pop.modules.kube.client.config.PodConfig;
import org.assertj.core.api.SoftAssertions;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        CaptureLogger captureLogger = new CaptureLogger("testLogger");
        kubernetesLauncher.setLogger(captureLogger);
        Agenda agenda = new Agenda();
        String customerId = "testCustomerId";
        agenda.setCustomerId(customerId);
        String agendaId = "testAgendaId";
        agenda.setId(agendaId);
        kubernetesLauncher.logAgendaMetadata(agenda);
        String capturedLog = captureLogger.getInfo();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(capturedLog).contains(customerId);
            softly.assertThat(capturedLog).contains(agendaId);
        });
    }

    @Test
    public void testLogNullAgenda()
    {
        CaptureLogger captureLogger = new CaptureLogger("testLogger");
        kubernetesLauncher.setLogger(captureLogger);
        kubernetesLauncher.logAgendaMetadata(null);
        String capturedLog = captureLogger.getInfo();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(capturedLog).contains("agenda owner not visible");
            softly.assertThat(capturedLog).contains("agendaId not visible");
        });
    }

    @Test
    public void testLogNullAgendaFields()
    {
        CaptureLogger captureLogger = new CaptureLogger("testLogger");
        kubernetesLauncher.setLogger(captureLogger);
        kubernetesLauncher.logAgendaMetadata(new Agenda());
        String capturedLog = captureLogger.getInfo();
        SoftAssertions.assertSoftly(softly -> {
            softly.assertThat(capturedLog).contains("agenda owner not visible");
            softly.assertThat(capturedLog).contains("agendaId not visible");
        });
    }
}