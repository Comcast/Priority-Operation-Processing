package com.theplatform.dfh.cp.endpoint.agenda.factory;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.endpoint.agenda.factory.template.AgendaTemplateMapper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Generates an Agenda based on the template specified in the TransformRequest
 */
public class DefaultAgendaFactory implements AgendaFactory
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultAgendaFactory.class);

    public static final String OBJECT_PAYLOAD_STRING = "fission.payload";
    public static final String TRANSFORM_PAYLOAD_STRING = "fission.transformRequest";

    private AgendaTemplateMapper agendaTemplateMapper;
    private JsonHelper jsonHelper = new JsonHelper();

    public DefaultAgendaFactory()
    {
        this.agendaTemplateMapper = new AgendaTemplateMapper();
    }

    @Override
    public Agenda createAgendaFromObject(AgendaTemplate agendaTemplate, Object payload, String progressId, String cid)
    {
        Agenda generatedAgenda = agendaTemplateMapper
            .map(agendaTemplate,
                jsonHelper.getObjectMapper().valueToTree(createDefaultPayloadMap(payload)));
        logger.info("Payload based Agenda generated from template: [{}]:[{}]", agendaTemplate.getId(), agendaTemplate.getTitle());

        if (!StringUtils.isBlank(progressId))
            generatedAgenda.setProgressId(progressId);

        if(StringUtils.isBlank(generatedAgenda.getCid()))
            generatedAgenda.setCid(cid);

        if(StringUtils.isBlank(generatedAgenda.getTitle()))
            generatedAgenda.setTitle(agendaTemplate.getTitle());

        return generatedAgenda;
    }

    protected Agenda generateTemplatedAgenda(AgendaTemplate agendaTemplate, TransformRequest transformRequest)
    {
        Agenda generatedAgenda = agendaTemplateMapper
            .map(agendaTemplate,
                jsonHelper.getObjectMapper().valueToTree(createDefaultPayloadMap(transformRequest)));
        //logger.info("Generated Agenda: {}", jsonHelper.getJSONString(generatedAgenda));
        logger.info("TransformRequest: [{}] Agenda generated from template: [{}]:[{}]", transformRequest.getId(), agendaTemplate.getId(), agendaTemplate.getTitle());
        return generatedAgenda;
    }

    protected Map<String, Object> createDefaultPayloadMap(Object payload)
    {
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put(OBJECT_PAYLOAD_STRING, payload);
        payloadMap.put(TRANSFORM_PAYLOAD_STRING, payload);
        return payloadMap;
    }

    public DefaultAgendaFactory setAgendaTemplateMapper(AgendaTemplateMapper agendaTemplateMapper)
    {
        this.agendaTemplateMapper = agendaTemplateMapper;
        return this;
    }

    public DefaultAgendaFactory setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
        return this;
    }
}
