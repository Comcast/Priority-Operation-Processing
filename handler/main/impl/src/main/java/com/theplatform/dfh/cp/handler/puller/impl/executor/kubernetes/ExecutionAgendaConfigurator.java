package com.theplatform.dfh.cp.handler.puller.impl.executor.kubernetes;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.handler.base.field.api.HandlerField;
import com.theplatform.dfh.cp.handler.base.payload.PayloadWriter;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.kube.client.config.ExecutionConfig;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecutionAgendaConfigurator
{
    private Logger logger = LoggerFactory.getLogger(ExecutionAgendaConfigurator.class);
    private final ExecutionConfig executionConfig;
    private final JsonHelper jsonHelper;
    private PayloadWriter payloadWriter;
    private String agendaId;

    public ExecutionAgendaConfigurator(ExecutionConfig executionConfig, JsonHelper jsonHelper, PayloadWriter payloadWriter)
    {
        this.executionConfig = executionConfig;
        this.jsonHelper = jsonHelper;
        this.payloadWriter = payloadWriter;
    }


    public void setEnvVars(Agenda agenda, AgendaProgress agendaProgress)
    {
        agendaId = agenda.getId();
        String payload = jsonHelper.getJSONString(agenda);
        String progressPayload = agendaProgress == null
            ? null
            : jsonHelper.getJSONString(agendaProgress);
        logger.debug("Launching Executor with Payload: {}", payload);

        payloadWriter.writePayload(payload);

        if(progressPayload != null)
            setEnvVar(HandlerField.LAST_PROGRESS.name(), progressPayload);
        setEnvVar(HandlerField.CID.name(), agenda.getCid());
        setEnvVar(HandlerField.AGENDA_ID.name(), agenda.getId());
        setEnvVar(HandlerField.CUSTOMER_ID.name(), agenda.getCustomerId());
        setEnvVar(HandlerField.PROGRESS_ID.name(), agenda.getProgressId());
    }

    private void setEnvVar(String key, String value)
    {
        if(!StringUtils.isBlank(value))
        {
            executionConfig.getEnvVars().put(key, value);
        }
        else
        {
            logger.warn("No value for key - " + key + " - was set on the Agenda: " + agendaId);
        }
    }

    protected void setLogger(Logger logger)
    {
        this.logger = logger;
    }

    public void setPayloadWriter(PayloadWriter payloadWriter)
    {
        this.payloadWriter = payloadWriter;
    }
}


