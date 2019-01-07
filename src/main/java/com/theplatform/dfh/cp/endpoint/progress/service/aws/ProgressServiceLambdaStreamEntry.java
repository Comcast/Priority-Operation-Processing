package com.theplatform.dfh.cp.endpoint.progress.service.aws;

import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.cp.endpoint.aws.JsonRequestStreamHandler;
import com.theplatform.dfh.cp.endpoint.aws.LambdaRequest;
import com.theplatform.dfh.cp.endpoint.aws.ResponseWriter;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.theplatform.dfh.cp.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.theplatform.dfh.cp.endpoint.progress.service.ProgressSummaryRequestProcessor;
import com.theplatform.dfh.cp.endpoint.progress.service.api.ProgressSummaryRequest;
import com.theplatform.dfh.cp.endpoint.progress.service.api.ProgressSummaryResult;
import com.theplatform.dfh.cp.modules.jsonhelper.JsonHelper;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class ProgressServiceLambdaStreamEntry implements JsonRequestStreamHandler
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private static final JsonHelper jsonHelper = new JsonHelper();
    private final EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();
    private DynamoDBAgendaProgressPersisterFactory agendaProgressPersisterFactory;
    private DynamoDBOperationProgressPersisterFactory operationProgressPersisterFactory;
    private ResponseWriter responseWriter = new ResponseWriter();

    public ProgressServiceLambdaStreamEntry()
    {
        agendaProgressPersisterFactory = new DynamoDBAgendaProgressPersisterFactory();
        operationProgressPersisterFactory = new DynamoDBOperationProgressPersisterFactory();
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context) throws IOException
    {
        // this is immediately made available for subclasses
        JsonNode rootRequestNode = jsonHelper.getObjectMapper().readTree(inputStream);
        handleRequest(rootRequestNode, outputStream, context);
    }

    @Override
    public void handleRequest(JsonNode inputStreamNode, OutputStream outputStream, Context context) throws IOException
    {
        logger.info("Progress Service endpoint request received.");
        LambdaRequest lambdaRequest = new LambdaRequest(inputStreamNode);

        ProgressSummaryResult responseObject = null;
        int httpStatusCode = 200;

        if(lambdaRequest.getAuthorizationHeader() == null)
        {
            throw new RuntimeException("No Authorization node found. Unable to process request.");
        }

        String httpMethod = lambdaRequest.getHTTPMethod("UNKNOWN").toUpperCase();
        try
        {
            switch (httpMethod)
            {
                case "POST":
                    String bodyJson = StringEscapeUtils.unescapeJson(inputStreamNode.at("/body").asText());
                    ProgressSummaryRequest progressSummaryRequest = jsonHelper.getObjectFromString(bodyJson, ProgressSummaryRequest.class);
                    responseObject = createProgressSummaryRequestProcessor(lambdaRequest)
                        .getProgressSummary(progressSummaryRequest);
                    break;
                default:
                    // todo: some bad response code
                    httpStatusCode = 405;
                    logger.warn("Unsupported method type {}.", httpMethod);
            }
        }
        catch(Exception e)
        {
            httpStatusCode = 500;
            responseObject = null; // TODO: need a standard error body?
            logger.error("Error retrieving progress", e);
        }
        responseWriter.writeResponse(outputStream, jsonHelper.getObjectMapper(), httpStatusCode, responseObject);
    }

    private ProgressSummaryRequestProcessor createProgressSummaryRequestProcessor(LambdaRequest lambdaRequest)
    {
        return new ProgressSummaryRequestProcessor(
            agendaProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA_PROGRESS)),
            operationProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.OPERATION_PROGRESS))
        );
    }
}