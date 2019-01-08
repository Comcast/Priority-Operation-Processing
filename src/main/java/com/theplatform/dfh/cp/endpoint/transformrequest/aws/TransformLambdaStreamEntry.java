package com.theplatform.dfh.cp.endpoint.transformrequest.aws;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.api.TransformRequest;
import com.theplatform.dfh.cp.api.progress.AgendaProgress;
import com.theplatform.dfh.cp.api.progress.OperationProgress;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.theplatform.dfh.cp.endpoint.aws.BaseAWSLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.LambdaObjectRequest;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.theplatform.dfh.cp.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.theplatform.dfh.cp.endpoint.transformrequest.TransformRequestProcessor;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.endpoint.api.query.scheduling.ByCustomerId;
import com.theplatform.dfh.http.idm.IDMHTTPUrlConnectionFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.AWSDynamoDBFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBCompressedObjectPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;
import com.theplatform.dfh.scheduling.aws.persistence.PersistentReadyAgendaConverter;

/**
 * Main entry point class for the AWS TransformRequest endpoint
 */
public class TransformLambdaStreamEntry extends BaseAWSLambdaStreamEntry<TransformRequest>
{
    private ObjectPersisterFactory<Agenda> agendaPersisterFactory;
    private ObjectPersisterFactory<AgendaProgress> agendaProgressPersisterFactory;
    private ObjectPersisterFactory<OperationProgress> operationProgressPersisterFactory;

    public TransformLambdaStreamEntry()
    {
        super(
            TransformRequest.class,
            new DynamoDBCompressedObjectPersisterFactory<>("id", TransformRequest.class)
        );
        agendaPersisterFactory = new DynamoDBAgendaPersisterFactory();
        agendaProgressPersisterFactory = new DynamoDBAgendaProgressPersisterFactory();
        operationProgressPersisterFactory = new DynamoDBOperationProgressPersisterFactory();
    }

    @Override
    protected TransformRequestProcessor getRequestProcessor(LambdaObjectRequest<TransformRequest> lambdaRequest, ObjectPersister<TransformRequest> objectPersister)
    {
        String authHeader = lambdaRequest.getAuthorizationHeader();
        if(authHeader == null)
        {
            throw new RuntimeException("No Authorization node found. Unable to process request.");
        }
        //@todo we need another non hardcoded way...
        TableIndexes readyAgendaTableIndexes =
            new TableIndexes().withIndex("customer_index", ByCustomerId.fieldName());
        DynamoDBConvertedObjectPersister<ReadyAgenda> readyAgendaPersister = new DynamoDBConvertedObjectPersister<>
            (
                environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.READY_AGENDA),
                "id",
                new AWSDynamoDBFactory(),
                ReadyAgenda.class,
                new PersistentReadyAgendaConverter(),
                readyAgendaTableIndexes
            );

        String insightURL = environmentLookupUtils.getAPIEndpointURL(lambdaRequest, "insightPath");
        String customerURL = environmentLookupUtils.getAPIEndpointURL(lambdaRequest, "customerPath");

        return new TransformRequestProcessor(
            objectPersister,
            agendaPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA)),
            agendaProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA_PROGRESS)),
            operationProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.OPERATION_PROGRESS)),
            readyAgendaPersister,
            new IDMHTTPUrlConnectionFactory(authHeader).setCid(lambdaRequest.getCID()),
            insightURL,
            customerURL);
    }

    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.TRANSFORM;
    }

}