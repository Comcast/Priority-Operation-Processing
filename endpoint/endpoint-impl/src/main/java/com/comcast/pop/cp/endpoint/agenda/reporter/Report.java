package com.comcast.pop.cp.endpoint.agenda.reporter;

public interface Report<T,V>
{
    public static final String ADDED_KEY = "added";
    public static final String CONCLUSION_STATUS_KEY = "conclusion_status";

    V report(T object);
}
