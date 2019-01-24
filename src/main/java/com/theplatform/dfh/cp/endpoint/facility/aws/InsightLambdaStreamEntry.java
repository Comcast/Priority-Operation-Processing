package com.theplatform.dfh.cp.endpoint.facility.aws;

import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.aws.DataObjectLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.LambdaDataObjectRequest;
import com.theplatform.dfh.cp.endpoint.base.RequestProcessor;
import com.theplatform.dfh.cp.endpoint.facility.InsightRequestProcessor;
import com.theplatform.dfh.cp.endpoint.facility.aws.persistence.DynamoDBInsightPersisterFactory;
import com.theplatform.dfh.persistence.api.ObjectPersister;

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
