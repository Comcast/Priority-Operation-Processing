package com.theplatform.dfh.cp.endpoint.agenda.aws;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.theplatform.dfh.cp.endpoint.aws.BaseAWSLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.cp.endpoint.aws.LambdaObjectRequest;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.query.scheduling.ByCustomerId;
import com.theplatform.dfh.http.idm.IDMHTTPUrlConnectionFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;
import com.theplatform.dfh.scheduling.aws.persistence.PersistentReadyAgendaConverter;

/**
 * Main entry point class for the AWS Agenda endpoint
 */
public class AgendaLambdaStreamEntry extends BaseAWSLambdaStreamEntry<Agenda>
{
    private EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();

    public AgendaLambdaStreamEntry()
    {
        super(
            Agenda.class,
            new DynamoDBAgendaPersisterFactory()
        );
    }

    @Override
    protected AgendaRequestProcessor getRequestProcessor(LambdaObjectRequest<Agenda> lambdaObjectRequest, ObjectPersister<Agenda> objectPersister)
    {
        String authHeader = lambdaObjectRequest.getAuthorizationHeader();
        if(authHeader == null)
        {
            throw new RuntimeException("No Authorization node found. Unable to process request.");
        }

        //@todo we need another non hardcoded way...
        TableIndexes readyAgendaTableIndexes =
            new TableIndexes().withIndex("customer_index", ByCustomerId.fieldName());
        DynamoDBConvertedObjectPersister<ReadyAgenda> readyAgendaPersister = new DynamoDBConvertedObjectPersister<>
        (
            environmentLookupUtils.getTableName(lambdaObjectRequest, TableEnvironmentVariableName.READY_AGENDA),
            "id",
            new AWSDynamoDBFactory(),
            ReadyAgenda.class,
            new PersistentReadyAgendaConverter(),
            readyAgendaTableIndexes
        );

        String progressURL = environmentLookupUtils.getAPIEndpointURL(lambdaObjectRequest, "progressPath");
        String operationProgressURL = environmentLookupUtils.getAPIEndpointURL(lambdaObjectRequest, "operationProgressPath");
        String customerURL = environmentLookupUtils.getAPIEndpointURL(lambdaObjectRequest, "customerPath");
        String insightURL = environmentLookupUtils.getAPIEndpointURL(lambdaObjectRequest, "insightPath");
        return new AgendaRequestProcessor(objectPersister,
            readyAgendaPersister,
            new IDMHTTPUrlConnectionFactory(authHeader).setCid(lambdaObjectRequest.getCID()),
            progressURL,
            operationProgressURL,
            insightURL,
            customerURL);
    }

    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.AGENDA;
    }
}
