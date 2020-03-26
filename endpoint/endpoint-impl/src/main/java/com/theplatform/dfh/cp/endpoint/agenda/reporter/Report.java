package com.theplatform.dfh.cp.endpoint.agenda.reporter;

import com.theplatform.dfh.cp.api.Agenda;

public interface Report<T,V>
{
    public static final String ADDED_KEY = "added";
    public static final String CONCLUSION_STATUS_KEY = "conclusion_status";

    V report(T object);
}
