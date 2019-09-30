package com.theplatform.dfh.cp.endpoint.agenda.factory;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.AgendaTemplate;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.endpoint.agenda.factory.template.AgendaTemplateMapper;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.endpoint.api.data.DataObjectResponse;
import com.theplatform.dfh.endpoint.api.data.query.ByTitle;
import com.theplatform.dfh.endpoint.client.ObjectClient;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;

/**
 * Generates an Agenda based on the template specified in the TransformRequest (defaulting to our Accelerate PrepOps generator)
 */
public class DefaultAgendaFactory implements AgendaFactory
{
    private static final Logger logger = LoggerFactory.getLogger(DefaultAgendaFactory.class);

    private ObjectClient<AgendaTemplate> agendaTemplateClient;
    private PrepOpsGenerator prepOpsGenerator;
    private AgendaTemplateMapper agendaTemplateMapper;
    private JsonHelper jsonHelper = new JsonHelper();

    public DefaultAgendaFactory(ObjectClient<AgendaTemplate> agendaTemplateClient)
    {
        this.agendaTemplateClient = agendaTemplateClient;
        this.prepOpsGenerator = new PrepOpsGenerator();
        this.agendaTemplateMapper = new AgendaTemplateMapper();
    }

    private DefaultAgendaFactory(){}

    @Override
    public Agenda createAgenda(TransformRequest transformRequest, String progressId, String cid)
    {
        Agenda agenda = generateTemplatedAgenda(transformRequest);
        if(agenda == null)
        {
            agenda = prepOpsGenerator.generateAgenda(transformRequest);
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

    protected Agenda generateTemplatedAgenda(TransformRequest transformRequest)
    {
        DataObjectResponse<AgendaTemplate> response = null;
        if(transformRequest.getAgendaTemplateId() != null)
            response = agendaTemplateClient.getObject(transformRequest.getAgendaTemplateId());
        else if(transformRequest.getAgendaTemplateTitle() != null)
            response = agendaTemplateClient.getObjects(Collections.singletonList(new ByTitle(transformRequest.getAgendaTemplateTitle())));

        if(response == null)
        {
            logger.info("No agendaTemplateId or agendaTemplateTitle provided on TransformRequest.");
            return null;
        }

        if(!response.isError() && response.getFirst() != null)
        {
            AgendaTemplate agendaTemplate = response.getFirst();
            Agenda generatedAgenda = agendaTemplateMapper
                .map(agendaTemplate,
                    jsonHelper.getObjectMapper().valueToTree(Collections.singletonMap("fission.transformRequest", transformRequest)));
            //logger.info("Generated Agenda: {}", jsonHelper.getJSONString(generatedAgenda));
            logger.info("TransformRequest: [{}] Agenda generated from template: [{}]:[{}]", transformRequest.getId(), agendaTemplate.getId(), agendaTemplate.getTitle());
            return generatedAgenda;
        }
        else
        {
            logger.error("No AgendaTemplate could be found by id: [{}] or title: [{}]", transformRequest.getAgendaTemplateId(), transformRequest.getAgendaTemplateTitle());
        }
        return null;
    }

    public DefaultAgendaFactory setAgendaTemplateClient(ObjectClient<AgendaTemplate> agendaTemplateClient)
    {
        this.agendaTemplateClient = agendaTemplateClient;
        return this;
    }

    public DefaultAgendaFactory setPrepOpsGenerator(PrepOpsGenerator prepOpsGenerator)
    {
        this.prepOpsGenerator = prepOpsGenerator;
        return this;
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
