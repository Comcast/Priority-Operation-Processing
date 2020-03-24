package com.theplatform.dfh.cp.endpoint.agenda.factory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.api.operation.OperationReference;
import com.theplatform.dfh.cp.api.params.GeneralParamKey;
import com.theplatform.dfh.cp.api.params.ParamsMap;
import com.theplatform.dfh.cp.handler.analysis.mediainfo.api.FileAnalysisHandlerInput;
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

    public Agenda generateAgenda(TransformRequest transformRequest)
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
        FileAnalysisHandlerInput fileAnalysisHandlerInput = new FileAnalysisHandlerInput();
        JsonNode mediaInfoPayload = objectMapper.valueToTree(fileAnalysisHandlerInput);

        jsonHelper.setNodeValue(mediaInfoPayload, "/inputs", jsonReferenceReplacer.generateReference(LDAP_NAME + OperationReference.OUTPUT.getSuffix(),
            "/transformRequest/inputs"));

        addOp(ops, ANALYSIS_NAME, "analysis", mediaInfoPayload);

        // setup the accelerate filter op
        AccelerateHandlerInput accelerateHandlerInput = new AccelerateHandlerInput();
        accelerateHandlerInput.setOriginalRequest(transformRequest);
        JsonNode acceleratePayload = objectMapper.valueToTree(accelerateHandlerInput);

        jsonHelper.setNodeValue(acceleratePayload, "/inputStreams", jsonReferenceReplacer.generateReference(ANALYSIS_NAME + OperationReference.OUTPUT.getSuffix(), "/inputStreams"));
        jsonHelper.setNodeValue(acceleratePayload, "/originalRequest", jsonReferenceReplacer.generateReference(LDAP_NAME + OperationReference.OUTPUT.getSuffix(), "/transformRequest"));

        addOp(ops, ACCELERATE_NAME, "accelerate", acceleratePayload);

        // setup the http request to post the agenda
        // (TODO: this will likely require the auth token to pass through or otherwise horrible things)
        HttpRequestHandlerInput httpRequestHandlerInput = new HttpRequestHandlerInput()
            .setPostDataEncoding("json")
            // this field will be replaced with the output of the accelerate operation
            .setPostData(jsonReferenceReplacer.generateReference(ACCELERATE_NAME + OperationReference.OUTPUT.getSuffix(), "/agenda"));
        addOp(ops, "agendaPost.1", "agendaPost", httpRequestHandlerInput);

        Agenda agenda = new Agenda();
        agenda.setOperations(ops);

        return agenda;
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
