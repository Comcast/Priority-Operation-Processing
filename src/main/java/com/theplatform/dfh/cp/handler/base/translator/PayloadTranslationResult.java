package com.theplatform.dfh.cp.handler.base.translator;

import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;

/**
 * Basic result object for calls to the implementations of the PayloadTranslator
 * @param <T>
 */
public class PayloadTranslationResult<T>
{
    private final T object;
    private final DiagnosticEvent diagnosticEvent;

    public PayloadTranslationResult(T object)
    {
        this.object = object;
        this.diagnosticEvent = null;
    }

    public PayloadTranslationResult(DiagnosticEvent diagnosticEvent)
    {
        this.object = null;
        this.diagnosticEvent = diagnosticEvent;
    }

    public T getObject()
    {
        return object;
    }

    public DiagnosticEvent getDiagnosticEvent()
    {
        return diagnosticEvent;
    }

    public boolean isSuccessful()
    {
        return object != null;
    }
}
