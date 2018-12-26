package com.theplatform.dfh.cp.endpoint.progress.aws;

import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.aws.LambdaObjectRequest;
import com.theplatform.dfh.cp.endpoint.progress.AgendaProgressRequestProcessor;
import com.theplatform.dfh.cp.endpoint.aws.BaseAWSLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.theplatform.dfh.http.idm.IDMHTTPUrlConnectionFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;

/**
 * Main entry point class for the AWS Agenda endpoint
 */
public class ProgressLambdaStreamEntry extends BaseAWSLambdaStreamEntry<AgendaProgress>
{
    public ProgressLambdaStreamEntry()
    {
        super(
            AgendaProgress.class,
            new DynamoDBAgendaProgressPersisterFactory()
        );
    }

    @Override
    protected AgendaProgressRequestProcessor getRequestProcessor(LambdaObjectRequest<AgendaProgress> lambdaObjectRequest, ObjectPersister<AgendaProgress> objectPersister)
    {
        String authHeader = lambdaObjectRequest.getAuthorizationHeader();
        if(authHeader == null)
        {
            throw new RuntimeException("No Authorization node found. Unable to process request.");
        }

        String operationProgressUrl = environmentLookupUtils.getAPIEndpointURL(lambdaObjectRequest, "operationProgressPath");
        return new AgendaProgressRequestProcessor(objectPersister, new IDMHTTPUrlConnectionFactory(authHeader).setCid(lambdaObjectRequest.getCID()), operationProgressUrl);
    }

    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.AGENDA_PROGRESS;
    }
}
