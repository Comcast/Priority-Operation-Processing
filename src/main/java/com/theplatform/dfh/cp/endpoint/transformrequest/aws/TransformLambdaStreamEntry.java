package com.theplatform.dfh.cp.endpoint.transformrequest.aws;

import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.aws.BaseAWSLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.LambdaObjectRequest;
import com.theplatform.dfh.cp.endpoint.transformrequest.TransformRequestProcessor;
import com.theplatform.dfh.http.idm.IDMHTTPUrlConnectionFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBCompressedObjectPersisterFactory;

/**
 * Main entry point class for the AWS TransformRequest endpoint
 */
public class TransformLambdaStreamEntry extends BaseAWSLambdaStreamEntry<TransformRequest>
{
    public TransformLambdaStreamEntry()
    {
        super(
            TransformRequest.class,
            new DynamoDBCompressedObjectPersisterFactory<>("id", TransformRequest.class)
        );

    }

    @Override
    protected TransformRequestProcessor getRequestProcessor(LambdaObjectRequest<TransformRequest> lambdaRequest, ObjectPersister<TransformRequest> objectPersister)
    {
        String authHeader = lambdaRequest.getAuthorizationHeader();
        if(authHeader == null)
        {
            throw new RuntimeException("No Authorization node found. Unable to process request.");
        }

        String agendaURL = environmentLookupUtils.getAPIEndpointURL(lambdaRequest, "agendaPath");
        String progressURL = environmentLookupUtils.getAPIEndpointURL(lambdaRequest, "progressPath");
        return new TransformRequestProcessor(objectPersister, new IDMHTTPUrlConnectionFactory(authHeader).setCid(lambdaRequest.getCID()), progressURL, agendaURL);
    }

    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.TRANSFORM;
    }

}