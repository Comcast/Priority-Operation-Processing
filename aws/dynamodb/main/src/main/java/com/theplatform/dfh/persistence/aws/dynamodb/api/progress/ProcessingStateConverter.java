package com.theplatform.dfh.persistence.aws.dynamodb.api.progress;

import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBTypeConverter;
import com.theplatform.dfh.cp.api.progress.ProcessingState;

/**
 */
public class ProcessingStateConverter implements DynamoDBTypeConverter<String, ProcessingState>
{

    @Override
    public String convert(ProcessingState processingState)
    {
        String stateString = null;
        try
        {
            if (processingState != null)
            {
                stateString = processingState.toString();
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return stateString;
    }

    @Override
    public ProcessingState unconvert(String s)
    {

        ProcessingState processingState = null;
        try
        {
            if (s != null && s.length() != 0)
            {
                processingState = ProcessingState.valueOf(s.toUpperCase());
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        return processingState;
    }
}
