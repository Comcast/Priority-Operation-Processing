package com.theplatform.dfh.cp.endpoint.progress.service.aws;

import com.fasterxml.jackson.databind.JsonNode;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.agenda.aws.persistence.DynamoDBAgendaPersisterFactory;
import com.theplatform.dfh.cp.endpoint.aws.AbstractLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.EnvironmentLookupUtils;
import com.theplatform.dfh.cp.endpoint.aws.LambdaRequest;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.cp.endpoint.operationprogress.aws.persistence.DynamoDBOperationProgressPersisterFactory;
import com.theplatform.dfh.cp.endpoint.progress.aws.persistence.DynamoDBAgendaProgressPersisterFactory;
import com.theplatform.dfh.cp.endpoint.progress.service.ProgressSummaryRequestProcessor;
import com.comcast.fission.endpoint.api.progress.ProgressSummaryRequest;
import com.comcast.fission.endpoint.api.progress.ProgressSummaryResponse;
import com.comcast.fission.endpoint.api.BadRequestException;
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