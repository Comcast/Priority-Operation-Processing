package com.theplatform.dfh.persistence.aws.dynamodb;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;

/**
 * Basic factory for creating AmazonDynamoDB instances
 */
public class AWSDynamoDBFactory
{
    public AmazonDynamoDB getAmazonDynamoDB()
    {
        return AmazonDynamoDBClientBuilder.defaultClient();
    }
}
