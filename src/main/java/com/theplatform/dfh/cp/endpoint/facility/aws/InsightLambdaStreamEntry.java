package com.theplatform.dfh.cp.endpoint.facility.aws;

import com.theplatform.dfh.cp.api.facility.Insight;
import com.theplatform.dfh.cp.endpoint.TableEnvironmentVariableName;
import com.theplatform.dfh.cp.endpoint.aws.BaseAWSLambdaStreamEntry;
import com.theplatform.dfh.cp.endpoint.aws.LambdaObjectRequest;
import com.theplatform.dfh.cp.endpoint.facility.InsightRequestProcessor;
import com.theplatform.dfh.cp.endpoint.facility.aws.persistence.DynamoDBInsightPersisterFactory;
import com.theplatform.dfh.cp.endpoint.facility.aws.persistence.PersistentInsightConverter;
import com.theplatform.dfh.persistence.api.ObjectPersister;
import com.theplatform.dfh.persistence.aws.dynamodb.DynamoDBConvertedPersisterFactory;
import com.theplatform.dfh.persistence.aws.dynamodb.TableIndexes;

public class InsightLambdaStreamEntry extends BaseAWSLambdaStreamEntry<Insight>
{
    public InsightLambdaStreamEntry()
    {
        super(
            Insight.class,
            new DynamoDBInsightPersisterFactory()
        );
    }

    @Override
    protected InsightRequestProcessor getRequestProcessor(LambdaObjectRequest<Insight> lambdaObjectRequest, ObjectPersister<Insight> objectPersister)
    {
        return new InsightRequestProcessor(objectPersister);
    }


    @Override
    protected String getTableEnvironmentVariableName()
    {
        return TableEnvironmentVariableName.INSIGHT;
    }
}
