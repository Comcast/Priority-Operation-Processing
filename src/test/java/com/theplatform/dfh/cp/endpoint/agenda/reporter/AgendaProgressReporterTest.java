package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;

import java.util.Date;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.*;

public class AgendaProgressReporterTest extends AgendaBaseTest
{

    Date startedTime = new Date(1L);
    Date completedTime = new Date(2L);

    TestAgendaProgressReporter agendaProgressReporter;
    private ObjectPersister<Agenda> agendaPersister;
    private Agenda agenda;

    @BeforeTest
    public void init() throws PersistenceException
    {
        agenda = makeAgenda();
        agendaPersister = mock(ObjectPersister.class);
        when(agendaPersister.retrieve(anyString())).thenReturn(agenda);
        agendaProgressReporter = new TestAgendaProgressReporter(agendaPersister);
    }

    @Test
    public void testLogCompletedAgenda()
    {
        CaptureLogger logger = new CaptureLogger();
        agendaProgressReporter.setLogger(logger);
        AgendaProgress agendaProgress = makeAgendaProgress("success");
        agendaProgress.setCompletedTime(completedTime);
        agendaProgress.setStartedTime(startedTime);
        DataObjectResponse<AgendaProgress> response = mock(DataObjectResponse.class);
        when(response.getFirst()).thenReturn(agendaProgress);

        agendaProgressReporter.logCompletedAgenda(response);
        String logs = logger.getMsg();

        AgendaValidator validator = new AgendaValidator();
        validator.validateLogs(logs);
    }

    @Test
    public void testGetElapsedTime()
    {

        String expectedDuration = Long.toString(completedTime.getTime() - startedTime.getTime());
        AgendaProgress agendaProgress = makeAgendaProgress("success");
        agendaProgress.setCompletedTime(completedTime);
        agendaProgress.setStartedTime(startedTime);

        String elapsedTime = agendaProgressReporter.getElapsedTime(agendaProgress);

        assertThat(elapsedTime).isEqualTo(expectedDuration);
    }

    @Test
    public void testValidateElapsedTime()
    {
        AgendaProgress agendaProgress = makeAgendaProgress("success");

        String elapsedTime = agendaProgressReporter.getElapsedTime(agendaProgress);

        assertThat(elapsedTime).isEqualTo("Elapsed exec time not available for agenda: " + agendaId);
    }
}