package com.theplatform.dfh.cp.endpoint.agenda.aws;

import com.theplatform.dfh.cp.api.Agenda;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.agenda.AgendaRequestProcessor;
import com.theplatform.dfh.cp.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.theplatform.dfh.cp.endpoint.aws.BaseAWSLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.cp.endpoint.aws.LambdaObjectRequest;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.theplatform.dfh.cp.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.theplatform.dfh.cp.scheduling.api.ReadyAgenda;
import com.theplatform.dfh.http.idm.IDMHTTPUrlConnectionFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.api.ObjectPersisterFactory;
import com.theplatform.dfh.scheduling.aws.persistence.DynamoDbReadyAgendaPersisterFactory;

/**
 * Main entry point class for the AWS Agenda endpoint
 */
public class AgendaLambdaStreamEntry extends BaseAWSLambdaStreamEntry<Agenda>
{
    private EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();
    private DynamoDBAgendaProgressPersisterFactory agendaProgressPersisterFactory;
    private DynamoDBOperationProgressPersisterFactory operationProgressPersisterFactory;
    private ObjectPersisterFactory<ReadyAgenda> readyAgendaPersisterFactory;

    public AgendaLambdaStreamEntry()
    {
        super(
            Agenda.class,
            new DynamoDBAgendaPersisterFactory()
        );
        agendaProgressPersisterFactory = new DynamoDBAgendaProgressPersisterFactory();
        operationProgressPersisterFactory = new DynamoDBOperationProgressPersisterFactory();
        readyAgendaPersisterFactory = new DynamoDbReadyAgendaPersisterFactory();
    }

    @Override
    protected AgendaRequestProcessor getRequestProcessor(LambdaObjectRequest<Agenda> lambdaRequest, ObjectPersister<Agenda> objectPersister)
    {
        String authHeader = lambdaRequest.getAuthorizationHeader();
        if(authHeader == null)
        {
            throw new RuntimeException("No Authorization node found. Unable to process request.");
        }

        String customerURL = environmentLookupUtils.getAPIEndpointURL(lambdaRequest, "customerPath");
        String insightURL = environmentLookupUtils.getAPIEndpointURL(lambdaRequest, "insightPath");
        return new AgendaRequestProcessor(objectPersister,
            agendaProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.AGENDA_PROGRESS)),
            readyAgendaPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.READY_AGENDA)),
            operationProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(lambdaRequest, TableEnvironmentVariableName.OPERATION_PROGRESS)),
            new IDMHTTPUrlConnectionFactory(authHeader).setCid(lambdaRequest.getCID()),
            insightURL,
            customerURL);
    }

    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.AGENDA;
    }
}
