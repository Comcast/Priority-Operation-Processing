package com.theplatform.dfh.cp.endpoint.transformrequest;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.endpoint.transformrequest.agenda.generator.PrepOpsGenerator;
import com.theplatform.dfh.endpoint.api.BadRequestException;
import com.theplatform.dfh.endpoint.api.ObjectPersistResponse;
import com.theplatform.dfh.cp.endpoint.base.BaseRequestProcessor;
import com.theplatform.dfh.endpoint.client.HttpCPObjectClient;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.http.api.HttpURLConnectionFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.PersistenceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

/**
 * TransformRequest specific RequestProcessor
 */
public class TransformRequestProcessor extends BaseRequestProcessor<TransformRequest>
{
    private static final Logger logger = LoggerFactory.getLogger(TransformRequestProcessor.class);
    private JsonHelper jsonHelper = new JsonHelper();

    private PrepOpsGenerator prepOpsGenerator;
    private HttpCPObjectClient<AgendaProgress> agendaProgressClient;
    private HttpCPObjectClient<Agenda> agendaClient;


    public TransformRequestProcessor(ObjectPersister<TransformRequest> transformRequestObjectPersister, HttpURLConnectionFactory httpURLConnectionFactory,
        String agendaProgressURL, String agendaURL)
    {
        super(transformRequestObjectPersister);
        prepOpsGenerator = new PrepOpsGenerator();
        // TODO: pull these from stage vars? (could also just use relative pathing based on the incoming request...)
        agendaProgressClient = new HttpCPObjectClient<>(agendaProgressURL, httpURLConnectionFactory, AgendaProgress.class);
        agendaClient = new HttpCPObjectClient<>(agendaURL, httpURLConnectionFactory, Agenda.class);
    }

    @Override
    public ObjectPersistResponse handlePOST(TransformRequest transformRequest) throws BadRequestException
    {
        String objectId = UUID.randomUUID().toString();
        transformRequest.setId(objectId);

        // TODO: this needs to clean up the objects on fail

        ////
        // persist the prep/exec progress
        ////
        ObjectPersistResponse prepAgendaProgressResponse = createAgendaProgress(transformRequest);
        ObjectPersistResponse execAgendaProgressResponse = createAgendaProgress(transformRequest);

        if(transformRequest.getParams() == null) transformRequest.setParams(new ParamsMap());
        // this information is used for prep agenda generation, maybe these don't belong on the transform request itself
        transformRequest.getParams().put(GeneralParamKey.progressId, prepAgendaProgressResponse.getId());
        transformRequest.getParams().put(GeneralParamKey.execProgressId, execAgendaProgressResponse.getId());

        ////
        // persist the prepAgenda (this is intentionally last as the Agenda may begin processing immediately)
        ////
        Agenda prepAgenda = prepOpsGenerator.generateAgenda(transformRequest, prepAgendaProgressResponse.getId());

        //logger.debug("Generated Agenda: {}", jsonHelper.getJSONString(agenda));
        ObjectPersistResponse prepAgendaResponse;
        try
        {
            prepAgendaResponse = agendaClient.persistObject(prepAgenda);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Failed to create connection to persist the Agenda generated from the TransformRequest.", e);
        }

        ////
        // persist the TransformRequest (TODO: on fail or mismatched id the progress object must be wiped/recreated)
        ////
        try
        {
            objectPersister.persist(transformRequest);
        }
        catch(PersistenceException e)
        {
            throw new BadRequestException("Unable to create object", e);
        }

        ////
        // set the progress id on the response object
        ObjectPersistResponse response = new ObjectPersistResponse(objectId);
        if(response.getParams() == null) response.setParams(new ParamsMap());
        response.getParams().put(GeneralParamKey.progressId, prepAgendaProgressResponse.getId());
        response.getParams().put(GeneralParamKey.execProgressId, execAgendaProgressResponse.getId());
        response.getParams().put(GeneralParamKey.agendaId, prepAgendaResponse.getId());

        return response;
    }

    private ObjectPersistResponse createAgendaProgress(TransformRequest transformRequest)
    {
        ObjectPersistResponse agendaProgressResponse;
        AgendaProgress agendaProgress = new AgendaProgress();
        // NOTE: link id is the transformRequest id
        agendaProgress.setLinkId(transformRequest.getId());
        agendaProgress.setExternalId(transformRequest.getExternalId());
        agendaProgress.setProcessingState(ProcessingState.WAITING);
        logger.debug("Generated AgendaProgress: {}", jsonHelper.getJSONString(agendaProgress));
        try
        {
            agendaProgressResponse = agendaProgressClient.persistObject(agendaProgress);
        }
        catch(Exception e)
        {
            throw new RuntimeException("Failed to create connection to persist the Progress generated from the TransformRequest.", e);
        }
        return agendaProgressResponse;
    }

    public void setPrepOpsGenerator(PrepOpsGenerator prepOpsGenerator)
    {
        this.prepOpsGenerator = prepOpsGenerator;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    public void setAgendaProgressClient(HttpCPObjectClient<AgendaProgress> agendaProgressClient)
    {
        this.agendaProgressClient = agendaProgressClient;
    }

    public void setAgendaClient(HttpCPObjectClient<Agenda> agendaClient)
    {
        this.agendaClient = agendaClient;
    }
}
