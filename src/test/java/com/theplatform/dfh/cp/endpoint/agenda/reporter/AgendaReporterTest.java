package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import org.assertj.core.api.SoftAssertions;
import org.slf4j.Logger;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.testng.Assert.*;

public class AgendaReporterTest extends AgendaBaseTest
{
    @BeforeTest
    public void init()
    {
        agendaReporter = new AgendaReporter(prefix, agendaReports);
        testLogger = new CaptureLogger("test logger");
        agendaReporter.setLogger(testLogger);
    }

    @Test
    public void testNullReportDoesNotFail()
    {
        Agenda agenda = new Agenda();
        agendaReporter.report(agenda);
        assertThat(testLogger.getMsg()).contains(prefix);
    }

    @Test
    public void testHappyReport()
    {
        Agenda agenda = makeAgenda();
        agendaReporter.reportInLine(agenda);
        agendaValidator.validatePattern(testLogger.getMsg());
    }

    @Test
    public void testNullReportInLineDoesNotFail()
    {
        Agenda agenda = new Agenda();
        agendaReporter.reportInLine(agenda);
        assertThat(testLogger.getMsg()).contains(prefix);
    }
}