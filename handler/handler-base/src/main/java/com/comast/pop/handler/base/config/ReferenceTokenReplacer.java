package com.comast.pop.handler.base.config;

import com.comcast.pop.api.tokens.AgendaToken;
import com.comast.pop.handler.base.field.api.HandlerField;
import com.comast.pop.handler.base.field.retriever.LaunchDataWrapper;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonReferenceReplacer;
import org.apache.commons.lang3.StringUtils;

/**
 *
 */
public class ReferenceTokenReplacer
{
    private JsonReferenceReplacer jsonReferenceReplacer;

    public ReferenceTokenReplacer()
    {
        this.jsonReferenceReplacer = new JsonReferenceReplacer();
    }

    public ReferenceTokenReplacer setJsonReferenceReplacer(JsonReferenceReplacer jsonReferenceReplacer)
    {
        this.jsonReferenceReplacer = jsonReferenceReplacer;
        return this;
    }

    public String replaceReferenceTokens(String tokens, LaunchDataWrapper launchDataWrapper)
    {
        // TODO: for now there's only one supported... (agenda id)
        String agendaReferenceString = jsonReferenceReplacer.generateReference(AgendaToken.AGENDA_ID.getToken(), null);
        String agendaId = launchDataWrapper.getEnvironmentRetriever().getField(HandlerField.AGENDA_ID.name(), agendaReferenceString);
        return StringUtils.replace(tokens, agendaReferenceString, agendaId);
    }
}
