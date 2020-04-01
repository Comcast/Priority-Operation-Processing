package com.comcast.fission.endpoint.api.auth.resourcepool;

import com.comcast.fission.endpoint.api.resourcepool.GetAgendaResponse;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class GetAgendaResponseTest
{
    private GetAgendaResponse response;

    @BeforeMethod
    public void setup()
    {
        response = new GetAgendaResponse();
    }

    @Test
    public void testRetrieveAgendaToProgressMapNoData()
    {
        Assert.assertNotNull(response.retrieveAgendaToProgressMap());
    }

    @Test
    public void testRetrieveAgendaToProgressMapAgendaOnly()
    {
        response.setAgendas(Collections.singletonList(createAgenda(UUID.randomUUID().toString(), UUID.randomUUID().toString())));
        Map<Agenda, AgendaProgress> agendaToProgressMap = response.retrieveAgendaToProgressMap();
        Assert.assertNotNull(agendaToProgressMap);
        Assert.assertEquals(agendaToProgressMap.size(), 1);
    }

    @Test
    public void testRetrieveAgendaToProgressMapSingle()
    {
        final String AGENDA_PROGRESS_ID = UUID.randomUUID().toString();
        Agenda agenda = createAgenda(UUID.randomUUID().toString(), AGENDA_PROGRESS_ID);
        response.setAgendas(Collections.singletonList(agenda));
        response.setAgendaProgresses(Collections.singletonList(createAgendaProgress(AGENDA_PROGRESS_ID)));
        Map<Agenda, AgendaProgress> agendaToProgressMap = response.retrieveAgendaToProgressMap();
        Assert.assertNotNull(agendaToProgressMap);
        Assert.assertEquals(agendaToProgressMap.size(), 1);
        Assert.assertNotNull(agendaToProgressMap.get(agenda));
    }

    @Test
    public void testRetrieveAgendaToProgressMapMix()
    {
        final String AGENDA_PROGRESS_ID = UUID.randomUUID().toString();
        Agenda agenda = createAgenda(UUID.randomUUID().toString(), AGENDA_PROGRESS_ID);
        Agenda agendaTwo = createAgenda(UUID.randomUUID().toString(), "");
        response.setAgendas(Arrays.asList(agenda, agendaTwo));
        response.setAgendaProgresses(Collections.singletonList(createAgendaProgress(AGENDA_PROGRESS_ID)));
        Map<Agenda, AgendaProgress> agendaToProgressMap = response.retrieveAgendaToProgressMap();
        Assert.assertNotNull(agendaToProgressMap);
        Assert.assertEquals(agendaToProgressMap.size(), 2);
        Assert.assertNotNull(agendaToProgressMap.get(agenda));
        Assert.assertNull(agendaToProgressMap.get(agendaTwo));
    }

    protected AgendaProgress createAgendaProgress(String agendaProgressId)
    {
        AgendaProgress agendaProgress = new AgendaProgress();
        agendaProgress.setId(agendaProgressId);
        return agendaProgress;
    }

    protected Agenda createAgenda(String agendaId, String agendaProgressId)
    {
        Agenda agenda = new Agenda();
        agenda.setId(agendaId);
        agenda.setProgressId(agendaProgressId);
        return agenda;
    }
}
