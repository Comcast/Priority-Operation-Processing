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

/**
 * Generates an Agenda based on the template specified in the TransformRequest
 */
public class DefaultAgendaFactory implements AgendaFactory
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultAgendaFactory.class);

    private AgendaTemplateMapper agendaTemplateMapper;
    private JsonHelper jsonHelper = new JsonHelper();

    public DefaultAgendaFactory()
    {
        this.agendaTemplateMapper = new AgendaTemplateMapper();
    }

    @Override
    public Agenda createAgenda(AgendaTemplate agendaTemplate, TransformRequest transformRequest, String progressId, String cid)
    {
        Agenda agenda = null;
        if(agendaTemplate != null)
        {
            agenda = generateTemplatedAgenda(agendaTemplate, transformRequest);
        }
        if(agenda == null)
        {
            return null;
        }
        agenda.setCustomerId(transformRequest.getCustomerId());
        // set the progress id on the agenda
        if(agenda.getParams() == null) agenda.setParams(new ParamsMap());
        addParamsFromTransformRequest(agenda, transformRequest);
        agenda.setJobId(transformRequest.getId());
        agenda.setLinkId(transformRequest.getLinkId());
        agenda.setCid(cid);
        agenda.setCustomerId(transformRequest.getCustomerId());
        if (!StringUtils.isBlank(progressId))
            agenda.setProgressId(progressId);
        return agenda;
    }

    @Override
    public Agenda createAgendaFromObject(AgendaTemplate agendaTemplate, Object payload, String progressId, String cid)
    {
        Agenda generatedAgenda = agendaTemplateMapper
            .map(agendaTemplate,
                jsonHelper.getObjectMapper().valueToTree(Collections.singletonMap("fission.payload", payload)));
        logger.info("Payload based Agenda generated from template: [{}]:[{}]", agendaTemplate.getId(), agendaTemplate.getTitle());

        if (!StringUtils.isBlank(progressId))
            generatedAgenda.setProgressId(progressId);

        return generatedAgenda;
    }

    /**
     * Adds any params to the Agenda from the TransformRequest
     * @param agenda The agenda to add any necessary params to
     * @param transformRequest The TransformRequest to pull information from
     */
    protected void addParamsFromTransformRequest(Agenda agenda, TransformRequest transformRequest)
    {
        if(agenda.getParams() == null) agenda.setParams(new ParamsMap());

        if(!StringUtils.isBlank(transformRequest.getExternalId())) agenda.getParams().put(GeneralParamKey.externalId, transformRequest.getExternalId());

        ParamsMap transformParams = transformRequest.getParams();
        if (transformParams != null && transformParams.containsKey(GeneralParamKey.doNotRun))
            agenda.getParams().put(GeneralParamKey.doNotRun, transformParams.get(GeneralParamKey.doNotRun));

        agenda.setCid(transformRequest.getCid());
    }

    protected Agenda generateTemplatedAgenda(AgendaTemplate agendaTemplate, TransformRequest transformRequest)
    {
        Agenda generatedAgenda = agendaTemplateMapper
            .map(agendaTemplate,
                jsonHelper.getObjectMapper().valueToTree(Collections.singletonMap("fission.transformRequest", transformRequest)));
        //logger.info("Generated Agenda: {}", jsonHelper.getJSONString(generatedAgenda));
        logger.info("TransformRequest: [{}] Agenda generated from template: [{}]:[{}]", transformRequest.getId(), agendaTemplate.getId(), agendaTemplate.getTitle());
        return generatedAgenda;
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
