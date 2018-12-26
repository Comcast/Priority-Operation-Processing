package com.theplatform.dfh.cp.endpoint.transformrequest.agenda.generator;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.handler.analysis.mediainfo.api.MediaInfoHandlerInput;
import com.theplatform.dfh.cp.handler.util.http.api.HttpRequestHandlerInput;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import com.theplatform.dfh.cp.modules.jsonhelper.replacement.JsonReferenceReplacer;
import com.theplatformdfh.cp.handler.filter.accelerate.api.AccelerateHandlerInput;
import com.theplatformdfh.cp.handler.filter.ldap.api.LDAPHandlerInput;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedList;
import java.util.List;

public class PrepOpsGenerator
{
    private JsonReferenceReplacer jsonReferenceReplacer = new JsonReferenceReplacer();
    private JsonHelper jsonHelper = new JsonHelper();
    private static ObjectMapper objectMapper = new ObjectMapper();
    final String OUT_SUFFIX = ".out";

    public Agenda generateAgenda(TransformRequest transformRequest, String progressId)
    {
        final String LDAP_NAME = "ldap.1";
        final String ANALYSIS_NAME = "mediaInfo.1";
        final String ACCELERATE_NAME = "accelerate.1";

        List<Operation> ops = new LinkedList<>();

        // setup the ldap op
        LDAPHandlerInput ldapHandlerInput = new LDAPHandlerInput();
        ldapHandlerInput.setOriginalRequest(transformRequest);

        addOp(ops, LDAP_NAME, "ldap", ldapHandlerInput);

        // setup the media info op
        MediaInfoHandlerInput mediaInfoHandlerInput = new MediaInfoHandlerInput();
        JsonNode mediaInfoPayload = objectMapper.valueToTree(mediaInfoHandlerInput);

        jsonHelper.setNodeValue(mediaInfoPayload, "/transformRequest", jsonReferenceReplacer.generateReference(LDAP_NAME + OUT_SUFFIX, "/transformRequest"));

        addOp(ops, ANALYSIS_NAME, "analysis", mediaInfoPayload);

        // setup the accelerate filter op
        AccelerateHandlerInput accelerateHandlerInput = new AccelerateHandlerInput();
        accelerateHandlerInput.setOriginalRequest(transformRequest);
        JsonNode acceleratePayload = objectMapper.valueToTree(accelerateHandlerInput);

        jsonHelper.setNodeValue(acceleratePayload, "/originalRequest", jsonReferenceReplacer.generateReference(ANALYSIS_NAME + OUT_SUFFIX, "/transformRequest"));

        addOp(ops, ACCELERATE_NAME, "accelerate", acceleratePayload);

        // setup the http request to post the agenda
        // (TODO: this will likely require the auth token to pass through or otherwise horrible things)
        HttpRequestHandlerInput httpRequestHandlerInput = new HttpRequestHandlerInput()
            .setPostDataEncoding("json")
            // this field will be replaced with the output of the accelerate operation
            .setPostData(jsonReferenceReplacer.generateReference(ACCELERATE_NAME + OUT_SUFFIX, "/agenda"));
        addOp(ops, "agendaPost.1", "agendaPost", httpRequestHandlerInput);

        Agenda agenda = new Agenda();

        // set the progress id on the agenda
        agenda.setParams(new ParamsMap());
        addParamsFromTransformRequest(agenda, transformRequest);
        agenda.setOperations(ops);
        agenda.setJobId(transformRequest.getId());
        agenda.setLinkId(transformRequest.getId());
        agenda.setCustomerId(transformRequest.getCustomerId());
        if(!StringUtils.isBlank(progressId)) agenda.setProgressId(progressId);

        return agenda;
    }

    /**
     * Adds any params to the Agenda from the TransformRequest
     * @param agenda The agenda to add any necessary params to
     * @param transformRequest The TransformRequest to pull information from
     */
    protected void addParamsFromTransformRequest(Agenda agenda, TransformRequest transformRequest)
    {
        if(!StringUtils.isBlank(transformRequest.getExternalId())) agenda.getParams().put(GeneralParamKey.externalId, transformRequest.getExternalId());

        ParamsMap transformRequestParams = transformRequest.getParams();
        if(transformRequestParams == null) return;
        if(agenda.getParams() == null) agenda.setParams(new ParamsMap());

        String cid = transformRequestParams.getString(GeneralParamKey.cid);
        if(!StringUtils.isBlank(cid))
        {
            agenda.getParams().put(GeneralParamKey.cid, cid);
        }
    }

    protected void addOp(List<Operation> ops, String name, String type, Object payload)
    {
        Operation operation = new Operation();
        operation.setName(name);
        operation.setType(type);
        operation.setPayload(payload);
        ops.add(operation);
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    public void setJsonReferenceReplacer(JsonReferenceReplacer jsonReferenceReplacer)
    {
        this.jsonReferenceReplacer = jsonReferenceReplacer;
    }
}
