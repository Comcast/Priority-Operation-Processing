package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.comcast.pop.api.Agenda;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class AgendaReporterTest extends AgendaBaseTest
{
    @BeforeTest
    public void init()
    {
        agendaReporter = new AgendaReporter(prefix, agendaReports);
    }

    @Test
    public void testHappyReport()
    {
        Agenda agenda = makeAgenda();
        String pattern = agendaReporter.reportInLine(agenda);
        agendaValidator.validatePattern(pattern);
    }

    @Test
    public void testNullReportInLineDoesNotFail()
    {
        Agenda agenda = new Agenda();
        String pattern = agendaReporter.reportInLine(agenda);
        assertThat(pattern).contains(prefix);
    }


    @Test
    public void testFormatInputMatch()
    {
        String pattern = "abc %s %s";

        String[] tokens = pattern.replace("%s", "a%sa").split("%s");
        String text1 = String.format(pattern, "first", "second", "extra");

        System.out.println(text1);

    }
}