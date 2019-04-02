package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.theplatform.dfh.cp.api.Agenda;

public interface AgendaReport
{
    public static final String ADDED_KEY = "added";

    String report(Agenda agenda);
}
