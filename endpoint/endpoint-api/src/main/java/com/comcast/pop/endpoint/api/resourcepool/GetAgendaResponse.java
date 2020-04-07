package com.comcast.pop.endpoint.api.resourcepool;

import com.comcast.pop.api.Agenda;
import com.comcast.pop.api.progress.AgendaProgress;
import com.comcast.pop.endpoint.api.DataObjectFeedServiceResponse;
import com.comcast.pop.endpoint.api.ErrorResponse;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class GetAgendaResponse extends DataObjectFeedServiceResponse<Agenda>
{
    private List<AgendaProgress> agendaProgresses;

    public GetAgendaResponse()
    {
    }

    public GetAgendaResponse(ErrorResponse errorResponse)
    {
        super(errorResponse);
    }

    public GetAgendaResponse(Collection<Agenda> dataObjects)
    {
        super(dataObjects);
    }

    public GetAgendaResponse(Collection<Agenda> dataObjects, List<AgendaProgress> agendaProgresses)
    {
        super(dataObjects);
        this.agendaProgresses = agendaProgresses;
    }

    public void setAgendas(Collection<Agenda> dataObjects)
    {
        super.setAll(dataObjects);
    }

    public Collection<Agenda> getAgendas()
    {
        return super.getAll();
    }

    public List<AgendaProgress> getAgendaProgresses()
    {
        return agendaProgresses;
    }

    public void setAgendaProgresses(List<AgendaProgress> agendaProgresses)
    {
        this.agendaProgresses = agendaProgresses;
    }

    /**
     * Retrieves a map of Agenda to AgendaProgress objects based on the response. The AgendaProgress may be null.
     * @return Map of Agenda to AgendaProgress objects
     */
    public Map<Agenda, AgendaProgress> retrieveAgendaToProgressMap()
    {
        Map<String, AgendaProgress> agendaProgressMap = getAgendaProgresses() == null
            ? new HashMap<>()
            : getAgendaProgresses().stream().collect(Collectors.toMap(AgendaProgress::getId, Function.identity()));

        Map<Agenda, AgendaProgress> agendaToProgressMap = new HashMap<>();
        if(getAgendas() != null)
        {
            for (Agenda agenda : getAgendas())
            {
                agendaToProgressMap.put(agenda, agendaProgressMap.get(agenda.getProgressId()));
            }
        }
        return agendaToProgressMap;
    }
}
