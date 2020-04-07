package com.theplatform.dfh.cp.endpoint.agendatemplate.aws;

import com.comcast.pop.api.AgendaTemplate;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.agendatemplate.AgendaTemplateRequestProcessor;
import com.theplatform.dfh.cp.endpoint.agendatemplate.aws.persistence.DynamoDBAgendaTemplatePersisterFactory;
import com.comcast.pop.endpoint.aws.DataObjectLambdaStreamEntry;
import com.comcast.pop.endpoint.aws.LambdaDataObjectRequest;
import com.theplatform.dfh.persistence.api.ObjectPersister;

/**
 * Main entry point class for the AWS Agenda endpoint
 */
public class AgendaTemplateLambdaStreamEntry extends DataObjectLambdaStreamEntry<AgendaTemplate>
{
    public AgendaTemplateLambdaStreamEntry()
    {
        super(
            AgendaTemplate.class,
            new DynamoDBAgendaTemplatePersisterFactory()
        );
    }

    @Override
    protected AgendaTemplateRequestProcessor getRequestProcessor(LambdaDataObjectRequest<AgendaTemplate> lambdaRequest, ObjectPersister<AgendaTemplate> objectPersister)
    {
        String authHeader = lambdaRequest.getAuthorizationHeader();
        if(authHeader == null)
        {
            throw new RuntimeException("No Authorization node found. Unable to process request.");
        }

        return new AgendaTemplateRequestProcessor(objectPersister);
    }

    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.AGENDA_TEMPLATE;
    }
}
