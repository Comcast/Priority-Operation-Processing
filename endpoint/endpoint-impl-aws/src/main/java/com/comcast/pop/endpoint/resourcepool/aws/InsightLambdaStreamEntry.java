package com.comcast.pop.endpoint.resourcepool.aws;

import com.comcast.pop.api.facility.Insight;
import com.comcast.pop.endpoint.TableEnvironmentVariableName;
import com.comcast.pop.endpoint.resourcepool.InsightRequestProcessor;
import com.comcast.pop.endpoint.resourcepool.aws.persistence.DynamoDBInsightPersisterFactory;
import com.comcast.pop.endpoint.aws.DataObjectLambdaStreamEntry;
import com.comcast.pop.endpoint.aws.LambdaDataObjectRequest;
import com.comcast.pop.persistence.api.ObjectPersister;

public class InsightLambdaStreamEntry extends DataObjectLambdaStreamEntry<Insight>
{
    public InsightLambdaStreamEntry()
    {
        super(
            Insight.class,
            new DynamoDBInsightPersisterFactory()
        );
    }

    @Override
    protected InsightRequestProcessor getRequestProcessor(LambdaDataObjectRequest<Insight> lambdaDataObjectRequest, ObjectPersister<Insight> objectPersister)
    {
        return new InsightRequestProcessor(objectPersister);
    }


    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.INSIGHT;
    }
}
