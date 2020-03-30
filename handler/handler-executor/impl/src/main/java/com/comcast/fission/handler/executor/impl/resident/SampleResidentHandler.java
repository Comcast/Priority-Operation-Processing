package com.comcast.fission.handler.executor.impl.resident;

import com.comcast.fission.handler.executor.impl.exception.AgendaExecutorException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.api.progress.ProcessingState;
import com.theplatform.dfh.cp.handler.base.ResidentHandler;
import com.theplatform.dfh.cp.handler.base.ResidentHandlerParams;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class SampleResidentHandler implements ResidentHandler
{
    private static final Logger logger = LoggerFactory.getLogger(SampleResidentHandler.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private JsonHelper jsonHelper;
    private static final String OUTPUT_OVERRIDE_PTR = "/resultPayload";

    public SampleResidentHandler()
    {
        jsonHelper = new JsonHelper();
    }

    @Override
    public String execute(ResidentHandlerParams params)
    {
        JsonNode outputNode = new ObjectNode(objectMapper.getNodeFactory());

        params.getReporter().reportProgress(createOperationProgress(ProcessingState.EXECUTING, "Init"));
        try
        {
            // extract the output override from the payload
            JsonNode rootNode = objectMapper.readTree(params.getPayload());
            JsonNode outputOverrideNode = rootNode.at(OUTPUT_OVERRIDE_PTR);
            if(!outputOverrideNode.isMissingNode())
            {
                outputNode = outputOverrideNode;
            }
        }
        catch(IOException e)
        {
            throw new AgendaExecutorException(e);
        }

        String outputPayload = jsonHelper.getJSONString(outputNode);
        params.getReporter().reportProgress(createOperationProgress(ProcessingState.COMPLETE, "Success"));
        return outputPayload;
    }

    private OperationProgress createOperationProgress(ProcessingState processingState, String processingStateMessage)
    {
        OperationProgress operationProgress = new OperationProgress();
        operationProgress.setProcessingState(processingState);
        operationProgress.setProcessingStateMessage(processingStateMessage);
        return operationProgress;
    }

    public void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }
}
