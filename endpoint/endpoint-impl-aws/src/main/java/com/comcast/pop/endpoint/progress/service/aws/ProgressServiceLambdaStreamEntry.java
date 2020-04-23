package com.comcast.pop.endpoint.progress.service.aws;

import com.comcast.pop.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.fasterxml.jackson.databind.JsonNode;
import com.comcast.pop.endpoint.TableEnvironmentVariableName;
import com.comcast.pop.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.comcast.pop.endpoint.aws.AbstractLambdaStreamEntry;
import com.comcast.pop.endpoint.aws.EnvironmentLookupUtils;
import com.comcast.pop.endpoint.aws.LambdaRequest;
import com.comcast.pop.endpoint.base.RequestProcessor;
import com.comcast.pop.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.comcast.pop.endpoint.progress.service.ProgressSummaryRequestProcessor;
import com.comcast.pop.endpoint.api.progress.ProgressSummaryRequest;
import com.comcast.pop.endpoint.api.progress.ProgressSummaryResponse;
import com.comcast.pop.endpoint.api.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProgressServiceLambdaStreamEntry extends AbstractLambdaStreamEntry<ProgressSummaryResponse, LambdaRequest<ProgressSummaryRequest>>
{
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final EnvironmentLookupUtils environmentLookupUtils = new EnvironmentLookupUtils();
    private DynamoDBAgendaProgressPersisterFactory agendaProgressPersisterFactory;
    private DynamoDBAgendaPersisterFactory agendaPersisterFactory;
    private DynamoDBOperationProgressPersisterFactory operationProgressPersisterFactory;

    public ProgressServiceLambdaStreamEntry()
    {
        agendaProgressPersisterFactory = new DynamoDBAgendaProgressPersisterFactory();
        operationProgressPersisterFactory = new DynamoDBOperationProgressPersisterFactory();
        agendaPersisterFactory = new DynamoDBAgendaPersisterFactory();
    }

    @Override
    public RequestProcessor getRequestProcessor(LambdaRequest<ProgressSummaryRequest> serviceRequest)
    {
        return new ProgressSummaryRequestProcessor(
            agendaProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(serviceRequest, TableEnvironmentVariableName.AGENDA_PROGRESS)),
            operationProgressPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(serviceRequest, TableEnvironmentVariableName.OPERATION_PROGRESS)),
            agendaPersisterFactory.getObjectPersister(environmentLookupUtils.getTableName(serviceRequest, TableEnvironmentVariableName.AGENDA))
        );
    }

    @Override
    public LambdaRequest<ProgressSummaryRequest> getRequest(JsonNode node) throws BadRequestException
    {
        return new LambdaRequest<>(node, ProgressSummaryRequest.class);
    }
}