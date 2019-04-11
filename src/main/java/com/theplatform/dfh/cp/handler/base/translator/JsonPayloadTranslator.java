package com.theplatform.dfh.cp.handler.base.translator;

import com.theplatform.dfh.cp.api.progress.DiagnosticEvent;
import com.theplatform.dfh.cp.handler.base.messages.HandlerMessages;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;

/**
 * Json translator implementation
 * @param <T>
 */
public class JsonPayloadTranslator<T> implements PayloadTranslator<T>
{
    private final JsonHelper jsonHelper;
    private DiagnosticEvent diagnosticEvent = null;

    public JsonPayloadTranslator(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    public JsonPayloadTranslator()
    {
        jsonHelper = new JsonHelper();
    }

    public PayloadTranslationResult<T> traslatePayload(String payload, Class<T> clazz)
    {
        try
        {
            return new PayloadTranslationResult<>(jsonHelper.getObjectFromString(payload, clazz));
        }
        catch (Exception e)
        {
            return new PayloadTranslationResult<>(new DiagnosticEvent(HandlerMessages.PAYLOAD_TRANSLATION_FAIL.getMessage(clazz.getSimpleName()), e));
        }
    }
}
