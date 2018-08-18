package com.theplatform.dfh.cp.handler.executor.impl.executor.local;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.theplatform.dfh.cp.api.operation.Operation;
import com.theplatform.dfh.cp.handler.executor.impl.exception.AgendaExecutorException;
import com.theplatform.dfh.cp.handler.executor.impl.executor.BaseOperationExecutor;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Local executor is just for testing/prototype. It does not perform any actual operations.
 */
public class LocalOperationExecutor extends BaseOperationExecutor
{
    private static final Logger logger = LoggerFactory.getLogger(LocalOperationExecutor.class);
    private ObjectMapper objectMapper = new ObjectMapper();
    private JsonHelper jsonHelper;
    private static final String OUTPUT_OVERRIDE_PTR = "/resultPayload";

    public LocalOperationExecutor(Operation operation)
    {
        super(operation);
        jsonHelper = new JsonHelper();
    }

    @Override
    public String execute(String payload)
    {
        logger.info("Operation {} INPUT  Payload: {}", operation.getId(), payload);

        JsonNode outputNode = new ObjectNode(objectMapper.getNodeFactory());
        try
        {
            // extract the output override from the payload
            JsonNode rootNode = objectMapper.readTree(payload);
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

        logger.info("Operation {} OUTPUT Payload: {}", operation.getId(), outputPayload);

        return outputPayload;
    }

    public void setJsonHelper(JsonHelper jsonHelper)
    {
        this.jsonHelper = jsonHelper;
    }

    public void setObjectMapper(ObjectMapper objectMapper)
    {
        this.objectMapper = objectMapper;
    }
}