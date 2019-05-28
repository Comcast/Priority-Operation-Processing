package com.theplatform.dfh.cp.endpoint.agenda.reporter;


import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.endpoint.api.agenda.service.GetAgendaResponse;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Collections;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AgendaResponseReporterTest extends AgendaBaseTest
{
    @BeforeTest
    public void init()
    {
        agendaReporter = new TestAgendaReporter(prefix, agendaReports);
        testLogger = new TestLogger("test logger");
        agendaReporter.setLogger(testLogger);
    }

    @Test
    public void TestAgendaResponse()
    {
        GetAgendaResponse getAgendaResponse = new GetAgendaResponse();
        getAgendaResponse.setAgendas(Collections.singletonList(makeAgenda()));
        AgendaResponseReporter agendaResponseReporter = new AgendaResponseReporter(getAgendaResponse, agendaReporter);
        agendaResponseReporter.setAgendaProgress(makeAgendaProgress("succeeded"));
        agendaResponseReporter.reportAgendaResponse();
        agendaValidator.validateLogs(testLogger.getMsg());
        assertThat(testLogger.getMsg()).contains(AgendaReports.AGENDA_STATUS.label);
        assertThat(testLogger.getMsg()).contains(AgendaConclusionStatus.succeeded.name());
    }




}